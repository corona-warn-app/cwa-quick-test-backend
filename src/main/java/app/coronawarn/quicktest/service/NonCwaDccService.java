/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.DccExternalServerClient;
import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.client.VerificationServerClient;
import app.coronawarn.quicktest.config.DccConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.DccPublicKey;
import app.coronawarn.quicktest.model.DccUploadData;
import app.coronawarn.quicktest.model.DccUploadResult;
import app.coronawarn.quicktest.model.RegistrationToken;
import app.coronawarn.quicktest.model.RegistrationTokenRequest;
import app.coronawarn.quicktest.model.UploadPublicKeyRequest;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.utils.PdfGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.DgcCryptedPublisher;
import eu.europa.ec.dgc.DgcGenerator;
import eu.europa.ec.dgc.dto.DgcData;
import feign.FeignException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NonCwaDccService extends DccServiceBase {

    private final DccExternalServerClient dccExternalServerClient;
    private final VerificationServerClient verificationServerClient;

    /**
     * Service creating dcc certificates for non CWA users.
     *
     * @param dccServerClient               internal dcc client
     * @param dccConfig                     dcc related config
     * @param quickTestRepository           quicktest repo
     * @param quickTestArchiveRepository    quicktest archive repo
     * @param dgcCryptedPublisher           dcc publisher
     * @param dgcGenerator                  dgc generation
     * @param quickTestConfig               quicktest related config
     * @param pdfGenerator                  pdf geneneration
     * @param dccExternalServerClient       external dcc client
     * @param verificationServerClient      external verification server client
     */
    public NonCwaDccService(DccServerClient dccServerClient, DccConfig dccConfig,
                            QuickTestRepository quickTestRepository,
                            QuickTestArchiveRepository quickTestArchiveRepository,
                            DgcCryptedPublisher dgcCryptedPublisher, DgcGenerator dgcGenerator,
                            QuickTestConfig quickTestConfig, PdfGenerator pdfGenerator,
                            DccExternalServerClient dccExternalServerClient,
                            VerificationServerClient verificationServerClient) {
        super(dccServerClient, dccConfig, quickTestRepository, quickTestArchiveRepository, dgcCryptedPublisher,
            dgcGenerator, quickTestConfig, pdfGenerator);
        this.dccExternalServerClient = dccExternalServerClient;
        this.verificationServerClient = verificationServerClient;
    }

    /**
     * Create a Dcc certificate for non-cwa users by using a dummy pubkey.
     * @param quickTest The QuickTest for which the dcc should be generated
     */
    public void createCertificate(QuickTest quickTest) {

        MessageDigest digest = createSha256Digest();
        String testIdHashed = getTestResultServerHashed(digest, quickTest);
        RegistrationToken registrationToken = getRegistrationToken(testIdHashed);

        uploadPublicKey(testIdHashed, registrationToken);

        DccPublicKey pub = getDccPublicKey(testIdHashed);

        DgcData dgcData = genDcc(quickTest, pub.getPublicKey(), pub.getDcci());
        DccUploadData dccUploadData = generateDccUploadData(dgcData);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String dccUploadDataJson = objectMapper.writeValueAsString(dccUploadData);
            quickTest.setDccSignData(dccUploadDataJson);
            quickTest.setPublicKey(pub.getPublicKey());
            quickTest.setDccUnsigned(Base64.getEncoder().encodeToString(dgcData.getDccData()));
            quickTestRepository.saveAndFlush(quickTest);

            DccUploadResult dccUploadResult = dccServerClient.uploadDcc(testIdHashed, dccUploadData);

            updateQuicktestArchive(quickTest, dccUploadResult);
        } catch (JsonProcessingException e) {
            log.error("can not create json data", e);
        } catch (IOException exception) {
            log.error("Appending Certificate to PDF failed.");
        } catch (FeignException feignException) {
            log.warn("Error during uploading dcc data {}", quickTest.getHashedGuid(), feignException);
        }
    }

    private RegistrationToken getRegistrationToken(String testIdHashed) {
        try {
            RegistrationTokenRequest tokenRequest = RegistrationTokenRequest.builder().key(testIdHashed).build();
            return verificationServerClient.getRegistrationToken(tokenRequest);
        } catch (FeignException feignException) {
            log.warn("Error while getting registration token for quicktest with testIdHashed=[{}]", testIdHashed,
                feignException);
            throw new IllegalArgumentException("No RegistrationToken from server");
        }
    }

    private void uploadPublicKey(String testIdHashed, RegistrationToken registrationToken) {
        try {
            PublicKey key = generatePublicKey();
            String keyString = Base64.getEncoder().encodeToString(key.getEncoded());
            UploadPublicKeyRequest uploadPublicKeyRequest =
              new UploadPublicKeyRequest(registrationToken.getRegistrationToken(), keyString);
            dccExternalServerClient.uploadPublicKey(uploadPublicKeyRequest);
            log.info("Uploaded generated PublicKey for quicktest with hashed id=[{}]", testIdHashed);

            //TODO Save reg token to DB?
            // No further token can be issued if anything fails beyond this point
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            log.error("No Such Algorithm");
        } catch (FeignException feignException) {
            log.warn("Error while uploading public key for testIdHashed=[{}]", testIdHashed, feignException);
            throw new IllegalArgumentException("Could not upload Public Key");
        }
    }

    private DccPublicKey getDccPublicKey(String testIdHashed) {
        List<DccPublicKey> dccPublicKeys = dccServerClient.searchPublicKeys(quickTestConfig.getLabId());
        List<DccPublicKey> keys = dccPublicKeys.stream()
          .filter(it -> it.getTestId().equals(testIdHashed))
          .collect(Collectors.toList());
        if (keys.size() > 1) {
            log.warn("More than one publicKey found at dcc for quicktest with hashed id=[{}]", testIdHashed);
        }
        if (keys.isEmpty()) {
            log.warn("Could not find PublicKey at DCC Server for quicktest with hashed id=[{}]", testIdHashed);
            throw new IllegalArgumentException("No PublicKey from DCC Server");
        }

        return keys.get(0);
    }

    private void updateQuicktestArchive(QuickTest quickTest, DccUploadResult dccUploadResult) throws IOException {
        byte[] coseSigned = dgcGenerator.dgcSetCosePartial(
          Base64.getDecoder().decode(quickTest.getDccUnsigned()),
          Base64.getDecoder().decode(dccUploadResult.getPartialDcc()));

        Optional<QuickTestArchive> quickTestArchiveOptional =
          quickTestArchiveRepository.findByHashedGuid(quickTest.getHashedGuid());
        if (quickTestArchiveOptional.isPresent()) {
            QuickTestArchive quickTestArchive = quickTestArchiveOptional.get();
            String dcc = dgcGenerator.coseToQrCode(coseSigned);
            quickTestArchive.setDcc(dcc);
            ByteArrayOutputStream pdf = pdfGenerator.appendCertificatePage(quickTestArchive.getPdf(), quickTest, dcc);
            quickTestArchive.setPdf(pdf.toByteArray());
            quickTestArchiveRepository.saveAndFlush(quickTestArchive);
        } else {
            log.warn("can not find quick test archive {}", quickTest.getHashedGuid());
        }
    }

    private PublicKey generatePublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair().getPublic();
    }
}

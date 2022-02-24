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

import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.config.DccConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.dcc.DccPublicKey;
import app.coronawarn.quicktest.model.dcc.DccUploadData;
import app.coronawarn.quicktest.model.dcc.DccUploadResult;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.utils.DccPdfGenerator;
import app.coronawarn.quicktest.utils.TestTypeUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.generation.DccTestBuilder;
import eu.europa.ec.dgc.generation.DgcCryptedPublisher;
import eu.europa.ec.dgc.generation.DgcGenerator;
import eu.europa.ec.dgc.generation.dto.DgcData;
import eu.europa.ec.dgc.generation.dto.DgcInitData;
import feign.FeignException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DccService {
    private final DccServerClient dccServerClient;
    private final DccConfig dccConfig;
    private final QuickTestRepository quickTestRepository;
    private final QuickTestArchiveRepository quickTestArchiveRepository;
    private final DgcCryptedPublisher dgcCryptedPublisher;
    private final DgcGenerator dgcGenerator;
    private final QuickTestConfig quickTestConfig;
    private final DccPdfGenerator dccPdfGenerator;

    // TODO to be scheduled look up for keys, then generate dcc and prepare for upload
    // should open transaction pro quick test

    /**
     * collect public keys.
     */
    @Scheduled(fixedDelayString = "${dcc.searchPublicKeysJob.fixedDelayString}")
    @SchedulerLock(name = "QuickTestSearchPublicKeys", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${dcc.searchPublicKeysJob.lockLimit}")
    public void collectPublicKeys() {
        Map<String, QuickTest> quickTestMap = new HashMap<>();
        MessageDigest digest = createSha256Digest();
        // preload all tests in expecting state
        for (QuickTest quickTest : quickTestRepository.findAllByDccStatus(DccStatus.pendingPublicKey)) {
            byte[] testIdHashed = digest.digest(quickTest.getTestResultServerHash().getBytes(StandardCharsets.UTF_8));
            digest.reset();
            quickTestMap.put(Hex.toHexString(testIdHashed), quickTest);
        }
        if (!quickTestMap.isEmpty()) {
            log.info("search public keys for {} keys", quickTestMap.size());
            List<DccPublicKey> publicKeys = dccServerClient.searchPublicKeys(quickTestConfig.getLabId());
            log.info("found public keys: size=[{}]", publicKeys.size());
            ObjectMapper objectMapper = new ObjectMapper();
            for (DccPublicKey dccPublicKey : publicKeys) {
                QuickTest quickTest = quickTestMap.get(dccPublicKey.getTestId());
                if (quickTest != null) {
                    log.debug("got public key for {} testid", dccPublicKey.getTestId());
                    DgcData dgcData = genDcc(quickTest, dccPublicKey.getPublicKey(), dccPublicKey.getDcci());
                    DccUploadData dccUploadData = new DccUploadData();
                    dccUploadData.setDccHash(Hex.toHexString(dgcData.getHash()));
                    dccUploadData.setDataEncryptionKey(Base64.getEncoder().encodeToString(dgcData.getDek()));
                    dccUploadData.setEncryptedDcc(Base64.getEncoder().encodeToString(dgcData.getDataEncrypted()));
                    try {
                        String dccUploadDataJson = objectMapper.writeValueAsString(dccUploadData);
                        quickTest.setDccStatus(DccStatus.pendingSignature);
                        quickTest.setDccSignData(dccUploadDataJson);
                        quickTest.setPublicKey(dccPublicKey.getPublicKey());
                        quickTest.setDccUnsigned(Base64.getEncoder().encodeToString(dgcData.getDccData()));
                        quickTestRepository.saveAndFlush(quickTest);
                    } catch (JsonProcessingException e) {
                        log.error("can not create json data", e);
                    }
                } else {
                    log.warn("got public key for " + dccPublicKey.getTestId()
                        + " which in not in state pendingPublicKey");
                }
            }
        } else {
            log.info("No new Quicktests with DCC available");
        }
    }

    /**
     * create sha256 digest.
     *
     * @return the digest.
     */
    public MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("have no SHA-256 digest", e);
        }
    }

    /**
     * upload dcc data.
     */
    @Scheduled(fixedDelayString = "${dcc.uploadDccJob.fixedDelayString}")
    @SchedulerLock(name = "QuickTestUploadDcc", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${dcc.uploadDccJob.lockLimit}")
    public void uploadDccData() {
        ObjectMapper objectMapper = new ObjectMapper();
        MessageDigest digest = createSha256Digest();

        List<QuickTest> quicktestsPending = quickTestRepository.findAllByDccStatus(DccStatus.pendingSignature);
        log.info("Upload dcc data for quicktests: size=[{}]", quicktestsPending.size());
        for (QuickTest quickTest : quicktestsPending) {
            log.debug("dcc sign {}", quickTest.getHashedGuid());
            try {
                DccUploadData dccUploadData = objectMapper.readValue(quickTest.getDccSignData(), DccUploadData.class);
                String testIdHashHex = Hex.toHexString(digest.digest(
                    quickTest.getTestResultServerHash().getBytes(StandardCharsets.UTF_8)));
                digest.reset();
                DccUploadResult dccUploadResult = dccServerClient.uploadDcc(testIdHashHex, dccUploadData);
                byte[] coseSigned = dgcGenerator.dgcSetCosePartial(
                    Base64.getDecoder().decode(quickTest.getDccUnsigned()),
                    Base64.getDecoder().decode(dccUploadResult.getPartialDcc()));
                Optional<QuickTestArchive> quickTestArchive =
                    quickTestArchiveRepository.findByHashedGuid(quickTest.getHashedGuid());
                if (quickTestArchive.isPresent()) {
                    String dcc = dgcGenerator.coseToQrCode(coseSigned);
                    quickTestArchive.get().setDcc(dcc);
                    try {
                        ByteArrayOutputStream pdf =
                            dccPdfGenerator.appendCertificatePage(quickTestArchive.get().getPdf(), quickTest, dcc);
                        quickTestArchive.get().setPdf(pdf.toByteArray());
                    } catch (IOException exception) {
                        log.warn("Appending Certificate to PDF failed for quicktest hashedGuid=[{}]",
                            quickTest.getHashedGuid());
                    } catch (Exception exception) {
                        log.warn("dcc: {}", dcc);
                        log.warn("quicktest: {}", quickTest);
                        log.warn("General Exception while appending certificate to PDF for quicktest hashedGuid=[{}]",
                            quickTest.getHashedGuid(), exception);
                    }
                    quickTestArchiveRepository.saveAndFlush(quickTestArchive.get());
                } else {
                    log.warn("can not find quick test archive {}", quickTest.getHashedGuid());
                }
                quickTestRepository.delete(quickTest);
            } catch (FeignException e) {
                log.warn("Error during uploading dcc data {}", quickTest.getHashedGuid(), e);
                if (HttpStatus.CONFLICT.value() == e.status()) {
                    // Delete quicktest if dcc server responds with conflict (already present)
                    log.warn("Dcc data already exists for hashedGuid=[{}]", quickTest.getHashedGuid());
                    quickTestRepository.delete(quickTest);
                }
            } catch (JsonProcessingException e) {
                log.warn("Error during signing {}", quickTest.getHashedGuid(), e);
            } catch (IllegalArgumentException e) {
                log.warn("Argument error during signing {}", quickTest.getHashedGuid(), e);
            }

        }
    }

    private DgcData genDcc(QuickTest quickTest, String publicKeyBase64, String dgci) {
        DgcInitData dgcInitData = new DgcInitData();
        ZonedDateTime created = quickTest.getCreatedAt().atZone(ZoneId.systemDefault());
        long issuetAt = created.toEpochSecond();
        long expiredAt = created.plus(dccConfig.getExpired()).toEpochSecond();
        dgcInitData.setIssuedAt(issuetAt);
        dgcInitData.setExpriation(expiredAt);
        dgcInitData.setIssuerCode(dccConfig.getCwtIssuer());
        dgcInitData.setAlgId(dccConfig.getAlgId());
        if (dccConfig.getKeyId() != null && dccConfig.getKeyId().length() > 0) {
            dgcInitData.setKeyId(Base64.getDecoder().decode(dccConfig.getKeyId()));
        }
        String dccJson = dccJsonFromQuickTest(quickTest, dgci);
        return dgcCryptedPublisher.createDgc(dgcInitData, dccJson, publicKeyFromBase64(publicKeyBase64));
    }

    private String dccJsonFromQuickTest(QuickTest quickTest, String dgci) {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        dccTestBuilder.fn(quickTest.getLastName()).gn(quickTest.getFirstName());
        dccTestBuilder.fnt(quickTest.getStandardisedFamilyName()).gnt(quickTest.getStandardisedGivenName());
        dccTestBuilder.dob(LocalDate.parse(quickTest.getBirthday()));
        boolean covidDetected;
        switch (quickTest.getTestResult()) {
          case QuickTest.TEST_RESULT_PCR_POSITIVE:
          case QuickTest.TEST_RESULT_PENDING:
              covidDetected = true;
              break;
          case QuickTest.TEST_RESULT_PCR_NEGATIVE:
          case QuickTest.TEST_RESULT_NEGATIVE:
              covidDetected = false;
              break;
          default:
              throw new IllegalArgumentException("can not map test result " + quickTest.getTestResult()
                        + " to positive or negative");
        }
        dccTestBuilder.detected(covidDetected)
                .testTypeRapid(TestTypeUtils.isRat(quickTest.getTestType()))
                .dgci(dgci)
                .country(dccConfig.getCountry())
                .testingCentre(quickTest.getPocId())
                .sampleCollection(quickTest.getUpdatedAt())
                .certificateIssuer(dccConfig.getIssuer());
        if (TestTypeUtils.isRat(quickTest.getTestType())) {
            dccTestBuilder.testIdentifier(quickTest.getTestBrandId());
        } else {
            dccTestBuilder.testName(quickTest.getTestBrandName());
        }
        return dccTestBuilder.toJsonString();
    }

    private PublicKey publicKeyFromBase64(String publicKeyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("can not extract public key");
        }
    }
}

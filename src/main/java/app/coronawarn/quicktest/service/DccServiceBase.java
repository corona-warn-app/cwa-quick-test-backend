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
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.DccUploadData;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.utils.PdfGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.DccTestBuilder;
import eu.europa.ec.dgc.DgcCryptedPublisher;
import eu.europa.ec.dgc.DgcGenerator;
import eu.europa.ec.dgc.dto.DgcData;
import eu.europa.ec.dgc.dto.DgcInitData;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;

@Slf4j
@RequiredArgsConstructor
public abstract class DccServiceBase {

    protected final DccServerClient dccServerClient;
    protected final DccConfig dccConfig;
    protected final QuickTestRepository quickTestRepository;
    protected final QuickTestArchiveRepository quickTestArchiveRepository;
    protected final DgcCryptedPublisher dgcCryptedPublisher;
    protected final DgcGenerator dgcGenerator;
    protected final QuickTestConfig quickTestConfig;
    protected final PdfGenerator pdfGenerator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected DgcData genDcc(QuickTest quickTest, String publicKeyBase64, String dgci) {
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
        dccTestBuilder.dob(quickTest.getBirthday());
        boolean covidDetected;
        switch (quickTest.getTestResult()) {
          case 7:
              covidDetected = true;
              break;
          case 6:
              covidDetected = false;
              break;
          default:
              throw new IllegalArgumentException("can not map test result " + quickTest.getTestResult()
                + " to positive or negative");
        }
        dccTestBuilder.detected(covidDetected)
          .testTypeRapid(true)
          .dgci(dgci)
          .countryOfTest(dccConfig.getCountry())
          .testingCentre(quickTest.getPocId())
          .testIdentifier(quickTest.getTestBrandId())
          .sampleCollection(quickTest.getUpdatedAt())
          .certificateIssuer(dccConfig.getIssuer());
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

    protected DccUploadData generateDccUploadData(DgcData dgcData) {
        DccUploadData dccUploadData = new DccUploadData();
        dccUploadData.setDccHash(Hex.toHexString(dgcData.getHash()));
        dccUploadData.setDataEncryptionKey(Base64.getEncoder().encodeToString(dgcData.getDek()));
        dccUploadData.setEncryptedDcc(Base64.getEncoder().encodeToString(dgcData.getDataEncrypted()));

        return dccUploadData;
    }

    /**
     * create sha256 digest.
     * @return the digest.
     */
    public MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("have no SHA-256 digest",e);
        }
    }

    protected String getTestResultServerHashed(MessageDigest digest, QuickTest quickTest) {
        byte[] hashed = digest.digest(quickTest.getTestResultServerHash().getBytes(StandardCharsets.UTF_8));
        digest.reset();
        return Hex.toHexString(hashed);
    }
}

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.config.DccConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.DccPublicKey;
import app.coronawarn.quicktest.model.DccPublicKeyList;
import app.coronawarn.quicktest.model.DccUploadData;
import app.coronawarn.quicktest.model.DccUploadResult;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.DccTestBuilder;
import eu.europa.ec.dgc.DgcCryptedPublisher;
import eu.europa.ec.dgc.DgcGenerator;
import eu.europa.ec.dgc.dto.DgcData;
import eu.europa.ec.dgc.dto.DgcInitData;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DccService {
    private final DccServerClient dccServerClient;
    private final DccConfig dccConfig;
    private final QuickTestRepository quickTestRepository;
    private final DgcCryptedPublisher dgcCryptedPublisher;
    private final DgcGenerator dgcGenerator;
    private final QuickTestConfig quickTestConfig;

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
        // preload all tests in expecting state
        for (QuickTest quickTest : quickTestRepository.findAllByDccStatus(DccStatus.pendingPublicKey)) {
            quickTestMap.put(quickTest.getHashedGuid(), quickTest);
        }
        if (!quickTestMap.isEmpty()) {
            log.info("search publick keys for {} keys", quickTestMap.size());
            DccPublicKeyList publicKeys = dccServerClient.searchPublicKeys(quickTestConfig.getLabId());
            ObjectMapper objectMapper = new ObjectMapper();
            for (DccPublicKey dccPublicKey : publicKeys.getPublicKeys()) {
                QuickTest quickTest = quickTestMap.get(dccPublicKey.getTestId());
                if (quickTest != null) {
                    log.info("got public key for {} testid", dccPublicKey.getTestId());
                    DgcData dgcData = genDcc(quickTest, dccPublicKey.getPublicKey(), dccPublicKey.getDcci());
                    DccUploadData dccUploadData = new DccUploadData();
                    dccUploadData.setDccHash(Base64.getEncoder().encodeToString(dgcData.getHash()));
                    dccUploadData.setDataEncryptionKey(Base64.getEncoder().encodeToString(dgcData.getDek()));
                    dccUploadData.setDccEnrypted(Base64.getEncoder().encodeToString(dgcData.getDataEncrypted()));
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
     * upload dcc data.
     */
    @Scheduled(fixedDelayString = "${dcc.uploadDccJob.fixedDelayString}")
    @SchedulerLock(name = "QuickTestUploadDcc", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${dcc.uploadDccJob.lockLimit}")
    public void uploadDccData() {
        ObjectMapper objectMapper = new ObjectMapper();
        for (QuickTest quickTest : quickTestRepository.findAllByDccStatus(DccStatus.pendingSignature)) {
            log.info("dcc sign {}", quickTest.getHashedGuid());
            try {
                DccUploadData dccUploadData = objectMapper.readValue(quickTest.getDccSignData(), DccUploadData.class);
                DccUploadResult dccUploadResult = dccServerClient.uploadDcc(quickTest.getHashedGuid(), dccUploadData);
                byte[] coseSigned = dgcGenerator.dgcSetCosePartial(
                        Base64.getDecoder().decode(quickTest.getDccUnsigned()),
                        Base64.getDecoder().decode(dccUploadResult.getPartialDcc()));
                quickTest.setDcc(dgcGenerator.coseToQrCode(coseSigned));
                quickTest.setDccStatus(DccStatus.complete);
                quickTestRepository.saveAndFlush(quickTest);
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
        dgcInitData.setIssuerCode(dccConfig.getIssuer());
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
}

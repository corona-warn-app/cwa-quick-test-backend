package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.config.DccConfig;
import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.DccSignatureData;
import app.coronawarn.quicktest.model.DccUploadData;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.DccTestBuilder;
import eu.europa.ec.dgc.DgcCryptedPublisher;
import eu.europa.ec.dgc.DgcGenerator;
import eu.europa.ec.dgc.DgciGenerator;
import eu.europa.ec.dgc.dto.DgcData;
import eu.europa.ec.dgc.dto.DgcInitData;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DccService {
    private final DccServerClient dccServerClient;
    private final DccConfig dccConfig;
    private final QuickTestRepository quickTestRepository;
    private final DgcCryptedPublisher dgcCryptedPublisher = new DgcCryptedPublisher();
    private final DgcGenerator dgcGenerator = new DgcGenerator();
    private final DgciGenerator dgciGenerator;

    // TODO to be scheduled look up for keys, then generate dcc and prepare for upload
    // should open transaction pro quick test
    public void collectPublicKeys() {
        Map<String,QuickTest> quickTestMap = new HashMap<>();
        List<String> testIds = new ArrayList<>();
        for (QuickTest quickTest : quickTestRepository.findAllByDccStatus(DccStatus.pendingPublicKey)) {
            testIds.add(quickTest.getHashedGuid());
            quickTestMap.put(quickTest.getHashedGuid(), quickTest);
        }
        if (!testIds.isEmpty()) {
            log.info("search publick keys for {} keys",testIds.size());
            Map<String, String> publicKeys = dccServerClient.searchPublicKeys(testIds);
            ObjectMapper objectMapper = new ObjectMapper();
            for (Map.Entry<String, String> entry : publicKeys.entrySet()) {
                QuickTest quickTest = quickTestMap.get(entry.getKey());
                log.info("got public key for {} testid",entry.getKey());
                if (quickTest != null) {
                    DgcData dgcData = genDcc(quickTest, entry.getValue());
                    DccUploadData dccUploadData = new DccUploadData();
                    dccUploadData.setDccHash(Base64.getEncoder().encodeToString(dgcData.getHash()));
                    dccUploadData.setDataEncryptionKey(Base64.getEncoder().encodeToString(dgcData.getDek()));
                    dccUploadData.setDccEnrypted(Base64.getEncoder().encodeToString(dgcData.getDataEncrypted()));
                    try {
                        String dccUploadDataJson = objectMapper.writeValueAsString(dccUploadData);
                        quickTest.setDccStatus(DccStatus.pendingSignature);
                        quickTest.setDccSignData(dccUploadDataJson);
                        quickTest.setPublicKey(entry.getValue());
                        quickTest.setDccUnsigned(Base64.getEncoder().encodeToString(dgcData.getDccData()));
                        quickTestRepository.saveAndFlush(quickTest);
                    } catch (JsonProcessingException e) {
                        log.error("can not create json data",e);
                    }
                }
            }
        }
    }

    // TODO upload dcc data, transaction pro quick test, scheduler
    public void uploadDCCData() {
        ObjectMapper objectMapper = new ObjectMapper();
        for (QuickTest quickTest : quickTestRepository.findAllByDccStatus(DccStatus.pendingSignature)) {
            log.info("dcc sign {}",quickTest.getHashedGuid());
            try {
                DccUploadData dccUploadData = objectMapper.readValue(quickTest.getDccSignData(), DccUploadData.class);
                DccSignatureData dccSignatureData = dccServerClient.uploadDCC(dccUploadData);
                byte[] coseSigned = dgcGenerator.dgcSetCoseSignature(
                        Base64.getDecoder().decode(quickTest.getDccUnsigned()),
                        Base64.getDecoder().decode(dccSignatureData.getSignature()));
                quickTest.setDcc(dgcGenerator.coseToQRCode(coseSigned));
                quickTest.setDccStatus(DccStatus.complete);
                quickTestRepository.saveAndFlush(quickTest);
            } catch (JsonProcessingException e) {
                log.warn("error during signing {}",quickTest.getHashedGuid(),e);
            }
        }
    }

    // TODO this should be called when the public key for test are available
    public DgcData genDcc(QuickTest quickTest, String publicKeyBase64) {
        DgcInitData dgcInitData = new DgcInitData();
        ZonedDateTime created = quickTest.getCreatedAt().atZone(ZoneId.systemDefault());
        long issuetAt = created.toEpochSecond();
        long expiredAt = created.plus(dccConfig.getExpired()).toEpochSecond();
        dgcInitData.setIssuedAt(issuetAt);
        dgcInitData.setExpriation(expiredAt);
        dgcInitData.setIssuerCode(dccConfig.getIssuer());
        dgcInitData.setAlgId(dccConfig.getAlgId());
        dgcInitData.setKeyId(Base64.getDecoder().decode(dccConfig.getKeyId()));
        String dgci = dgciGenerator.newDgci();
        String dccJson = dccJsonFromQuickTest(quickTest, dgci);
        return dgcCryptedPublisher.createDGC(dgcInitData,dccJson,publicKeyFromBase64(publicKeyBase64));
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
                throw new IllegalArgumentException("can not map test result "+quickTest.getTestResult()+ " to positive or negative");
        }
        dccTestBuilder.detected(covidDetected)
                .testTypeRapid(true)
                .dgci(dgci)
                .countryOfTest(dccConfig.getIssuer())
                // TODO is pocid the testing centre
                .testingCentre(quickTest.getPocId())
                // TODO what is dcc sample collection date - we assume the db time is utc already
                .sampleCollection(quickTest.getUpdatedAt())
                // TODO is certificate issuer the tenant id
                .certificateIssuer(quickTest.getTenantId());


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

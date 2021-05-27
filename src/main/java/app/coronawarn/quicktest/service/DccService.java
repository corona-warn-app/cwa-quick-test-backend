package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.config.DccConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.repository.QuickTestRepository;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import liquibase.pro.packaged.D;
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
    // TODO take dgci prefix this from config
    private final DgciGenerator dgciGenerator = new DgciGenerator("URN:UVCI:V1:DE");

    // TODO to be scheduled look up for keys, then generate dcc and prepare for upload
    // should open transaction pro quick test
    public void collectPublicKeys() {
        // TODO collect this in DB
        List<String> testIds = new ArrayList<>();
        Map<String, String> publicKeys = dccServerClient.searchPublicKeys(testIds);
        for (Map.Entry<String,String> entry : publicKeys.entrySet()) {
            Optional<QuickTest> quickTest = quickTestRepository.findById(entry.getKey());
            if (quickTest.isPresent()) {
                DgcData dgcData = genDcc(quickTest.get(),entry.getValue());
            }
        }
    }

    // TODO upload dcc data, transaction pro quick test, scheduler
    public void uploadDCCData() {

    }

    // TODO this should be called when the public key for test are available
    public DgcData genDcc(QuickTest quickTest, String publicKeyBase64) {
        DgcInitData dgcInitData = new DgcInitData();
        ZonedDateTime created = quickTest.getCreatedAt().atZone(ZoneId.systemDefault());
        long issuetAt = created.toEpochSecond();
        long expiredAt = created.plus(dccConfig.getExpired()).toEpochSecond();
        dgcInitData.setIssuedAt(issuetAt);
        dgcInitData.setExpriation(expiredAt);
        dgcInitData.setAlgId(dccConfig.getAlgId());
        dgcInitData.setKeyId(Base64.getDecoder().decode(dccConfig.getKeyId()));
        String dgci = dgciGenerator.newDgci();
        String dccJson = dccJsonFromQuickTest(quickTest, dgci);
        return dgcCryptedPublisher.createDGC(dgcInitData,dccJson,publicKeyFromBase64(publicKeyBase64));
    }

    private String dccJsonFromQuickTest(QuickTest quickTest, String dgci) {
        DccTestBuilder dccTestBuilder = new DccTestBuilder();
        dccTestBuilder.fn(quickTest.getFirstName()).gn(quickTest.getLastName());
        // TODO fill other dcc fields from quicktest
        dccTestBuilder.dgci(dgci);

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

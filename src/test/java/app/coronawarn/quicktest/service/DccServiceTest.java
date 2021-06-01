package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.DccPublicKey;
import app.coronawarn.quicktest.model.DccPublicKeyList;
import app.coronawarn.quicktest.model.DccUploadResult;
import app.coronawarn.quicktest.model.DccUploadData;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import eu.europa.ec.dgc.DgciGenerator;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@Slf4j
@SpringBootTest
class DccServiceTest {
    @Autowired
    DccService dccService;

    @Autowired
    QuickTestRepository quickTestRepository;

    private DgciGenerator dgciGenerator = new DgciGenerator("URN:UVCI:V1:DE");

    @MockBean
    DccServerClient dccServerClient;
    private String publicKeyBase64;

    @Test
    void dccCollectPublicKeys() throws Exception {
        genKeys();
        QuickTest quickTest = getData();
        quickTest.setDccStatus(DccStatus.pendingPublicKey);
        quickTestRepository.saveAndFlush(quickTest);
        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        quickTest.setTestResult((short)6);
        quickTestRepository.saveAndFlush(quickTest);

        initDccMockPublicKey(quickTest);

        dccService.collectPublicKeys();

        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        assertNotNull(quickTest.getPublicKey());
        assertNotNull(quickTest.getDccUnsigned());
        assertNotNull(quickTest.getDccSignData());
        assertEquals(DccStatus.pendingSignature, quickTest.getDccStatus());
    }

    private void initDccMockPublicKey(QuickTest quickTest) {
        DccPublicKeyList dccPublicKeyList = new DccPublicKeyList();
        List<DccPublicKey> publicKeys = new ArrayList<>();
        DccPublicKey dccPublicKey = new DccPublicKey();
        dccPublicKey.setPublicKey(publicKeyBase64);
        dccPublicKey.setTestId(quickTest.getHashedGuid());
        dccPublicKey.setDcci(dgciGenerator.newDgci());
        publicKeys.add(dccPublicKey);
        dccPublicKeyList.setPublicKeys(publicKeys);

        given(dccServerClient.searchPublicKeys(any())).willReturn(dccPublicKeyList);
    }

    @Test
    void dccSignDcc() throws Exception {
        genKeys();
        QuickTest quickTest = getData();
        quickTest.setDccStatus(DccStatus.pendingPublicKey);
        quickTestRepository.saveAndFlush(quickTest);
        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        quickTest.setTestResult((short)6);
        quickTestRepository.saveAndFlush(quickTest);

        initDccMockPublicKey(quickTest);

        DccUploadResult dccUploadResult = new DccUploadResult();
        dccUploadResult.setPartialDcc("0oRDoQEmoQRIDEsVUSvpFAFYLE5DaW8rN3NPRWdVb3UyQktabmh4QWlnT0cxNG0yM1pqUE9OOGlrV" +
                "W01RVU9WEBp4Y4j9Nfh3wCYga4Fc7FnFWyabFJFv7kBASW/yltT9rk98q95SyB3OLKa1p8Y+w+BzgPLE5+6VLL/LHVsMRA6");
        given(dccServerClient.uploadDcc(any(),any(DccUploadData.class))).willReturn(dccUploadResult);

        dccService.collectPublicKeys();

        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        assertNotNull(quickTest.getDccSignData());
        assertEquals(DccStatus.pendingSignature, quickTest.getDccStatus());

        dccService.uploadDccData();

        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        assertEquals(DccStatus.complete, quickTest.getDccStatus());
        assertNotNull(quickTest.getDcc());
        System.out.println(quickTest.getDcc());

    }

    private void genKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(3072);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    private QuickTest getData() throws Exception {
        QuickTest quickTest = new QuickTest();
        quickTest.setZipCode("12345");
        quickTest.setTestResult((short) 6);
        Random random = new Random();
        byte[] rndBytes = new byte[32];
        random.nextBytes(rndBytes);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String hash = Base64.getEncoder().encodeToString(digest.digest(rndBytes));

        quickTest.setHashedGuid(hash);
        quickTest.setCity("oyvkpigcga");
        quickTest.setConfirmationCwa(Boolean.TRUE);
        quickTest.setShortHashedGuid(hash.substring(0,8));
        quickTest.setPhoneNumber("00491777777777777");
        quickTest.setEmail("test@test.test");
        quickTest.setTenantId("4711");
        quickTest.setPocId("4711-A");
        quickTest.setTestBrandId("AT116/21");
        quickTest.setBirthday("2001-02-01");
        quickTest.setTestBrandName("Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)");
        quickTest.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        quickTest.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quickTest.setFirstName("Joe");
        quickTest.setLastName("Miller");
        quickTest.setStandardisedGivenName("JOE");
        quickTest.setStandardisedFamilyName("MILLER");
        quickTest.setStreet("Boe");
        quickTest.setHouseNumber("11");
        quickTest.setPrivacyAgreement(Boolean.FALSE);
        quickTest.setSex(Sex.DIVERSE);
        return quickTest;
    }
}
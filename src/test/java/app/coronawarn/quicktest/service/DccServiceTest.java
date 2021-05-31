package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.DccServerClient;
import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.DccSignatureData;
import app.coronawarn.quicktest.model.DccUploadData;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
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

        Map<String,String> testPublicKey = new HashMap<>();
        testPublicKey.put(quickTest.getHashedGuid(),publicKeyBase64);

        given(dccServerClient.searchPublicKeys(anyList())).willReturn(testPublicKey);

        dccService.collectPublicKeys();

        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        assertNotNull(quickTest.getPublicKey());
        assertNotNull(quickTest.getDccUnsigned());
        assertNotNull(quickTest.getDccSignData());
        assertEquals(DccStatus.pendingSignature, quickTest.getDccStatus());
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

        Map<String,String> testPublicKey = new HashMap<>();
        testPublicKey.put(quickTest.getHashedGuid(),publicKeyBase64);

        given(dccServerClient.searchPublicKeys(anyList())).willReturn(testPublicKey);

        DccSignatureData dccSignatureData = new DccSignatureData();
        dccSignatureData.setSignature("qmC/fFnfBDPWmHN5+w9usV0G3HERoPiM4WyeMsoYGqBNHc3c" +
                "DfUkpYvvcQ34IAsRDFTUw/3fhZtCs3epi9dAPw==");
        given(dccServerClient.uploadDCC(any(DccUploadData.class))).willReturn(dccSignatureData);

        dccService.collectPublicKeys();

        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        assertNotNull(quickTest.getDccSignData());
        assertEquals(DccStatus.pendingSignature, quickTest.getDccStatus());

        dccService.uploadDCCData();

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

    private QuickTest getData() {
        QuickTest quickTest = new QuickTest();
        quickTest.setZipCode("12345");
        quickTest.setTestResult((short) 6);
        quickTest.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quickTest.setCity("oyvkpigcga");
        quickTest.setConfirmationCwa(Boolean.TRUE);
        quickTest.setShortHashedGuid("cjfybkfn");
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
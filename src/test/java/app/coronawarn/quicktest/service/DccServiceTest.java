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
import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.dcc.DccPublicKey;
import app.coronawarn.quicktest.model.dcc.DccUploadResult;
import app.coronawarn.quicktest.model.dcc.DccUploadData;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.DgciGenerator;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Hex;
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

    @Autowired
    QuickTestArchiveRepository quickTestArchiveRepository;

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
        String testIdHashHex = Hex.toHexString(dccService.createSha256Digest()
                .digest(quickTest.getTestResultServerHash().getBytes(StandardCharsets.UTF_8)));

        List<DccPublicKey> publicKeys = new ArrayList<>();
        DccPublicKey dccPublicKey = new DccPublicKey();
        dccPublicKey.setPublicKey(publicKeyBase64);
        dccPublicKey.setTestId(testIdHashHex);
        dccPublicKey.setDcci(dgciGenerator.newDgci());
        publicKeys.add(dccPublicKey);

        given(dccServerClient.searchPublicKeys(any())).willReturn(publicKeys);
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

        QuickTestArchive quickTestArchive = mappingQuickTestToQuickTestArchive(quickTest,"dummy".getBytes(StandardCharsets.UTF_8));
        quickTestArchiveRepository.saveAndFlush(quickTestArchive);

        initDccMockPublicKey(quickTest);

        DccUploadResult dccUploadResult = new DccUploadResult();
        dccUploadResult.setPartialDcc("0oRDoQEmoQRIDEsVUSvpFAFYLE5DaW8rN3NPRWdVb3UyQktabmh4QWlnT0cxNG0yM1pqUE9OOGlrV" +
                "W01RVU9WEBp4Y4j9Nfh3wCYga4Fc7FnFWyabFJFv7kBASW/yltT9rk98q95SyB3OLKa1p8Y+w+BzgPLE5+6VLL/LHVsMRA6");
        given(dccServerClient.uploadDcc(any(),any(DccUploadData.class))).willReturn(dccUploadResult);

        dccService.collectPublicKeys();

        quickTest = quickTestRepository.findById(quickTest.getHashedGuid()).get();
        assertNotNull(quickTest.getDccSignData());
        assertEquals(DccStatus.pendingSignature, quickTest.getDccStatus());
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("\n### upload data ###\n"+quickTest.getDccSignData());


        dccService.uploadDccData();

        List<QuickTest> list = quickTestRepository.findAllById(Collections.singletonList(quickTest.getHashedGuid()));
        assertEquals(0,list.size());

        Optional<QuickTestArchive> quickTestArchiveDb = quickTestArchiveRepository.findById(quickTest.getHashedGuid());
        assertTrue(quickTestArchiveDb.isPresent());
        assertNotNull(quickTestArchiveDb.get().getDcc());
        System.out.println(quickTestArchiveDb.get().getDcc());

    }

    private QuickTestArchive mappingQuickTestToQuickTestArchive(
            QuickTest quickTest, byte[] pdf) {
        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setShortHashedGuid(quickTest.getShortHashedGuid());
        quickTestArchive.setHashedGuid(quickTest.getHashedGuid());
        quickTestArchive.setConfirmationCwa(quickTest.getConfirmationCwa());
        quickTestArchive.setCreatedAt(quickTest.getCreatedAt());
        quickTestArchive.setUpdatedAt(quickTest.getUpdatedAt());
        quickTestArchive.setTenantId(quickTest.getTenantId());
        quickTestArchive.setPocId(quickTest.getPocId());
        quickTestArchive.setTestResult(quickTest.getTestResult());
        quickTestArchive.setPrivacyAgreement(quickTest.getPrivacyAgreement());
        quickTestArchive.setFirstName(quickTest.getFirstName());
        quickTestArchive.setLastName(quickTest.getLastName());
        quickTestArchive.setBirthday(quickTest.getBirthday());
        quickTestArchive.setEmail(quickTest.getEmail());
        quickTestArchive.setPhoneNumber(quickTest.getPhoneNumber());
        quickTestArchive.setSex(quickTest.getSex());
        quickTestArchive.setStreet(quickTest.getStreet());
        quickTestArchive.setHouseNumber(quickTest.getHouseNumber());
        quickTestArchive.setZipCode(quickTest.getZipCode());
        quickTestArchive.setCity(quickTest.getCity());
        quickTestArchive.setTestBrandId(quickTest.getTestBrandId());
        quickTestArchive.setTestBrandName(quickTest.getTestBrandName());
        quickTestArchive.setTestResultServerHash(quickTest.getTestResultServerHash());
        quickTestArchive.setPdf(pdf);
        return quickTestArchive;
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
        random.nextBytes(rndBytes);
        digest.reset();

        quickTest.setTestResultServerHash(Hex.toHexString(digest.digest(rndBytes)));
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

    @Test
    void createTestIdHash() throws Exception {
        MessageDigest digest = dccService.createSha256Digest();
        String testId = "bf7dfeeb7cbe4dab33b79dd04ab39b276fb2b1b405d944e93db9e95c835530e4";
        String testIdHash = "1dff2f115fc7821c2c526103ea92c16870c53fffbe510364d23405fd4872e3aa";
        assertEquals(testIdHash, Hex.toHexString(digest.digest(testId.getBytes(StandardCharsets.UTF_8))));
        digest.reset();
        assertEquals(testIdHash, Hex.toHexString(digest.digest(testId.getBytes(StandardCharsets.UTF_8))));
    }
}

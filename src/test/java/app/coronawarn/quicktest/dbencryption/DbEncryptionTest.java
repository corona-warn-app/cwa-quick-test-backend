/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2020 - 2021 T-Systems International GmbH and all other contributors
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

package app.coronawarn.quicktest.dbencryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.sql.Timestamp;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;

@SpringBootTest
@Slf4j
public class DbEncryptionTest {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final Cipher cipher = AesBytesEncryptor.CipherAlgorithm.CBC.createCipher();
    private final Key key = new SecretKeySpec("abcdefghjklmnopq".getBytes(StandardCharsets.UTF_8), "AES");

    @Autowired
    QuickTestRepository quickTestRepository;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    @AfterEach
    public void setup() {
        quickTestRepository.deleteAll();
    }

    @Test
    public void testThatDiagnosisKeyDataIsStoredEncrypted() {
        QuickTest quickTest = new QuickTest();
        quickTest.setTenantId("08");
        quickTest.setPocId("15");
        quickTest.setHashedGuid("8fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        quickTest.setShortHashedGuid("8fa4dcec");
        quickTest.setConfirmationCwa(true);
        quickTest.setPrivacyAgreement(false);
        quickTest.setLastName("LastName");
        quickTest.setFirstName("FirstName");
        quickTest.setEmail("email@email.email");
        quickTest.setPhoneNumber("0800000001");
        quickTest.setSex(Sex.FEMALE);
        quickTest.setStreet("fakestreet");
        quickTest.setHouseNumber("2a");
        quickTest.setZipCode("10000");
        quickTest.setCity("FakeCity");
        quickTest.setTestBrandId("FakeBrand");
        quickTest.setTestBrandName("FakeId");
        quickTest = quickTestRepository.saveAndFlush(quickTest);

        Object databaseEntry =
            entityManager.createNativeQuery("SELECT * FROM quick_test q WHERE HASHED_GUID='" +
                "8fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4'")
                .getSingleResult();

        assertEquals(quickTest.getHashedGuid(), ((Object[]) databaseEntry)[0]);
        assertEquals(quickTest.getShortHashedGuid(), ((Object[]) databaseEntry)[1]);
        assertEquals(quickTest.getTenantId(), ((Object[]) databaseEntry)[2]);
        assertEquals(quickTest.getPocId(), ((Object[]) databaseEntry)[3]);
        assertEquals(quickTest.getCreatedAt().withNano(0),
            ((Timestamp) ((Object[]) databaseEntry)[4]).toLocalDateTime().withNano(0));
        assertEquals(quickTest.getUpdatedAt().withNano(0),
            ((Timestamp) ((Object[]) databaseEntry)[5]).toLocalDateTime().withNano(0));
        assertEquals(quickTest.getVersion(), ((Object[]) databaseEntry)[6]);

        assertNotEquals(quickTest.getConfirmationCwa(), ((Object[]) databaseEntry)[7]);
        assertNotEquals(quickTest.getTestResult(), ((Object[]) databaseEntry)[8]);
        assertNotEquals(quickTest.getFirstName(), ((Object[]) databaseEntry)[9]);
        assertNotEquals(quickTest.getLastName(), ((Object[]) databaseEntry)[10]);
        assertNotEquals(quickTest.getEmail(), ((Object[]) databaseEntry)[11]);
        assertNotEquals(quickTest.getPhoneNumber(), ((Object[]) databaseEntry)[12]);
        assertNotEquals(quickTest.getSex(), ((Object[]) databaseEntry)[13]);
        assertNotEquals(quickTest.getStreet(), ((Object[]) databaseEntry)[14]);
        assertNotEquals(quickTest.getHouseNumber(), ((Object[]) databaseEntry)[15]);
        assertNotEquals(quickTest.getZipCode(), ((Object[]) databaseEntry)[16]);
        assertNotEquals(quickTest.getCity(), ((Object[]) databaseEntry)[17]);
        assertNotEquals(quickTest.getTestBrandId(), ((Object[]) databaseEntry)[18]);
        assertNotEquals(quickTest.getTestBrandName(), ((Object[]) databaseEntry)[19]);
        assertNotEquals(quickTest.getPrivacyAgreement(), ((Object[]) databaseEntry)[20]);
        try {
            assertEquals(quickTest.getConfirmationCwa(), Boolean.valueOf(new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[7]))), CHARSET)));

            assertEquals(quickTest.getTestResult(), Short.valueOf(new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[8]))), CHARSET)));

            assertEquals(quickTest.getFirstName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[9]))), CHARSET));

            assertEquals(quickTest.getLastName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[10]))), CHARSET));

            assertEquals(quickTest.getEmail(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[11]))), CHARSET));

            assertEquals(quickTest.getPhoneNumber(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[12]))), CHARSET));

            assertEquals(quickTest.getSex().name(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[13]))), CHARSET));

            assertEquals(quickTest.getStreet(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[14]))), CHARSET));

            assertEquals(quickTest.getHouseNumber(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[15]))), CHARSET));

            assertEquals(quickTest.getZipCode(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[16]))), CHARSET));

            assertEquals(quickTest.getCity(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[17]))), CHARSET));

            assertEquals(quickTest.getTestBrandId(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[18]))), CHARSET));

            assertEquals(quickTest.getTestBrandName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[19]))), CHARSET));

            assertEquals(quickTest.getPrivacyAgreement(),
                Boolean.valueOf(new String(decrypt(Base64.getDecoder().decode(
                    String.valueOf(((Object[]) databaseEntry)[20]))), CHARSET)));

        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }


    private byte[] decrypt(byte[] encrypted)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        synchronized (cipher) {
            cipher.init(Cipher.DECRYPT_MODE, key, getInitializationVector());
            return cipher.doFinal(encrypted);
        }
    }

    private byte[] encrypt(byte[] plain)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        synchronized (cipher) {
            cipher.init(Cipher.ENCRYPT_MODE, key, getInitializationVector());
            return cipher.doFinal(plain);
        }

    }

    private IvParameterSpec getInitializationVector() {
        return new IvParameterSpec("WnU2IQhlAAN@bK~L".getBytes(CHARSET));
    }
}

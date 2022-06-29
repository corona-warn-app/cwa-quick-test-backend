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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.utils.Utilities;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureCache
@ImportAutoConfiguration
@Slf4j
public class DbEncryptionTest {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final Cipher decryptCipher = AesBytesEncryptor.CipherAlgorithm.GCM.createCipher();
    private final Key key = new SecretKeySpec("abcdefghjklmnopq".getBytes(StandardCharsets.UTF_8), "AES");

    @Autowired
    QuickTestArchiveRepository quickTestArchiveRepository;

    @Autowired
    EntityManager entityManager;

    @MockBean
    Utilities utilities;

    @BeforeEach
    @AfterEach
    public void setup() {
        quickTestArchiveRepository.deleteAll();
    }

    @Test
    public void testThatDiagnosisKeyDataIsStoredEncrypted() {
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setShortHashedGuid("8fa4dcec");
        quickTestArchive.setHashedGuid("8fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        quickTestArchive.setTenantId("08");
        quickTestArchive.setTestResult((short) 5);
        quickTestArchive.setPocId("15");
        quickTestArchive.setCreatedAt(LocalDateTime.now());
        quickTestArchive.setUpdatedAt(LocalDateTime.now());
        quickTestArchive.setConfirmationCwa(true);
        quickTestArchive.setPrivacyAgreement(false);
        quickTestArchive.setLastName("LastName");
        quickTestArchive.setFirstName("FirstName");
        quickTestArchive.setEmail("email@email.email");
        quickTestArchive.setPhoneNumber("0800000001");
        quickTestArchive.setSex(Sex.FEMALE);
        quickTestArchive.setStreet("fakestreet");
        quickTestArchive.setHouseNumber("2a");
        quickTestArchive.setZipCode("10000");
        quickTestArchive.setCity("FakeCity");
        quickTestArchive.setTestBrandId("FakeBrand");
        quickTestArchive.setTestBrandName("FakeId");
        quickTestArchive.setBirthday("01.02.1990");
        quickTestArchive.setTestResultServerHash("f1a8a9da03155aa760e0c38f9bed645c48fa4dcecf716d8dd96c9e927dda5484");
        quickTestArchive.setPdf(pdf.toByteArray());
        quickTestArchive = quickTestArchiveRepository.saveAndFlush(quickTestArchive);
        Object databaseEntry =
            entityManager.createNativeQuery("SELECT * FROM quick_test_archive q WHERE " +
                "HASHED_GUID='" +
                "8fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4'")
                .getSingleResult();
        assertEquals(quickTestArchive.getShortHashedGuid(), ((Object[]) databaseEntry)[0]);
        assertEquals(quickTestArchive.getHashedGuid(), ((Object[]) databaseEntry)[1]);
        assertEquals(quickTestArchive.getTenantId(), ((Object[]) databaseEntry)[2]);
        assertEquals(quickTestArchive.getPocId(), ((Object[]) databaseEntry)[3]);
        assertEquals(quickTestArchive.getCreatedAt().withNano(0),
            ((Timestamp) ((Object[]) databaseEntry)[4]).toLocalDateTime().withNano(0));
        assertEquals(quickTestArchive.getUpdatedAt().withNano(0),
            ((Timestamp) ((Object[]) databaseEntry)[5]).toLocalDateTime().withNano(0));
        assertNotEquals(quickTestArchive.getConfirmationCwa(), ((Object[]) databaseEntry)[7]);
        assertEquals(quickTestArchive.getTestResult(), ((Object[]) databaseEntry)[8]);
        assertNotEquals(quickTestArchive.getFirstName(), ((Object[]) databaseEntry)[9]);
        assertNotEquals(quickTestArchive.getLastName(), ((Object[]) databaseEntry)[10]);
        assertNotEquals(quickTestArchive.getEmail(), ((Object[]) databaseEntry)[11]);
        assertNotEquals(quickTestArchive.getPhoneNumber(), ((Object[]) databaseEntry)[12]);
        assertNotEquals(quickTestArchive.getSex(), ((Object[]) databaseEntry)[13]);
        assertNotEquals(quickTestArchive.getStreet(), ((Object[]) databaseEntry)[14]);
        assertNotEquals(quickTestArchive.getHouseNumber(), ((Object[]) databaseEntry)[15]);
        assertNotEquals(quickTestArchive.getZipCode(), ((Object[]) databaseEntry)[16]);
        assertNotEquals(quickTestArchive.getCity(), ((Object[]) databaseEntry)[17]);
        assertNotEquals(quickTestArchive.getTestBrandId(), ((Object[]) databaseEntry)[18]);
        assertNotEquals(quickTestArchive.getTestBrandName(), ((Object[]) databaseEntry)[19]);
        assertNotEquals(quickTestArchive.getBirthday(), ((Object[]) databaseEntry)[20]);
        assertNotEquals(quickTestArchive.getPrivacyAgreement(), ((Object[]) databaseEntry)[21]);
        assertNotEquals(quickTestArchive.getTestResultServerHash(), ((Object[]) databaseEntry)[22]);
        try {
            assertEquals(quickTestArchive.getConfirmationCwa(),
                Boolean.valueOf(new String(decrypt(Base64.getDecoder().decode(
                    String.valueOf(((Object[]) databaseEntry)[7]))), CHARSET)));

            assertEquals(quickTestArchive.getPrivacyAgreement(),
                Boolean.valueOf(new String(decrypt(Base64.getDecoder().decode(
                    String.valueOf(((Object[]) databaseEntry)[9]))), CHARSET)));

            assertEquals(quickTestArchive.getFirstName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[10]))), CHARSET));

            assertEquals(quickTestArchive.getLastName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[11]))), CHARSET));

            assertEquals(quickTestArchive.getEmail(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[12]))), CHARSET));

            assertEquals(quickTestArchive.getPhoneNumber(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[13]))), CHARSET));

            assertEquals(quickTestArchive.getSex().name(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[14]))), CHARSET));

            assertEquals(quickTestArchive.getStreet(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[15]))), CHARSET));

            assertEquals(quickTestArchive.getHouseNumber(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[16]))), CHARSET));

            assertEquals(quickTestArchive.getZipCode(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[17]))), CHARSET));

            assertEquals(quickTestArchive.getCity(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[18]))), CHARSET));

            assertEquals(quickTestArchive.getTestBrandId(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[19]))), CHARSET));

            assertEquals(quickTestArchive.getTestBrandName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[20]))), CHARSET));

            assertArrayEquals(quickTestArchive.getPdf(),
                decrypt(Base64.getDecoder()
                    .decode(IOUtils.toByteArray(((Clob) ((Object[]) databaseEntry)[21]).getAsciiStream()))));

            assertEquals(quickTestArchive.getTestResultServerHash(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[22]))), CHARSET));

            assertEquals(quickTestArchive.getBirthday(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[23]))), CHARSET));

        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | SQLException | IOException e) {
            fail();
            e.printStackTrace();
        }
    }

    private byte[] decrypt(byte[] ciphertext)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        synchronized (decryptCipher) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(ciphertext);
            byte[] iv = new byte[12];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);
            decryptCipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return decryptCipher.doFinal(encrypted);
        }
    }
}

package app.coronawarn.quicktest.migration.v001tov002;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestArchiveMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestArchiveRepositoryMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestRepositoryMigrationV001;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
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
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import lombok.SneakyThrows;
import org.apache.pdfbox.io.IOUtils;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@ImportAutoConfiguration
class MigrateDataFromV001toV002Test {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private final Cipher cipher = AesBytesEncryptor.CipherAlgorithm.CBC.createCipher();
    private final Key key = new SecretKeySpec("abcdefghjklmnopq".getBytes(StandardCharsets.UTF_8), "AES");
    private final Cipher decryptCipher = AesBytesEncryptor.CipherAlgorithm.GCM.createCipher();

    @Autowired
    QuickTestRepository quickTestRepository;
    @Autowired
    QuickTestArchiveRepository quickTestArchiveRepository;
    @Autowired
    QuickTestRepositoryMigrationV001 quickTestRepositoryMigrationV001;
    @Autowired
    QuickTestArchiveRepositoryMigrationV001 quickTestArchiveRepositoryMigrationV001;

    @InjectMocks
    MigrateDataFromV001toV002 migrateDataFromV001toV002;

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    ModelMapper modelMapper;

    QuickTestMigrationV001 quickTestMigrationV001;
    QuickTestArchiveMigrationV001 quickTestArchiveMigrationV001;

    @BeforeEach
    public void setup() {
        createMigrationTables();
        quickTestArchiveRepository.deleteAll();
        quickTestRepository.deleteAll();
    }

    @AfterEach
    public void cleanup() {
        dropMigrationTables();
        quickTestArchiveRepository.deleteAll();
        quickTestRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    void migrationV001ToV002Test() {
        quickTestMigrationV001 = createQuickTestMigrationV001();
        quickTestArchiveMigrationV001 = createQuickTestArchiveMigrationV001();

        Session session = (Session) entityManager.getEntityManager().getDelegate();
        session.doWork(connection -> {
            try {
                createQuickTestInOldDb(quickTestMigrationV001);
                createQuickTestArchiveInOldDb(quickTestArchiveMigrationV001);
                Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                migrateDataFromV001toV002.setUp();
                migrateDataFromV001toV002.execute(database);
                entityManager.flush();

                assertEqualsQuickTest(
                    quickTestRepositoryMigrationV001.findById(quickTestMigrationV001.getHashedGuid()).get(),
                    quickTestRepository.findById(quickTestMigrationV001.getHashedGuid()).get()
                );
                testDecyrptionQuickTest(quickTestRepository.findById(quickTestMigrationV001.getHashedGuid()).get());

                assertEqualsQuickTestArchive(
                    quickTestArchiveRepositoryMigrationV001.findById(quickTestArchiveMigrationV001.getHashedGuid())
                        .get(),
                    quickTestArchiveRepository.findById(quickTestArchiveMigrationV001.getHashedGuid()).get()
                );
                testDecyrptionQuickTestArchive(
                    quickTestArchiveRepository.findById(quickTestArchiveMigrationV001.getHashedGuid()).get());
            } catch (Exception e) {
                fail();
            }
        });
    }

    private void createQuickTestInOldDb(QuickTestMigrationV001 quickTestMigrationV001)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        entityManager.getEntityManager().createNativeQuery("INSERT INTO QUICK_TEST_V001 (HASHED_GUID," +
            "SHORT_HASHED_GUID,TENANT_ID," +
            "POC_ID,CREATED_AT,UPDATED_AT,VERSION,CONFIRMATION_CWA,TEST_RESULT,FIRST_NAME,LAST_NAME,EMAIL, " +
            "PHONE_NUMBER,SEX,STREET,HOUSE_NUMBER,ZIP_CODE,CITY,TEST_BRAND_ID,TEST_BRAND_NAME,BIRTHDAY, " +
            "PRIVACY_AGREEMENT,TEST_RESULT_SERVER_HASH) VALUES " +
            "('" + quickTestMigrationV001.getHashedGuid() + "'," +
            "'" + quickTestMigrationV001.getShortHashedGuid() + "'," +
            "'" + quickTestMigrationV001.getTenantId() + "'," +
            "'" + quickTestMigrationV001.getPocId() + "'," +
            "'2021-04-19 10:52:05'," +
            "'2021-04-19 10:53:05'," +
            "'" + 11 + "'," +
            "'" + encrypt(quickTestMigrationV001.getConfirmationCwa().toString().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getTestResult().toString().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getFirstName().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getLastName().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getEmail().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getPhoneNumber().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getSex().toString().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getStreet().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getHouseNumber().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getZipCode().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getCity().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getTestBrandId().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getTestBrandName().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getBirthday().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getPrivacyAgreement().toString().getBytes()) + "'," +
            "'" + encrypt(quickTestMigrationV001.getTestResultServerHash().getBytes()) + "');"
        ).executeUpdate();
        entityManager.flush();
    }

    private void createQuickTestArchiveInOldDb(QuickTestArchiveMigrationV001 quickTestArchiveMigrationV001)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        entityManager.getEntityManager()
            .createNativeQuery("INSERT INTO QUICK_TEST_ARCHIVE_V001 (SHORT_HASHED_GUID,HASHED_GUID," +
                "TENANT_ID," +
                "POC_ID,CREATED_AT,UPDATED_AT,VERSION,CONFIRMATION_CWA,TEST_RESULT,FIRST_NAME,LAST_NAME,EMAIL, " +
                "PHONE_NUMBER,SEX,STREET,HOUSE_NUMBER,ZIP_CODE,CITY,TEST_BRAND_ID,TEST_BRAND_NAME,PDF,BIRTHDAY, " +
                "PRIVACY_AGREEMENT,TEST_RESULT_SERVER_HASH) VALUES " +
                "('" + quickTestArchiveMigrationV001.getShortHashedGuid() + "'," +
                "'" + quickTestArchiveMigrationV001.getHashedGuid() + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getTenantId().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getPocId().getBytes()) + "'," +
                "'2021-04-19 10:52:05'," +
                "'2021-04-19 10:53:05'," +
                "'" + 15 + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getConfirmationCwa().toString().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getTestResult().toString().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getFirstName().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getLastName().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getEmail().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getPhoneNumber().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getSex().toString().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getStreet().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getHouseNumber().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getZipCode().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getCity().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getTestBrandId().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getTestBrandName().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getPdf()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getBirthday().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getPrivacyAgreement().toString().getBytes()) + "'," +
                "'" + encrypt(quickTestArchiveMigrationV001.getTestResultServerHash().getBytes()) + "');"
            ).executeUpdate();
        entityManager.flush();
    }

    private QuickTestMigrationV001 createQuickTestMigrationV001() {
        QuickTestMigrationV001 quickTest = new QuickTestMigrationV001();
        quickTest.setZipCode("12345");
        quickTest.setTestResult(Short.parseShort("5"));
        quickTest.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quickTest.setCity("oyvkpigcga");
        quickTest.setConfirmationCwa(Boolean.TRUE);
        quickTest.setShortHashedGuid("cjfybkfn");
        quickTest.setPhoneNumber("00491777777777777");
        quickTest.setEmail("test@test.test");
        quickTest.setTenantId("4711");
        quickTest.setPocId("4711-A");
        quickTest.setTestBrandId("AT116/21");
        quickTest.setTestBrandName("Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)");
        quickTest.setFirstName("Joe");
        quickTest.setLastName("Miller");
        quickTest.setStreet("Boe");
        quickTest.setHouseNumber("11");
        quickTest.setPrivacyAgreement(Boolean.FALSE);
        quickTest.setSex(Sex.DIVERSE);
        quickTest.setBirthday("01.01.1990");
        quickTest.setTestResultServerHash("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        return quickTest;
    }

    private QuickTestArchiveMigrationV001 createQuickTestArchiveMigrationV001() {
        QuickTestArchiveMigrationV001 quickTest = new QuickTestArchiveMigrationV001();
        quickTest.setZipCode("12345");
        quickTest.setTestResult(Short.parseShort("5"));
        quickTest.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quickTest.setCity("oyvkpigcga");
        quickTest.setConfirmationCwa(Boolean.TRUE);
        quickTest.setShortHashedGuid("cjfybkfn");
        quickTest.setPhoneNumber("00491777777777777");
        quickTest.setEmail("test@test.test");
        quickTest.setTenantId("4711");
        quickTest.setPocId("4711-A");
        quickTest.setTestBrandId("AT116/21");
        quickTest.setTestBrandName("Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)");
        quickTest.setFirstName("Joe");
        quickTest.setLastName("Miller");
        quickTest.setStreet("Boe");
        quickTest.setHouseNumber("11");
        quickTest.setPrivacyAgreement(Boolean.FALSE);
        quickTest.setSex(Sex.DIVERSE);
        quickTest.setBirthday("01.01.1990");
        quickTest.setPdf("testPDF".getBytes());
        quickTest.setTestResultServerHash("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        return quickTest;
    }

    private String encrypt(byte[] plain)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        synchronized (cipher) {
            cipher.init(Cipher.ENCRYPT_MODE, key, getInitializationVector());
            return Base64.getEncoder().encodeToString(cipher.doFinal(plain));
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

    private IvParameterSpec getInitializationVector() {
        return new IvParameterSpec("WnU2IQhlAAN@bK~L".getBytes(CHARSET));
    }

    private void assertEqualsQuickTest(QuickTestMigrationV001 expected, QuickTest act) {
        assertEquals(expected.getHashedGuid(), act.getHashedGuid());
        assertEquals(expected.getShortHashedGuid(), act.getShortHashedGuid());
        assertEquals(expected.getTenantId(), act.getTenantId());
        assertEquals(expected.getPocId(), act.getPocId());
        assertEquals(expected.getCreatedAt(), act.getCreatedAt());
        assertEquals(expected.getCreatedAt(), act.getCreatedAt());
//        assertEquals(expected.getVersion(), act.getVersion());
        assertEquals(expected.getConfirmationCwa(), act.getConfirmationCwa());
        assertEquals(expected.getFirstName(), act.getFirstName());
        assertEquals(expected.getLastName(), act.getLastName());
        assertEquals(expected.getEmail(), act.getEmail());
        assertEquals(expected.getPhoneNumber(), act.getPhoneNumber());
        assertEquals(expected.getSex(), act.getSex());
        assertEquals(expected.getStreet(), act.getStreet());
        assertEquals(expected.getHouseNumber(), act.getHouseNumber());
        assertEquals(expected.getZipCode(), act.getZipCode());
        assertEquals(expected.getCity(), act.getCity());
        assertEquals(expected.getBirthday(), act.getBirthday());
        assertEquals(expected.getPrivacyAgreement(), act.getPrivacyAgreement());
        assertEquals(expected.getTestResultServerHash(), act.getTestResultServerHash());
    }

    private void assertEqualsQuickTestArchive(QuickTestArchiveMigrationV001 expected, QuickTestArchive act) {
        assertEquals(expected.getHashedGuid(), act.getHashedGuid());
        assertEquals(expected.getShortHashedGuid(), act.getShortHashedGuid());
        assertEquals(expected.getTenantId(), act.getTenantId());
        assertEquals(expected.getPocId(), act.getPocId());
        assertEquals(expected.getCreatedAt(), act.getCreatedAt());
        assertEquals(expected.getCreatedAt(), act.getCreatedAt());
//        assertEquals(expected.getVersion(), act.getVersion());
        assertEquals(expected.getConfirmationCwa(), act.getConfirmationCwa());
        assertEquals(expected.getFirstName(), act.getFirstName());
        assertEquals(expected.getLastName(), act.getLastName());
        assertEquals(expected.getEmail(), act.getEmail());
        assertEquals(expected.getPhoneNumber(), act.getPhoneNumber());
        assertEquals(expected.getSex(), act.getSex());
        assertEquals(expected.getStreet(), act.getStreet());
        assertEquals(expected.getHouseNumber(), act.getHouseNumber());
        assertEquals(expected.getZipCode(), act.getZipCode());
        assertEquals(expected.getCity(), act.getCity());
        assertEquals(expected.getBirthday(), act.getBirthday());
        assertEquals(expected.getPrivacyAgreement(), act.getPrivacyAgreement());
        assertEquals(expected.getTestResultServerHash(), act.getTestResultServerHash());
        assertEquals(new String(expected.getPdf()), new String(act.getPdf()));
    }

    private void testDecyrptionQuickTest(QuickTest quickTest) {
        Object databaseEntry =
            entityManager.getEntityManager().createNativeQuery("SELECT * FROM quick_test q WHERE HASHED_GUID='" +
                quickTest.getHashedGuid() + "'")
                .getSingleResult();
        assertEquals(quickTest.getShortHashedGuid(), ((Object[]) databaseEntry)[0]);
        assertEquals(quickTest.getHashedGuid(), ((Object[]) databaseEntry)[1]);
        assertEquals(quickTest.getTenantId(), ((Object[]) databaseEntry)[2]);
        assertEquals(quickTest.getPocId(), ((Object[]) databaseEntry)[3]);
        assertEquals(quickTest.getCreatedAt().withNano(0),
            ((Timestamp) ((Object[]) databaseEntry)[4]).toLocalDateTime().withNano(0));
        assertEquals(quickTest.getUpdatedAt().withNano(0),
            ((Timestamp) ((Object[]) databaseEntry)[5]).toLocalDateTime().withNano(0));
        assertNotEquals(quickTest.getConfirmationCwa(), ((Object[]) databaseEntry)[7]);
        assertEquals(quickTest.getTestResult(), ((Object[]) databaseEntry)[8]);
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
        assertNotEquals(quickTest.getBirthday(), ((Object[]) databaseEntry)[20]);
        assertNotEquals(quickTest.getPrivacyAgreement(), ((Object[]) databaseEntry)[21]);
        assertNotEquals(quickTest.getTestResultServerHash(), ((Object[]) databaseEntry)[22]);
        try {
            assertEquals(quickTest.getConfirmationCwa(), Boolean.valueOf(new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[7]))), CHARSET)));

            assertEquals(quickTest.getPrivacyAgreement(), Boolean.valueOf(new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[9]))), CHARSET)));

            assertEquals(quickTest.getFirstName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[10]))), CHARSET));

            assertEquals(quickTest.getLastName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[11]))), CHARSET));

            assertEquals(quickTest.getEmail(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[12]))), CHARSET));

            assertEquals(quickTest.getPhoneNumber(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[13]))), CHARSET));

            assertEquals(quickTest.getSex().name(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[14]))), CHARSET));

            assertEquals(quickTest.getStreet(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[15]))), CHARSET));

            assertEquals(quickTest.getHouseNumber(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[16]))), CHARSET));

            assertEquals(quickTest.getZipCode(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[17]))), CHARSET));

            assertEquals(quickTest.getCity(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[18]))), CHARSET));

            assertEquals(quickTest.getTestBrandId(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[19]))), CHARSET));

            assertEquals(quickTest.getTestBrandName(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[20]))), CHARSET));

            assertEquals(quickTest.getTestResultServerHash(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[21]))), CHARSET));

            assertEquals(quickTest.getBirthday(), new String(decrypt(Base64.getDecoder().decode(
                String.valueOf(((Object[]) databaseEntry)[22]))), CHARSET));

        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            fail();
            e.printStackTrace();
        }
    }

    private void testDecyrptionQuickTestArchive(QuickTestArchive quickTestArchive) {
        Object databaseEntry =
            entityManager.getEntityManager()
                .createNativeQuery("SELECT * FROM quick_test_archive q WHERE HASHED_GUID='" +
                    quickTestArchive.getHashedGuid() + "'")
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
        assertNotEquals(quickTestArchive.getPdf(), ((Object[]) databaseEntry)[20]);
        assertNotEquals(quickTestArchive.getTestResultServerHash(), ((Object[]) databaseEntry)[21]);
        assertNotEquals(quickTestArchive.getBirthday(), ((Object[]) databaseEntry)[22]);
        assertNotEquals(quickTestArchive.getPrivacyAgreement(), ((Object[]) databaseEntry)[23]);
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

    private void createMigrationTables() {
        entityManager.getEntityManager()
            .createNativeQuery("CREATE TABLE QUICK_TEST_V001 (HASHED_GUID varchar(108),SHORT_HASHED_GUID " +
                "varchar(108),TENANT_ID varchar(255)," +
                "POC_ID varchar(255),CREATED_AT datetime,UPDATED_AT datetime,VERSION int,CONFIRMATION_CWA varchar(50)," +
                "TEST_RESULT varchar(24),FIRST_NAME varchar(200),LAST_NAME varchar(200),EMAIL varchar(550), " +
                "PHONE_NUMBER varchar(200),SEX varchar(60),STREET varchar(550),HOUSE_NUMBER varchar(70),ZIP_CODE varchar" +
                "(50),CITY varchar(550),TEST_BRAND_ID varchar(70),TEST_BRAND_NAME varchar(200),BIRTHDAY varchar(550)," +
                "PRIVACY_AGREEMENT varchar(50),TEST_RESULT_SERVER_HASH varchar(170))").executeUpdate();

        entityManager.getEntityManager()
            .createNativeQuery("CREATE TABLE QUICK_TEST_ARCHIVE_V001 (HASHED_GUID varchar(108)," +
                "SHORT_HASHED_GUID varchar(108),TENANT_ID varchar(255)," +
                "POC_ID varchar(255),CREATED_AT datetime,UPDATED_AT datetime,VERSION int,CONFIRMATION_CWA varchar(50)," +
                "TEST_RESULT varchar(24),FIRST_NAME varchar(200),LAST_NAME varchar(200),EMAIL varchar(550), " +
                "PHONE_NUMBER varchar(200),SEX varchar(60),STREET varchar(550),HOUSE_NUMBER varchar(70),ZIP_CODE varchar" +
                "(50),CITY varchar(550),TEST_BRAND_ID varchar(70),TEST_BRAND_NAME varchar(200),PDF clob,BIRTHDAY varchar" +
                "(550)," +
                "PRIVACY_AGREEMENT varchar(50),TEST_RESULT_SERVER_HASH varchar(170))").executeUpdate();
        entityManager.flush();
    }

    private void dropMigrationTables() {
        entityManager.getEntityManager().createNativeQuery("DROP TABLE QUICK_TEST_V001").executeUpdate();
        entityManager.getEntityManager().createNativeQuery("DROP TABLE QUICK_TEST_ARCHIVE_V001").executeUpdate();

        entityManager.flush();
    }

}

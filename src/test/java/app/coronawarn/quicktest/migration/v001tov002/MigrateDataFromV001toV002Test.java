package app.coronawarn.quicktest.migration.v001tov002;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.postgresql.hostchooser.HostRequirement.any;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestArchiveMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.domain.QuickTestMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestArchiveRepositoryMigrationV001;
import app.coronawarn.quicktest.migration.v001tov002.repository.QuickTestRepositoryMigrationV001;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.sql.Clob;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
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
    EntityManager entityManager;

    @Autowired
    ModelMapper modelMapper;

    @BeforeEach
    public void setup() {
    }

    @Test
    void migrationV001ToV002Test()
        throws SetupException, CustomChangeException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
        BadPaddingException, InvalidKeyException {
        QuickTestMigrationV001 quickTestMigrationV001 = createQuickTestMigrationV001();
        QuickTestArchiveMigrationV001 quickTestArchiveMigrationV001 = createQuickTestArchiveMigrationV001();

        createQuickTestInOldDb(quickTestMigrationV001);
        createQuickTestArchiveInOldDb(quickTestArchiveMigrationV001);


        migrateDataFromV001toV002.setUp();
        migrateDataFromV001toV002.execute(null);
        QuickTestMigrationV001 quickTestMigrationV0011 =
            modelMapper.map(quickTestRepository.findById(quickTestMigrationV001.getHashedGuid()).get(),
            QuickTestMigrationV001.class);

        assertEquals(
            modelMapper.map(quickTestRepository.findById(quickTestMigrationV001.getHashedGuid()).get(),
                QuickTestMigrationV001.class),
            modelMapper.map(quickTestRepositoryMigrationV001.findById(quickTestMigrationV001.getHashedGuid()).get(),
                QuickTestMigrationV001.class)
        );
        assertEquals(
            modelMapper.map(quickTestArchiveRepository.findById(quickTestArchiveMigrationV001.getHashedGuid()).get(),
                QuickTestArchiveMigrationV001.class),
            modelMapper.map( quickTestArchiveRepositoryMigrationV001.findById(quickTestArchiveMigrationV001.getHashedGuid()).get(),
                QuickTestArchiveMigrationV001.class)
        );

    }

    private void createQuickTestInOldDb(QuickTestMigrationV001 quickTestMigrationV001)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        entityManager.createNativeQuery("INSERT INTO QUICK_TEST_V001 (HASHED_GUID,SHORT_HASHED_GUID,TENANT_ID," +
            "POC_ID,CREATED_AT,UPDATED_AT,VERSION,CONFIRMATION_CWA,TEST_RESULT,FIRST_NAME,LAST_NAME,EMAIL, " +
            "PHONE_NUMBER,SEX,STREET,HOUSE_NUMBER,ZIP_CODE,CITY,TEST_BRAND_ID,TEST_BRAND_NAME,BIRTHDAY, " +
            "PRIVACY_AGREEMENT,TEST_RESULT_SERVER_HASH) VALUES " +
            "('"+ quickTestMigrationV001.getHashedGuid() +"',"+
            "'"+ quickTestMigrationV001.getShortHashedGuid() +"',"+
            "'"+ quickTestMigrationV001.getTenantId() +"',"+
            "'"+ quickTestMigrationV001.getPocId() +"',"+
            "'2021-04-19 10:52:05',"+
            "'2021-04-19 10:53:05',"+
            "'"+ 0 +"',"+
            "'"+ encrypt(quickTestMigrationV001.getConfirmationCwa().toString().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getTestResult().toString().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getFirstName().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getLastName().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getEmail().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getPhoneNumber().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getSex().toString().getBytes())+"',"+
            "'"+ encrypt(quickTestMigrationV001.getStreet().getBytes())+"',"+
            "'"+ encrypt(quickTestMigrationV001.getHouseNumber().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getZipCode().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getCity().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getTestBrandId().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getTestBrandName().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getBirthday().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getPrivacyAgreement().toString().getBytes()) +"',"+
            "'"+ encrypt(quickTestMigrationV001.getTestResultServerHash().getBytes()) +"');"
        ).executeUpdate();
        entityManager.flush();
    }

    private void createQuickTestArchiveInOldDb(QuickTestArchiveMigrationV001 quickTestArchiveMigrationV001)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        entityManager.createNativeQuery("INSERT INTO QUICK_TEST_ARCHIVE_V001 (SHORT_HASHED_GUID,HASHED_GUID," +
            "TENANT_ID," +
            "POC_ID,CREATED_AT,UPDATED_AT,VERSION,CONFIRMATION_CWA,TEST_RESULT,FIRST_NAME,LAST_NAME,EMAIL, " +
            "PHONE_NUMBER,SEX,STREET,HOUSE_NUMBER,ZIP_CODE,CITY,TEST_BRAND_ID,TEST_BRAND_NAME,PDF,BIRTHDAY, " +
            "PRIVACY_AGREEMENT,TEST_RESULT_SERVER_HASH) VALUES " +
            "('"+ quickTestArchiveMigrationV001.getShortHashedGuid() +"',"+
            "'"+ quickTestArchiveMigrationV001.getHashedGuid() +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getTenantId().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getPocId().getBytes()) +"',"+
            "'2021-04-19 10:52:05',"+
            "'2021-04-19 10:53:05',"+
            "'"+ 0 +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getConfirmationCwa().toString().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getTestResult().toString().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getFirstName().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getLastName().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getEmail().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getPhoneNumber().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getSex().toString().getBytes())+"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getStreet().getBytes())+"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getHouseNumber().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getZipCode().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getCity().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getTestBrandId().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getTestBrandName().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getPdf()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getBirthday().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getPrivacyAgreement().toString().getBytes()) +"',"+
            "'"+ encrypt(quickTestArchiveMigrationV001.getTestResultServerHash().getBytes()) +"');"
        ).executeUpdate();
        entityManager.flush();
    }

    private QuickTestMigrationV001 createQuickTestMigrationV001(){
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

    private QuickTestArchiveMigrationV001 createQuickTestArchiveMigrationV001(){
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
        quickTest.setPdf("".getBytes());
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
}

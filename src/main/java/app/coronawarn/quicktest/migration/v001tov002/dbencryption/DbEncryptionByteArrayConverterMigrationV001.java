package app.coronawarn.quicktest.migration.v001tov002.dbencryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.persistence.AttributeConverter;
import javax.persistence.PersistenceException;

public class DbEncryptionByteArrayConverterMigrationV001 implements AttributeConverter<byte[], String> {

    @Override
    public String convertToDatabaseColumn(byte[] s) {
        try {
            return DbEncryptionServiceMigrationV001.getInstance().encryptByteArray(s);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public byte[] convertToEntityAttribute(String s) {
        try {
            return DbEncryptionServiceMigrationV001.getInstance().decryptByteArray(s);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            throw new PersistenceException(e);
        }
    }

}

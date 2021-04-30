package app.coronawarn.quicktest.dbencryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.persistence.AttributeConverter;
import javax.persistence.PersistenceException;

public class DbEncryptionByteArrayConverter implements AttributeConverter<byte[], String> {

    @Override
    public String convertToDatabaseColumn(byte[] s) {
        try {
            return DbEncryptionService.getInstance().encryptByteArray(s);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public byte[] convertToEntityAttribute(String s) {
        try {
            return DbEncryptionService.getInstance().decryptByteArray(s);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException
            | BadPaddingException | IllegalBlockSizeException e) {
            try {
                return DbEncryptionServiceOld.getInstance().decryptByteArray(s);
            } catch (InvalidAlgorithmParameterException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException exceptionOld) {
                throw new PersistenceException(exceptionOld);
            }
        }
    }

}

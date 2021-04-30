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

import app.coronawarn.quicktest.config.QuickTestConfig;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Service;

@Slf4j
@Configuration
public class DbEncryptionServiceOld {

    // TODO: Can be removed if a long-term archive DB is implemented and all old data has thus been moved

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static DbEncryptionServiceOld instance;
    private final Cipher cipher;
    private final Key key;

    /**
     * Constructor for DbEncryptionService.
     * Initializes Cipher with ciphersuite configured in application properties.
     */
    public DbEncryptionServiceOld(QuickTestConfig quickTestConfig) {
        cipher = AesBytesEncryptor.CipherAlgorithm.CBC.createCipher();
        String dbEncryptionPassword = quickTestConfig.getDbEncryptionPassword();

        if (dbEncryptionPassword != null) {
            int passwordLength = dbEncryptionPassword.length();
            if (passwordLength != 16 && passwordLength != 24 && passwordLength != 32) {
                throw new ValidationException(
                    "Invalid Application Configuration: Database password must be a string with length of 16, 24 or 32"
                );
            }

            key = new SecretKeySpec(dbEncryptionPassword.getBytes(), "AES");
        } else {
            throw new ValidationException("DB encryption password must be set!");
        }

        DbEncryptionServiceOld.instance = this;
    }

    /**
     * Returns an instance of Singleton-DbEncryptionService.
     *
     * @return The DbEncryptionService instance
     */
    public static DbEncryptionServiceOld getInstance() {
        return instance;
    }

    /**
     * Decrypts a given AES-256 encrypted and base64 encoded String.
     *
     * @param encrypted the encrypted string
     * @return decrypted string
     * @throws InvalidKeyException                if the given key is inappropriate for initializing this cipher
     * @throws BadPaddingException                if this cipher is in decryption mode, and (un)padding has been
     *                                            requested, but the decrypted data is not bounded by the appropriate
     *                                            padding bytes
     * @throws IllegalBlockSizeException          if this cipher is a block cipher,
     *                                            no padding has been requested (only in encryption mode), and the
     *                                            total input length of the data processed by this cipher is not a
     *                                            multiple of block size;
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters are inappropriate for this cipher
     */
    public String decryptString(String encrypted)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        return new String(decrypt(Base64.getDecoder().decode(encrypted)), CHARSET);
    }

    /**
     * Decrypts a given AES-256 encrypted and base64 encoded String.
     *
     * @param encrypted the encrypted string
     * @return decrypted short
     * @throws InvalidKeyException                if the given key is inappropriate for initializing this cipher
     * @throws BadPaddingException                if this cipher is in decryption mode, and (un)padding has been
     *                                            requested, but the decrypted data is not bounded by the appropriate
     *                                            padding bytes
     * @throws IllegalBlockSizeException          if this cipher is a block cipher,
     *                                            no padding has been requested (only in encryption mode), and the
     *                                            total input length of the data processed by this cipher is not a
     *                                            multiple of block size;
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters are inappropriate for this cipher
     */
    public Short decryptShort(String encrypted)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        return Short.valueOf(decryptString(encrypted));
    }

    /**
     * Decrypts a given AES-256 encrypted and base64 encoded String.
     *
     * @param encrypted the encrypted string
     * @return decrypted short
     * @throws InvalidKeyException                if the given key is inappropriate for initializing this cipher
     * @throws BadPaddingException                if this cipher is in decryption mode, and (un)padding has been
     *                                            requested, but the decrypted data is not bounded by the appropriate
     *                                            padding bytes
     * @throws IllegalBlockSizeException          if this cipher is a block cipher,
     *                                            no padding has been requested (only in encryption mode), and the
     *                                            total input length of the data processed by this cipher is not a
     *                                            multiple of block size;
     * @throws InvalidAlgorithmParameterException if the given algorithm parameters are inappropriate for this cipher
     */
    public Boolean decryptBoolean(String encrypted)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        return Boolean.valueOf(decryptString(encrypted));
    }

    public byte[] decryptByteArray(String encrypted) throws InvalidKeyException,
        BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        return decrypt(Base64.getDecoder().decode(encrypted));
    }

    private byte[] decrypt(byte[] encrypted)
        throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        synchronized (cipher) {
            cipher.init(Cipher.DECRYPT_MODE, key, getInitializationVector());
            return cipher.doFinal(encrypted);
        }
    }

    private IvParameterSpec getInitializationVector() {
        return new IvParameterSpec("WnU2IQhlAAN@bK~L".getBytes(CHARSET));
    }
}


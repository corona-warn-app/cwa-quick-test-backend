/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
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

package app.coronawarn.quicktest.service.cryption;

import app.coronawarn.quicktest.exception.UncheckedBadPaddingException;
import app.coronawarn.quicktest.exception.UncheckedIllegalBlockSizeException;
import app.coronawarn.quicktest.exception.UncheckedInvalidAlgorithmParameterException;
import app.coronawarn.quicktest.exception.UncheckedInvalidKeyException;
import app.coronawarn.quicktest.exception.UncheckedInvalidKeySpecException;
import app.coronawarn.quicktest.exception.UncheckedNoSuchAlgorithmException;
import app.coronawarn.quicktest.exception.UncheckedNoSuchPaddingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AesGcmNoPadding256Cryption implements AesCryption {
    
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";

    private static final int TAG_LENGTH_BIT = 128;

    private static final int IV_LENGTH_BYTE = 16;

    private static final int SALT_LENGTH_BYTE = 32;

    private static final int AES_KEY_BIT = 256;

    @Value("${archive.crypt.AesGcmNoPadding256.iterations:12345}")
    private int iterations;

    @Override
    public boolean supportAlgorithm(String algorithm) {
        return ENCRYPT_ALGO.equalsIgnoreCase(algorithm);
    }

    @Override
    public String getAlgorithm() {
        return ENCRYPT_ALGO;
    }

    @Override
    public String encrypt(final String secret, final String plainText) {
        final String usedSecret = secret != null ? secret : "";
        try {
            final byte[] salt = this.getRandomNonce(SALT_LENGTH_BYTE);
            final byte[] iv = this.getRandomNonce(IV_LENGTH_BYTE);
            final SecretKey key = this.buildKey(usedSecret.toCharArray(), salt);

            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            final byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
                    .put(iv)
                    .put(salt)
                    .put(cipherText)
                    .array();
            return this.encodeText(cipherTextWithIvSalt);
        } catch (final InvalidKeyException e) {
            throw new UncheckedInvalidKeyException(e);
        } catch (final InvalidAlgorithmParameterException e) {
            throw new UncheckedInvalidAlgorithmParameterException(e);
        } catch (final BadPaddingException e) {
            throw new UncheckedBadPaddingException(e);
        } catch (final IllegalBlockSizeException e) {
            throw new UncheckedIllegalBlockSizeException(e);
        } catch (final NoSuchAlgorithmException e) {
            throw new UncheckedNoSuchAlgorithmException(e);
        } catch (final NoSuchPaddingException e) {
            throw new UncheckedNoSuchPaddingException(e);
        } catch (final InvalidKeySpecException e) {
            throw new UncheckedInvalidKeySpecException(e);
        }
    }

    @Override
    public String decrypt(final String secret, final String encryptedText) {
        final String usedSecret = secret != null ? secret : "";
        try {
            final ByteBuffer bb = ByteBuffer.wrap(this.decodeText(encryptedText));
            final byte[] iv = new byte[IV_LENGTH_BYTE];
            bb.get(iv);
            final byte[] salt = new byte[SALT_LENGTH_BYTE];
            bb.get(salt);
            final byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);

            final SecretKey key = this.buildKey(usedSecret.toCharArray(), salt);

            final Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            final byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (final InvalidKeyException e) {
            throw new UncheckedInvalidKeyException(e);
        } catch (final InvalidAlgorithmParameterException e) {
            throw new UncheckedInvalidAlgorithmParameterException(e);
        } catch (final BadPaddingException e) {
            throw new UncheckedBadPaddingException(e);
        } catch (final IllegalBlockSizeException e) {
            throw new UncheckedIllegalBlockSizeException(e);
        } catch (final NoSuchAlgorithmException e) {
            throw new UncheckedNoSuchAlgorithmException(e);
        } catch (final NoSuchPaddingException e) {
            throw new UncheckedNoSuchPaddingException(e);
        } catch (final InvalidKeySpecException e) {
            throw new UncheckedInvalidKeySpecException(e);
        }
    }

    private String encodeText(final byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }

    private byte[] decodeText(final String text) {
        return Base64.getDecoder().decode(text);
    }

    private byte[] getRandomNonce(final int numBytes) {
        final byte[] nonce = new byte[numBytes];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    private SecretKey buildKey(final char[] password, final byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        final KeySpec spec = new PBEKeySpec(password, salt, this.iterations, AES_KEY_BIT);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
}

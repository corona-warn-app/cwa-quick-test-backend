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

package app.coronawarn.quicktest.service.cryption;

import app.coronawarn.quicktest.exception.UncheckedBadPaddingException;
import app.coronawarn.quicktest.exception.UncheckedIllegalBlockSizeException;
import app.coronawarn.quicktest.exception.UncheckedInvalidKeyException;
import app.coronawarn.quicktest.exception.UncheckedInvalidKeySpecException;
import app.coronawarn.quicktest.exception.UncheckedNoSuchAlgorithmException;
import app.coronawarn.quicktest.exception.UncheckedNoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.springframework.stereotype.Service;

@Service
public class RsaEcbOaepWithSha512AndMgf1PaddingCryption implements RsaCryption {

    private static final String TRANSFORMATION = "RSA/ECB/OAEPwithSHA-512andMGF1Padding";

    @Override
    public boolean supportAlgorithm(String algorithm) {
        return TRANSFORMATION.equalsIgnoreCase(algorithm);
    }

    @Override
    public String getAlgorithm() {
        return TRANSFORMATION;
    }

    @Override
    public String encrypt(final String secret, final String plainText) {
        return this.encrypt(getPublicKey(plainText), plainText);
    }

    @Override
    public String encrypt(final PublicKey publicKey, final String plainText) {
        final byte[] plain = plainText.getBytes(StandardCharsets.UTF_8);
        try {
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            final byte[] cipherText = cipher.doFinal(plain);
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (final InvalidKeyException e) {
            throw new UncheckedInvalidKeyException(e);
        } catch (final NoSuchAlgorithmException e) {
            throw new UncheckedNoSuchAlgorithmException(e);
        } catch (final NoSuchPaddingException e) {
            throw new UncheckedNoSuchPaddingException(e);
        } catch (final IllegalBlockSizeException e) {
            throw new UncheckedIllegalBlockSizeException(e);
        } catch (final BadPaddingException e) {
            throw new UncheckedBadPaddingException(e);
        }
    }

    @Override
    public String decrypt(final String secret, final String encryptedText) {
        return this.decrypt(getPrivateKey(secret), encryptedText);
    }

    @Override
    public String decrypt(final PrivateKey privateKey, final String encryptedText)  {
        final byte[] encrypted = Base64.getDecoder().decode(encryptedText);
        try {
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);    
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedNoSuchAlgorithmException(e);
        } catch (NoSuchPaddingException e) {
            throw new UncheckedNoSuchPaddingException(e);
        } catch (InvalidKeyException e) {
            throw new UncheckedInvalidKeyException(e);
        } catch (IllegalBlockSizeException e) {
            throw new UncheckedIllegalBlockSizeException(e);
        } catch (BadPaddingException e) {
            throw new UncheckedBadPaddingException(e);
        }
    }

    private PublicKey getPublicKey(final String base64PublicKey) {
        return this.getPublicKey(base64PublicKey, "RSA");
    }

    private PublicKey getPublicKey(final String base64PublicKey, final String algorithm) {
        try {
            final String keyContent = this.cleanKeyContent(base64PublicKey);
            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
                    Base64.getDecoder().decode(keyContent.getBytes()));
            return KeyFactory.getInstance(algorithm).generatePublic(keySpec);
        } catch (final NoSuchAlgorithmException e) {
            throw new UncheckedNoSuchAlgorithmException(e);
        } catch (final InvalidKeySpecException e) {
            throw new UncheckedInvalidKeySpecException(e);
        }
    }

    private PrivateKey getPrivateKey(final String base64PrivateKey) {
        return this.getPrivateKey(base64PrivateKey, "RSA");
    }

    private PrivateKey getPrivateKey(final String base64PrivateKey, final String algorithm) {
        try {
            final String keyContent = this.cleanKeyContent(base64PrivateKey);
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(
                    Base64.getDecoder().decode(keyContent.getBytes()), algorithm);
            return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
        } catch (final NoSuchAlgorithmException e) {
            throw new UncheckedNoSuchAlgorithmException(e);
        } catch (final InvalidKeySpecException e) {
            throw new UncheckedInvalidKeySpecException(e);
        }
    }

    private String cleanKeyContent(final String keyContent) {
        return keyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\n", "");
    }
}

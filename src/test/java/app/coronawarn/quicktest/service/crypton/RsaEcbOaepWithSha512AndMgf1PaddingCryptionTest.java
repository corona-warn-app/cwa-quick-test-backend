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

package app.coronawarn.quicktest.service.crypton;

import static org.assertj.core.api.Assertions.assertThat;

import app.coronawarn.quicktest.service.KeyProvider;
import app.coronawarn.quicktest.service.cryption.RsaEcbOaepWithSha512AndMgf1PaddingCryption;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RsaEcbOaepWithSha512AndMgf1PaddingCryptionTest {

    @Autowired
    private RsaEcbOaepWithSha512AndMgf1PaddingCryption cryption;

    @Autowired
    private KeyProvider keyProvider;

    /**
     * There are several public keys, which is why the test is carried out several times. It is likely that different
     * keys are used.
     */
    @Test
    void encryptAndDecript() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // GIVEN
        final String plain = "B5hCYqxtkHtSkkhZcEEfVyL48zWEn8vqhJJxAQhr4eQTtHZZEHrVsNnbc4kKT2TPqLWKJ25A4VydVavp";
        final PublicKey publicKey = this.keyProvider.getPublicKey();
        // WHEN 
        final String encrypted = this.cryption.encrypt(publicKey, plain);
        final String decrypted = this.keyProvider.decrypt(encrypted, "test");
        // THEN 
        assertThat(encrypted).isNotNull();
        assertThat(decrypted).isNotNull().isEqualTo(plain);
    }
}

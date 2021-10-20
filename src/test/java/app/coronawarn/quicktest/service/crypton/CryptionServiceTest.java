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

package app.coronawarn.quicktest.service.crypton;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.exception.DccException;
import app.coronawarn.quicktest.service.cryption.AesCryption;
import app.coronawarn.quicktest.service.cryption.Cryption;
import app.coronawarn.quicktest.service.cryption.CryptionService;
import app.coronawarn.quicktest.service.cryption.RsaCryption;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CryptionServiceTest {

    @Autowired
    private CryptionService cryptionService;

    @Autowired
    private QuickTestConfig properties;

    @Test
    void getAesCryption() {
        // GIVEN
        // WHEN
        final Cryption result = this.cryptionService.getAesCryption();
        // THEN
        assertThat(result).isNotNull().isInstanceOf(AesCryption.class);
    }

    @Test
    void getRsaCryption() {
        // GIVEN
        // WHEN
        final Cryption result = this.cryptionService.getRsaCryption();
        // THEN
        assertThat(result).isNotNull().isInstanceOf(RsaCryption.class);
    }

    @RepeatedTest(name = RepeatedTest.LONG_DISPLAY_NAME, value = 10)
    void generateRandomSecret() {
        // GIVEN
        final int length = this.properties.getArchive().getCrypt().getSecretLength();
        final boolean letters = this.properties.getArchive().getCrypt().isSecretLetters();
        final boolean numbers = this.properties.getArchive().getCrypt().isSecretNumbers();
        // WHEN
        final String result = this.cryptionService.generateRandomSecret();
        // THEN
        assertThat(result).isNotBlank().hasSize(length);
        if (letters) {
            assertThat(result).containsPattern("\\w");
        }
        if (numbers) {
            assertThat(result).containsPattern("\\d");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "RSA/ECB/OAEPwithSHA-512andMGF1Padding", "AES/GCM/NoPadding" })
    void getByAlgorithm(final String algorithm) {
        // GIVEN algorithm param
        // WHEN
        final Cryption result = this.cryptionService.getByAlgorithm(algorithm);
        // THEN
        assertThat(result).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getByAlgorithm_withNullAndEmpty(final String algorithm) {
        // GIVEN algorithm param
        // WHEN
        DccException ex = assertThrows(DccException.class, () -> {
            this.cryptionService.getByAlgorithm(algorithm);
        });
        // THEN
        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).contains("not found");
    }
}

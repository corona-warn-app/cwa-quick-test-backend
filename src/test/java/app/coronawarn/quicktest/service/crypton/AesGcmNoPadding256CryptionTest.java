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

import app.coronawarn.quicktest.service.cryption.AesGcmNoPadding256Cryption;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AesGcmNoPadding256CryptionTest {

    @Autowired
    private AesGcmNoPadding256Cryption cryption;

    private final String plain = "{\"glossary\":{\"title\":\"exampleglossary\",\"GlossDiv\":{\"title\":\"S\","
            + "\"GlossList\":{\"GlossEntry\":{\"ID\":\"SGML\",\"SortAs\":\"SGML\","
            + "\"GlossTerm\":\"StandardGeneralizedMarkupLanguage\",\"Acronym\":\"SGML\","
            + "\"Abbrev\":\"ISO8879:1986\",\"GlossDef\":"
            + "{\"para\":\"Ameta-markuplanguage,usedtocreatemarkuplanguagessuchasDocBook.\","
            + "\"GlossSeeAlso\":[\"GML\",\"XML\"]},\"GlossSee\":\"markup\"}}}}}";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "B5hCYqxtkHtSkkhZcEEfVyL48zWEn8vqhJJxAQhr4eQTtHZZEH",
        "rVsNnbc4kKT2TPqLWKJ25A4VydVavpGsudHbChe5WpwpuKksxx",
        "2Vn4BAn4ByDrer25v7Y8LmhPcXhWrsWBf3vGDRTCtk7Ah95MHg" })
    void encryptAndDecrypt(String secret) {
        // GIVEN secret param
        // WHEN
        final String encrypted = this.cryption.encrypt(secret, this.plain);
        final String decrypted = this.cryption.decrypt(secret, encrypted);
        // THEN
        assertThat(encrypted).isNotNull();
        assertThat(decrypted).isNotNull().isEqualTo(this.plain);
    }
}

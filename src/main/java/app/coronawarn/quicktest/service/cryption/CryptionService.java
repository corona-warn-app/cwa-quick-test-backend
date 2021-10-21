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

import app.coronawarn.quicktest.config.ArchiveProperties;
import app.coronawarn.quicktest.exception.DccException;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CryptionService {

    private final ArchiveProperties properties;

    private final List<Cryption> cryptions;

    public AesCryption getAesCryption() {
        return (AesCryption) this.getByAlgorithm(this.properties.getCrypt().getDefaultAes());
    }

    public RsaCryption getRsaCryption() {
        return (RsaCryption) this.getByAlgorithm(this.properties.getCrypt().getDefaultRsa());
    }

    /**
     * Provides an cryption bean based on the algorithm.
     * 
     * @param algorithm {@link String}
     * @return {@link Cryption}
     */
    public Cryption getByAlgorithm(final String algorithm) {
        return this.cryptions.stream()
                .filter(cryption -> cryption.supportAlgorithm(algorithm))
                .findFirst()
                .orElseThrow(() -> new DccException(HttpStatus.NOT_FOUND,
                        String.format("Cryption not found by algorithm: %s", algorithm)));
    }

    /**
     * Creates a random secret based on the settings.
     * 
     * @return {@link String}
     */
    public String generateRandomSecret() {
        final int length = this.properties.getCrypt().getSecretLength();
        final boolean letters = this.properties.getCrypt().isSecretLetters();
        final boolean numbers = this.properties.getCrypt().isSecretNumbers();
        return RandomStringUtils.random(length, 33, 126, letters, numbers, null, new SecureRandom());
    }
}

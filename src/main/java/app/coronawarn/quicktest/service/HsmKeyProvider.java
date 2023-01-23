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

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.ArchiveProperties;
import app.coronawarn.quicktest.exception.DccException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Primary
@Profile({ "hsm" })
@Service
@RequiredArgsConstructor
public class HsmKeyProvider implements KeyProvider {

    private final ArchiveProperties properties;

    @Override
    public PublicKey getPublicKey() {
        throw new DccException(HttpStatus.NOT_IMPLEMENTED, "not implemented");
    }

    @Override
    public byte[] getPepper() {
        return this.properties.getHash().getPepper().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String decrypt(String encrypted, String context) {
        throw new DccException(HttpStatus.INTERNAL_SERVER_ERROR, "not allowed access");
    }

    @Override
    public String encrypt(String plain, String context) {
        throw new DccException(HttpStatus.NOT_IMPLEMENTED, "not implemented");
    }
}

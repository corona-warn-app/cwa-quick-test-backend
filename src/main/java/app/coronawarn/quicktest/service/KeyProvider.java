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

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.exception.DccException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyProvider {

    /**
     * Delivers a public key which is to be used for the encryption of the archive.
     * 
     * @return public key for archive
     */
    PublicKey getPublicKey();

    /**
     * Provides the pepper for hashing values.
     * 
     * @return pepper
     */
    byte[] getPepper();

    /**
     * Returns the private key to the public key, if this function is supported.
     * 
     * @param publicKey {@link PublicKey}
     * @return {@link PrivateKey}
     * @throws DccException if this function is supported or key not found
     */
    PrivateKey getPrivateKey(PublicKey publicKey);
}
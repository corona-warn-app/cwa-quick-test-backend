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

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.exception.DccException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

/**
 * A mock that is used for development and provides several keys. It is supposed to simulate a vault that creates and
 * stores the keys.
 */
@Profile({ "default", "dev", "local", "test" })
@Service
@RequiredArgsConstructor
public class JksKeyProvider implements KeyProvider {

    private static final String ALIAS_KEY_PREFIX = "archive_key_";

    private final QuickTestConfig properties;

    private final List<PrivateKeyEntry> entries = new ArrayList<>();

    /**
     * Reads all keys from the JKS.
     * 
     * @throws IOException if the resource cannot be resolved
     * @throws GeneralSecurityException if the algorithm cannot be found
     */
    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        final String filePath = this.properties.getArchive().getJks().getPath();
        final File jksFile = ResourceUtils.getFile(filePath);

        try (final InputStream jksStream = new FileInputStream(jksFile)) {
            final KeyStore jks = KeyStore.getInstance("JKS");
            final char[] jksPassword = this.properties.getArchive().getJks().getPassword().toCharArray();
            jks.load(jksStream, jksPassword);

            final PasswordProtection entryPassword = new PasswordProtection("".toCharArray());
            final Enumeration<String> aliases = jks.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if (alias.startsWith(ALIAS_KEY_PREFIX)) {
                    final Entry entry = jks.getEntry(alias, entryPassword);
                    if (entry instanceof PrivateKeyEntry) {
                        this.entries.add((PrivateKeyEntry) entry);
                    }
                }
            }
        }
    }

    /**
     * Returns a random public key from the JKS.
     */
    @Override
    public PublicKey getPublicKey() {
        int random = (int) (Math.random() * this.entries.size());
        return this.entries.get(random).getCertificate().getPublicKey();
    }

    @Override
    public byte[] getPepper() {
        return this.properties.getArchive().getHash().getPepper().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public PrivateKey getPrivateKey(PublicKey publicKey) {
        return this.entries.stream()
                .filter(key -> key.getCertificate().getPublicKey().equals(publicKey))
                .findFirst()
                .orElseThrow(() -> new DccException(HttpStatus.NOT_FOUND, "Private key not found by public key"))
                .getPrivateKey();
    }
}

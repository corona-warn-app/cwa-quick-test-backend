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

package app.coronawarn.quicktest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("archive")
public class ArchiveProperties {
    private ArchiveJks jks = new ArchiveJks();
    private Job moveToArchiveJob = new Job();
    private Hash hash = new Hash();
    private Crypt crypt = new Crypt();
    private VaultTransit vaultTransit = new VaultTransit();

    @Data
    public static final class VaultTransit {
        private String dek;
    }

    @Data
    public static final class ArchiveJks {
        private String path;
        private String password;
    }

    @Data
    public static final class Job {
        private String cron;
        private long locklimit = -1;
        private long olderThanInSeconds = -1;
        private int chunkSize = 1000;
    }

    @Data
    public static final class Hash {
        private String algorithm;
        private String pepper;
    }

    @Data
    public static final class Crypt {
        private String defaultAes;
        private String defaultRsa;
        private int secretLength = 50;
        private boolean secretLetters = true;
        private boolean secretNumbers = true;
    }
}

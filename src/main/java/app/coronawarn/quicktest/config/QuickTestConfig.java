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

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("quicktest")
public class QuickTestConfig {

    private CleanUpSettings cleanUpSettings;
    private String pointOfCareIdName;
    private String tenantIdKey;
    private String groupKey;
    private String bsnrKey;
    private String tenantPointOfCareIdKey;
    private String pointOfCareInformationName;
    private String pointOfCareInformationDelimiter;
    private String groupInformationDelimiter;
    private String dbEncryptionKey;
    private String labId;
    private String pcrEnabledKey;

    private FrontendContextConfig frontendContextConfig = new FrontendContextConfig();
    private CancellationConfig cancellation = new CancellationConfig();

    @Getter
    @Setter
    public static class CancellationConfig {

        private int finalDeletionDays = 28;
        private int completePendingTestsHours = 24;
        private int readyToArchiveHours = 48;

    }

    @Getter
    @Setter
    public static class FrontendContextConfig {

        private String rulesServerUrl;
        private String environmentName;
    }

    @Getter
    @Setter
    public static class CleanUpSettings {
        private String cron;
        private int maxAgeInMinutes;
        private int locklimit;
        private int chunkSize;
    }
}

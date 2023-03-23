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

package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.utils.Utilities;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties("auditlogs")
public class SecurityAuditListenerQuickTest {

    private final QuickTestConfig quickTestConfig;
    private final Utilities utilities;
    private String pattern = "User: {}; tenantId: {}; pocID: {}; action: {}; Object: {}; ID: {}";
    private static final String SELECT_ACTION = "Select";
    private static final String SAVE_ACTION = "Save";
    private static final String REMOVE_ACTION = "Remove";

    @PostLoad
    private void afterSelectQuickTest(QuickTest quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, SELECT_ACTION);
    }

    @PostPersist
    @PostUpdate
    private void afterSaveQuickTest(QuickTest quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, SAVE_ACTION);
    }

    @PostRemove
    private void afterRemoveQuickTest(QuickTest quickTest) throws ResponseStatusException {
        createAuditLog(quickTest, REMOVE_ACTION);
    }

    private void createAuditLog(QuickTest quickTest, String action) {
        String name;
        String tenantId;
        String pocId;

        try {
            name = utilities.getUserNameFromToken();
            tenantId = utilities.getIdsFromToken().get(quickTestConfig.getTenantIdKey());
            pocId = utilities.getIdsFromToken().get(quickTestConfig.getTenantPointOfCareIdKey());
            log.info(pattern,
                    name,
                    tenantId,
                    pocId,
                    action,
                    "QuickTest",
                    quickTest.getHashedGuid());
        } catch (ResponseStatusException e) {
            name = "called by Backend";
            tenantId = quickTest.getTenantId();
            pocId = quickTest.getPocId();
            if (!action.equals(SELECT_ACTION)) {
                log.info(pattern,
                        name,
                        tenantId,
                        pocId,
                        action,
                        "QuickTest",
                        quickTest.getHashedGuid());
            }
        }
    }

}

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuickTestCleanupService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestService quickTestService;

    /**
     * Cleanup task to delete all QuickTest which are older then configured.
     */
    @Scheduled(cron = "${quicktest.clean-up-settings.cron}")
    @SchedulerLock(name = "QuickTestCleanupService_cleanupQuickTests", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${quicktest.clean-up-settings.locklimit}")
    protected void cleanupQuickTests() {
        LocalDateTime deleteTimestamp =
            Instant.now().atZone(ZoneId.of("UTC"))
                .minusMinutes(quickTestConfig.getCleanUpSettings().getMaxAgeInMinutes()).toLocalDateTime();

        log.info("Starting QuickTest cleanup");
        quickTestService.removeAllBefore(deleteTimestamp);
        log.info("QuickTest cleanup finished.");
    }
}

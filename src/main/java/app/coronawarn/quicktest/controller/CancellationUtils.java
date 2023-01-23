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

package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.service.CancellationService;
import app.coronawarn.quicktest.utils.Utilities;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CancellationUtils {

    private final CancellationService cancellationService;

    private final QuickTestConfig quickTestConfig;

    private final Utilities utilities;

    /**
     * Check if cancellation is already started for Partner of currently logged-in user.
     *
     * @return true whether cancellation is in progress.
     */
    public boolean isCancellationStarted() {
        Optional<Cancellation> cancellationOptional =
                cancellationService.getByPartnerId(utilities.getTenantIdFromToken());

        if (cancellationOptional.isEmpty()) {
            return false;
        }

        Cancellation cancellation = cancellationOptional.get();
        return ZonedDateTime.now().isAfter(cancellation.getCancellationDate());
    }

    /**
     * Check if cancellation is already started and submitting new test results is forbidden for Partner of
     * currently logged-in user.
     *
     * @return true whether cancellation is in progress.
     */
    public boolean isCancellationStartedAndTestResultSubmittingDenied() {
        Optional<Cancellation> cancellationOptional =
                cancellationService.getByPartnerId(utilities.getTenantIdFromToken());

        if (cancellationOptional.isEmpty()) {
            return false;
        }

        Cancellation cancellation = cancellationOptional.get();
        return ZonedDateTime.now()
                .minusHours(quickTestConfig.getCancellation().getCompletePendingTestsHours())
                .isAfter(cancellation.getCancellationDate());
    }
}

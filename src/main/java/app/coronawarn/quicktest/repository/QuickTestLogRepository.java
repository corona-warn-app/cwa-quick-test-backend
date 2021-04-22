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

package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.QuickTestLog;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuickTestLogRepository extends JpaRepository<QuickTestLog, String> {

    int countAllByPocIdAndCreatedAtBetween(String pocId, LocalDateTime utcDateFrom, LocalDateTime utcDateTo);

    int countAllByPocIdAndPositiveTestResultIsTrueAndCreatedAtBetween(String pocId, LocalDateTime utcDateFrom,
                                                                      LocalDateTime utcDateTo);

    int countAllByTenantIdAndCreatedAtBetween(String tenantId, LocalDateTime utcDateFrom, LocalDateTime utcDateTo);

    int countAllByTenantIdAndPositiveTestResultIsTrueAndCreatedAtBetween(String tenantId, LocalDateTime utcDateFrom,
                                                                         LocalDateTime utcDateTo);

}

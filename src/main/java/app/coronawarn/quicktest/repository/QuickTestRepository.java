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

import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuickTestRepository extends JpaRepository<QuickTest, String> {

    QuickTest findByTenantIdAndPocIdAndShortHashedGuid(String tenantId, String pocId, String shortHash);

    Optional<QuickTest> findByTenantIdAndPocIdAndShortHashedGuidOrHashedGuid(String tenantId, String pocId,
                                                                             String shortHash, String hashedGuid);


    List<QuicktestView> getShortHashedGuidByTenantIdAndPocIdAndTestResultAndVersionIsGreaterThan(String tenantId,
                                                                                                 String pocId,
                                                                                                 Short testResult,
                                                                                                 Integer version);

    int countAllByCreatedAtBeforeAndVersionIsGreaterThan(LocalDateTime time,
                                                         Integer version);

    List<QuickTest> findAllByCreatedAtBeforeAndVersionIsGreaterThan(LocalDateTime time,
                                                                    Integer version,
                                                                    Pageable pageable);

    List<QuickTest> findAllByDccStatus(DccStatus dccStatus);

    @Query("DELETE FROM QuickTest q WHERE q.createdAt < :time")
    @Modifying
    void deleteByCreatedAtBefore(@Param("time") LocalDateTime time);

    int countAllByTenantIdIsAndPocIdIsIn(String tenantId, List<String> pocIds);

}

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

import app.coronawarn.quicktest.domain.QuickTestStatistics;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuickTestStatisticsRepository extends JpaRepository<QuickTestStatistics, String> {

    Optional<QuickTestStatistics> findByPocIdAndCreatedAt(String pocId, LocalDate localDate);


    @Modifying
    @Query("UPDATE QuickTestStatistics qts SET qts.totalTestCount = qts.totalTestCount + 1 WHERE "
        + "qts.pocId = :poc_id AND qts.createdAt = :date ")
    void incrementTotalTestCount(@Param("poc_id") String pocId, @Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE QuickTestStatistics qts SET qts.positiveTestCount = qts.positiveTestCount + 1, "
        + "qts.totalTestCount = qts.totalTestCount + 1"
        + " WHERE qts.pocId = :poc_id AND qts.createdAt = :date ")
    void incrementPositiveAndTotalTestCount(@Param("poc_id") String pocId, @Param("date") LocalDate date);

}

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

package app.coronawarn.quicktest.repository;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuickTestArchiveRepository extends JpaRepository<QuickTestArchive, String> {

    Optional<QuickTestArchive> findByHashedGuid(String hashedGuid);

    List<QuickTestArchiveView> findAllByTenantIdAndPocIdAndUpdatedAtBetween(
        String tenantId,
        String pocId,
        LocalDateTime dateFrom,
        LocalDateTime dateTo
    );

    List<QuickTestArchiveView> findAllByTenantIdAndPocIdAndTestResultInAndUpdatedAtBetween(
            String tenantId,
            String pocId,
            List<Short> testResult,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    );

    Stream<QuickTestArchiveDataView> findAllByUpdatedAtBefore(LocalDateTime updatedAt, Pageable pageable);

    @Transactional
    @Modifying
    @Query("DELETE FROM QuickTestArchive q WHERE q.hashedGuid = :hashedGuid")
    int deleteByHashedGuid(@Param("hashedGuid") String hashedGuid);

    Stream<QuickTestArchiveDataView> findAllByTenantId(String tenantId, Pageable pageable);
}

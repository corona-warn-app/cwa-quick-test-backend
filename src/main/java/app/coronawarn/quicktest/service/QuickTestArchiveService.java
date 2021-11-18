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
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestArchiveView;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickTestArchiveService {

    private final QuickTestArchiveRepository quickTestArchiveRepository;
    private final QuickTestConfig quickTestConfig;

    /**
     * Stores quicktest with pdf in archive table.
     *
     * @param hashedGuid to identify quicktest
     * @return PDF as byte array
     * @throws ResponseStatusException if quicktest not found.
     */
    public byte[] getPdf(String hashedGuid)
        throws ResponseStatusException {
        Optional<QuickTestArchive> quickTestArchive = quickTestArchiveRepository.findByHashedGuid(hashedGuid);
        if (quickTestArchive.isEmpty()) {
            log.debug("Requested Quick Test with HashedGuid {} could not be found or wrong poc", hashedGuid);
            log.info("Requested Quick Test with HashedGuid could not be found or wrong poc");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return quickTestArchive.get().getPdf();
    }

    /**
     * Finds all quicktests in archive table by test result and time range.
     *
     * @param testResult test result value (5...9) or null
     * @param dateFrom   Start date
     * @param dateTo     End date
     * @return quickTestArchives List of all found quickTestArchives
     */
    @Transactional(readOnly = true)
    public List<QuickTestArchiveView> findByTestResultAndUpdatedAtBetween(
        Map<String, String> ids, Short testResult, LocalDateTime dateFrom, LocalDateTime dateTo) {
        List<QuickTestArchiveView> archives;
        if (testResult == null) {
            archives = quickTestArchiveRepository.findAllByTenantIdAndPocIdAndUpdatedAtBetween(
                ids.get(quickTestConfig.getTenantIdKey()),
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                dateFrom,
                dateTo);
        } else {
            archives = quickTestArchiveRepository.findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(
                ids.get(quickTestConfig.getTenantIdKey()),
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                testResult,
                dateFrom,
                dateTo);
        }
        return archives;
    }

}

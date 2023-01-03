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

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.model.quicktest.PcrTestResult;
import app.coronawarn.quicktest.model.quicktest.QuickTestResult;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.utils.TestTypeUtils;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickTestDeletionService {

    private final QuickTestRepository quickTestRepository;
    private final TestResultService testResultService;
    private final QuickTestConfig quickTestConfig;


    /**
     * Handle a chunk of old tests to delete.
     * Requires_New opens a new transaction and commits it after the method finishes.
     *
     * @param deleteTimestamp find all before
     * @param chunkSize desired chunk size
     * @param chunks amount of chunks
     * @param i current chunk
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleChunk(LocalDateTime deleteTimestamp, int chunkSize, int chunks, int i) {
        log.info("Deleting chunk {} of {}", i, chunks);

        List<QuickTest> quickTestChunk = quickTestRepository.findAllByCreatedAtBeforeAndVersionIsGreaterThan(
          deleteTimestamp,
          0,
          PageRequest.of(i - 1, chunkSize));

        quickTestChunk.forEach(quickTest -> sendResultToTestResultServer(
          quickTest.getTestResultServerHash(),
          TestResult.REDEEMED.getValue(),
          deleteTimestamp.toEpochSecond(ZoneOffset.UTC),
          quickTest.getConfirmationCwa() != null ? quickTest.getConfirmationCwa() : false,
                TestTypeUtils.isPcr(quickTest.getTestType())));

        log.info("Set Status of quicktests on TRS. Deleting QuickTests in DB");

        try {
            quickTestRepository.deleteAll(quickTestChunk);
            quickTestRepository.flush();
        } catch (final Exception exception) {
            log.warn("Could not delete chunk on db, trying to continue with the next chunk.");
        }

        log.info("Processing of chunk {} of {} finished.", i, chunks);
    }

    /**
     * Send to TRS.
     * @param testResultServerHash the hash
     * @param result the result (short)
     * @param sc samle colletion timestamp
     * @param confirmationCwa if cwa requested
     * @throws ResponseStatusException possible error
     */
    private void sendResultToTestResultServer(String testResultServerHash, short result, Long sc,
                                              boolean confirmationCwa, boolean isPcr) throws ResponseStatusException {

        if (confirmationCwa && testResultServerHash != null) {
            if (isPcr) {
                log.info("Sending PCR TestResult to TestResult-Server");
                PcrTestResult pcrTestResult = new PcrTestResult();
                pcrTestResult.setId(testResultServerHash);
                pcrTestResult.setResult(result);
                pcrTestResult.setSampleCollection(sc);
                pcrTestResult.setLabId(quickTestConfig.getLabId());
                testResultService.createOrUpdatePcrTestResult(pcrTestResult);
                log.info("Update PCR TestResult on TestResult-Server successfully.");
            } else {
                log.info("Sending TestResult to TestResult-Server");
                QuickTestResult quickTestResult = new QuickTestResult();
                quickTestResult.setId(testResultServerHash);
                quickTestResult.setResult(result);
                quickTestResult.setSampleCollection(sc);
                testResultService.createOrUpdateTestResult(quickTestResult);
                log.info("Update TestResult on TestResult-Server successfully.");
            }
        }
    }
}

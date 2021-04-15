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
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.model.TestResultList;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class QuickTestService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestRepository quickTestRepository;
    private final TestResultService testResultService;

    /**
     * Checks if an other quick test with given short hash already exists.
     * If not a new entity of QuickTest will be created and stored.
     * Also a pending TestResult will be sent to TestResult-Server.
     *
     * @param ids        Map with tenantId and testscopeId
     * @param hashedGuid SHA256 hash of the test GUID.
     * @return saved QuickTest
     * @throws QuickTestServiceException with reason CONFLICT if a QuickTest with short hash already exists.
     */
    public QuickTest createNewQuickTest(Map<String, String> ids, String hashedGuid)
        throws QuickTestServiceException {
        String shortHash = hashedGuid.substring(0, 8);
        log.debug("Searching for existing QuickTests with shortHash {}", shortHash);

        Optional<QuickTest> conflictingQuickTestByHashed =
            quickTestRepository.findByPocIdAndShortHashedGuidOrHashedGuid(hashedGuid,
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHash);

        if (conflictingQuickTestByHashed.isPresent()) {
            log.debug("QuickTest with Guid {} already exists", shortHash);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.INSERT_CONFLICT);
        }

        QuickTest newQuickTest = new QuickTest();
        newQuickTest.setShortHashedGuid(shortHash);
        newQuickTest.setTenantId(ids.get(quickTestConfig.getTenantIdKey()));
        newQuickTest.setPocId(ids.get(quickTestConfig.getTenantPointOfCareIdKey()));
        newQuickTest.setHashedGuid(hashedGuid);

        log.debug("Persisting QuickTest in database");
        try {
            newQuickTest = quickTestRepository.save(newQuickTest);
            log.info("Created new QuickTest with hashedGUID {}", hashedGuid);
        } catch (Exception e) {
            log.error("Failed to insert new QuickTest, hashedGuid = {}", hashedGuid);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.INTERNAL_ERROR);
        }
        return newQuickTest;
    }

    /**
     * Updates a QuickTest entity in persistence.
     *
     * @param shortHash the short-hash of the testresult to be updated
     * @param result    the result of the quick test.
     */
    public void updateQuickTest(Map<String, String> ids, String shortHash, short result)
        throws QuickTestServiceException {
        QuickTest quicktest = getQuickTest(ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHash);
        log.debug("Updating TestResult on TestResult-Server for hash {}", quicktest.getHashedGuid());

        quicktest.setTestResult(result);
        quickTestRepository.saveAndFlush(quicktest);
        try {
            sendResultToTestResultServer(quicktest.getHashedGuid(), result);
        } catch (Exception e) {
            log.error("Failed to send updated TestResult on TestResult-Server", e);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.TEST_RESULT_SERVER_ERROR);
        }
        log.info("Updated TestResult for hashedGuid {} with TestResult {}", quicktest.getHashedGuid(), result);
    }

    /**
     * Updates a QuickTest entity with personaldata in persistence.
     *
     * @param shortHash             the short-hash of the testresult to be updated
     * @param quickTestPersonalData the quick test personaldata.
     */
    public void updateQuickTestWithPersonalData(Map<String, String> ids, String shortHash,
                                                QuickTest quickTestPersonalData)
        throws QuickTestServiceException {
        QuickTest quicktest = getQuickTest(ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHash);
        // TODO with merge
        quicktest.setConfirmationCwa(quickTestPersonalData.getConfirmationCwa());
        quicktest.setInsuranceBillStatus(quickTestPersonalData.getInsuranceBillStatus());
        quicktest.setLastName(quickTestPersonalData.getLastName());
        quicktest.setFirstName(quickTestPersonalData.getFirstName());
        quicktest.setEmail(quickTestPersonalData.getEmail());
        quicktest.setPhoneNumber(quickTestPersonalData.getPhoneNumber());
        quicktest.setSex(quickTestPersonalData.getSex());
        quicktest.setStreet(quickTestPersonalData.getStreet());
        quicktest.setHouseNumber(quickTestPersonalData.getHouseNumber());
        quicktest.setZipCode(quickTestPersonalData.getZipCode());
        quicktest.setCity(quickTestPersonalData.getCity());
        quicktest.setTestBrandId(quickTestPersonalData.getTestBrandId());
        quicktest.setTestBrandName(quickTestPersonalData.getTestBrandName());
        quickTestRepository.saveAndFlush(quicktest);


        log.debug("Sending TestResult to TestResult-Server");
        try {
            sendResultToTestResultServer(quicktest.getHashedGuid(), quicktest.getTestResult());
        } catch (Exception e) {
            // TODO reset it not available
            log.error("Failed to send TestResult to TestResult-Server", e);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.TEST_RESULT_SERVER_ERROR);
        }

    }

    private QuickTest getQuickTest(String pocId, String shortHash) throws QuickTestServiceException {
        log.debug("Requesting QuickTest for short Hash {}", shortHash);
        QuickTest quicktest = quickTestRepository.findByPocIdAndShortHashedGuid(pocId, shortHash);
        if (quicktest == null) {
            log.info("Requested Quick Test with shortHash {} could not be found.", shortHash);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.UPDATE_NOT_FOUND);
        }
        return quicktest;
    }

    private void sendResultToTestResultServer(String hashedGuid, int result) {
        TestResult testResultObject = new TestResult();
        testResultObject.setResult(result);
        testResultObject.setId(hashedGuid);

        TestResultList testResultList = new TestResultList();
        testResultList.setTestResults(Collections.singletonList(testResultObject));

        testResultService.updateTestResult(testResultList);
    }
}

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
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.model.QuickTestResult;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.repository.QuickTestStatisticsRepository;
import app.coronawarn.quicktest.utils.PdfGenerator;
import app.coronawarn.quicktest.utils.Utilities;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
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
    private final QuickTestArchiveRepository quickTestArchiveRepository;
    private final QuickTestStatisticsRepository quickTestStatisticsRepository;
    private final PdfGenerator pdf;

    /**
     * Checks if an other quick test with given short hash already exists.
     * If not a new entity of QuickTest will be created and stored.
     * Also a pending TestResult will be sent to TestResult-Server.
     *
     * @param ids        Map with tenantId and testscopeId
     * @param hashedGuid SHA256 hash of the test GUID.
     * @throws QuickTestServiceException with reason CONFLICT if a QuickTest with short hash already exists.
     */
    public void createNewQuickTest(Map<String, String> ids, String hashedGuid)
        throws QuickTestServiceException {
        String shortHash = hashedGuid.substring(0, 8);
        log.debug("Searching for existing QuickTests with shortHash {}", shortHash);

        Optional<QuickTest> conflictingQuickTestByHashed =
            quickTestRepository.findByPocIdAndShortHashedGuidOrHashedGuid(
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHash, hashedGuid);

        Optional<QuickTestArchive> conflictingQuickTestArchiveByHashed =
            quickTestArchiveRepository.findByHashedGuid(hashedGuid);

        if (conflictingQuickTestByHashed.isPresent() || conflictingQuickTestArchiveByHashed.isPresent()) {
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
            quickTestRepository.save(newQuickTest);
            log.info("Created new QuickTest with hashedGUID {}", hashedGuid);
        } catch (Exception e) {
            log.error("Failed to insert new QuickTest, hashedGuid = {}", hashedGuid);
            throw new QuickTestServiceException(QuickTestServiceException.Reason.SAVE_FAILED);
        }
    }

    /**
     * Updates a QuickTest entity in persistence.
     *
     * @param shortHash the short-hash of the testresult to be updated
     * @param result    the result of the quick test.
     */
    @Transactional(rollbackOn = QuickTestServiceException.class)
    public void updateQuickTest(Map<String, String> ids, String shortHash, short result, String testBrandId,
                                String testBrandName, List<String> pocInformation,
                                String user) throws QuickTestServiceException {
        QuickTest quicktest = getQuickTest(ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHash);
        log.debug("Updating TestResult on TestResult-Server for hash {}", quicktest.getHashedGuid());

        quicktest.setTestResult(result);
        quicktest.setTestBrandId(testBrandId);
        quicktest.setTestBrandName(testBrandName);
        quickTestRepository.saveAndFlush(quicktest);

        addStatistics(quicktest);
        byte[] pdf;
        try {
            pdf = createPdf(quicktest, pocInformation, user);
        } catch (IOException e) {
            log.error("generating PDF failed. Exception = {}", e.getMessage());
            throw new QuickTestServiceException(QuickTestServiceException.Reason.PDF_GENERATOR);
        }
        try {
            quickTestArchiveRepository.save(mappingQuickTestToQuickTestAchive(quicktest, pdf));
            log.debug("New QuickTestArchive created for poc {} and shortHashedGuid {}",
                    quicktest.getPocId(), quicktest.getShortHashedGuid());
        } catch (Exception e) {
            log.error("Could save quickTestArchive. updateQuickTest failed. Exception "
                    + "= {}", e.getMessage());
            throw new QuickTestServiceException(QuickTestServiceException.Reason.SAVE_FAILED);
        }
        try {
            quickTestRepository.deleteById(quicktest.getHashedGuid());
            log.debug("QuickTest moved to QuickTestArchive for poc {} and shortHashedGuid {}",
                    quicktest.getPocId(), quicktest.getShortHashedGuid());
        } catch (Exception e) {
            log.error("updateQuickTest failed. Exception = {}", e.getMessage());
            throw new QuickTestServiceException(QuickTestServiceException.Reason.DELETE_FAILED);
        }

        if (quicktest.getConfirmationCwa() != null && quicktest.getConfirmationCwa()) {
            log.debug("Sending TestResult to TestResult-Server");
            try {
                sendResultToTestResultServer(quicktest.getTestResultServerHash(), result);
            } catch (TestResultServiceException e) {
                log.error("Failed to send updated TestResult on TestResult-Server", e);
                throw new QuickTestServiceException(QuickTestServiceException.Reason.TEST_RESULT_SERVER_ERROR);
            }
        }

        log.info("Updated TestResult for hashedGuid {} with TestResult {}", quicktest.getHashedGuid(), result);
    }

    /**
     * Updates a QuickTest entity with personaldata in persistence.
     *
     * @param shortHash             the short-hash of the testresult to be updated
     * @param quickTestPersonalData the quick test personaldata.
     */
    @Transactional(rollbackOn = QuickTestServiceException.class)
    public void updateQuickTestWithPersonalData(Map<String, String> ids, String shortHash,
                                                QuickTest quickTestPersonalData)
        throws QuickTestServiceException {
        QuickTest quicktest = getQuickTest(ids.get(quickTestConfig.getTenantPointOfCareIdKey()), shortHash);
        // TODO with merge
        quicktest.setConfirmationCwa(quickTestPersonalData.getConfirmationCwa());
        quicktest.setPrivacyAgreement(quickTestPersonalData.getPrivacyAgreement());
        quicktest.setLastName(quickTestPersonalData.getLastName());
        quicktest.setFirstName(quickTestPersonalData.getFirstName());
        quicktest.setEmail(quickTestPersonalData.getEmail());
        quicktest.setPhoneNumber(quickTestPersonalData.getPhoneNumber());
        quicktest.setSex(quickTestPersonalData.getSex());
        quicktest.setStreet(quickTestPersonalData.getStreet());
        quicktest.setHouseNumber(quickTestPersonalData.getHouseNumber());
        quicktest.setZipCode(quickTestPersonalData.getZipCode());
        quicktest.setCity(quickTestPersonalData.getCity());
        quicktest.setBirthday(quickTestPersonalData.getBirthday());
        quicktest.setTestResultServerHash(quickTestPersonalData.getTestResultServerHash());
        try {
            quickTestRepository.saveAndFlush(quicktest);
        } catch (Exception e) {
            log.error("Could not save. updateQuickTestWithPersonalData failed. Exception = {}", e.getMessage());
            throw new QuickTestServiceException(QuickTestServiceException.Reason.SAVE_FAILED);
        }

        if (quickTestPersonalData.getConfirmationCwa()) {
            log.debug("Sending TestResult to TestResult-Server");
            try {
                sendResultToTestResultServer(quicktest.getTestResultServerHash(), quicktest.getTestResult());
            } catch (TestResultServiceException e) {
                log.error("Failed to send TestResult to TestResult-Server", e);
                throw new QuickTestServiceException(QuickTestServiceException.Reason.TEST_RESULT_SERVER_ERROR);
            }
        }

        log.info("Updated TestResult for hashedGuid {} with PersonalData", quicktest.getHashedGuid());

    }

    @Transactional
    protected void addStatistics(QuickTest quickTest) {
        LocalDate currentDate = Utilities.getCurrentLocalDateInGermany();
        if (quickTestStatisticsRepository.findByPocIdAndCreatedAt(quickTest.getPocId(), currentDate).isEmpty()) {
            quickTestStatisticsRepository.save(new QuickTestStatistics(quickTest.getPocId(), quickTest.getTenantId()));
            log.debug("New QuickTestStatistics created for poc {}", quickTest.getPocId());
        }
        if (quickTest.getTestResult() == 7) {
            quickTestStatisticsRepository.incrementPositiveAndTotalTestCount(quickTest.getPocId(), currentDate);
        } else {
            quickTestStatisticsRepository.incrementTotalTestCount(quickTest.getPocId(), currentDate);
        }
    }

    private QuickTestArchive mappingQuickTestToQuickTestAchive(
            QuickTest quickTest, byte[] pdf) {
        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setShortHashedGuid(quickTest.getShortHashedGuid());
        quickTestArchive.setHashedGuid(quickTest.getHashedGuid());
        quickTestArchive.setConfirmationCwa(quickTest.getConfirmationCwa());
        quickTestArchive.setCreatedAt(quickTest.getCreatedAt());
        quickTestArchive.setUpdatedAt(quickTest.getUpdatedAt());
        quickTestArchive.setTenantId(quickTest.getTenantId());
        quickTestArchive.setPocId(quickTest.getPocId());
        quickTestArchive.setTestResult(quickTest.getTestResult());
        quickTestArchive.setPrivacyAgreement(quickTest.getPrivacyAgreement());
        quickTestArchive.setFirstName(quickTest.getFirstName());
        quickTestArchive.setLastName(quickTest.getLastName());
        quickTestArchive.setBirthday(quickTest.getBirthday());
        quickTestArchive.setEmail(quickTest.getEmail());
        quickTestArchive.setPhoneNumber(quickTest.getPhoneNumber());
        quickTestArchive.setSex(quickTest.getSex());
        quickTestArchive.setStreet(quickTest.getStreet());
        quickTestArchive.setHouseNumber(quickTest.getHouseNumber());
        quickTestArchive.setZipCode(quickTest.getZipCode());
        quickTestArchive.setCity(quickTest.getCity());
        quickTestArchive.setTestBrandId(quickTest.getTestBrandId());
        quickTestArchive.setTestBrandName(quickTest.getTestBrandName());
        quickTestArchive.setPdf(pdf);
        quickTestArchive.setTestResultServerHash(quickTest.getTestResultServerHash());
        return quickTestArchive;
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

    private void sendResultToTestResultServer(String testResultServerHash, short result)
            throws TestResultServiceException {
        QuickTestResult quickTestResult = new QuickTestResult();
        quickTestResult.setId(testResultServerHash);
        quickTestResult.setResult(result);
        testResultService.createOrUpdateTestResult(quickTestResult);
    }

    private byte[] createPdf(QuickTest quicktest, List<String> pocInformation, String user) throws IOException {
        return pdf.generatePdf(pocInformation, quicktest, user).toByteArray();
    }
}

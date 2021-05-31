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
import app.coronawarn.quicktest.domain.DccStatus;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.domain.QuickTestLog;
import app.coronawarn.quicktest.model.QuickTestResult;
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.utils.PdfGenerator;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class QuickTestService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestRepository quickTestRepository;
    private final TestResultService testResultService;
    private final QuickTestArchiveRepository quickTestArchiveRepository;
    private final QuickTestLogRepository quickTestLogRepository;
    private final PdfGenerator pdf;

    /**
     * Checks if an other quick test with given short hash already exists.
     * If not a new entity of QuickTest will be created and stored.
     * Also a pending TestResult will be sent to TestResult-Server.
     *
     * @param ids        Map with tenantId and testscopeId
     * @param hashedGuid SHA256 hash of the test GUID.
     * @throws ResponseStatusException with status CONFLICT if a QuickTest with short hash already exists.
     */
    public void createNewQuickTest(Map<String, String> ids, String hashedGuid)
        throws ResponseStatusException {
        String shortHash = hashedGuid.substring(0, 8);
        log.debug("Searching for existing QuickTests with shortHash {}", shortHash);

        Optional<QuickTest> conflictingQuickTestByHashed =
            quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuidOrHashedGuid(
                ids.get(quickTestConfig.getTenantIdKey()), ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                shortHash, hashedGuid);

        Optional<QuickTestArchive> conflictingQuickTestArchiveByHashed =
            quickTestArchiveRepository.findByHashedGuid(hashedGuid);

        if (conflictingQuickTestByHashed.isPresent() || conflictingQuickTestArchiveByHashed.isPresent()) {
            log.debug("QuickTest with Guid {} already exists", shortHash);
            log.info("QuickTest with Guid already exists");
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        QuickTest newQuickTest = new QuickTest();
        newQuickTest.setShortHashedGuid(shortHash);
        newQuickTest.setTenantId(ids.get(quickTestConfig.getTenantIdKey()));
        newQuickTest.setPocId(ids.get(quickTestConfig.getTenantPointOfCareIdKey()));
        newQuickTest.setHashedGuid(hashedGuid);

        log.debug("Persisting QuickTest in database");
        try {
            quickTestRepository.save(newQuickTest);
            log.debug("Created new QuickTest with hashedGUID {}", hashedGuid);
            log.info("Created new QuickTest with hashedGUID");
        } catch (Exception e) {
            log.debug("Failed to insert new QuickTest, hashedGuid = {}", hashedGuid);
            log.error("Failed to insert new QuickTest");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates a QuickTest entity in persistence.
     *
     * @param shortHash the short-hash of the testresult to be updated
     * @param result    the result of the quick test.
     */
    @Transactional(rollbackOn = ResponseStatusException.class)
    public void updateQuickTest(Map<String, String> ids, String shortHash, short result, String testBrandId,
                                String testBrandName, List<String> pocInformation,
                                String user) throws ResponseStatusException {
        QuickTest quicktest = getQuickTest(
            ids.get(quickTestConfig.getTenantIdKey()),
            ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
            shortHash
        );
        log.debug("Updating TestResult on TestResult-Server for hash {}", quicktest.getHashedGuid());
        log.info("Updating TestResult on TestResult-Server for hash");
        quicktest.setTestResult(result);
        quicktest.setTestBrandId(testBrandId);
        quicktest.setTestBrandName(testBrandName);
        if (quicktest.getDccConsent()!=null && quicktest.getDccConsent()) {
            // Result needs to be positive or negative
            if ((quicktest.getTestResult()==6 || quicktest.getTestResult()==7) && quicktest.getDccStatus()==null) {
                if (quicktest.getConfirmationCwa()!=null && quicktest.getConfirmationCwa()) {
                    quicktest.setDccStatus(DccStatus.pendingPublicKey);
                } else {
                    quicktest.setDccStatus(DccStatus.pendingSignatureNoCWA);
                }
            }
        }

        addStatistics(quicktest);
        byte[] pdf;
        try {
            pdf = createPdf(quicktest, pocInformation, user);
        } catch (IOException e) {
            log.error("generating PDF failed.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            quickTestArchiveRepository.save(mappingQuickTestToQuickTestArchive(quicktest, pdf));
            log.debug("New QuickTestArchive created for poc {} and shortHashedGuid {}",
                quicktest.getPocId(), quicktest.getShortHashedGuid());
        } catch (Exception e) {
            log.error("Could not save quickTestArchive. updateQuickTest failed.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            quickTestRepository.deleteById(quicktest.getHashedGuid());
            log.debug("QuickTest moved to QuickTestArchive for poc {} and shortHashedGuid {}",
                quicktest.getPocId(), quicktest.getShortHashedGuid());
        } catch (Exception e) {
            log.error("Could not delete QuickTest. updateQuickTest failed.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        sendResultToTestResultServer(quicktest.getTestResultServerHash(), result,
            quicktest.getUpdatedAt().toEpochSecond(ZoneOffset.UTC),
            quicktest.getConfirmationCwa() != null ? quicktest.getConfirmationCwa() : false);
        log.debug("Updated TestResult for hashedGuid {} with TestResult {}", quicktest.getHashedGuid(), result);
        log.info("Updated TestResult for hashedGuid with TestResult");
    }

    /**
     * Updates a QuickTest entity with personaldata in persistence.
     *
     * @param shortHash             the short-hash of the testresult to be updated
     * @param quickTestPersonalData the quick test personaldata.
     */
    @Transactional(rollbackOn = ResponseStatusException.class)
    public void updateQuickTestWithPersonalData(Map<String, String> ids, String shortHash,
                                                QuickTest quickTestPersonalData)
        throws ResponseStatusException {
        QuickTest quicktest = getQuickTest(
            ids.get(quickTestConfig.getTenantIdKey()),
            ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
            shortHash
        );
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
        quicktest.setStandardisedFamilyName(quickTestPersonalData.getStandardisedFamilyName());
        quicktest.setStandardisedGivenName(quickTestPersonalData.getStandardisedGivenName());
        quicktest.setDiseaseAgentTargeted(quickTestPersonalData.getDiseaseAgentTargeted());
        quicktest.setTestResultServerHash(quickTestPersonalData.getTestResultServerHash());
        quicktest.setDccConsent(quickTestPersonalData.getDccConsent());
        try {
            quickTestRepository.saveAndFlush(quicktest);
        } catch (Exception e) {
            log.error("Could not save. updateQuickTestWithPersonalData failed.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        sendResultToTestResultServer(quicktest.getTestResultServerHash(), quicktest.getTestResult(),
            quicktest.getUpdatedAt().toEpochSecond(ZoneOffset.UTC),
            quickTestPersonalData.getConfirmationCwa() != null ? quickTestPersonalData.getConfirmationCwa() : false);
        log.debug("Updated TestResult for hashedGuid {} with PersonalData", quicktest.getHashedGuid());
        log.info("Updated TestResult for hashedGuid with PersonalData");

    }

    /**
     * Remove all quicktests before timestamp.
     * If quicktest already has personal data, a fail-Result is sent to the testresultserver.
     *
     * @param deleteTimestamp Timestamp before which everything will be deleted
     */
    public void removeAllBefore(LocalDateTime deleteTimestamp) {
        quickTestRepository.findAllByCreatedAtBeforeAndVersionIsGreaterThan(deleteTimestamp, 0).forEach(quickTest -> {
            this.sendResultToTestResultServer(quickTest.getTestResultServerHash(),
                TestResult.FAILED.getValue(),
                deleteTimestamp.toEpochSecond(ZoneOffset.UTC),
                quickTest.getConfirmationCwa() != null ? quickTest.getConfirmationCwa() : false);
        });

        quickTestRepository.deleteByCreatedAtBefore(deleteTimestamp);
    }

    protected void addStatistics(QuickTest quickTest) {
        QuickTestLog quickTestLog = new QuickTestLog();
        quickTestLog.setCreatedAt(quickTest.getCreatedAt());
        quickTestLog.setPocId(quickTest.getPocId());
        quickTestLog.setPositiveTestResult(quickTest.getTestResult() == TestResult.fromName("positive").getValue());
        quickTestLog.setTenantId(quickTest.getTenantId());
        quickTestLogRepository.save(quickTestLog);
    }

    private QuickTestArchive mappingQuickTestToQuickTestArchive(
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

    private QuickTest getQuickTest(String tenantId, String pocId, String shortHash) throws ResponseStatusException {
        log.debug("Requesting QuickTest for short Hash {}", shortHash);
        QuickTest quicktest = quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(tenantId, pocId, shortHash);
        if (quicktest == null) {
            log.debug("Requested Quick Test with shortHash {} could not be found.", shortHash);
            log.info("Requested Quick Test with shortHash could not be found.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return quicktest;
    }

    /**
     * Finds all pending quicktests for tenant and poc containing personal data.
     *
     * @param ids Map with tenantId und pocId from token
     * @return List including found quicktests
     */
    public List<QuickTest> findAllPendingQuickTestsByTenantIdAndPocId(Map<String, String> ids) {
        List<QuickTest> quickTests = quickTestRepository.findAllByTenantIdAndPocIdAndVersionIsGreaterThan(
            ids.get(quickTestConfig.getTenantIdKey()),
            ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
            0
        );
        return quickTests;
    }

    private void sendResultToTestResultServer(String testResultServerHash, short result, Long sc,
                                              boolean confirmationCwa)throws ResponseStatusException {
        if (confirmationCwa && testResultServerHash != null) {
            log.info("Sending TestResult to TestResult-Server");
            QuickTestResult quickTestResult = new QuickTestResult();
            quickTestResult.setId(testResultServerHash);
            quickTestResult.setResult(result);
            quickTestResult.setSampleCollection(sc);
            testResultService.createOrUpdateTestResult(quickTestResult);
            log.info("Update TestResult on TestResult-Server successfully.");
        }
    }

    protected byte[] createPdf(QuickTest quicktest, List<String> pocInformation, String user) throws IOException {
        return pdf.generatePdf(pocInformation, quicktest, user).toByteArray();
    }
}

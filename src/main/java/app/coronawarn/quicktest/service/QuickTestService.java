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
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.model.quicktest.PcrTestResult;
import app.coronawarn.quicktest.model.quicktest.QuickTestDccConsent;
import app.coronawarn.quicktest.model.quicktest.QuickTestResult;
import app.coronawarn.quicktest.model.quicktest.QuickTestUpdateRequest;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.repository.QuicktestView;
import app.coronawarn.quicktest.utils.PdfGenerator;
import app.coronawarn.quicktest.utils.TestTypeUtils;
import app.coronawarn.quicktest.utils.Utilities;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    private final Utilities utilities;

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
        newQuickTest.setGroupName(utilities.getSubGroupFromToken().orElse(""));

        log.debug("Persisting QuickTest in database");
        try {
            quickTestRepository.save(newQuickTest);
            log.debug("Created new QuickTest with hashedGUID {}", hashedGuid);
            log.info("Created new QuickTest with hashedGUID");
        } catch (Exception e) {
            log.debug("Failed to insert new QuickTest, hashedGuid = {}, message=[{}]", hashedGuid, e.getMessage());
            log.error("Failed to insert new QuickTest");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * process quicktest result.
     *
     * @param ids                    ids
     * @param shortHash              shortHash
     * @param quickTestUpdateRequest quickTestUpdateRequest
     * @param pocInformation         pocInformation
     * @param user                   user
     * @throws ResponseStatusException exception
     */
    @Transactional(rollbackFor = ResponseStatusException.class)
    public void updateQuickTest(Map<String, String> ids, String shortHash,
                                QuickTestUpdateRequest quickTestUpdateRequest, List<String> pocInformation,
                                String user) throws ResponseStatusException {
        QuickTest quicktest = getQuickTest(
                ids.get(quickTestConfig.getTenantIdKey()),
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                shortHash
        );

        if (quicktest.getTestResult() != QuickTest.TEST_RESULT_PENDING
            && quicktest.getTestResult() != QuickTest.TEST_RESULT_PCR_PENDING) {
            log.info("Requested Quick Test with shortHash is not pending.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not pending");
        }
        log.debug("Updating TestResult on TestResult-Server for hash {}", quicktest.getHashedGuid());
        log.info("Updating TestResult on TestResult-Server for hash");
        quicktest.setTestResult(quickTestUpdateRequest.getResult());

        validateTestType(quickTestUpdateRequest, quicktest);

        quicktest.setUpdatedAt(LocalDateTime.now());

        if ((quicktest.getTestResult() == QuickTest.TEST_RESULT_PCR_NEGATIVE
            || quicktest.getTestResult() == QuickTest.TEST_RESULT_PCR_POSITIVE
            || quicktest.getTestResult() == QuickTest.TEST_RESULT_NEGATIVE
            || quicktest.getTestResult() == QuickTest.TEST_RESULT_POSITIVE)
                && quicktest.getDccStatus() == null) {
            if (quicktest.getConfirmationCwa() != null && quicktest.getConfirmationCwa()
                    && quicktest.getDccConsent() != null && quicktest.getDccConsent()) {
                quicktest.setDccStatus(DccStatus.pendingPublicKey);
            } else {
                quicktest.setDccStatus(DccStatus.pendingSignatureNoCWA);
            }
        }
        addStatistics(quicktest);
        byte[] pdf;
        try {
            // TODO PDF change
            pdf = createPdf(quicktest, pocInformation, user);
        } catch (IOException e) {
            log.error("generating PDF failed.");
            log.debug("generating PDF failed, message=[{}]", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            quickTestArchiveRepository.save(mappingQuickTestToQuickTestArchive(quicktest, pdf));
            log.debug("New QuickTestArchive created for poc {} and shortHashedGuid {}",
                    quicktest.getPocId(), quicktest.getShortHashedGuid());
        } catch (Exception e) {
            log.error("Could not save quickTestArchive. updateQuickTest failed.");
            log.debug("Could not save quickTestArchive, message=[{}]", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            if (quicktest.getDccStatus() == null) {
                quickTestRepository.deleteById(quicktest.getHashedGuid());
                log.debug("QuickTest moved to QuickTestArchive for poc {} and shortHashedGuid {}",
                        quicktest.getPocId(), quicktest.getShortHashedGuid());
            }
        } catch (Exception e) {
            log.error("Could not delete QuickTest. updateQuickTest failed.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        sendResultToTestResultServer(quicktest.getTestResultServerHash(), quickTestUpdateRequest.getResult(),
                quicktest.getUpdatedAt().toEpochSecond(ZoneOffset.UTC),
                quicktest.getConfirmationCwa() != null ? quicktest.getConfirmationCwa() : false,
                TestTypeUtils.isPcr(quicktest.getTestType()));
        log.debug("Updated TestResult for hashedGuid {} with TestResult {}", quicktest.getHashedGuid(),
                quickTestUpdateRequest.getResult());
        log.info("Updated TestResult for hashedGuid with TestResult");
    }

    private void validateTestType(QuickTestUpdateRequest quickTestUpdateRequest, QuickTest quicktest) {
        if (TestTypeUtils.isPcr(quicktest.getTestType())) {
            if (StringUtils.isBlank(quickTestUpdateRequest.getPcrTestName())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                  "PcrTestName must be set for NAAT Tests");
            }

            if (quicktest.getTestResult() > 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                  "TestResult not allowed for NAAT Tests");
            }

            //TODO set both to name?
            quicktest.setTestBrandId(quickTestUpdateRequest.getPcrTestName());
            quicktest.setTestBrandName(quickTestUpdateRequest.getPcrTestName());
        } else {
            if (quicktest.getDccConsent() != null && quicktest.getDccConsent()) {
                if (StringUtils.isBlank(quickTestUpdateRequest.getDccTestManufacturerId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                      "DccTestManufacturerId must be set for DCC Tests");
                }
                if (quicktest.getTestResult() < 5) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                      "TestResult not allowed for RAT Tests");
                }

                quicktest.setTestBrandId(quickTestUpdateRequest.getDccTestManufacturerId());
                quicktest.setTestBrandName(sanitiseInput(quickTestUpdateRequest.getDccTestManufacturerDescription()));
            } else {
                quicktest.setTestBrandId(quickTestUpdateRequest.getTestBrandId());
                quicktest.setTestBrandName(sanitiseInput(quickTestUpdateRequest.getTestBrandName()));
            }
        }
    }

    /**
     * Deletes a QuickTest entity if it is in its inital state.
     *
     * @param shortHash the short-hash of the testresult to be updated
     * @param ids       ids
     * @param user      User
     */
    public void deleteQuicktest(Map<String, String> ids, String shortHash, String user)
            throws ResponseStatusException {
        try {
            QuickTest quicktest = getQuickTest(
                    ids.get(quickTestConfig.getTenantIdKey()),
                    ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                    shortHash);
            if (quicktest.getVersion() > 0) {
                log.warn("User {} tried to delete QT with version > 0", user);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Deletion of filled Quicktests not permitted");
            }
            quickTestRepository.deleteById(quicktest.getHashedGuid());
        } catch (ResponseStatusException e) {
            log.error("Failed to delete Quicktest");
            throw e;
        }

    }

    /**
     * Updates a QuickTest entity with personaldata in persistence.
     *
     * @param shortHash             the short-hash of the testresult to be updated
     * @param quickTestPersonalData the quick test personaldata.
     */
    @Transactional(rollbackFor = ResponseStatusException.class)
    public void updateQuickTestWithPersonalData(Map<String, String> ids, String shortHash,
                                                QuickTest quickTestPersonalData)
            throws ResponseStatusException {

        QuickTest quicktest = getQuickTest(
          ids.get(quickTestConfig.getTenantIdKey()),
          ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
          shortHash
        );

        final String testType = StringUtils.isBlank(quickTestPersonalData.getTestType())
          ? TestTypeUtils.getDefaultType() : quickTestPersonalData.getTestType();
        switch (TestTypeUtils.getTestType(testType)) {
          case INVALID:
              log.warn("TestType {} not supported.", testType);
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
          case NAAT:
              quicktest.setTestResult((short) 0);
              quicktest.setTestType(testType);
              break;
          default:
              quicktest.setTestType(testType);
              break;
        }
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
        quicktest.setDccConsent(quickTestPersonalData.getDccConsent() != null && quickTestPersonalData.getDccConsent());
        quicktest.setAdditionalInfo(quickTestPersonalData.getAdditionalInfo());
        try {
            quickTestRepository.saveAndFlush(quicktest);
        } catch (Exception e) {
            log.error("Could not save. updateQuickTestWithPersonalData failed.");
            log.debug("Could not save updateQuickTestWithPersonalData, message=[{}]", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // only create entry in TR if rat, TR only accepts results 1-3
        if (TestTypeUtils.isRat(quicktest.getTestType())) {
            sendResultToTestResultServer(quicktest.getTestResultServerHash(), quicktest.getTestResult(),
                    quicktest.getUpdatedAt().toEpochSecond(ZoneOffset.UTC),
                    quicktest.getConfirmationCwa() != null ? quicktest.getConfirmationCwa() : false,
                    false);
        }
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
        log.info("Deleting QuickTests from DB");

        int totalCount = quickTestRepository.countAllByCreatedAtBeforeAndVersionIsGreaterThan(deleteTimestamp, 0);
        int chunkSize = quickTestConfig.getCleanUpSettings().getChunkSize();
        int chunks = totalCount / chunkSize + 1;

        log.info("Found {} QuickTests which need to set to failed on TRS in {} chunks", totalCount, chunks);

        for (int i = 1; i <= chunks; i++) {
            log.info("Deleting chunk {} of {}", i, chunks);

            List<QuickTest> quickTestChunk = quickTestRepository.findAllByCreatedAtBeforeAndVersionIsGreaterThan(
                    deleteTimestamp,
                    0,
                    PageRequest.of(i - 1, chunkSize));

            quickTestChunk.forEach(quickTest -> sendResultToTestResultServer(
                    quickTest.getTestResultServerHash(),
                    TestResult.FAILED.getValue(),
                    deleteTimestamp.toEpochSecond(ZoneOffset.UTC),
                    quickTest.getConfirmationCwa() != null ? quickTest.getConfirmationCwa() : false,
                    TestTypeUtils.isPcr(quickTest.getTestType())));

            log.info("Set Status of quicktests on TRS. Deleting QuickTests in DB");

            quickTestRepository.deleteAll(quickTestChunk);
            quickTestRepository.flush();

            log.info("Processing of chunk {} of {} finished.", i, chunks);
        }

        log.info("Delete remaining QuickTests");
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
        quickTestArchive.setAdditionalInfo(quickTest.getAdditionalInfo());
        quickTestArchive.setGroupName(quickTest.getGroupName());
        quickTestArchive.setTestType(quickTest.getTestType());
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
    @Transactional(readOnly = true)
    public List<QuicktestView> findAllPendingQuickTestsByTenantIdAndPocId(Map<String, String> ids) {
        return quickTestRepository.getShortHashedGuidByTenantIdAndPocIdAndTestResultInAndVersionIsGreaterThan(
                ids.get(quickTestConfig.getTenantIdKey()),
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                List.of(QuickTest.TEST_RESULT_PENDING, QuickTest.TEST_RESULT_PCR_PENDING),
                0
        );
    }

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

    protected byte[] createPdf(QuickTest quicktest, List<String> pocInformation, String user) throws IOException {
        return pdf.generatePdf(pocInformation, quicktest, user).toByteArray();
    }

    /**
     * get dcc consent.
     *
     * @param ids       ids
     * @param shortHash shortHash
     * @return the data you need
     */
    public QuickTestDccConsent getDccConsent(Map<String, String> ids, String shortHash) {
        String tenantId = ids.get(quickTestConfig.getTenantIdKey());
        String pocId = ids.get(quickTestConfig.getTenantPointOfCareIdKey());
        QuickTest quicktest = quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(tenantId, pocId, shortHash);
        if (quicktest == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        QuickTestDccConsent quickTestDccConsent = new QuickTestDccConsent();
        quickTestDccConsent.setDccConsent(quicktest.getDccConsent());
        quickTestDccConsent.setTestResult(quicktest.getTestResult());
        quickTestDccConsent.setTestType(quicktest.getTestType());
        return quickTestDccConsent;
    }

    private String sanitiseInput(String input) {
        // Unicode Block FF00 to FFEF is not availabe in pdf font
        // Replace with whitespace
        return input != null ? input.replaceAll("[\\uFF00-\\uFFEF]", " ") : null;
    }

    /**
     * Checks whether pending Quick Tests for a given Tenant ID and a given List of Poc Ids exists.
     *
     * @param tenantId ID of the tenant
     * @param pocIds   List with the Poc IDs
     * @return true is at least one test exists, false otherwise.
     */
    public boolean pendingTestsForTenantAndPocsExists(String tenantId, List<String> pocIds) {
        return quickTestRepository.countAllByTenantIdIsAndPocIdIsIn(tenantId, pocIds) > 0;
    }
}

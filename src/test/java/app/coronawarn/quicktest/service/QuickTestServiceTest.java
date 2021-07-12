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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.QuickTestDccConsent;
import app.coronawarn.quicktest.model.QuickTestResult;
import app.coronawarn.quicktest.model.QuickTestUpdateRequest;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.utils.PdfGenerator;
import app.coronawarn.quicktest.utils.Utilities;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@SpringBootTest
public class QuickTestServiceTest {
    @InjectMocks
    private QuickTestService quickTestService;
    @Mock
    private QuickTestConfig quickTestConfig;
    @Mock
    private QuickTestRepository quickTestRepository;
    @Mock
    private QuickTestArchiveRepository quickTestArchiveRepository;
    @Mock
    private QuickTestLogRepository quickTestLogRepository;
    @Mock
    private TestResultService testResultService;

    @Mock
    private PdfGenerator pdf;
    @Mock
    private Utilities utilities;

    @Test
    void conflictInQuickTestShortAndFullHashTest() {
        QuickTest conflictingQuickTestByHashed = new QuickTest();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuidOrHashedGuid(any(), any(), any(), any()))
            .thenReturn(Optional.of(conflictingQuickTestByHashed));
        try {
            quickTestService.createNewQuickTest(utilities.getIdsFromToken(),
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
            fail("conflict did not recognized");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.CONFLICT, "wrong status");
        }
    }

    @Test
    void conflictInQuickTestArchiveLongHashTest() {
        QuickTestArchive conflictingQuickTestByHashed = new QuickTestArchive();
        when(quickTestArchiveRepository.findByHashedGuid(
            "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4"))
            .thenReturn(Optional.of(conflictingQuickTestByHashed));
        try {
            quickTestService.createNewQuickTest(utilities.getIdsFromToken(),
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
            fail("conflict did not recognized");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.CONFLICT, "wrong status");
        }
    }

    @Test
    void createNewQuickTestSaveFailedTest() {
        when(quickTestRepository.save(any())).thenThrow(new RuntimeException());
        try {
            quickTestService.createNewQuickTest(utilities.getIdsFromToken(),
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        }
    }

    @Test
    void addStatisticsInUpdateQuickTestIsCalledTest() throws ResponseStatusException {
        QuickTestService qs = spy(quickTestService);
        Map<String, String> ids = new HashMap<>();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(createPendingTest());
        try {
            QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
            quickTestUpdateRequest.setTestBrandId("testBrandId");
            quickTestUpdateRequest.setResult((short)6);
            quickTestUpdateRequest.setTestBrandName("TestBrandName");
            qs.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                quickTestUpdateRequest,
                new ArrayList<>(),
                "User");
        } catch (NullPointerException e) {
        }
        verify(qs, times(1)).addStatistics(any());
    }

    @Test
    void createPdfInUpdateQuickTestIoExceptionTest() throws IOException {
        Map<String, String> ids = new HashMap<>();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(createPendingTest());
        when(pdf.generatePdf(any(), any(), any()))
            .thenThrow(new IOException());
        try {
            QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
            quickTestUpdateRequest.setTestBrandId("testBrandId");
            quickTestUpdateRequest.setResult((short)6);
            quickTestUpdateRequest.setTestBrandName("TestBrandName");
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                    quickTestUpdateRequest,
                new ArrayList<>(),
                "User");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        }
    }

    @Test
    void UpdateNotFoundTest() throws IOException {
        Map<String, String> ids = new HashMap<>();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(null);
        try {
            QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
            quickTestUpdateRequest.setTestBrandId("testBrandId");
            quickTestUpdateRequest.setResult((short)6);
            quickTestUpdateRequest.setTestBrandName("TestBrandName");
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                    quickTestUpdateRequest,
                new ArrayList<>(),
                "User");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.NOT_FOUND, "wrong status");
        }
    }

    private QuickTest createPendingTest() {
        QuickTest quickTest = new QuickTest();
        quickTest.setTestResult(QuickTest.TEST_RESULT_PENDING);
        return quickTest;
    }

    @Test
    void saveFailedInUpdateQuickTestTest() throws IOException {
        Map<String, String> ids = new HashMap<>();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(createPendingTest());
        when(pdf.generatePdf(any(), any(), any()))
            .thenReturn(new ByteArrayOutputStream());
        when(quickTestArchiveRepository.save(any()))
            .thenThrow(new NullPointerException());
        try {
            QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
            quickTestUpdateRequest.setTestBrandId("testBrandId");
            quickTestUpdateRequest.setResult((short)6);
            quickTestUpdateRequest.setTestBrandName("TestBrandName");
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                    quickTestUpdateRequest,
                new ArrayList<>(),
                "User");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        }
    }

    @Test
    void deleteFailedInUpdateQuickTestTest() throws IOException {
        Map<String, String> ids = new HashMap<>();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(createPendingTest());
        when(pdf.generatePdf(any(), any(), any()))
            .thenReturn(new ByteArrayOutputStream());
        doThrow(new NullPointerException()).when(quickTestRepository).deleteById(any());
        try {
            QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
            quickTestUpdateRequest.setTestBrandId("testBrandId");
            quickTestUpdateRequest.setResult((short)8);
            quickTestUpdateRequest.setTestBrandName("TestBrandName");
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                // 6 and 7 are still there because needed for dcc processing
                    quickTestUpdateRequest,
                new ArrayList<>(),
                "User");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        }
    }

    @Test
    void callResultServerInUpdateQuickTestTest() throws IOException, ResponseStatusException {
        LocalDateTime now = ZonedDateTime.now().withNano(0).toLocalDateTime();
        Map<String, String> ids = new HashMap<>();
        QuickTest quickTest = createPendingTest();
        quickTest.setConfirmationCwa(true);
        quickTest.setTestResultServerHash("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        quickTest.setUpdatedAt(now);
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(quickTest);
        when(pdf.generatePdf(any(), any(), any()))
            .thenReturn(new ByteArrayOutputStream());
        QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
        quickTestUpdateRequest.setTestBrandId("testBrandId");
        quickTestUpdateRequest.setResult((short)6);
        quickTestUpdateRequest.setTestBrandName("TestBrandName");
        quickTestService.updateQuickTest(ids,
            "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                quickTestUpdateRequest,
            new ArrayList<>(),
            "User");
        verify(testResultService, times(1)).createOrUpdateTestResult(any());
    }

    @Test
    void updateQuickTestSaveFailedTest() {
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(true);
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(quickTest);
        when(quickTestRepository.saveAndFlush(any())).thenThrow(new RuntimeException());
        try {
            quickTestService.updateQuickTestWithPersonalData(utilities.getIdsFromToken(),
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                quickTest);
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        }
    }

    @Test
    void callResultServerInUpdateQuickTestWithPersonalDataTest() {
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(true);
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(quickTest);
        try {
            quickTestService.updateQuickTestWithPersonalData(utilities.getIdsFromToken(),
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                quickTest);
            fail("has to throw exception");
        } catch (NullPointerException e) {
            verify(testResultService, times(0)).createOrUpdateTestResult(any());
            // Test passed. testResultService not initialized. Call try successful
        }
    }

    @Test
    void removeAllBeforeTest() {
        LocalDateTime now = ZonedDateTime.now().withNano(0).toLocalDateTime();
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(true);
        quickTest.setTestResultServerHash("");
        quickTest.setTestResult((short) 8);
        quickTest.setUpdatedAt(now);

        QuickTest quickTest1 = new QuickTest();
        quickTest1.setConfirmationCwa(false);
        quickTest1.setTestResultServerHash("");
        quickTest1.setTestResult((short) 8);
        quickTest1.setUpdatedAt(now);


        QuickTestResult quickTestResult = new QuickTestResult();
        quickTestResult.setId(quickTest.getTestResultServerHash());
        quickTestResult.setResult(quickTest.getTestResult());
        quickTestResult.setSampleCollection(quickTest.getUpdatedAt().toEpochSecond(ZoneOffset.UTC));

        when(quickTestRepository.findAllByCreatedAtBeforeAndVersionIsGreaterThan(now ,0))
            .thenReturn(Arrays.asList(quickTest, quickTest, quickTest1));
        quickTestService.removeAllBefore(now);
        verify(quickTestRepository, times(1)).findAllByCreatedAtBeforeAndVersionIsGreaterThan(now, 0);
        verify(testResultService, times(2)).createOrUpdateTestResult(quickTestResult);
        verify(quickTestRepository, times(1)).deleteByCreatedAtBefore(now);
    }

    @Test
    void findAllPendingQuickTestsByTenantIdAndPocIdTest() {
        Map<String, String> ids = new HashMap<>();
        List<QuickTest> quickTests = new ArrayList<>();
        QuickTest quickTest = new QuickTest();
        quickTest.setPrivacyAgreement(true);
        quickTest.setShortHashedGuid("00000000");
        quickTest.setDccConsent(true);
        quickTests.add(quickTest);
        when(quickTestRepository.findAllByTenantIdAndPocIdAndTestResultAndVersionIsGreaterThan(any(), any(), any(), any()))
                .thenReturn(quickTests);
        List<QuickTest> quickTests1 = quickTestService.findAllPendingQuickTestsByTenantIdAndPocId(ids);
        assertEquals(quickTests1.get(0).getPrivacyAgreement(), true);
        assertEquals(quickTests1.get(0).getShortHashedGuid(), "00000000");
    }

    @Test
    void getDccContent() {
        Map<String, String> ids = new HashMap<>();
        QuickTest quickTest = new QuickTest();
        quickTest.setPrivacyAgreement(true);
        quickTest.setShortHashedGuid("00000000");
        quickTest.setDccConsent(true);
        quickTest.setTestResult((short)5);
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
                .thenReturn(quickTest);
        QuickTestDccConsent dccContent = quickTestService.getDccConsent(ids, "00000000");
        assertNotNull(dccContent);
        assertTrue(dccContent.getDccConsent());

    }

}

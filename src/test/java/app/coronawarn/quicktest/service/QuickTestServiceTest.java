package app.coronawarn.quicktest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.EmailConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.QuickTestPersonalDataRequest;
import app.coronawarn.quicktest.model.QuickTestResult;
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
    private EmailConfig emailConfig;
    @Mock
    private EmailService emailService;
    @Mock
    private HealthDepartmentService healthDepartmentService;

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
            .thenReturn(new QuickTest());
        try {
            qs.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                (short) 6,
                "testBrandId",
                "TestBrandName",
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
            .thenReturn(new QuickTest());
        when(pdf.generatePdf(any(), any(), any()))
            .thenThrow(new IOException());
        try {
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                (short) 6,
                "testBrandId",
                "TestBrandName",
                new ArrayList<>(),
                "User");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        }
    }

    @Test
    void sendMailInUpdateQuickTest() throws IOException, EmailService.EmailServiceException {
        Map<String, String> ids = new HashMap<>();
        ids.put("postal_code", "12345");
        List<String> pocInfo = new ArrayList<>();
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(false);
        quickTest.setShortHashedGuid("0");
        quickTest.setHashedGuid("0");
        quickTest.setPrivacyAgreement(true);
        quickTest.setEmailNotificationAgreement(true);
        quickTest.setEmail("mail@example.tld");
        quickTest.setZipCode("12345");
        EmailConfig.TestedPerson tp = new EmailConfig.TestedPerson();
        tp.setEnabled(true);
        EmailConfig.HealthDepartment hd = new EmailConfig.HealthDepartment();
        hd.setEnabled(true);

        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any())).thenReturn(quickTest);
        when(quickTestRepository.save(any())).thenReturn(null);
        when(pdf.generatePdf(any(), any(), any())).thenReturn(new ByteArrayOutputStream());
        when(pdf.encryptPdf(any(), any())).thenReturn(new ByteArrayOutputStream());
        when(healthDepartmentService.findHealthDepartmentEmailByZipCode("12345")).thenReturn("covid@example.tld");
        when(emailConfig.getTestedPerson()).thenReturn(tp);
        when(emailConfig.getHealthDepartment()).thenReturn(hd);
        when(quickTestConfig.getPointOfCareZipcodeKey()).thenReturn("postal_code");

        quickTestService.updateQuickTest(ids, "0", (short) 6, "testBrandId", "testBrandName", pocInfo, "user");
        verify(pdf, times(1)).encryptPdf(any(), any());
        verify(emailService, times(1)).sendMailToTestedPerson(any(), any());
        verify(emailService, times(0)).sendMailToHealthDepartment(any(), any());

        quickTestService.updateQuickTest(ids, "0", (short) 7, "testBrandId", "testBrandName", pocInfo, "user");
        verify(emailService, times(2)).sendMailToTestedPerson(any(), any());
        verify(emailService, times(1)).sendMailToHealthDepartment(any(), any());
    }

    @Test
    void sendMailInUpdateQuickDisabledTest() throws IOException, EmailService.EmailServiceException {
        Map<String, String> ids = new HashMap<>();
        ids.put("postal_code", "12345");
        List<String> pocInfo = new ArrayList<>();
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(false);
        quickTest.setShortHashedGuid("0");
        quickTest.setHashedGuid("0");
        quickTest.setPrivacyAgreement(true);
        quickTest.setEmailNotificationAgreement(true);
        quickTest.setEmail("mail@example.tld");
        quickTest.setZipCode("12345");
        EmailConfig.TestedPerson tp = new EmailConfig.TestedPerson();
        tp.setEnabled(false);
        EmailConfig.HealthDepartment hd = new EmailConfig.HealthDepartment();
        hd.setEnabled(false);

        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any())).thenReturn(quickTest);
        when(quickTestRepository.save(any())).thenReturn(null);
        when(pdf.generatePdf(any(), any(), any())).thenReturn(new ByteArrayOutputStream());
        when(pdf.encryptPdf(any(), any())).thenReturn(new ByteArrayOutputStream());
        when(healthDepartmentService.findHealthDepartmentEmailByZipCode("12345")).thenReturn("covid@example.tld");
        when(emailConfig.getTestedPerson()).thenReturn(tp);
        when(emailConfig.getHealthDepartment()).thenReturn(hd);
        when(quickTestConfig.getPointOfCareZipcodeKey()).thenReturn("postal_code");

        quickTestService.updateQuickTest(ids, "0", (short) 7, "testBrandId", "testBrandName", pocInfo, "user");
        verify(emailService, times(0)).sendMailToTestedPerson(any(), any());
        verify(emailService, times(0)).sendMailToHealthDepartment(any(), any());
    }

    @Test
    void UpdateNotFoundTest() throws IOException {
        Map<String, String> ids = new HashMap<>();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(null);
        try {
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                (short) 6,
                "testBrandId",
                "TestBrandName",
                new ArrayList<>(),
                "User");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.NOT_FOUND, "wrong status");
        }
    }

    @Test
    void saveFailedInUpdateQuickTestTest() throws IOException {
        Map<String, String> ids = new HashMap<>();
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(new QuickTest());
        when(pdf.generatePdf(any(), any(), any()))
            .thenReturn(new ByteArrayOutputStream());
        when(quickTestArchiveRepository.save(any()))
            .thenThrow(new NullPointerException());
        try {
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                (short) 6,
                "testBrandId",
                "TestBrandName",
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
            .thenReturn(new QuickTest());
        when(pdf.generatePdf(any(), any(), any()))
            .thenReturn(new ByteArrayOutputStream());
        doThrow(new NullPointerException()).when(quickTestRepository).deleteById(any());
        try {
            quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                (short) 6,
                "testBrandId",
                "TestBrandName",
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
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(true);
        quickTest.setTestResultServerHash("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        quickTest.setUpdatedAt(now);
        when(quickTestRepository.findByTenantIdAndPocIdAndShortHashedGuid(any(), any(), any()))
            .thenReturn(quickTest);
        when(pdf.generatePdf(any(), any(), any()))
            .thenReturn(new ByteArrayOutputStream());
        quickTestService.updateQuickTest(ids,
            "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
            (short) 6,
            "testBrandId",
            "TestBrandName",
            new ArrayList<>(),
            "User");
        verify(testResultService, times(1)).createOrUpdateTestResult(any());
    }

    @Test
    void updateQuickTestSaveFailedTest() {
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(true);
        quickTest.setPrivacyAgreement(true);
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
        quickTests.add(quickTest);
        when(quickTestRepository.findAllByTenantIdAndPocIdAndVersionIsGreaterThan(any(), any(), any()))
                .thenReturn(quickTests);
        List<QuickTest> quickTests1 = quickTestService.findAllPendingQuickTestsByTenantIdAndPocId(ids);
        assertEquals(quickTests1.get(0).getPrivacyAgreement(), true);
        assertEquals(quickTests1.get(0).getShortHashedGuid(), "00000000");
    }

}

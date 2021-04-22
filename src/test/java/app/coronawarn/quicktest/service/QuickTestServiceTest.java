package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.repository.QuickTestStatisticsRepository;
import app.coronawarn.quicktest.utils.PdfGenerator;
import app.coronawarn.quicktest.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.constraints.AssertTrue;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private TestResultService testResultService;
    @Mock
    private QuickTestArchiveRepository quickTestArchiveRepository;
    @Mock
    private QuickTestStatisticsRepository quickTestStatisticsRepository;
    @Mock
    private PdfGenerator pdf;
    @Mock
    private Utilities utilities;

    @Test
    void conflictInQuickTestShortAndFullHashTest() {
        QuickTest conflictingQuickTestByHashed = new QuickTest();
        when(quickTestRepository.findByPocIdAndShortHashedGuidOrHashedGuid(any(), any(), any()))
                .thenReturn(Optional.of(conflictingQuickTestByHashed));
        try {
            quickTestService.createNewQuickTest(utilities.getIdsFromToken(),
                    "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
            fail("conflict did not recognized");
        } catch (QuickTestServiceException e) {
            assertTrue(e.getReason().equals(QuickTestServiceException.Reason.INSERT_CONFLICT),
                    "wrong exception");
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
        } catch (QuickTestServiceException e) {
            assertTrue(e.getReason().equals(QuickTestServiceException.Reason.INSERT_CONFLICT),
                    "wrong exception");
        }
    }

    @Test
    void createNewQuickTestSaveFailedTest() {
        when(quickTestRepository.save(any())).thenThrow(new RuntimeException());
        try {
            quickTestService.createNewQuickTest(utilities.getIdsFromToken(),
                    "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        } catch (QuickTestServiceException e) {
            assertTrue(e.getReason().equals(QuickTestServiceException.Reason.SAVE_FAILED),
                    "wrong exception");
        }
    }


}

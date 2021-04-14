package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.repository.QuickTestStatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class QuickTestArchiveServiceTest {

    @Mock
    private QuickTestConfig quickTestConfig;
    @Mock
    private QuickTestRepository quickTestRepository;
    @Mock
    private QuickTestArchiveRepository quickTestArchiveRepository;
    @Mock
    private QuickTestStatisticsRepository quickTestStatisticsRepository;

    @InjectMocks
    private QuickTestArchiveService quickTestArchiveService;

    @Test
    void createNewQuickTestArchiveQuickTestNotFound() {
        try {
            quickTestArchiveService.createNewQuickTestArchive(new HashMap<>(), "shg", null);
            fail("has to throw exception");
        } catch (QuickTestServiceException e) {
            assertTrue(e.getReason().equals(QuickTestServiceException.Reason.UPDATE_NOT_FOUND), "wrong exception");
        }
    }
}

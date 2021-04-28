package app.coronawarn.quicktest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import app.coronawarn.quicktest.config.QuickTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class QuickTestCleanupServiceTest {

    @Autowired
    QuickTestConfig quickTestConfig;

    @Mock
    QuickTestService quickTestService;


    @Test
    void cleanupQuickTests() {
        QuickTestCleanupService quickTestCleanupService =
            new QuickTestCleanupService(quickTestConfig, quickTestService);
        quickTestCleanupService.cleanupQuickTests();
        verify(quickTestService, times(1)).removeAllBefore(any());
    }
}

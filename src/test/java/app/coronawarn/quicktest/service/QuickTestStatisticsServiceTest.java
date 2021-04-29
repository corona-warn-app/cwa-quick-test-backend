package app.coronawarn.quicktest.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.model.QuickTestStatistics;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QuickTestStatisticsServiceTest {

    @Mock
    private QuickTestConfig quickTestConfig;
    @Mock
    private QuickTestLogRepository quickTestLogRepository;
    @InjectMocks
    private QuickTestStatisticsService quickTestStatisticsService;

    @Test
    void getStatistics() {
        QuickTestStatistics quickTestStatistics = QuickTestStatistics.builder()
            .totalTestCount(5)
            .positiveTestCount(3)
            .build();

        when(quickTestLogRepository.countAllByTenantIdAndPocIdAndCreatedAtBetween(any(), any(), any(), any()))
            .thenReturn(
                quickTestStatistics.getTotalTestCount());
        when(quickTestLogRepository.countAllByTenantIdAndPocIdAndPositiveTestResultIsTrueAndCreatedAtBetween(any(),
            any(), any(), any())).thenReturn(
            quickTestStatistics.getPositiveTestCount());

        QuickTestStatistics actQuickTestStatistics = quickTestStatisticsService.getStatistics(new HashMap<>(),
            LocalDateTime.now(), LocalDateTime.now());
        assertEquals(quickTestStatistics.getTotalTestCount(), actQuickTestStatistics.getTotalTestCount());
        assertEquals(quickTestStatistics.getPositiveTestCount(), actQuickTestStatistics.getPositiveTestCount());
    }
}

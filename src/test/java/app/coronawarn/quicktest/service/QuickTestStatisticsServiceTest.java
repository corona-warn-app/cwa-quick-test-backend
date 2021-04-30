package app.coronawarn.quicktest.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestLog;
import app.coronawarn.quicktest.model.Aggregation;
import app.coronawarn.quicktest.model.QuickTestStatistics;
import app.coronawarn.quicktest.model.QuickTestTenantStatistics;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
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

    @Test
    void getStatisticsForTenantTest(){
        when(quickTestLogRepository.findAllByTenantIdAndCreatedAtBetweenOrderByPocIdAscCreatedAtAsc(any(),
            any(), any())).thenReturn(getQuickTestLogTestData());
        List<QuickTestTenantStatistics> result = quickTestStatisticsService.getStatisticsForTenant(new HashMap<>(),
            LocalDateTime.now(), LocalDateTime.now(), Aggregation.NONE);

        assertIterableEquals(getQuickTestTenantStatisticsTestData(),result);
    }

    private List<QuickTestLog> getQuickTestLogTestData(){
        List<QuickTestLog> quickTestLogs = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for(int i = 0; i<43600; i++) {
            quickTestLogs.add(generateQuickTestLog(i, time, "pocId_1", "tenant", i%10==0));
            time = time.plusMinutes(1);
        }
        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for(int i = 0; i<43600; i++) {
            quickTestLogs.add(generateQuickTestLog(i, time, "pocId_2", "tenant", i%20==0));
            time = time.plusMinutes(1);
        }
        return quickTestLogs;
    }

    private QuickTestLog generateQuickTestLog(long id, LocalDateTime created, String pocId, String tenantId,
                                              boolean positive) {
        QuickTestLog quickTestLog = new QuickTestLog();
        quickTestLog.setCreatedAt(created);
        quickTestLog.setPocId(pocId);
        quickTestLog.setPositiveTestResult(positive);
        quickTestLog.setId(id);
        quickTestLog.setTenantId(tenantId);
        return quickTestLog;
    }


    private List<QuickTestTenantStatistics> getQuickTestTenantStatisticsTestData(){
        List<QuickTestTenantStatistics> quickTestTenantStatistics = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for(int i = 0; i<43600; i++) {
            quickTestTenantStatistics.add(generateQuickTestLog(1, i%10==0?1:0, time, "pocId_1", Aggregation.NONE));
            time = time.plusMinutes(1);
        }
        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for(int i = 0; i<43600; i++) {
            quickTestTenantStatistics.add(generateQuickTestLog(1, i%20==0?1:0, time, "pocId_2", Aggregation.NONE));
            time = time.plusMinutes(1);
        }
        return quickTestTenantStatistics;
    }


    private QuickTestTenantStatistics generateQuickTestLog(int totalCount, int positiveCount, LocalDateTime created, String pocId,
                                              Aggregation aggregation) {
        return QuickTestTenantStatistics.builder()
            .quickTestStatistics(QuickTestStatistics.builder()
                .totalTestCount(totalCount)
                .positiveTestCount(positiveCount)
                .build())
            .aggregation(aggregation)
            .timestamp(created.atZone(ZoneId.of("UTC")))
            .pocId(pocId)
            .build();
    }
}

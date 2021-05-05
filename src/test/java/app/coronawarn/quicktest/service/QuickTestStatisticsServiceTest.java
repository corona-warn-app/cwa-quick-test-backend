package app.coronawarn.quicktest.service;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
    void getStatisticsForTenantTest() {
        LocalDateTime startTime = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        LocalDateTime endTime = startTime.plusHours(727).minusNanos(1);
        when(quickTestLogRepository.findAllByTenantIdAndCreatedAtBetweenOrderByPocIdAscCreatedAtAsc(any(),
            any(), any())).thenReturn(getQuickTestLogTestData());

        List<QuickTestTenantStatistics> result = quickTestStatisticsService.getStatisticsForTenant(null,
            startTime, endTime, Aggregation.NONE);
        assertIterableEquals(getQuickTestTenantStatisticsTestDataAggregationNone(), result);

        result = quickTestStatisticsService.getStatisticsForTenant(null,
            startTime, endTime, Aggregation.HOUR);
        assertIterableEquals(getQuickTestTenantStatisticsTestDataAggregationHour(), result);

        result = quickTestStatisticsService.getStatisticsForTenant(null,
            startTime, endTime, Aggregation.DAY);
        assertIterableEquals(getQuickTestTenantStatisticsTestDataAggregationDay(), result);


        List<QuickTestLog> quickTestLogs = new ArrayList<>();
        List<QuickTestTenantStatistics> quickTestTenantStatisticsHour = new ArrayList<>();
        List<QuickTestTenantStatistics> quickTestTenantStatisticsDay = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        quickTestLogs.add(generateQuickTestLog(1, time, "pocId", "tenant", false));
        time = LocalDateTime.of(2020, 4, 1, 23, 59, 59);
        quickTestLogs.add(generateQuickTestLog(1, time, "pocId", "tenant", true));
        time = LocalDateTime.of(2020, 4, 2, 0, 0, 0);
        quickTestLogs.add(generateQuickTestLog(1, time, "pocId", "tenant", false));
        time = LocalDateTime.of(2020, 4, 2, 0, 0, 1);
        quickTestLogs.add(generateQuickTestLog(1, time, "pocId", "tenant", true));


        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        // 0 => 1
        quickTestTenantStatisticsHour.add(generateQuickTestTenantStatistics(1, 0, time, "pocId", Aggregation.HOUR));
        for (int i = 1; i < 23; i++) {
            // 1=>2 .... 22=>23
            time = time.plusHours(1);
            quickTestTenantStatisticsHour.add(generateQuickTestTenantStatistics(0, 0, time, "pocId", Aggregation.HOUR));
        }
        time = time.plusHours(1);
        // 23=>0
        quickTestTenantStatisticsHour.add(generateQuickTestTenantStatistics(1, 1, time, "pocId", Aggregation.HOUR));
        time = time.plusHours(1);
        // 0=>1
        quickTestTenantStatisticsHour.add(generateQuickTestTenantStatistics(2, 1, time, "pocId", Aggregation.HOUR));
        for (int i = 1; i < 24; i++) {
            // 1=>2 .... 23=>0
            time = time.plusHours(1);
            quickTestTenantStatisticsHour.add(generateQuickTestTenantStatistics(0, 0, time, "pocId", Aggregation.HOUR));
        }

        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        quickTestTenantStatisticsDay.add(generateQuickTestTenantStatistics(2, 1, time, "pocId", Aggregation.DAY));
        time = time.plusDays(1);
        quickTestTenantStatisticsDay.add(generateQuickTestTenantStatistics(2, 1, time, "pocId", Aggregation.DAY));

        when(quickTestLogRepository.findAllByTenantIdAndCreatedAtBetweenOrderByPocIdAscCreatedAtAsc(any(),
            any(), any())).thenReturn(quickTestLogs);

        startTime = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        endTime = startTime.plusDays(2).minusNanos(1);

        result = quickTestStatisticsService.getStatisticsForTenant(null,
            startTime, endTime, Aggregation.HOUR);
        assertIterableEquals(quickTestTenantStatisticsHour, result);

        result = quickTestStatisticsService.getStatisticsForTenant(null,
            startTime, endTime, Aggregation.DAY);
        assertIterableEquals(quickTestTenantStatisticsDay, result);
    }

    @Test
    void getStatisticsForTenantTestNoAgg() {
        when(quickTestLogRepository.findAllByTenantIdAndCreatedAtBetweenOrderByPocIdAscCreatedAtAsc(any(),
                any(), any())).thenReturn(manuallyGeneratedLogTestData());
        LocalDateTime startTime = LocalDateTime.of(2020, 3, 26, 8, 0, 0);
        LocalDateTime endTime = startTime.plusDays(7);
        List<QuickTestTenantStatistics> result = quickTestStatisticsService.getStatisticsForTenant("t",
                startTime, endTime, Aggregation.NONE);
        assertEquals(336*4, result.size());
    }

    @Test
    void getStatisticsForTenantTestAggHour() {
        when(quickTestLogRepository.findAllByTenantIdAndCreatedAtBetweenOrderByPocIdAscCreatedAtAsc(any(),
                any(), any())).thenReturn(manuallyGeneratedLogTestData());
        LocalDateTime startTime = LocalDateTime.of(2020, 3, 26, 8, 0, 0);
        LocalDateTime endTime = startTime.plusDays(7).minusNanos(1);
        List<QuickTestTenantStatistics> result = quickTestStatisticsService.getStatisticsForTenant("t",
                startTime, endTime, Aggregation.HOUR);
        assertEquals(336, result.size());
        result.forEach(r -> {
            assertEquals(4, r.getQuickTestStatistics().getTotalTestCount(),
                    "fail @ row with time: "+r.getTimestamp());
            assertEquals(2, r.getQuickTestStatistics().getPositiveTestCount(),
                    "fail @ row with time: "+r.getTimestamp());
        });
        assertEquals("pocId_1", result.get(167).getPocId());
        assertEquals("pocId_2", result.get(168).getPocId());
    }

    @Test
    void getStatisticsForTenantTestAggDay() {
        when(quickTestLogRepository.findAllByTenantIdAndCreatedAtBetweenOrderByPocIdAscCreatedAtAsc(any(),
                any(), any())).thenReturn(manuallyGeneratedLogTestData());
        LocalDateTime startTime = LocalDateTime.of(2020, 3, 26, 8, 0, 0);
        LocalDateTime endTime = startTime.plusDays(7);
        List<QuickTestTenantStatistics> result = quickTestStatisticsService.getStatisticsForTenant("t",
                startTime, endTime, Aggregation.DAY);
        assertEquals(16, result.size());
        assertEquals("pocId_1", result.get(7).getPocId());
        assertEquals("pocId_2", result.get(8).getPocId());

        assertEquals(16*4, result.get(0).getQuickTestStatistics().getTotalTestCount());
        assertEquals(16*2, result.get(0).getQuickTestStatistics().getPositiveTestCount());

        for (int i = 1; i != 7; i++) {
            assertEquals(24*4, result.get(i).getQuickTestStatistics().getTotalTestCount(), "index: "+i);
            assertEquals(24*2, result.get(i).getQuickTestStatistics().getPositiveTestCount(), "index: "+i);

            assertEquals(24*4, result.get(i+8).getQuickTestStatistics().getTotalTestCount(), "index: "+(i+8));
            assertEquals(24*2, result.get(i+8).getQuickTestStatistics().getPositiveTestCount(), "index: "+(i+8));
        }

        assertEquals(8*4, result.get(7).getQuickTestStatistics().getTotalTestCount());
        assertEquals(8*2, result.get(7).getQuickTestStatistics().getPositiveTestCount());

        assertEquals(8*4, result.get(15).getQuickTestStatistics().getTotalTestCount());
        assertEquals(8*2, result.get(15).getQuickTestStatistics().getPositiveTestCount());

    }

    private List<QuickTestLog> manuallyGeneratedLogTestData() {
        LocalDateTime startTime = LocalDateTime.of(2020, 3, 26, 8, 1, 0);
        List<QuickTestLog> manuallyGeneratedLogs = new ArrayList<>();
        int currentId = 11511;
        LocalDateTime currentTime = startTime;
        // from 2020-03-26T08:01 to 2020-04-02T07:32 = 168h
        for (int i = 0; i != 336; i++) {
            manuallyGeneratedLogs.add(generateQuickTestLog(currentId, currentTime,
                    "pocId_1", "tenant", false));
            currentId++;
            manuallyGeneratedLogs.add(generateQuickTestLog(currentId, currentTime,
                    "pocId_2", "tenant", false));
            currentId++;

            manuallyGeneratedLogs.add(generateQuickTestLog(currentId, currentTime.plusMinutes(1),
                    "pocId_1", "tenant", true));
            currentId++;
            manuallyGeneratedLogs.add(generateQuickTestLog(currentId, currentTime.plusMinutes(1),
                    "pocId_2", "tenant", true));
            currentId++;

            currentTime = currentTime.plusMinutes(30);
        }
        manuallyGeneratedLogs.sort(Comparator.comparing(QuickTestLog::getId));
        return manuallyGeneratedLogs;
    }

    private List<QuickTestLog> getQuickTestLogTestData() {
        LocalDateTime time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        List<QuickTestLog> quickTestLogs = new ArrayList<>();
        for (int i = 0; i < 43620; i++) {
            quickTestLogs.add(generateQuickTestLog(i, time, "pocId_1", "tenant", i % 10 == 0));
            time = time.plusMinutes(1);
        }
        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for (int i = 0; i < 43620; i++) {
            quickTestLogs.add(generateQuickTestLog(i, time, "pocId_2", "tenant", i % 20 == 0));
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


    private List<QuickTestTenantStatistics> getQuickTestTenantStatisticsTestDataAggregationNone() {
        List<QuickTestTenantStatistics> quickTestTenantStatistics = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for (int i = 0; i < 43620; i++) {
            quickTestTenantStatistics
                .add(generateQuickTestTenantStatistics(1, i % 10 == 0 ? 1 : 0, time, "pocId_1", Aggregation.NONE));
            time = time.plusMinutes(1);
        }
        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for (int i = 0; i < 43620; i++) {
            quickTestTenantStatistics
                .add(generateQuickTestTenantStatistics(1, i % 20 == 0 ? 1 : 0, time, "pocId_2", Aggregation.NONE));
            time = time.plusMinutes(1);
        }
        return quickTestTenantStatistics;
    }

    private List<QuickTestTenantStatistics> getQuickTestTenantStatisticsTestDataAggregationHour() {
        List<QuickTestTenantStatistics> quickTestTenantStatistics = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for (int i = 0; i < 727; i++) {
            quickTestTenantStatistics
                .add(generateQuickTestTenantStatistics(60, 60 / 10, time, "pocId_1", Aggregation.HOUR));
            time = time.plusMinutes(60);
        }
        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for (int i = 0; i < 727; i++) {
            quickTestTenantStatistics
                .add(generateQuickTestTenantStatistics(60, 60 / 20, time, "pocId_2", Aggregation.HOUR));
            time = time.plusMinutes(60);
        }
        return quickTestTenantStatistics;
    }

    private List<QuickTestTenantStatistics> getQuickTestTenantStatisticsTestDataAggregationDay() {
        List<QuickTestTenantStatistics> quickTestTenantStatistics = new ArrayList<>();
        LocalDateTime time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for (int i = 0; i < 30; i++) {
            quickTestTenantStatistics
                .add(generateQuickTestTenantStatistics(60 * 24, 60 * 24 / 10, time, "pocId_1", Aggregation.DAY));
            time = time.plusDays(1);
        }
        quickTestTenantStatistics.add(generateQuickTestTenantStatistics(60 * 7,
            60 * 7 / 10,
            time,
            "pocId_1",
            Aggregation.DAY));
        time = LocalDateTime.of(2020, 4, 1, 0, 0, 0);
        for (int i = 0; i < 30; i++) {
            quickTestTenantStatistics
                .add(generateQuickTestTenantStatistics(60 * 24, 60 * 24 / 20, time, "pocId_2", Aggregation.DAY));
            time = time.plusDays(1);
        }
        quickTestTenantStatistics.add(generateQuickTestTenantStatistics(60 * 7,
            60 * 7 / 20,
            time,
            "pocId_2",
            Aggregation.DAY));
        return quickTestTenantStatistics;
    }

    private QuickTestTenantStatistics generateQuickTestTenantStatistics(int totalCount, int positiveCount,
                                                                        LocalDateTime created, String pocId,
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

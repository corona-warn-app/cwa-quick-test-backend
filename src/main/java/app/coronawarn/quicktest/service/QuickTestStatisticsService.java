package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestLog;
import app.coronawarn.quicktest.model.Aggregation;
import app.coronawarn.quicktest.model.QuickTestStatistics;
import app.coronawarn.quicktest.model.QuickTestTenantStatistics;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuickTestStatisticsService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestLogRepository quickTestLogRepository;

    /**
     * Return statistic for QuickTest by pocid and time range.
     */
    public QuickTestStatistics getStatistics(Map<String, String> ids, LocalDateTime utcDateFrom,
                                             LocalDateTime utcDateTo) {
        int totalCount = quickTestLogRepository.countAllByTenantIdAndPocIdAndCreatedAtBetween(
            ids.get(quickTestConfig.getTenantIdKey()), ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
            utcDateFrom, utcDateTo);

        int totalPositiveCount = quickTestLogRepository
            .countAllByTenantIdAndPocIdAndPositiveTestResultIsTrueAndCreatedAtBetween(
                ids.get(quickTestConfig.getTenantIdKey()), ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                utcDateFrom, utcDateTo);

        return QuickTestStatistics.builder()
            .totalTestCount(totalCount).positiveTestCount(totalPositiveCount).build();
    }

    /**
     * Return aggregated statistic for QuickTest by tenant and time range.
     */
    public List<QuickTestTenantStatistics> getStatisticsForTenant(String tenantId, LocalDateTime utcDateFrom,
                                                                  LocalDateTime utcDateTo, Aggregation aggregation) {

        List<QuickTestTenantStatistics> quickTestTenantStatistics = new ArrayList<>();

        // Map by pocId and list sorted by time
        Map<String, List<QuickTestLog>> quickTestLogSortedByPocId =
            getQuickTestLogSortedByPocId(tenantId, utcDateFrom, utcDateTo);

        quickTestLogSortedByPocId.forEach((pocId, quickTestLogs) -> {
            if (aggregation != Aggregation.NONE) {
                //aggregate values for the given aggregation
                for (LocalDateTime start = utcDateFrom; Duration.between(start, utcDateTo).getSeconds() > 0; start =
                    start.plusSeconds(aggregation.getValue())) {
                    int totalTestCount =
                        countOfQuickTestsInTimewindow(quickTestLogs,
                            start.minusSeconds(1), start.plusSeconds(aggregation.getValue()),
                            false);
                    int positiveTestCount =
                        countOfQuickTestsInTimewindow(quickTestLogs,
                            start.minusSeconds(1), start.plusSeconds(aggregation.getValue()),
                            true);

                    createEntryForQuickTestTenantStatistics(
                        quickTestTenantStatistics, aggregation, pocId, start, totalTestCount,
                        positiveTestCount);
                }
            } else {
                quickTestLogs.forEach(quickTestLog -> createEntryForQuickTestTenantStatistics(
                    quickTestTenantStatistics, aggregation, pocId, quickTestLog.getCreatedAt(), 1,
                    quickTestLog.getPositiveTestResult() ? 1 : 0));
            }
        });

        return quickTestTenantStatistics;
    }

    private void createEntryForQuickTestTenantStatistics(List<QuickTestTenantStatistics> quickTestTenantStatistics,
                                                         Aggregation aggregation,
                                                         String pocId, LocalDateTime timestamp, int totalCount,
                                                         int postiveCount) {
        QuickTestStatistics quickTestStatistics = QuickTestStatistics.builder()
            .totalTestCount(totalCount)
            .positiveTestCount(postiveCount)
            .build();

        quickTestTenantStatistics.add(generateQuickTestTenantStatistics(quickTestStatistics, pocId,
            timestamp, aggregation));
    }

    private Map<String, List<QuickTestLog>> getQuickTestLogSortedByPocId(String tenantId,
                                                                         LocalDateTime utcDateFrom,
                                                                         LocalDateTime utcDateTo) {
        return quickTestLogRepository.findAllByTenantIdAndCreatedAtBetweenOrderByPocIdAscCreatedAtAsc(
            tenantId,
            utcDateFrom, utcDateTo).stream().collect(Collectors.groupingBy(QuickTestLog::getPocId));
    }

    private QuickTestTenantStatistics generateQuickTestTenantStatistics(QuickTestStatistics quickTestStatistics,
                                                                        String pocId, LocalDateTime timestamp,
                                                                        Aggregation aggregation) {
        return QuickTestTenantStatistics.builder()
            .quickTestStatistics(quickTestStatistics)
            .pocId(pocId)
            .timestamp(timestamp.atZone(ZoneId.of("UTC")))
            .aggregation(aggregation)
            .build();

    }

    private int countOfQuickTestsInTimewindow(List<QuickTestLog> quickTestLogs, LocalDateTime afterTimestamp,
                                              LocalDateTime beforeTimestamp, boolean onlyPositive) {
        return (int) quickTestLogs.stream()
            .filter(quickTestLog ->
                quickTestLog.getCreatedAt().isBefore(beforeTimestamp)
                    && quickTestLog.getCreatedAt().isAfter(afterTimestamp)
                    && (quickTestLog.getPositiveTestResult() || !onlyPositive)).count();
    }
}

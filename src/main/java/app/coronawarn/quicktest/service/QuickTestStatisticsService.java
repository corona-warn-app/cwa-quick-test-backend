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
import app.coronawarn.quicktest.domain.QuickTestLog;
import app.coronawarn.quicktest.model.Aggregation;
import app.coronawarn.quicktest.model.quicktest.QuickTestStatistics;
import app.coronawarn.quicktest.model.quicktest.QuickTestTenantStatistics;
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
@Transactional(readOnly = true)
public class QuickTestStatisticsService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestLogRepository quickTestLogRepository;

    /**
     * Return statistic for QuickTest by pocid and time range.
     */
    public QuickTestStatistics getStatistics(Map<String, String> ids, LocalDateTime utcDateFrom,
                                             LocalDateTime utcDateTo) {
        String tenantId = ids.get(quickTestConfig.getTenantIdKey());
        String pocId = ids.get(quickTestConfig.getTenantPointOfCareIdKey());
        String pcr = "LP6464-4";

        int totalCount = quickTestLogRepository.countAllByTenantIdAndPocIdAndCreatedAtBetween(tenantId, pocId,
                utcDateFrom, utcDateTo);

        int totalPositiveCount = quickTestLogRepository
            .countAllByTenantIdAndPocIdAndPositiveTestResultIsTrueAndCreatedAtBetween(tenantId, pocId, utcDateFrom,
                    utcDateTo);

        int totalPcrCount = quickTestLogRepository.countAllByTenantIdAndPocIdAndTestTypeAndCreatedAtBetween(tenantId,
                pocId, pcr, utcDateFrom, utcDateTo);

        int positivePcrCount = quickTestLogRepository
                .countAllByTenantIdAndPocIdAndAndTestTypeAndPositiveTestResultIsTrueAndCreatedAtBetween(tenantId, pocId,
                        pcr, utcDateFrom, utcDateTo);

        return QuickTestStatistics.builder()
            .totalTestCount(totalCount).positiveTestCount(totalPositiveCount)
                .pcrTestCount(totalPcrCount).pcrPositiveTestCount(positivePcrCount)
                .ratTestCount(totalCount - totalPcrCount).ratPositiveTestCount(totalPositiveCount - positivePcrCount)
                .build();
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
                LocalDateTime dateFrom = utcDateFrom;
                LocalDateTime dateTo = utcDateTo;
                if (aggregation == Aggregation.HOUR) {
                    dateFrom = utcDateFrom.withMinute(0).withSecond(0).withNano(0);
                    dateTo = utcDateTo.withMinute(0).withSecond(0).withNano(0).plusHours(1).minusNanos(1);
                } else if (aggregation == Aggregation.DAY) {
                    dateFrom = utcDateFrom.withMinute(0).withSecond(0).withNano(0).withHour(0);
                    dateTo = utcDateTo.withMinute(0).withSecond(0).withNano(0).withHour(0).plusDays(1).minusNanos(1);
                }
                for (LocalDateTime start = dateFrom; Duration.between(start, dateTo).getSeconds() > 0; start =
                    start.plusSeconds(aggregation.getValue())) {
                    int totalTestCount =
                        countOfQuickTestsInTimewindow(quickTestLogs,
                            start.minusNanos(1), start.plusSeconds(aggregation.getValue()),
                            false);
                    int positiveTestCount =
                        countOfQuickTestsInTimewindow(quickTestLogs,
                            start.minusNanos(1), start.plusSeconds(aggregation.getValue()),
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

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestLog;
import app.coronawarn.quicktest.model.Aggregation;
import app.coronawarn.quicktest.model.QuickTestStatistics;
import app.coronawarn.quicktest.model.QuickTestTenantStatistics;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        QuickTestStatistics quickTestStatistics = new QuickTestStatistics();
        quickTestStatistics.setTotalTestCount(totalCount);
        quickTestStatistics.setPositiveTestCount(totalPositiveCount);
        return quickTestStatistics;
    }

    public List<QuickTestTenantStatistics> getStatisticsForTenant(Map<String, String> ids, LocalDateTime utcDateFrom,
                                                                  LocalDateTime utcDateTo){

        List<QuickTestTenantStatistics> quickTestTenantStatistics = new ArrayList<>();

        Aggregation aggregation = determineAggregation(utcDateFrom, utcDateTo);

        List<QuickTestLog> quickTestLogs = quickTestLogRepository.findAllByTenantIdAndCreatedAtBetween(
            ids.get(quickTestConfig.getTenantIdKey()),
            utcDateFrom, utcDateTo);


        Map<String, QuickTestTenantStatistics> aggregatedQuickTestTenantStatistics = new HashMap<>();





        QuickTestTenantStatistics statistics = new QuickTestTenantStatistics();
        statistics.setAggregation(aggregation);

        return Collections.emptyList();
    }


    private Aggregation determineAggregation(LocalDateTime utcDateFrom,
                                             LocalDateTime utcDateTo){

        Duration duration = Duration.between(utcDateFrom,utcDateTo);
        duration.getSeconds();
        int secondsOfDay= 86400;
        if (duration.getSeconds() <= secondsOfDay){
            return Aggregation.NONE;
        }
        else if (duration.getSeconds() <= secondsOfDay*5){
            return Aggregation.HOUR;
        }
        else if (duration.getSeconds() <= secondsOfDay*5*4){
            return Aggregation.DAY;
        }
        else{
            return Aggregation.MONTH;
        }
    }



}

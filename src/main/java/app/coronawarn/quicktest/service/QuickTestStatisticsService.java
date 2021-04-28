package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.model.QuickTestStatistics;
import app.coronawarn.quicktest.repository.QuickTestLogRepository;
import java.time.LocalDateTime;
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
    private final QuickTestLogRepository quickTestStatisticRepository;

    /**
     * Return statistic for QuickTest by pocid and time range.
     */
    public QuickTestStatistics getStatistics(Map<String, String> ids, LocalDateTime utcDateFrom,
                                             LocalDateTime utcDateTo) {
        int totalCount = quickTestStatisticRepository.countAllByTenantIdAndPocIdAndCreatedAtBetween(
            ids.get(quickTestConfig.getTenantIdKey()), ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
            utcDateFrom, utcDateTo);

        int totalPositiveCount = quickTestStatisticRepository
            .countAllByTenantIdAndPocIdAndPositiveTestResultIsTrueAndCreatedAtBetween(
                ids.get(quickTestConfig.getTenantIdKey()), ids.get(quickTestConfig.getTenantPointOfCareIdKey()),
                utcDateFrom, utcDateTo);

        QuickTestStatistics quickTestStatistics = new QuickTestStatistics();
        quickTestStatistics.setTotalTestCount(totalCount);
        quickTestStatistics.setPositiveTestCount(totalPositiveCount);
        return quickTestStatistics;
    }
}

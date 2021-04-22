package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.repository.QuickTestStatisticsRepository;
import app.coronawarn.quicktest.utils.Utilities;
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
    private final QuickTestStatisticsRepository quickTestStatisticRepository;

    /**
     * Return statistic for QuickTest for today by pocid.
     */
    public QuickTestStatistics getStatistics(Map<String, String> ids, LocalDateTime utcDateFrom,
                                             LocalDateTime utcDateTo) {
        int totalCount = quickTestStatisticRepository.countAllByPocIdAndCreatedAtBetween(
                        ids.get(quickTestConfig.getTenantPointOfCareIdKey()), utcDateFrom, utcDateTo);

        int totalPositiveCount = quickTestStatisticRepository
                .countAllByPocIdAndPositiveTestResultIsTrueAndCreatedAtBetween(
                        ids.get(quickTestConfig.getTenantPointOfCareIdKey()), utcDateFrom, utcDateTo);

        QuickTestStatistics quickTestStatistics = new QuickTestStatistics();
        quickTestStatistics.setTotalTestCount(totalCount);
        quickTestStatistics.setPositiveTestCount(totalPositiveCount);
        return quickTestStatistics;
    }

}

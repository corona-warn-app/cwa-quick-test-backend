package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.repository.QuickTestStatisticsRepository;
import app.coronawarn.quicktest.utils.Utilities;
import java.util.Map;
import java.util.Optional;
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
    public QuickTestStatistics getStatistics(Map<String, String> ids) {
        Optional<QuickTestStatistics> quickTestStatisticsOptional =
            quickTestStatisticRepository.findByPocIdAndCreatedAt(
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()), Utilities.getCurrentLocalDateInGermany());
        if (quickTestStatisticsOptional.isPresent()) {
            return quickTestStatisticsOptional.get();
        } else {
            log.info("Statistics not present yet. Set stats to 0");
            QuickTestStatistics emptyStats = new QuickTestStatistics();
            emptyStats.setTotalTestCount(0);
            emptyStats.setPositiveTestCount(0);
            return emptyStats;
        }
    }

}

package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.repository.QuickTestStatisticsRepository;
import java.time.LocalDate;
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
     *
     * @throws QuickTestServiceException if no data available.
     */
    public QuickTestStatistics getStatistics(Map<String, String> ids) throws QuickTestServiceException {
        Optional<QuickTestStatistics> quickTestStatisticsOptional =
            quickTestStatisticRepository.findByPocIdAndCreatedAt(
                ids.get(quickTestConfig.getTenantPointOfCareIdKey()), LocalDate.now());
        if (quickTestStatisticsOptional.isPresent()) {
            return quickTestStatisticsOptional.get();
        } else {
            log.error("Could not read statistics");
            throw new QuickTestServiceException(QuickTestServiceException.Reason.EMPTY_OR_NOT_FOUND);
        }
    }

}

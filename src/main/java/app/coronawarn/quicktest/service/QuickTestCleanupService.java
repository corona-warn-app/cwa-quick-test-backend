package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.QuickTestConfig;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuickTestCleanupService {

    private final QuickTestConfig quickTestConfig;
    private final QuickTestService quickTestService;

    /**
     * Cleanup task to delete all DiagnosisKeys and DiagnosisKeyBatches which are older then configured.
     */
    @Scheduled(cron = "${quicktest.clean-up-settings.cron}")
    @SchedulerLock(name = "QuickTestCleanupService_cleanupQuickTests", lockAtLeastFor = "PT0S",
        lockAtMostFor = "${quicktest.clean-up-settings.locklimit}")
    public void cleanupQuickTests() {
        LocalDateTime deleteTimestamp =
            Instant.now().atZone(ZoneId.of("UTC"))
                .minusMinutes(quickTestConfig.getCleanUpSettings().getMaxAgeInMinutes()).toLocalDateTime();

        log.info("Starting QuickTest cleanup");
        quickTestService.removeAllBefore(deleteTimestamp);
        log.info("QuickTest cleanup finished.");
    }
}

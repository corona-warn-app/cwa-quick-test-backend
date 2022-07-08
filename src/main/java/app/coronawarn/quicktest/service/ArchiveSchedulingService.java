package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.domain.Cancellation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ArchiveSchedulingService {

    private final ArchiveService archiveService;
    private final CancellationService cancellationService;

    /**
     * Scheduler used for moving quicktests from qt archive to longterm.
     */
    @Scheduled(cron = "${archive.moveToArchiveJob.cron}")
    @SchedulerLock(name = "MoveToArchiveJob", lockAtLeastFor = "PT0S",
      lockAtMostFor = "${archive.moveToArchiveJob.locklimit}")
    public void moveToArchiveJob() {
        log.info("Starting Job: moveToArchiveJob");
        archiveService.moveToArchive();
        log.info("Completed Job: moveToArchiveJob");
    }

    /**
     * Scheduler used for moving quicktests from qt archive to longterm when a cancellation was triggered.
     */
    @Scheduled(cron = "${archive.cancellationArchiveJob.cron}")
    @SchedulerLock(name = "CancellationArchiveJob", lockAtLeastFor = "PT0S",
      lockAtMostFor = "${archive.cancellationArchiveJob.locklimit}")
    public void cancellationArchiveJob() {
        log.info("Starting Job: cancellationArchiveJob");
        List<Cancellation> cancellations = cancellationService.getReadyToArchive();
        for (Cancellation cancellation : cancellations) {
            String partnerId = cancellation.getPartnerId();
            archiveService.moveToArchiveByTenantId(partnerId);
            cancellationService.updateMovedToLongterm(cancellation, LocalDateTime.now());
        }
        log.info("Completed Job: cancellationArchiveJob");
    }
}

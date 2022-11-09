package app.coronawarn.quicktest.service;

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
}

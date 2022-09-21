package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.archive.domain.ArchiveCipherDtoV1;
import app.coronawarn.quicktest.config.CsvUploadConfig;
import app.coronawarn.quicktest.domain.Cancellation;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
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

    private final CsvUploadConfig s3Config;
    private final AmazonS3 s3Client;

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

    /**
     * Scheduler used for moving longterm archives to bucket as a csv.
     */
    @Scheduled(cron = "${archive.csvUploadJob.cron}")
    @SchedulerLock(name = "CsvUploadJob", lockAtLeastFor = "PT0S",
      lockAtMostFor = "${archive.csvUploadJob.locklimit}")
    public void csvUploadJob() {
        log.info("Starting Job: csvUploadJob");
        List<Cancellation> cancellations = cancellationService.getReadyToUpload();
        for (Cancellation cancellation : cancellations) {
            try {
                List<ArchiveCipherDtoV1> quicktests =
                  archiveService.getQuicktestsFromLongtermByTenantId(cancellation.getPartnerId());
                StringWriter stringWriter = new StringWriter();
                CSVWriter csvWriter =
                  new CSVWriter(stringWriter, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
                StatefulBeanToCsv<ArchiveCipherDtoV1> beanToCsv =
                  new StatefulBeanToCsvBuilder<ArchiveCipherDtoV1>(csvWriter)
                    .build();
                beanToCsv.write(quicktests);
                InputStream inputStream = new ByteArrayInputStream(stringWriter.toString().getBytes());
                String id = cancellation.getPartnerId() + ".csv";
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(stringWriter.toString().getBytes().length);
                s3Client.putObject(s3Config.getBucketName(), id, inputStream, metadata);
                log.info("File stored to S3 with id {}", id);
                cancellationService.updateCsvCreated(cancellation, LocalDateTime.now(), id);
            } catch (Exception e) {
                log.error("Could not convert Quicktest to CSV: " + e.getLocalizedMessage());
                cancellationService.updateDataExportError(cancellation, e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        }
        log.info("Completed Job: csvUploadJob");
    }
}

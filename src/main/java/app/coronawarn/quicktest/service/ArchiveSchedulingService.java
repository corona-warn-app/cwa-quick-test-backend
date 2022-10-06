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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.codec.Hex;
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
        processCancellationArchiveBatchRecursion(cancellationService.getReadyToArchiveBatch());
        log.info("Completed Job: cancellationArchiveJob");
    }

    private void processCancellationArchiveBatchRecursion(List<Cancellation> cancellations) {
        log.info("Process Cancellation Archive Batch with size of {}", cancellations.size());
        for (Cancellation cancellation : cancellations) {
            String partnerId = cancellation.getPartnerId();
            archiveService.moveToArchiveByTenantId(partnerId);
            cancellationService.updateMovedToLongterm(cancellation, ZonedDateTime.now());
        }

        List<Cancellation> nextBatch = cancellationService.getReadyToArchiveBatch();
        if (!nextBatch.isEmpty()) {
            processCancellationArchiveBatchRecursion(nextBatch);
        }
    }

    /**
     * Scheduler used for moving longterm archives to bucket as a csv.
     */
    @Scheduled(cron = "${archive.csvUploadJob.cron}")
    @SchedulerLock(name = "CsvUploadJob", lockAtLeastFor = "PT0S",
      lockAtMostFor = "${archive.csvUploadJob.locklimit}")
    public void csvUploadJob() {
        log.info("Starting Job: csvUploadJob");
        processCsvUploadBatchRecursion(cancellationService.getReadyToUploadBatch());
        log.info("Completed Job: csvUploadJob");
    }

    private void processCsvUploadBatchRecursion(List<Cancellation> cancellations) {
        log.info("Process CSV Upload Batch with size of {}", cancellations.size());
        for (Cancellation cancellation : cancellations) {
            try {
                List<ArchiveCipherDtoV1> quicktests =
                  archiveService.getQuicktestsFromLongtermByTenantId(cancellation.getPartnerId());

                StringWriter stringWriter = new StringWriter();
                CSVWriter csvWriter =
                  new CSVWriter(stringWriter, '\t', CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
                StatefulBeanToCsv<ArchiveCipherDtoV1> beanToCsv =
                  new StatefulBeanToCsvBuilder<ArchiveCipherDtoV1>(csvWriter)
                    .build();
                beanToCsv.write(quicktests);
                byte[] csvBytes = stringWriter.toString().getBytes(StandardCharsets.UTF_8);

                String objectId = cancellation.getPartnerId() + ".csv";

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(csvBytes.length);

                s3Client.putObject(
                    s3Config.getBucketName(),
                    objectId,
                    new ByteArrayInputStream(csvBytes), metadata);

                log.info("File stored to S3 with id {}", objectId);

                cancellationService.updateCsvCreated(cancellation, ZonedDateTime.now(), objectId,
                    getHash(csvBytes), quicktests.size(), csvBytes.length);
            } catch (Exception e) {
                String errorMessage = e.getClass().getName() + ": " + e.getMessage();

                log.error("Could not convert Quicktest to CSV: " + errorMessage);
                cancellationService.updateDataExportError(cancellation, errorMessage);
            }
        }

        List<Cancellation> nextBatch = cancellationService.getReadyToUploadBatch();
        if (!nextBatch.isEmpty()) {
            processCsvUploadBatchRecursion(nextBatch);
        }
    }

    private String getHash(byte[] bytes) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA256");
            byte[] hashBytes = sha256.digest(bytes);
            return String.valueOf(Hex.encode(hashBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to load SHA-256 Message Digest");
        }
    }
}

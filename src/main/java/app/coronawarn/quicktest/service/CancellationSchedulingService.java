/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CancellationSchedulingService {

    private final ArchiveService archiveService;
    private final CancellationService cancellationService;

    private final KeycloakService keycloakService;

    private final CsvUploadConfig s3Config;
    private final AmazonS3 s3Client;

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

    /**
     * Scheduler used for deleting SearchPortal entries.
     */
    @Scheduled(cron = "${archive.cancellationSearchPortalDeleteJob.cron}")
    @SchedulerLock(name = "CancellationSearchPortalDeleteJob", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${archive.cancellationSearchPortalDeleteJob.locklimit}")
    public void cancellationSearchPortalDeleteJob() {
        log.info("Starting Job: cancellationSearchPortalDeleteJob");
        processCancellationDeleteSearchPortalBatch(cancellationService.getReadyToDeleteSearchPortal());
        log.info("Completed Job: cancellationSearchPortalDeleteJob");
    }

    private void processCancellationDeleteSearchPortalBatch(List<Cancellation> cancellations) {
        log.info("Process Cancellation DeleteSearchPortal Batch with size of {}", cancellations.size());
        for (Cancellation cancellation : cancellations) {
            GroupRepresentation rootGroup = keycloakService.getRootGroupByName(cancellation.getPartnerId());

            if (rootGroup == null) {
                log.error("Could not find RootGroup for Partner {}", cancellation.getPartnerId());
            } else {
                keycloakService.deleteSubGroupsFromMapService(rootGroup);
            }

            cancellationService.updateSearchPortalDeleted(cancellation, ZonedDateTime.now());
        }

        List<Cancellation> nextBatch = cancellationService.getReadyToArchiveBatch();
        if (!nextBatch.isEmpty()) {
            processCancellationArchiveBatchRecursion(nextBatch);
        }
    }

    private String getHash(byte[] bytes) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha256.digest(bytes);
            return String.valueOf(Hex.encode(hashBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to load SHA-256 Message Digest");
        }
    }
}

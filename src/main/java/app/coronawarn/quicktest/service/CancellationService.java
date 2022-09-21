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

import app.coronawarn.quicktest.config.CsvUploadConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.repository.CancellationRepository;
import com.amazonaws.services.s3.AmazonS3;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancellationService {
    private final CancellationRepository cancellationRepository;

    private final ArchiveService archiveService;

    private final AmazonS3 s3Client;

    private final CsvUploadConfig s3Config;

    private final QuickTestConfig quickTestConfig;

    /**
     * Gets the date when this cancellation will be finally deleted.
     *
     * @param cancellation Cancellation Entity
     * @return LocalDateTime
     */
    public LocalDateTime getFinalDeletion(Cancellation cancellation) {
        return cancellation.getCancellationDate()
                .plusDays(quickTestConfig.getCancellation().getFinalDeletionDays());

    }

    /**
     * Create a new Cancellation Entity for given PartnerId.
     * If cancellation already exists it will be returned and not modified.
     *
     * @param partnerId        partner ID of partner.
     * @param cancellationDate Date of cancellation will be active.
     * @return the created and persisted entity.
     */
    public Cancellation createCancellation(String partnerId, LocalDateTime cancellationDate) {
        Optional<Cancellation> existingCancellation = getByPartnerId(partnerId);

        if (existingCancellation.isPresent()) {
            return existingCancellation.get();
        } else {
            Cancellation newEntity = new Cancellation();
            newEntity.setPartnerId(partnerId);
            newEntity.setCancellationDate(cancellationDate);

            newEntity = cancellationRepository.save(newEntity);

            newEntity.setFinalDeletion(getFinalDeletion(newEntity));

            return newEntity;
        }
    }

    /**
     * Searches in the DB for an existing cancellation entity by given partnerId.
     *
     * @param partnerId the partnerId to search for
     * @return Optional holding the entity if found.
     */
    public Optional<Cancellation> getByPartnerId(String partnerId) {
        Optional<Cancellation> foundCancellation = cancellationRepository.findById(partnerId);
        foundCancellation.ifPresent(cancellation -> cancellation.setFinalDeletion(getFinalDeletion(cancellation)));

        return foundCancellation;
    }

    /**
     * Set DownloadRequested Flag/Timestamp and persist entity.
     *
     * @param cancellation      Cancellation Entity
     * @param downloadRequested timestamp of user interaction
     */
    public void updateDownloadRequested(Cancellation cancellation, LocalDateTime downloadRequested, String requester) {
        cancellation.setDownloadRequested(downloadRequested);
        cancellation.setDownloadRequestedBy(requester);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set MovedToLongtermArchive Flag/Timestamp and persist entity.
     *
     * @param cancellation           Cancellation Entity
     * @param movedToLongtermArchive timestamp of job completion
     */
    public void updateMovedToLongterm(Cancellation cancellation, LocalDateTime movedToLongtermArchive) {
        cancellation.setMovedToLongtermArchive(movedToLongtermArchive);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set CsvCreated Flag/Timestamp and persist entity.
     *
     * @param cancellation Cancellation Entity
     * @param csvCreated   timestamp of job completion
     */
    public void updateCsvCreated(Cancellation cancellation, LocalDateTime csvCreated, String bucketObjectId) {
        cancellation.setCsvCreated(csvCreated);
        cancellation.setBucketObjectId(bucketObjectId);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set DownloadLinkRequested Flag/Timestamp and persist entity.
     *
     * @param cancellation          Cancellation Entity
     * @param downloadLinkRequested timestamp of user interaction
     */
    public void updateDownloadLinkRequested(Cancellation cancellation, LocalDateTime downloadLinkRequested) {
        cancellation.setDownloadLinkRequested(downloadLinkRequested);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set DataDeleted Flag/Timestamp and persist entity.
     *
     * @param cancellation Cancellation Entity
     * @param dataDeleted  timestamp of complete deletion
     */
    public void updateDataDeleted(Cancellation cancellation, LocalDateTime dataDeleted) {
        cancellation.setDataDeleted(dataDeleted);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set DataExportError Value and persist entity.
     *
     * @param cancellation Cancellation Entity
     * @param errorMessage occured error to save in entity
     */
    public void updateDataExportError(Cancellation cancellation, String errorMessage) {
        if (errorMessage.length() > 200) {
            errorMessage = errorMessage.substring(0, 200);
        }

        cancellation.setDataExportError(errorMessage);
        cancellationRepository.save(cancellation);
    }

    /**
     * Searches in the DB for an existing cancellation entity which download request is older than 48h and not
     * moved_to_longterm_archive.
     *
     * @return List holding all entities found.
     */
    public List<Cancellation> getReadyToArchive() {
        LocalDateTime ldt = LocalDateTime.now().minusHours(quickTestConfig.getCancellation().getReadyToArchiveHours());

        return cancellationRepository.findByMovedToLongtermArchiveIsNullAndDownloadRequestedBefore(ldt);
    }

    /**
     * Searches in the DB for an existing cancellation entity which moved_to_longterm_archive is not null but
     * csv_created is null.
     *
     * @return List holding all entities found.
     */
    public List<Cancellation> getReadyToUpload() {
        return cancellationRepository.findByMovedToLongtermArchiveNotNullAndCsvCreatedIsNull();
    }

    /**
     * Job sets download_requested of all cancellations that end in less then 7 days to current time.
     */
    @Scheduled(cron = "${quicktest.cancellation.trigger-download-job.cron}")
    @SchedulerLock(name = "TriggerDownloadJob", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${quicktest.cancellation.trigger-download-job.locklimit}")
    public void triggerDownloadJob() {
        log.info("Starting Job: triggerDownloadJob");

        LocalDateTime triggerThreshold = LocalDateTime.now()
                .minusDays(quickTestConfig.getCancellation().getFinalDeletionDays())
                .plusDays(quickTestConfig.getCancellation().getTriggerDownloadDaysBeforeFinalDelete());

        List<Cancellation> cancellations =
                cancellationRepository.findByDownloadRequestedIsNullAndCancellationDateBefore(triggerThreshold);

        cancellations.forEach(cancellation -> updateDownloadRequested(
                cancellation, LocalDateTime.now(), "triggerDownloadJob"));

        log.info("Completed Job: triggerDownloadJob");
    }

    /**
     * Final job to delete data.
     */
    @Scheduled(cron = "${quicktest.cancellation.final-delete-job.cron}")
    @SchedulerLock(name = "FinalDeleteJob", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${quicktest.cancellation.final-delete-job.locklimit}")
    public void finalDeleteJob() {
        log.info("Starting Job: finalDeleteJob");
        LocalDateTime triggerThreshold = LocalDateTime.now()
                .minusDays(quickTestConfig.getCancellation().getFinalDeletionDays());

        List<Cancellation> cancellations =
                cancellationRepository.findByCancellationDateBeforeAndDataDeletedIsNull(triggerThreshold);

        cancellations.forEach(cancellation -> {
            archiveService.deleteByTenantId(cancellation.getPartnerId());
            String id = cancellation.getPartnerId() + ".csv";
            s3Client.deleteObject(s3Config.getBucketName(), id);
            updateDataDeleted(cancellation, LocalDateTime.now());
        });

        log.info("Completed Job: finalDeleteJob");
    }
}

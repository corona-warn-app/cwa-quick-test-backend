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

import app.coronawarn.quicktest.config.ArchiveProperties;
import app.coronawarn.quicktest.config.CsvUploadConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.repository.CancellationRepository;
import com.amazonaws.services.s3.AmazonS3;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
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

    private final ArchiveProperties archiveProperties;

    private final QuickTestConfig quickTestConfig;

    /**
     * Gets the date when this cancellation will be finally deleted.
     *
     * @param cancellation Cancellation Entity
     * @return LocalDateTime
     */
    public ZonedDateTime getFinalDeletion(Cancellation cancellation) {
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
    public Cancellation createCancellation(String partnerId, ZonedDateTime cancellationDate) {
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
     * Set MovedToLongtermArchive Flag/Timestamp and persist entity.
     *
     * @param cancellation           Cancellation Entity
     * @param movedToLongtermArchive timestamp of job completion
     */
    public void updateMovedToLongterm(Cancellation cancellation, ZonedDateTime movedToLongtermArchive) {
        cancellation.setMovedToLongtermArchive(movedToLongtermArchive);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set CsvCreated Flag/Timestamp and persist entity.
     *
     * @param cancellation Cancellation Entity
     * @param csvCreated   timestamp of job completion
     */
    public void updateCsvCreated(Cancellation cancellation, ZonedDateTime csvCreated, String bucketObjectId,
                                 String hash, int entityCount, int fileSize) {
        cancellation.setCsvCreated(csvCreated);
        cancellation.setBucketObjectId(bucketObjectId);
        cancellation.setCsvHash(hash);
        cancellation.setCsvEntityCount(entityCount);
        cancellation.setCsvSize(fileSize);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set DownloadLinkRequested Flag/Timestamp and persist entity.
     *
     * @param cancellation          Cancellation Entity
     * @param downloadLinkRequested timestamp of user interaction
     * @param requester             Username of the user who requested the download link
     */
    public void updateDownloadLinkRequested(
            Cancellation cancellation, ZonedDateTime downloadLinkRequested, String requester) {
        cancellation.setDownloadLinkRequested(downloadLinkRequested);
        cancellation.setDownloadLinkRequestedBy(requester);
        cancellationRepository.save(cancellation);
    }

    /**
     * Set DataDeleted Flag/Timestamp and persist entity.
     *
     * @param cancellation Cancellation Entity
     * @param dataDeleted  timestamp of complete deletion
     */
    public void updateDataDeleted(Cancellation cancellation, ZonedDateTime dataDeleted) {
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
     * Set SearchPortalDeleted Flag/Timestamp and persist entity.
     *
     * @param cancellation Cancellation Entity
     * @param dataDeleted  timestamp of search portal deletion
     */
    public void updateSearchPortalDeleted(Cancellation cancellation, ZonedDateTime dataDeleted) {
        cancellation.setSearchPortalDeleted(dataDeleted);
        cancellationRepository.save(cancellation);
    }

    /**
     * Searches in the DB for an existing cancellation entity which download request is older than 48h and not
     * moved_to_longterm_archive.
     * Returns only one batch of entities. Batch Size depends on configuration.
     *
     * @return List holding all entities found.
     */
    public List<Cancellation> getReadyToArchiveBatch() {
        ZonedDateTime ldt = ZonedDateTime.now()
                .minusHours(quickTestConfig.getCancellation().getReadyToArchiveHours());

        return cancellationRepository.findByMovedToLongtermArchiveIsNullAndCancellationDateBefore(
                ldt, PageRequest.of(0, archiveProperties.getCancellationArchiveJob().getChunkSize()));
    }

    /**
     * Searches in the DB for an existing cancellation entity with searchPortalDeleted null and
     * cancellation_date in past.
     * Returns only one batch of entities. Batch Size depends on configuration.
     *
     * @return List holding all entities found.
     */
    public List<Cancellation> getReadyToDeleteSearchPortal() {
        ZonedDateTime ldt = ZonedDateTime.now();

        return cancellationRepository.findBySearchPortalDeletedIsNullAndCancellationDateBefore(
                ldt, PageRequest.of(0, archiveProperties.getCancellationArchiveJob().getChunkSize()));
    }

    /**
     * Searches in the DB for an existing cancellation entity which moved_to_longterm_archive is not null but
     * csv_created is null.
     * Returns only one batch of entities. Batch Size depends on configuration.
     *
     * @return List holding all entities found.
     */
    public List<Cancellation> getReadyToUploadBatch() {
        return cancellationRepository.findByMovedToLongtermArchiveNotNullAndCsvCreatedIsNull(
                PageRequest.of(0, archiveProperties.getCsvUploadJob().getChunkSize()));
    }

    /**
     * Final job to delete data.
     */
    @Scheduled(cron = "${quicktest.cancellation.final-delete-job.cron}")
    @SchedulerLock(name = "FinalDeleteJob", lockAtLeastFor = "PT0S",
            lockAtMostFor = "${quicktest.cancellation.final-delete-job.locklimit}")
    public void finalDeleteJob() {
        log.info("Starting Job: finalDeleteJob");
        ZonedDateTime triggerThreshold = ZonedDateTime.now()
                .minusDays(quickTestConfig.getCancellation().getFinalDeletionDays());

        List<Cancellation> cancellations =
                cancellationRepository.findByCancellationDateBeforeAndDataDeletedIsNull(triggerThreshold);

        cancellations.forEach(cancellation -> {
            archiveService.deleteByTenantId(cancellation.getPartnerId());
            s3Client.deleteObject(s3Config.getBucketName(), cancellation.getBucketObjectId());
            updateDataDeleted(cancellation, ZonedDateTime.now());
        });

        log.info("Completed Job: finalDeleteJob");
    }
}

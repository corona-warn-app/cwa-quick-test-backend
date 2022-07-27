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
import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.repository.CancellationRepository;
import com.amazonaws.services.s3.AmazonS3;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
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


    @Value("${cancellation.triggerDownloadDaysBeforeFinalDelete}")
    private int triggerDownloadDaysBeforeFinalDelete;

    @Value("${cancellation.readyToArchiveHours}")
    private int readyToArchiveHours;

    /**
     * Create a new Cancellation Entity for given PartnerId.
     * If cancellation already exists it will be returned and not modified.
     *
     * @param partnerId     partner ID of partner.
     * @param finalDeletion Date of final deletion when all data should be deleted.
     * @return the created and persisted entity.
     */
    public Cancellation createCancellation(String partnerId, LocalDateTime finalDeletion) {
        Optional<Cancellation> existingCancellation = getByPartnerId(partnerId);

        if (existingCancellation.isPresent()) {
            return existingCancellation.get();
        } else {
            Cancellation newEntity = new Cancellation();
            newEntity.setPartnerId(partnerId);
            newEntity.setFinalDeletion(finalDeletion);

            return cancellationRepository.save(newEntity);
        }
    }

    /**
     * Searches in the DB for an existing cancellation entity by given partnerId.
     *
     * @param partnerId the partnerId to search for
     * @return Optional holding the entity if found.
     */
    public Optional<Cancellation> getByPartnerId(String partnerId) {
        return cancellationRepository.findById(partnerId);
    }

    /**
     * Set DownloadRequested Flag/Timestamp and persist entity.
     *
     * @param cancellation      Cancellation Entity
     * @param downloadRequested timestamp of user interaction
     */
    public void updateDownloadRequested(Cancellation cancellation, LocalDateTime downloadRequested) {
        cancellation.setDownloadRequested(downloadRequested);
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
     * Set FinalDeletion Flag/Timestamp and persist entity.
     *
     * @param cancellation Cancellation Entity
     * @param dataDeleted  timestamp of complete deletion
     */
    public void updateFinalDeletion(Cancellation cancellation, LocalDateTime dataDeleted) {
        cancellation.setFinalDeletion(dataDeleted);
        cancellationRepository.save(cancellation);
    }

    /**
     * Searches in the DB for an existing cancellation entity which download request is older than 48h and not
     * moved_to_longterm_archive.
     *
     * @return List holding all entities found.
     */
    public List<Cancellation> getReadyToArchive() {
        LocalDateTime ldt = LocalDateTime.now().minusHours(readyToArchiveHours);
        Date expiryDate = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
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
    @Scheduled(cron = "${cancellation.triggerDownloadJob.cron}")
    @SchedulerLock(name = "TriggerDownloadJob", lockAtLeastFor = "PT0S",
      lockAtMostFor = "${cancellation.triggerDownloadJob.locklimit}")
    public void triggerDownloadJob() {
        log.info("Starting Job: triggerDownloadJob");
        List<Cancellation> cancellations =
          cancellationRepository.findByDownloadRequestedIsNullAndFinalDeletionBefore(
            LocalDateTime.now().plusDays(triggerDownloadDaysBeforeFinalDelete));
        for (Cancellation cancellation : cancellations) {
            updateDownloadRequested(cancellation, LocalDateTime.now());
        }
        log.info("Completed Job: triggerDownloadJob");
    }

    /**
     * Final job to delete data.
     */
    @Scheduled(cron = "${cancellation.finalDeleteJob.cron}")
    @SchedulerLock(name = "FinalDeleteJob", lockAtLeastFor = "PT0S",
      lockAtMostFor = "${cancellation.finalDeleteJob.locklimit}")
    public void finalDeleteJob() {
        log.info("Starting Job: finalDeleteJob");
        List<Cancellation> cancellations =
          cancellationRepository.findByFinalDeletionBefore(LocalDateTime.now());
        for (Cancellation cancellation : cancellations) {
            archiveService.deleteByTenantId(cancellation.getPartnerId());
            String id = cancellation.getPartnerId() + ".csv";
            s3Client.deleteObject(s3Config.getBucketName(), id);
            updateFinalDeletion(cancellation,LocalDateTime.now());
        }
        log.info("Completed Job: finalDeleteJob");
    }
}

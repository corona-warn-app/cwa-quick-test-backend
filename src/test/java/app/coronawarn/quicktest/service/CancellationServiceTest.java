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

import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.repository.CancellationRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CancellationServiceTest {

    @Autowired
    private CancellationService cancellationService;

    @Autowired
    private CancellationRepository cancellationRepository;

    @BeforeEach
    void setUp() {
        cancellationRepository.deleteAll();
    }

    public static final LocalDateTime FINAL_DELETION = LocalDateTime.now().plusDays(30).truncatedTo(ChronoUnit.MINUTES);
    public static final LocalDateTime NEW_STATE_DATE = LocalDateTime.now().plusDays(10).truncatedTo(ChronoUnit.MINUTES);
    public static final String PARTNER_ID = "P10000";

    @Test
    void testCreateNewCancellation() {
        cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION);

        Assertions.assertEquals(1, cancellationRepository.count());
        Optional<Cancellation> createdCancellation = cancellationRepository.findById(PARTNER_ID);

        Assertions.assertTrue(createdCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION, createdCancellation.get().getFinalDeletion());
        Assertions.assertNotNull(createdCancellation.get().getCreatedAt());
        Assertions.assertNotNull(createdCancellation.get().getUpdatedAt());
        Assertions.assertNull(createdCancellation.get().getCsvCreated());
        Assertions.assertNull(createdCancellation.get().getDataDeleted());
        Assertions.assertNull(createdCancellation.get().getBucketObjectId());
        Assertions.assertNull(createdCancellation.get().getDownloadLinkRequested());
        Assertions.assertNull(createdCancellation.get().getMovedToLongtermArchive());
        Assertions.assertNull(createdCancellation.get().getDownloadRequested());

        cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION.plusDays(1));
        Assertions.assertEquals(1, cancellationRepository.count());
        createdCancellation = cancellationRepository.findById(PARTNER_ID);

        Assertions.assertTrue(createdCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION, createdCancellation.get().getFinalDeletion());
        Assertions.assertNotNull(createdCancellation.get().getCreatedAt());
        Assertions.assertNotNull(createdCancellation.get().getUpdatedAt());
        Assertions.assertNull(createdCancellation.get().getCsvCreated());
        Assertions.assertNull(createdCancellation.get().getDataDeleted());
        Assertions.assertNull(createdCancellation.get().getBucketObjectId());
        Assertions.assertNull(createdCancellation.get().getDownloadLinkRequested());
        Assertions.assertNull(createdCancellation.get().getMovedToLongtermArchive());
        Assertions.assertNull(createdCancellation.get().getDownloadRequested());
    }

    @Test
    void testGetCancellationByPartnerId() {
        cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION);

        Optional<Cancellation> createdCancellation = cancellationService.getByPartnerId(PARTNER_ID);

        Assertions.assertTrue(createdCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION, createdCancellation.get().getFinalDeletion());
        Assertions.assertNotNull(createdCancellation.get().getCreatedAt());
        Assertions.assertNotNull(createdCancellation.get().getUpdatedAt());
        Assertions.assertNull(createdCancellation.get().getCsvCreated());
        Assertions.assertNull(createdCancellation.get().getDataDeleted());
        Assertions.assertNull(createdCancellation.get().getBucketObjectId());
        Assertions.assertNull(createdCancellation.get().getDownloadLinkRequested());
        Assertions.assertNull(createdCancellation.get().getMovedToLongtermArchive());
        Assertions.assertNull(createdCancellation.get().getDownloadRequested());

        Assertions.assertTrue(cancellationService.getByPartnerId("NOTEXISTINGPARTNERID").isEmpty());
    }

    @Test
    void testUpdateDownloadRequested() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION);

        cancellationService.updateDownloadRequested(cancellation, NEW_STATE_DATE);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION.truncatedTo(ChronoUnit.MINUTES), updatedCancellation.get().getFinalDeletion().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        Assertions.assertNotEquals(updatedCancellation.get().getCreatedAt(), updatedCancellation.get().getUpdatedAt());
        Assertions.assertNull(updatedCancellation.get().getCsvCreated());
        Assertions.assertNull(updatedCancellation.get().getDataDeleted());
        Assertions.assertNull(updatedCancellation.get().getBucketObjectId());
        Assertions.assertNull(updatedCancellation.get().getDownloadLinkRequested());
        Assertions.assertNull(updatedCancellation.get().getMovedToLongtermArchive());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getDownloadRequested());
    }

    @Test
    void testUpdateMovedToLongtermArchive() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION);

        cancellationService.updateMovedToLongterm(cancellation, NEW_STATE_DATE);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION.truncatedTo(ChronoUnit.MINUTES), updatedCancellation.get().getFinalDeletion().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        Assertions.assertNotEquals(updatedCancellation.get().getCreatedAt(), updatedCancellation.get().getUpdatedAt());
        Assertions.assertNull(updatedCancellation.get().getCsvCreated());
        Assertions.assertNull(updatedCancellation.get().getDataDeleted());
        Assertions.assertNull(updatedCancellation.get().getBucketObjectId());
        Assertions.assertNull(updatedCancellation.get().getDownloadLinkRequested());
        Assertions.assertNull(updatedCancellation.get().getDownloadRequested());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getMovedToLongtermArchive());
    }

    @Test
    void testUpdateCsvCreated() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION);

        cancellationService.updateCsvCreated(cancellation, NEW_STATE_DATE, PARTNER_ID + ".csv");

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION.truncatedTo(ChronoUnit.MINUTES), updatedCancellation.get().getFinalDeletion().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        Assertions.assertNotEquals(updatedCancellation.get().getCreatedAt(), updatedCancellation.get().getUpdatedAt());
        Assertions.assertNull(updatedCancellation.get().getMovedToLongtermArchive());
        Assertions.assertNull(updatedCancellation.get().getDataDeleted());
        Assertions.assertEquals(PARTNER_ID + ".csv", updatedCancellation.get().getBucketObjectId());
        Assertions.assertNull(updatedCancellation.get().getDownloadLinkRequested());
        Assertions.assertNull(updatedCancellation.get().getDownloadRequested());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getCsvCreated());
    }

    @Test
    void testUpdateDownloadLinkRequested() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION);

        cancellationService.updateDownloadLinkRequested(cancellation, NEW_STATE_DATE);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION.truncatedTo(ChronoUnit.MINUTES), updatedCancellation.get().getFinalDeletion().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        Assertions.assertNotEquals(updatedCancellation.get().getCreatedAt(), updatedCancellation.get().getUpdatedAt());
        Assertions.assertNull(updatedCancellation.get().getCsvCreated());
        Assertions.assertNull(updatedCancellation.get().getDataDeleted());
        Assertions.assertNull(updatedCancellation.get().getBucketObjectId());
        Assertions.assertNull(updatedCancellation.get().getDownloadRequested());
        Assertions.assertNull(updatedCancellation.get().getMovedToLongtermArchive());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getDownloadLinkRequested());
    }

    @Test
    void testUpdateDataDeleted() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, FINAL_DELETION);

        cancellationService.updateDataDeleted(cancellation, NEW_STATE_DATE);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION.truncatedTo(ChronoUnit.MINUTES), updatedCancellation.get().getFinalDeletion().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        Assertions.assertNotEquals(updatedCancellation.get().getCreatedAt(), updatedCancellation.get().getUpdatedAt());
        Assertions.assertNull(updatedCancellation.get().getCsvCreated());
        Assertions.assertNull(updatedCancellation.get().getMovedToLongtermArchive());
        Assertions.assertNull(updatedCancellation.get().getBucketObjectId());
        Assertions.assertNull(updatedCancellation.get().getDownloadLinkRequested());
        Assertions.assertNull(updatedCancellation.get().getDownloadRequested());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getDataDeleted());
    }
}

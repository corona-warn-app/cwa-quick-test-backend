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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.CancellationRepository;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import com.amazonaws.services.s3.AmazonS3;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CancellationServiceTest {

    @Autowired
    private CancellationService cancellationService;

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private QuickTestArchiveRepository quickTestArchiveRepository;

    @Autowired
    private CancellationRepository cancellationRepository;

    @MockBean
    private AmazonS3 s3Client;

    @BeforeEach
    void setUp() {
        cancellationRepository.deleteAll();
    }

    public static final ZonedDateTime CANCELLATION_DATE = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);

    public static final ZonedDateTime FINAL_DELETION = CANCELLATION_DATE.plusWeeks(4);
    public static final ZonedDateTime NEW_STATE_DATE = ZonedDateTime.now().plusDays(10).truncatedTo(ChronoUnit.MINUTES);
    public static final String PARTNER_ID = "P10000";
    public static final String PARTNER_ID_UPDATE = "P10099";

    public static final String USER_ID = "user_id";

    @Test
    void testCreateNewCancellation() {
        Cancellation returnedCancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, returnedCancellation.getFinalDeletion());

        Assertions.assertEquals(1, cancellationRepository.count());
        Optional<Cancellation> createdCancellation = cancellationRepository.findById(PARTNER_ID);

        Assertions.assertTrue(createdCancellation.isPresent());
        Assertions.assertEquals(CANCELLATION_DATE, createdCancellation.get().getCancellationDate());
        Assertions.assertNotNull(createdCancellation.get().getCreatedAt());
        Assertions.assertNotNull(createdCancellation.get().getUpdatedAt());
        assertNull(createdCancellation.get().getCsvCreated());
        assertNull(createdCancellation.get().getDataDeleted());
        assertNull(createdCancellation.get().getBucketObjectId());
        assertNull(createdCancellation.get().getDownloadLinkRequested());
        assertNull(createdCancellation.get().getMovedToLongtermArchive());

        returnedCancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE.plusDays(1));
        Assertions.assertEquals(FINAL_DELETION, returnedCancellation.getFinalDeletion());
        Assertions.assertEquals(1, cancellationRepository.count());
        createdCancellation = cancellationRepository.findById(PARTNER_ID);

        Assertions.assertTrue(createdCancellation.isPresent());
        Assertions.assertEquals(CANCELLATION_DATE, createdCancellation.get().getCancellationDate());
        Assertions.assertNotNull(createdCancellation.get().getCreatedAt());
        Assertions.assertNotNull(createdCancellation.get().getUpdatedAt());
        assertNull(createdCancellation.get().getCsvCreated());
        assertNull(createdCancellation.get().getDataDeleted());
        assertNull(createdCancellation.get().getBucketObjectId());
        assertNull(createdCancellation.get().getDownloadLinkRequested());
        assertNull(createdCancellation.get().getMovedToLongtermArchive());
    }

    @Test
    void testGetCancellationByPartnerId() {
        Cancellation returnedCancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, returnedCancellation.getFinalDeletion());

        Optional<Cancellation> createdCancellation = cancellationService.getByPartnerId(PARTNER_ID);

        Assertions.assertTrue(createdCancellation.isPresent());
        Assertions.assertEquals(FINAL_DELETION, createdCancellation.get().getFinalDeletion());
        Assertions.assertEquals(CANCELLATION_DATE, createdCancellation.get().getCancellationDate());
        Assertions.assertNotNull(createdCancellation.get().getCreatedAt());
        Assertions.assertNotNull(createdCancellation.get().getUpdatedAt());
        assertNull(createdCancellation.get().getCsvCreated());
        assertNull(createdCancellation.get().getDataDeleted());
        assertNull(createdCancellation.get().getBucketObjectId());
        assertNull(createdCancellation.get().getDownloadLinkRequested());
        assertNull(createdCancellation.get().getMovedToLongtermArchive());

        Assertions.assertTrue(cancellationService.getByPartnerId("NOTEXISTINGPARTNERID").isEmpty());
    }

    @Test
    void testUpdateMovedToLongtermArchive() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, cancellation.getFinalDeletion());

        cancellationService.updateMovedToLongterm(cancellation, NEW_STATE_DATE);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(CANCELLATION_DATE.truncatedTo(ChronoUnit.MINUTES),
          updatedCancellation.get().getCancellationDate().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        assertNull(updatedCancellation.get().getCsvCreated());
        assertNull(updatedCancellation.get().getDataDeleted());
        assertNull(updatedCancellation.get().getBucketObjectId());
        assertNull(updatedCancellation.get().getDownloadLinkRequested());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getMovedToLongtermArchive());
    }

    @Test
    void testUpdateCsvCreated() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, cancellation.getFinalDeletion());

        cancellationService.updateCsvCreated(cancellation, NEW_STATE_DATE, PARTNER_ID + ".csv",
          "hash", 10, 200);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(CANCELLATION_DATE.truncatedTo(ChronoUnit.MINUTES),
          updatedCancellation.get().getCancellationDate().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        assertNull(updatedCancellation.get().getMovedToLongtermArchive());
        assertNull(updatedCancellation.get().getDataDeleted());
        Assertions.assertEquals(PARTNER_ID + ".csv", updatedCancellation.get().getBucketObjectId());
        assertNull(updatedCancellation.get().getDownloadLinkRequested());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getCsvCreated());
        Assertions.assertEquals("hash", updatedCancellation.get().getCsvHash());
        Assertions.assertEquals(10, updatedCancellation.get().getCsvEntityCount());
        Assertions.assertEquals(200, updatedCancellation.get().getCsvSize());
    }

    @Test
    void testUpdateDownloadLinkRequested() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, cancellation.getFinalDeletion());

        cancellationService.updateDownloadLinkRequested(cancellation, NEW_STATE_DATE, USER_ID);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(CANCELLATION_DATE.truncatedTo(ChronoUnit.MINUTES),
          updatedCancellation.get().getCancellationDate().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        assertNull(updatedCancellation.get().getCsvCreated());
        assertNull(updatedCancellation.get().getDataDeleted());
        assertNull(updatedCancellation.get().getBucketObjectId());
        assertNull(updatedCancellation.get().getMovedToLongtermArchive());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getDownloadLinkRequested());
    }

    @Test
    void testUpdateDataDeleted() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, cancellation.getFinalDeletion());

        cancellationService.updateDataDeleted(cancellation, NEW_STATE_DATE);

        Optional<Cancellation> updatedCancellation = cancellationRepository.findById(PARTNER_ID);
        Assertions.assertTrue(updatedCancellation.isPresent());
        Assertions.assertEquals(CANCELLATION_DATE.truncatedTo(ChronoUnit.MINUTES),
          updatedCancellation.get().getCancellationDate().truncatedTo(ChronoUnit.MINUTES));
        Assertions.assertNotNull(updatedCancellation.get().getCreatedAt());
        assertNull(updatedCancellation.get().getCsvCreated());
        assertNull(updatedCancellation.get().getMovedToLongtermArchive());
        assertNull(updatedCancellation.get().getBucketObjectId());
        assertNull(updatedCancellation.get().getDownloadLinkRequested());
        Assertions.assertEquals(NEW_STATE_DATE, updatedCancellation.get().getDataDeleted());
    }

    @Test
    void testGetReadyToArchive() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, cancellation.getFinalDeletion());
        assertNull(cancellation.getMovedToLongtermArchive());

        cancellation.setCancellationDate(CANCELLATION_DATE.minusHours(48));
        cancellationRepository.save(cancellation);

        List<Cancellation> results = cancellationService.getReadyToArchiveBatch();
        assertFalse(results.isEmpty());
    }

    @Test
    void testGetReadyToArchiveYoungerThen48h() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, cancellation.getFinalDeletion());
        assertNull(cancellation.getMovedToLongtermArchive());
        List<Cancellation> results = cancellationService.getReadyToArchiveBatch();
        assertTrue(results.isEmpty());
    }

    @Test
    void testGetReadyToArchiveMovedNotNull() {
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        Assertions.assertEquals(FINAL_DELETION, cancellation.getFinalDeletion());
        cancellationService.updateMovedToLongterm(cancellation, ZonedDateTime.now());
        List<Cancellation> results = cancellationService.getReadyToArchiveBatch();
        assertTrue(results.isEmpty());
    }
    @Test
    void testFinalDeleteJob() {
        doNothing().when(s3Client).deleteObject(any(),any());
        final QuickTestArchive test = buildCancellationQuickTestArchive();
        quickTestArchiveRepository.saveAndFlush(test);
        archiveService.moveToArchiveByTenantId(PARTNER_ID);
        var archiveEntries = archiveService.getQuicktestsFromLongterm(null, PARTNER_ID);
        assertFalse(archiveEntries.isEmpty());
        Cancellation cancellation = cancellationService.createCancellation(PARTNER_ID, CANCELLATION_DATE);
        cancellationService.finalDeleteJob();
        archiveEntries = archiveService.getQuicktestsFromLongterm(null, PARTNER_ID);
        assertFalse(archiveEntries.isEmpty());
        cancellation.setCancellationDate(ZonedDateTime.now().minusWeeks(4));
        cancellationRepository.save(cancellation);
        cancellationService.finalDeleteJob();
        archiveEntries = archiveService.getQuicktestsFromLongterm(null, PARTNER_ID);
        assertTrue(archiveEntries.isEmpty());
    }

    private QuickTestArchive buildCancellationQuickTestArchive() {
        QuickTestArchive qta = new QuickTestArchive();
        qta.setShortHashedGuid("27a9ac48");
        qta.setHashedGuid("27a9ac470b7832857ad03b25dc96032e0a1056ff4f5afb538de4994e0a63d229");
        qta.setTenantId(PARTNER_ID);
        qta.setPocId("poc_id2");
        qta.setCreatedAt(LocalDateTime.now().minusMonths(3));
        qta.setUpdatedAt(LocalDateTime.now().minusMonths(2));
        qta.setConfirmationCwa(Boolean.TRUE);
        qta.setTestResult(Short.valueOf("5"));
        qta.setPrivacyAgreement(Boolean.TRUE);
        qta.setLastName("last_name2");
        qta.setFirstName("first_name2");
        qta.setEmail("email2");
        qta.setPhoneNumber("phone_number2");
        qta.setSex(Sex.MALE);
        qta.setStreet("street2");
        qta.setHouseNumber("house_number2");
        qta.setZipCode("12345");
        qta.setCity("city2");
        qta.setTestBrandId("test_brand_id2");
        qta.setTestBrandName("test_brand_name2");
        qta.setBirthday("2000-01-02");
        qta.setPdf("PDF".getBytes());
        qta.setTestResultServerHash("test_result_server_hash2");
        qta.setDcc("dcc2");
        qta.setAdditionalInfo("additional_info2");
        qta.setGroupName("group_name2");
        return qta;
    }
}

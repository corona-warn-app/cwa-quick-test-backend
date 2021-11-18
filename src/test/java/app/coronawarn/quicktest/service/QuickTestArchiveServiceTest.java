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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.model.demis.DemisStatus;
import app.coronawarn.quicktest.repository.DemisReceiptRepository;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.repository.QuickTestArchiveView;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@SpringBootTest
public class QuickTestArchiveServiceTest {

    QuickTestArchive quickTestArchive;
    @Mock
    private QuickTestConfig quickTestConfig;
    @Mock
    private QuickTestArchiveRepository quickTestArchiveRepository;
    @Mock
    private DemisReceiptRepository demisReceiptRepository;

    @InjectMocks
    private QuickTestArchiveService quickTestArchiveService;

    @BeforeEach
    void setUp() {
        quickTestArchive = new QuickTestArchive();
        quickTestArchive.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        quickTestArchive.setShortHashedGuid("6fa4dcec");
        quickTestArchive.setTenantId("tenant");
        quickTestArchive.setCreatedAt(LocalDateTime.now());
        quickTestArchive.setUpdatedAt(LocalDateTime.now());
        quickTestArchive.setConfirmationCwa(true);
        quickTestArchive.setTestResult((short) 6);
        quickTestArchive.setPrivacyAgreement(true);
        quickTestArchive.setLastName("1");
        quickTestArchive.setFirstName("1");
        quickTestArchive.setEmail("v@e.o");
        quickTestArchive.setPhoneNumber("+490000");
        quickTestArchive.setSex(Sex.DIVERSE);
        quickTestArchive.setStreet("f");
        quickTestArchive.setHouseNumber("a");
        quickTestArchive.setZipCode("11111");
        quickTestArchive.setCity("f");
        quickTestArchive.setTestBrandId("testbrand");
        quickTestArchive.setTestBrandName("brandname");
        quickTestArchive.setBirthday(LocalDate.now().toString());
        quickTestArchive.setPdf("test output".getBytes());
    }

    @Test
    void createNewQuickTestArchiveQuickTest() throws IOException {
        when(quickTestArchiveRepository.findByHashedGuid(any())).thenReturn(Optional.of(quickTestArchive));
        when(demisReceiptRepository.findByHashedGuid(any())).thenReturn(Optional.empty());
        assertEquals(quickTestArchive.getPdf(), quickTestArchiveService.getPdf("sgserh"));
    }

    @Test
    void createNewQuickTestArchiveQuickTestNotFound() throws IOException {
        when(quickTestArchiveRepository.findByHashedGuid(any())).thenReturn(Optional.empty());
        try {
            quickTestArchiveService.getPdf("sgserh");
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.NOT_FOUND, "wrong status");
        }
    }

    @Test
    void findByTestResultAndUpdatedAtBetweenTest() {
        String hashedGuid = quickTestArchive.getHashedGuid();
        when(quickTestArchiveRepository.findAllByTenantIdAndPocIdAndUpdatedAtBetween(any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(getProjection()));
        when(quickTestArchiveRepository.findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(any(),
            any(), anyShort(), any(), any())).thenReturn(Collections.singletonList(getProjection()));
        List<QuickTestArchiveView> quickTestArchives =
            quickTestArchiveService.findByTestResultAndUpdatedAtBetween(
                new HashMap<>(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        assertEquals(1, quickTestArchives.size());
        verify(quickTestArchiveRepository, times(1))
            .findAllByTenantIdAndPocIdAndUpdatedAtBetween(any(), any(), any(), any());
        verify(quickTestArchiveRepository, times(0)).findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(any(),
            any(), anyShort(), any(), any());
        checkResponse(quickTestArchive,
            quickTestArchives.get(0));

        quickTestArchives =
            quickTestArchiveService.findByTestResultAndUpdatedAtBetween(
                new HashMap<>(),
                (short) 5,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        assertEquals(1, quickTestArchives.size());
        verify(quickTestArchiveRepository, times(1))
            .findAllByTenantIdAndPocIdAndUpdatedAtBetween(any(), any(), any(), any());
        verify(quickTestArchiveRepository, times(1)).findAllByTenantIdAndPocIdAndTestResultAndUpdatedAtBetween(any(),
            any(), anyShort(), any(), any());
        checkResponse(quickTestArchive,
            quickTestArchives.get(0));

    }


    private void checkResponse(QuickTestArchive expected, QuickTestArchiveView act) {
        assertEquals(expected.getHashedGuid(), act.getHashedGuid());
    }

    private QuickTestArchiveView getProjection() {
        return new QuickTestArchiveView() {
            @Override
            public String getHashedGuid() {
                return "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4";
            }

            @Override
            public DemisStatus getDemisStatus() {
                return DemisStatus.NONE;
            }
        };
    }
}


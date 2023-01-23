/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.quicktest.QuickTestResult;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class QuickTestDeletionServiceTest {

    @InjectMocks
    private QuickTestDeletionService underTest;

    @Mock
    private QuickTestRepository quickTestRepository;

    @Mock
    private TestResultService testResultService;

    @Mock
    private QuickTestConfig quickTestConfig;


    @Test
    void removeAllBeforeTest() {
        LocalDateTime now = ZonedDateTime.now().withNano(0).toLocalDateTime();
        QuickTest quickTest = new QuickTest();
        quickTest.setConfirmationCwa(true);
        quickTest.setTestResultServerHash("");
        quickTest.setTestResult(QuickTest.TEST_RESULT_REDEEMED);
        quickTest.setUpdatedAt(now);

        QuickTest quickTest1 = new QuickTest();
        quickTest1.setConfirmationCwa(false);
        quickTest1.setTestResultServerHash("");
        quickTest1.setTestResult(QuickTest.TEST_RESULT_REDEEMED);
        quickTest1.setUpdatedAt(now);

        QuickTest quickTestPcr = new QuickTest();
        quickTestPcr.setConfirmationCwa(true);
        quickTestPcr.setTestResultServerHash("");
        quickTestPcr.setTestResult(QuickTest.TEST_RESULT_PCR_REDEEMED);
        quickTestPcr.setUpdatedAt(now);
        quickTestPcr.setTestType("LP6464-4");

        QuickTestResult quickTestResult = new QuickTestResult();
        quickTestResult.setId(quickTest.getTestResultServerHash());
        quickTestResult.setResult(quickTest.getTestResult());
        quickTestResult.setSampleCollection(quickTest.getUpdatedAt().toEpochSecond(ZoneOffset.UTC));

        QuickTestConfig.CleanUpSettings cleanUpSettings = new QuickTestConfig.CleanUpSettings();
        cleanUpSettings.setChunkSize(1000);

        when(quickTestConfig.getLabId()).thenReturn("lab4711");
        when(quickTestRepository.findAllByCreatedAtBeforeAndVersionIsGreaterThan(eq(now), eq(0), any()))
          .thenReturn(Arrays.asList(quickTest, quickTest, quickTest1, quickTestPcr));
        underTest.handleChunk(now, cleanUpSettings.getChunkSize(), 1, 1);
        verify(quickTestRepository, times(1)).findAllByCreatedAtBeforeAndVersionIsGreaterThan(eq(now), eq(0), any());
        verify(testResultService, times(2)).createOrUpdateTestResult(quickTestResult);
        verify(testResultService, times(1)).createOrUpdatePcrTestResult(any());
    }
}

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

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.*;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuickTestService {

    private final QuickTestRepository quickTestRepository;

    private final TestResultService testResultService;

    public QuickTest saveQuickTest(QuicktestCreationRequest quicktestCreationRequest) {
        QuickTest newQuickTest = new QuickTest();
        newQuickTest.setHashedGuid(quicktestCreationRequest.getHashedGuid());
        return quickTestRepository.save(newQuickTest);
    }

    public TestResult getTestResult(String hashedGuid) {
        return testResultService.result(new HashedGuid(hashedGuid));
    }


    public ResponseEntity<?> updateQuickTest(QuickTestUpdateRequest quickTestUpdateRequest) {
        QuickTest quicktest = quickTestRepository.findByHashedGuidIsStartingWith(quickTestUpdateRequest.getShortHash());
        TestResult testResult = new TestResult();
        testResult.setResult(quickTestUpdateRequest.getResult().getResult());
        testResult.setId(quicktest.getHashedGuid());

        TestResultList testResultList = new TestResultList();
        testResultList.setTestResults(Collections.singletonList(testResult));

        return testResultService.updateTestResult(testResultList);
    }
}

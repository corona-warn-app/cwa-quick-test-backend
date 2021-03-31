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
import app.coronawarn.quicktest.model.HashedGuid;
import app.coronawarn.quicktest.model.QuickTestUpdateRequest;
import app.coronawarn.quicktest.model.QuicktestCreationRequest;
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.model.TestResultList;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuickTestService {

    private final QuickTestRepository quickTestRepository;

    private final TestResultService testResultService;

    /**
     * Persist a new QuickTest.
     *
     * @param quicktestCreationRequest The creation request with hashed guid.
     * @return saved QuickTest
     */
    public QuickTest saveQuickTest(QuicktestCreationRequest quicktestCreationRequest) {
        QuickTest newQuickTest = new QuickTest();
        newQuickTest.setHashedGuid(quicktestCreationRequest.getHashedGuid());
        return quickTestRepository.save(newQuickTest);
    }

    /**
     * Queries the TestResult Server by hashed GUID.
     *
     * @param hashedGuid the hashedGuid to search for
     * @return TestResult.
     */
    public TestResult getTestResult(String hashedGuid) {
        return testResultService.result(new HashedGuid(hashedGuid));
    }


    /**
     * Updates a QuickTest entity in persistence.
     *
     * @param quickTestUpdateRequest Request to update QuickTest entity
     * @return Empty Response
     */
    public ResponseEntity<Void> updateQuickTest(QuickTestUpdateRequest quickTestUpdateRequest) {
        QuickTest quicktest = quickTestRepository.findByHashedGuidIsStartingWith(quickTestUpdateRequest.getShortHash());
        TestResult testResult = new TestResult();
        testResult.setResult(quickTestUpdateRequest.getResult());
        testResult.setId(quicktest.getHashedGuid());

        TestResultList testResultList = new TestResultList();
        testResultList.setTestResults(Collections.singletonList(testResult));

        testResultService.updateTestResult(testResultList);

        return ResponseEntity.ok().build();
    }
}

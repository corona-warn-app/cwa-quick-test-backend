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
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuickTestService {

    private final QuickTestRepository quickTestRepository;

    private final TestResultService testResultService;

    public void saveQuickTest(QuickTest quickTest) {
    }

    public void updateQuickTest(QuickTest quickTest) {
    }

    public QuickTest updateQuickTest(String uuid, QuickTest quickTest) {
        return null;
    }

    public TestResult getTestResult(String uuid) {
        return null;
    }


}

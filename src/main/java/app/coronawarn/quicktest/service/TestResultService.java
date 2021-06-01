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

import app.coronawarn.quicktest.client.TestResultServerClient;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.model.QuickTestResult;
import app.coronawarn.quicktest.model.QuickTestResultList;
import feign.FeignException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultServerClient testResultServerClient;
    private final QuickTestConfig quickTestConfig;

    /**
     * Creates or updates a QuickTest in TestResult Server.
     *
     * @param quickTestResult comment.
     */
    public void createOrUpdateTestResult(QuickTestResult quickTestResult) throws ResponseStatusException {
        try {
            QuickTestResultList resultList = new QuickTestResultList();
            resultList.setLabId(quickTestConfig.getLabId());
            resultList.setTestResults(Collections.singletonList(quickTestResult));
            if (testResultServerClient.results(resultList).getStatusCode() != HttpStatus.NO_CONTENT) {
                log.error("Failed to update testresult");
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (FeignException e) {
            log.error("Failed to update testresult");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

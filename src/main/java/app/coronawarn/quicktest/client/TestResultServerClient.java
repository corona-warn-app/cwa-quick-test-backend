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

package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.HashedGuid;
import app.coronawarn.quicktest.model.TestResult;
import app.coronawarn.quicktest.model.TestResultList;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@FeignClient(
    name = "testResultServerClient",
    url = "${cwa-testresult-server.url}",
    configuration = TestResultServerClientConfig.class)
public interface TestResultServerClient {

    /**
     * This method gets a testResult from the LabServer.
     *
     * @param guid for TestResult
     * @return TestResult from server
     */
    @PostMapping(value = "/api/v1/app/result",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    TestResult result(HashedGuid guid);

    /**
     * Insert or update the test results.
     *
     * @param list the test result list request
     * @return the response
     */
    @PostMapping(
            value = "/api/v1/lab/results",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<?> results(@RequestBody @NotNull @Valid TestResultList list   ) ;
}

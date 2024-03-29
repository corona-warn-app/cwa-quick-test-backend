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

package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.model.quicktest.PcrTestResultList;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "testResultServerPcrClient",
        url = "${cwa-testresult-server.url-pcr}",
        configuration = TestResultServerPcrClientConfig.class
)
public interface TestResultServerPcrClient {

    /**
     * Insert or update the pcr test results.
     *
     * @param testResults for TestResults
     */
    @PostMapping(value = "/api/v1/pocnat/results",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Void> pcrResults(@RequestBody @NotNull @Valid PcrTestResultList testResults);
}

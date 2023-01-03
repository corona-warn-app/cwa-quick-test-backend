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

package app.coronawarn.quicktest.model.quicktest;


import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Schema(
  description = "The pcr test result list model."
)
@Getter
@ToString
@EqualsAndHashCode
public class PcrTestResultList {

    @NotNull
    @NotEmpty
    private List<@Valid PcrTestResult> testResults;

    public PcrTestResultList setTestResults(List<PcrTestResult> pcrTestResults) {
        this.testResults = pcrTestResults;
        return this;
    }

    private String labId;

    public void setLabId(String labId) {
        this.labId = labId;
    }

}

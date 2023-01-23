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

import app.coronawarn.quicktest.validation.ValidGuid;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Model of the pcr test result.
 */
@Schema(
  description = "The pcr test result model."
)
@Getter
@ToString
@EqualsAndHashCode
public class PcrTestResult {

    /**
     * Hash (SHA256) of test result id (aka QR-Code, GUID) encoded as hex string.
     */
    @NotBlank
    @ValidGuid
    private String id;

    /**
     * The test result.
     * 1: negative
     * 2: positive
     * 3: invalid
     * 4: redeemed
     * 5: quick-test-Pending
     * 6: quick-test-Negative
     * 7: quick-test-Positive
     * 8: quick-test-Invalid
     * 9: quick-test-Redeemed
     */
    @Min(10)
    @Max(13)
    @NotNull
    @Schema(description = "the result of the PoC-NAT test", required = true)
    private Short result;

    /**
     * Timestamp of the SampleCollection (sc).
     */
    private Long sc;

    /**
     * The lab id.
     */
    private String labId;

    public PcrTestResult setId(String id) {
        this.id = id;
        return this;
    }

    public PcrTestResult setResult(Short result) {
        this.result = result;
        return this;
    }

    public PcrTestResult setSampleCollection(Long sc) {
        this.sc = sc;
        return this;
    }

    public PcrTestResult setLabId(String labId) {
        this.labId = labId;
        return this;
    }
}

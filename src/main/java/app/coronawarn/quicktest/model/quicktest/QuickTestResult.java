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
 * Model of the test result.
 */
@Schema(
    description = "The test result model."
)
@Getter
@ToString
@EqualsAndHashCode
public class QuickTestResult {

    /**
     * Hash (SHA256) of test result id (aka QR-Code, GUID) encoded as hex string.
     */
    @NotBlank
    @ValidGuid
    private String id;

    /**
     * The test result.
     * 5: Pending
     * 6: Negative
     * 7: Positive
     * 8: Invalid
     * 9: Redeemed
     */
    @Min(5)
    @Max(9)
    @NotNull
    private Short result;

    /**
     * Timestamp of the SampleCollection (sc).
     */
    private Long sc;
    private String testCenter;
    private String countryOfTest;
    private String certificateIssuer;

    public QuickTestResult setId(String id) {
        this.id = id;
        return this;
    }

    public QuickTestResult setResult(Short result) {
        this.result = result;
        return this;
    }

    public QuickTestResult setSampleCollection(Long sc) {
        this.sc = sc;
        return this;
    }

    public QuickTestResult setTestCenter(String testCenter) {
        this.testCenter = testCenter;
        return this;
    }

    public QuickTestResult setCountryOfTest(String countryOfTest) {
        this.countryOfTest = countryOfTest;
        return this;
    }

    public QuickTestResult setCertificateIssuer(String certificateIssuer) {
        this.certificateIssuer = certificateIssuer;
        return this;
    }

}

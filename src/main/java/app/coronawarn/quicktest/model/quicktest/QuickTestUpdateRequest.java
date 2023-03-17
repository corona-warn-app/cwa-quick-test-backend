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

import app.coronawarn.quicktest.validation.ValidCommonCharAndNumber;
import app.coronawarn.quicktest.validation.ValidTestUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
    description = "The quick test  or PoC-NAT update."
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidTestUpdate
public class QuickTestUpdateRequest {
    /**
     * The test result.
     * 5: Pending
     * 6: Negative
     * 7: Positive
     * 8: Invalid
     * 9: Redeemed
     * 10 Pending PoC-NAT
     * 11 Negative PoC-NAT
     * 12 Positive PoC-NAT
     * 13 Invalid PoC-NAT
     */
    @Min(6)
    @Max(13)
    private short result;

    @ValidCommonCharAndNumber
    @Size(min = 1, max = 15)
    private String testBrandId;

    @Size(max = 200)
    @ValidCommonCharAndNumber
    private String testBrandName;

    @ValidCommonCharAndNumber
    @Size(min = 1, max = 15)
    private String dccTestManufacturerId;

    @ValidCommonCharAndNumber
    @Size(min = 1, max = 128)
    private String dccTestManufacturerDescription;

    @ValidCommonCharAndNumber
    @Size(min = 1, max = 128)
    private String pcrTestName;

}

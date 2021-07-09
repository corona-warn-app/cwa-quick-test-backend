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

package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.validation.ValidCommonCharAndNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
    description = "The quick test update."
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickTestUpdateRequest {
    /**
     * The test result.
     * 5: Pending
     * 6: Negative
     * 7: Positive
     * 8: Invalid
     * 9: Redeemed
     */
    @Min(6)
    @Max(8)
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

}

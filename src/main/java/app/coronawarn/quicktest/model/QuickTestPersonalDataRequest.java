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

import app.coronawarn.quicktest.validation.ValidPhoneNumber;
import app.coronawarn.quicktest.validation.ValidZipCode;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(
    description = "The quick test personaldata model."
)
@Data
public class QuickTestPersonalDataRequest {

    @NotNull
    private Boolean confirmationCwa;

    @NotNull
    private Boolean insuranceBillStatus;

    @NotNull
    @Size(max = 15)
    private String testBrandId;

    @Size(max = 79)
    private String testBrandName;

    @NotNull
    @Size(max = 79)
    private String lastName;

    @NotNull
    @Size(max = 79)
    private String firstName;

    @Email
    @NotNull
    @Size(min = 5)
    @Size(max = 255)
    private String email;

    @ValidPhoneNumber
    @NotNull
    @Size(min = 1)
    @Size(max = 79)
    private String phoneNumber;

    @NotNull
    private Sex sex;

    @NotNull
    @Size(min = 1)
    @Size(max = 255)
    private String street;

    @NotNull
    @Size(min = 1)
    @Size(max = 15)
    private String houseNumber;

    @ValidZipCode
    @NotNull
    @Size(min = 5)
    @Size(max = 5)
    private String zipCode;

    @NotNull
    @Size(max = 255)
    private String city;
}

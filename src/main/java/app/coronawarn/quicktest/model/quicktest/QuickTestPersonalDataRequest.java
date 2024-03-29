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

import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.validation.ValidCommonChar;
import app.coronawarn.quicktest.validation.ValidCommonCharAndNumber;
import app.coronawarn.quicktest.validation.ValidGuid;
import app.coronawarn.quicktest.validation.ValidPersonalData;
import app.coronawarn.quicktest.validation.ValidPhoneNumber;
import app.coronawarn.quicktest.validation.ValidZipCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(
    description = "The quick test personaldata model."
)
@Data
@ValidPersonalData
public class QuickTestPersonalDataRequest {

    @NotNull
    private Boolean confirmationCwa;

    @NotNull
    private Boolean privacyAgreement;

    @NotNull
    @ValidCommonChar
    @Size(min = 1, max = 79)
    private String lastName;

    @NotNull
    @ValidCommonChar
    @Size(min = 1, max = 79)
    private String firstName;

    @Email
    @Size(min = 5, max = 255)
    private String email;

    @ValidPhoneNumber
    @NotNull
    @Size(min = 1, max = 79)
    private String phoneNumber;

    @NotNull
    private Sex sex;

    @NotNull
    @ValidCommonCharAndNumber
    @Size(min = 1, max = 255)
    private String street;

    @Size(min = 1, max = 15)
    @ValidCommonCharAndNumber
    private String houseNumber;

    @ValidZipCode
    @NotNull
    @Size(min = 5, max = 5)
    private String zipCode;

    @NotNull
    @ValidCommonCharAndNumber
    @Size(min = 1, max = 255)
    private String city;

    @NotNull
    private LocalDate birthday;

    @ValidCommonChar
    @Size(min = 1, max = 150)
    private String standardisedFamilyName;

    @ValidCommonChar
    @Size(min = 1, max = 150)
    private String standardisedGivenName;

    @NotNull
    @ValidCommonCharAndNumber
    private String diseaseAgentTargeted;
    
    @ValidGuid
    private String testResultServerHash;

    private Boolean dccConsent;

    @ValidCommonCharAndNumber
    @Size(min = 1, max = 250)
    private String additionalInfo;

    @Size(min = 1, max = 30)
    //May be null to ensure backwards compatibility
    private String testType;
}

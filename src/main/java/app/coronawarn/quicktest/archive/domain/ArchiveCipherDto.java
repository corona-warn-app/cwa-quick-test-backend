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

package app.coronawarn.quicktest.archive.domain;

import app.coronawarn.quicktest.model.Sex;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public abstract class ArchiveCipherDto {

    private String hashedGuid;

    private String shortHashedGuid;

    private String tenantId;

    private String pocId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Persistence (Hibernate) version from 'quick_test_archive' table
    private Integer version;

    private Boolean confirmationCwa;

    private Short testResult;

    private Boolean privacyAgreement;

    private String lastName;

    private String firstName;

    private String email;

    private String phoneNumber;

    private Sex sex;

    private String street;

    private String houseNumber;

    private String zipCode;

    private String city;

    private String testBrandId;

    private String testBrandName;

    private String birthday;

    private String pdfBase64;

    private String testResultServerHash;

    private String dcc;

    private String additionalInfo;

    private String groupName;
}

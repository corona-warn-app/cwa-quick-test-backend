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

package app.coronawarn.quicktest.domain;

import app.coronawarn.quicktest.model.Sex;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class QuickTest {

    static final long SERIAL_VERSION_UID = 1L;

    @Size(max = 8)
    @Id
    private String shortHashedGuid;

    @Size(max = 64)
    private String hashedGuid;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean confirmationCwa;

    @Size(max = 255)
    private String tenantId;

    @Size(max = 255)
    private String testSpotId;

    @Min(5)
    @Max(9)
    private Short testResult;

    @Setter(AccessLevel.NONE)
    @Version
    private Long version;

    private Boolean insuranceBillStatus;

    private String lastName;

    private String firstName;

    @Email
    private String email;

    @Size(max = 100)
    private String phoneNumber;

    @Enumerated(value = EnumType.STRING)
    private Sex sex;

    private String street;

    private String houseNumber;

    @Size(max = 10)
    private String zipCode;

    private String city;

    @Size(max = 10)
    private String testBrandId;

    @Size(max = 255)
    private String testBrandName;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        testResult = 5;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}

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

import app.coronawarn.quicktest.dbencryption.DbEncryptionBooleanConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionSexTypeConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionShortConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionStringConverter;
import app.coronawarn.quicktest.model.Sex;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quick_test")
public class QuickTest {

    static final long SERIAL_VERSION_UID = 1L;

    @Id
    @Column(name = "short_hashed_guid")
    private String shortHashedGuid;

    @Column(name = "hashed_guid")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String hashedGuid;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirmation_cwa")
    @Convert(converter = DbEncryptionBooleanConverter.class)
    private Boolean confirmationCwa;

    @Column(name = "tenant_id")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String tenantId;

    @Column(name = "poc_id")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String pocId;

    @Column(name = "test_result")
    @Convert(converter = DbEncryptionShortConverter.class)
    private Short testResult;

    @Column(name = "version")
    @Setter(AccessLevel.NONE)
    @Version
    private Integer version;

    @Column(name = "insurance_bill_status")
    @Convert(converter = DbEncryptionBooleanConverter.class)
    private Boolean insuranceBillStatus;

    @Column(name = "last_name")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String lastName;

    @Column(name = "first_name")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String firstName;

    @Column(name = "email")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String email;

    @Column(name = "phone_number")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String phoneNumber;

    @Column(name = "sex")
    @Convert(converter = DbEncryptionSexTypeConverter.class)
    private Sex sex;

    @Column(name = "street")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String street;

    @Column(name = "house_number")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String houseNumber;

    @Column(name = "zip_code")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String zipCode;

    @Column(name = "city")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String city;

    @Column(name = "test_brand_id")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String testBrandId;

    @Column(name = "test_brand_name")
    @Convert(converter = DbEncryptionStringConverter.class)
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

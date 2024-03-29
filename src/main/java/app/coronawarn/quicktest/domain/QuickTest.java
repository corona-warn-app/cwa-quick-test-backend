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

package app.coronawarn.quicktest.domain;

import app.coronawarn.quicktest.dbencryption.DbEncryptionBooleanConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionSexTypeConverter;
import app.coronawarn.quicktest.dbencryption.DbEncryptionStringConverter;
import app.coronawarn.quicktest.model.SecurityAuditListenerQuickTest;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.utils.Utilities;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(SecurityAuditListenerQuickTest.class)
@Entity
@Table(name = "quick_test")
public class QuickTest {

    static final long SERIAL_VERSION_UID = 1L;

    public static final short TEST_RESULT_PCR_PENDING = 10;
    public static final short TEST_RESULT_PCR_NEGATIVE = 11;
    public static final short TEST_RESULT_PCR_POSITIVE = 12;
    public static final short TEST_RESULT_PCR_INVALID = 13;
    public static final short TEST_RESULT_PCR_REDEEMED = 14;
    public static final short TEST_RESULT_PENDING = 5;
    public static final short TEST_RESULT_NEGATIVE = 6;
    public static final short TEST_RESULT_POSITIVE = 7;
    public static final short TEST_RESULT_INVALID = 8;
    public static final short TEST_RESULT_REDEEMED = 9;
  
    /**
     * alias TestId in another systems.
     */
    @Id
    @Column(name = "hashed_guid")
    private String hashedGuid;

    @Column(name = "short_hashed_guid")
    private String shortHashedGuid;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "poc_id")
    private String pocId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "version")
    @Setter(AccessLevel.NONE)
    @Version
    private Integer version;

    @Column(name = "confirmation_cwa")
    @Convert(converter = DbEncryptionBooleanConverter.class)
    private Boolean confirmationCwa;

    /**
     * The test result.
     * 5: Pending
     * 6: Negative
     * 7: Positive
     * 8: Invalid
     * 9: Redeemed
     */
    @Column(name = "test_result")
    private Short testResult;

    @Column(name = "privacy_agreement")
    @Convert(converter = DbEncryptionBooleanConverter.class)
    private Boolean privacyAgreement;

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

    @Column(name = "birthday")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String birthday;

    @Column(name = "standardised_family_name")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String standardisedFamilyName;

    @Column(name = "standardised_given_name")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String standardisedGivenName;

    @Column(name = "disease_agent_targeted")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String diseaseAgentTargeted;

    @Column(name = "test_result_server_hash")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String testResultServerHash;

    @Column(name = "dcc_sign_data")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String dccSignData;

    @Column(name = "dcc_unsigned")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String dccUnsigned;

    @Column(name = "dcc_consent")
    @Convert(converter = DbEncryptionBooleanConverter.class)
    private Boolean dccConsent;

    @Column(name = "dcc_public_key")
    private String publicKey;

    @Column(name = "dcc_status")
    private DccStatus dccStatus;

    @Column(name = "additional_info")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String additionalInfo;

    @Column(name = "group_name")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String groupName;

    @Column(name = "test_type")
    @Convert(converter = DbEncryptionStringConverter.class)
    private String testType;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = Utilities.getCurrentLocalDateTimeUtc();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        testResult = TEST_RESULT_PENDING;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Utilities.getCurrentLocalDateTimeUtc();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        QuickTest quickTest = (QuickTest) o;

        return Objects.equals(hashedGuid, quickTest.hashedGuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedGuid);
    }
}

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

import app.coronawarn.quicktest.utils.Utilities;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quick_test_statistics")
public class QuickTestStatistics {

    static final long SERIAL_VERSION_UID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "poc_id")
    private String pocId;
    @Column(name = "tenant_id")
    private String tenantId;
    @Column(name = "created_at")
    private LocalDate createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "total_test_count")
    private Integer totalTestCount;
    @Column(name = "positive_test_count")
    private Integer positiveTestCount;

    public QuickTestStatistics(String pocId, String tenantId) {
        this.pocId = pocId;
        this.tenantId = tenantId;
    }

    @PrePersist
    private void onCreate() {
        createdAt = Utilities.getCurrentLocalDateInGermany();
        updatedAt = Utilities.getCurrentLocalDateTimeUtc();
        totalTestCount = 0;
        positiveTestCount = 0;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Utilities.getCurrentLocalDateTimeUtc();
    }

}

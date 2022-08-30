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
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cancellation")
public class Cancellation {

    static final long SERIAL_VERSION_UID = 1L;

    @Id
    @Column(name = "partner_id", length = 20)
    private String partnerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "download_requested")
    private LocalDateTime downloadRequested;

    @Column(name = "moved_to_longterm_archive")
    private LocalDateTime movedToLongtermArchive;

    @Column(name = "csv_created")
    private LocalDateTime csvCreated;

    @Column(name = "download_link_requested")
    private LocalDateTime downloadLinkRequested;

    @Column(name = "data_deleted")
    private LocalDateTime dataDeleted;

    @Column(name = "bucket_object_id")
    private String bucketObjectId;

    @Transient()
    private LocalDateTime finalDeletion;

    @PrePersist
    private void onCreate() {
        LocalDateTime now = Utilities.getCurrentLocalDateTimeUtc();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Utilities.getCurrentLocalDateTimeUtc();
    }
}

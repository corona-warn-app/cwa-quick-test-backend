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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.ZonedDateTime;
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
    @Column(name = "partner_id", length = 64)
    private String partnerId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "cancellation_date")
    private ZonedDateTime cancellationDate;

    @Column(name = "moved_to_longterm_archive")
    private ZonedDateTime movedToLongtermArchive;

    @Column(name = "csv_created")
    private ZonedDateTime csvCreated;

    @Column(name = "download_link_requested")
    private ZonedDateTime downloadLinkRequested;

    @Column(name = "download_link_requested_by")
    @JsonIgnore
    private String downloadLinkRequestedBy;

    @Column(name = "data_deleted")
    private ZonedDateTime dataDeleted;

    @Column(name = "bucket_object_id")
    private String bucketObjectId;

    @Column(name = "csv_entity_count")
    @JsonIgnore
    private Integer csvEntityCount;

    @Column(name = "db_entity_count")
    @JsonIgnore
    private Integer dbEntityCount;

    @Column(name = "csv_hash")
    @JsonIgnore
    private String csvHash;

    @Column(name = "csv_size")
    @JsonIgnore
    private Integer csvSize;

    @Column(name = "data_export_error")
    @JsonIgnore
    private String dataExportError;

    @Column(name = "search_portal_deleted")
    private ZonedDateTime searchPortalDeleted;

    @Transient()
    private ZonedDateTime finalDeletion;

    @PrePersist
    private void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}

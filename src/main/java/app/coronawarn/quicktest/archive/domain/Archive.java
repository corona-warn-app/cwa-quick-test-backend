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

package app.coronawarn.quicktest.archive.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString(exclude = {"secret", "ciphertext"})
@Entity
@Table(name = "archive")
public class Archive {

    @Id
    @Column(name = "hashed_guid", columnDefinition = "varchar(108)", nullable = false)
    private String hashedGuid;

    @Column(name = "identifier", columnDefinition = "varchar(255)", nullable = false)
    private String identifier;

    @Column(name = "tenant_id", columnDefinition = "varchar(255)", nullable = false)
    private String tenantId;

    @Column(name = "poc_id", columnDefinition = "varchar(255)", nullable = false)
    private String pocId;

    @Lob
    @Column(name = "secret", nullable = false)
    private String secret;

    @Lob
    @Column(name = "ciphertext", nullable = false)
    private String ciphertext;

    @Column(name = "algorithm_aes", columnDefinition = "varchar(255)", nullable = false)
    private String algorithmAes;

    @Column(name = "created_at", columnDefinition = "datetime", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "datetime", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Setter(AccessLevel.NONE)
    @Column(name = "version", columnDefinition = "int", nullable = false)
    private Integer version;
}

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

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString(exclude = { "secret", "ciphertext", "publicKey" })
@Entity
@Table(name = "archive")
public class Archive {

    @Id
    @Column(name = "hashed_guid", columnDefinition = "varchar(108)", nullable = false)
    private String hashedGuid;

    @Column(name = "identifier", columnDefinition = "varchar(255)", nullable = false)
    private String identifier;

    @Column(name = "secret", columnDefinition = "longtext", nullable = false)
    private String secret;

    @Column(name = "ciphertext", columnDefinition = "longtext", nullable = false)
    private String ciphertext;

    @Column(name = "public_key", columnDefinition = "longtext", nullable = false)
    private String publicKey;

    @Column(name = "algorithm_aes", columnDefinition = "varchar(255)", nullable = false)
    private String algorithmAes;
    
    @Column(name = "algorithm_rsa", columnDefinition = "varchar(255)", nullable = false)
    private String algorithmRsa;

    @Column(name = "created_at", columnDefinition = "datetime", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "datetime", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Setter(AccessLevel.NONE)
    @Column(name = "version", columnDefinition = "int", nullable = false)
    private Integer version;
}

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

package app.coronawarn.quicktest.service;

import static org.assertj.core.api.Assertions.assertThat;
import app.coronawarn.quicktest.archive.domain.Archive;
import app.coronawarn.quicktest.archive.domain.ArchiveCipherDtoV1;
import app.coronawarn.quicktest.archive.repository.ArchiveRepository;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.service.cryption.AesCryption;
import app.coronawarn.quicktest.service.cryption.CryptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArchiveServiceTest {

    @Autowired
    private ArchiveService archiveService;

    @Autowired
    private QuickTestArchiveRepository quickTestArchiveRepository;

    @Autowired
    private ArchiveRepository archiveRepository;

    @Autowired
    private KeyProvider keyProvider;

    @Autowired
    private CryptionService cryptionService;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        this.quickTestArchiveRepository.deleteAll();
    }

    @Test
    void moveToArchive() throws Exception {
        // GIVEN
        final int initialArchive = this.archiveRepository.findAll().size();
        final QuickTestArchive test = this.buildQuickTestArchive();
        this.quickTestArchiveRepository.saveAndFlush(test);
        // WHEN
        this.archiveService.moveToArchive();
        // THEN
        final List<Archive> results = this.archiveRepository.findAll();
        assertThat(results).isNotNull().hasSize(initialArchive + 1);
        // AND result
        final Archive result = results.stream().filter(it -> test.getHashedGuid().equals(it.getHashedGuid())).findFirst().orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getHashedGuid()).isEqualTo(test.getHashedGuid());
        assertThat(result.getIdentifier()).isEqualTo(this.archiveService
                .buildIdentifier(test.getBirthday(), test.getLastName()));
        assertThat(result.getTenantId()).isEqualTo(this.archiveService.createHash(test.getTenantId()));
        assertThat(result.getPocId()).isEqualTo(this.archiveService.createHash(test.getPocId()));
        assertThat(result.getCreatedAt()).isAfterOrEqualTo(LocalDateTime.now().minusMinutes(5));
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(LocalDateTime.now().minusMinutes(5));
        assertThat(result.getVersion()).isNotNull().isNotNegative();
        // AND RSA encryption
        assertThat(result.getSecret()).isNotNull();
        final String secret = this.keyProvider.decrypt(result.getSecret());
        assertThat(secret).isNotBlank();
        // AND AES encryption
        assertThat(result.getCiphertext()).isNotNull();
        final AesCryption aesCryption = this.cryptionService.getAesCryption();
        final String json = aesCryption.decrypt(secret, result.getCiphertext());
        assertThat(json).isNotBlank().contains("ArchiveCipherDtoV1");
        assertThat(result.getAlgorithmAes()).isEqualTo(aesCryption.getAlgorithm());
        // AND DTO (Ciphertext)
        final ArchiveCipherDtoV1 dto = this.mapper.readValue(json, ArchiveCipherDtoV1.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getClassName()).isEqualTo(ArchiveCipherDtoV1.class.getSimpleName());
        assertThat(dto.getHashedGuid()).isEqualTo(test.getHashedGuid());
        assertThat(dto.getShortHashedGuid()).isEqualTo(test.getShortHashedGuid());
        assertThat(dto.getTenantId()).isEqualTo(test.getTenantId());
        assertThat(dto.getPocId()).isEqualTo(test.getPocId());
        assertThat(dto.getCreatedAt()).isEqualToIgnoringNanos(test.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualToIgnoringNanos(test.getUpdatedAt());
        assertThat(dto.getVersion()).isEqualTo(test.getVersion());
        assertThat(dto.getConfirmationCwa()).isEqualTo(test.getConfirmationCwa());
        assertThat(dto.getTestResult()).isEqualTo(test.getTestResult());
        assertThat(dto.getPrivacyAgreement()).isEqualTo(test.getPrivacyAgreement());
        assertThat(dto.getLastName()).isEqualTo(test.getLastName());
        assertThat(dto.getFirstName()).isEqualTo(test.getFirstName());
        assertThat(dto.getEmail()).isEqualTo(test.getEmail());
        assertThat(dto.getPhoneNumber()).isEqualTo(test.getPhoneNumber());
        assertThat(dto.getSex()).isEqualTo(test.getSex());
        assertThat(dto.getStreet()).isEqualTo(test.getStreet());
        assertThat(dto.getHouseNumber()).isEqualTo(test.getHouseNumber());
        assertThat(dto.getZipCode()).isEqualTo(test.getZipCode());
        assertThat(dto.getCity()).isEqualTo(test.getCity());
        assertThat(dto.getTestBrandId()).isEqualTo(test.getTestBrandId());
        assertThat(dto.getTestBrandName()).isEqualTo(test.getTestBrandName());
        assertThat(dto.getBirthday()).isEqualTo(test.getBirthday());
        assertThat(dto.getTestResultServerHash()).isEqualTo(test.getTestResultServerHash());
        assertThat(dto.getDcc()).isEqualTo(test.getDcc());
        assertThat(dto.getAdditionalInfo()).isEqualTo(test.getAdditionalInfo());
        assertThat(dto.getGroupName()).isEqualTo(test.getGroupName());
        // AND deleted QuickTestArchive
        final Optional<QuickTestArchive> deletedTest = this.quickTestArchiveRepository.findById(test.getHashedGuid());
        assertThat(deletedTest).isEmpty();
    }

    private QuickTestArchive buildQuickTestArchive() {
        QuickTestArchive qta = new QuickTestArchive();
        qta.setShortHashedGuid("27a9ac47");
        qta.setHashedGuid("27a9ac470b7832857ad03b25dc96032e0a1056ff4f5afb538de4994e0a63d227");
        qta.setTenantId("tenant_id");
        qta.setPocId("poc_id");
        qta.setCreatedAt(LocalDateTime.now().minusMonths(3));
        qta.setUpdatedAt(LocalDateTime.now().minusMonths(2));
        qta.setConfirmationCwa(Boolean.TRUE);
        qta.setTestResult(Short.valueOf("6"));
        qta.setPrivacyAgreement(Boolean.TRUE);
        qta.setLastName("last_name");
        qta.setFirstName("first_name");
        qta.setEmail("email");
        qta.setPhoneNumber("phone_number");
        qta.setSex(Sex.MALE);
        qta.setStreet("street");
        qta.setHouseNumber("house_number");
        qta.setZipCode("zip_code");
        qta.setCity("city");
        qta.setTestBrandId("test_brand_id");
        qta.setTestBrandName("test_brand_name");
        qta.setBirthday("2000-01-01");
        qta.setPdf("PDF".getBytes());
        qta.setTestResultServerHash("test_result_server_hash");
        qta.setDcc("dcc");
        qta.setAdditionalInfo("additional_info");
        qta.setGroupName("group_name");
        return qta;
    }
}

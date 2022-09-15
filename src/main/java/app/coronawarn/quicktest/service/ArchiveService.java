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

import app.coronawarn.quicktest.archive.domain.Archive;
import app.coronawarn.quicktest.archive.domain.ArchiveCipherDtoV1;
import app.coronawarn.quicktest.archive.repository.ArchiveRepository;
import app.coronawarn.quicktest.config.ArchiveProperties;
import app.coronawarn.quicktest.exception.UncheckedJsonProcessingException;
import app.coronawarn.quicktest.exception.UncheckedNoSuchAlgorithmException;
import app.coronawarn.quicktest.repository.QuickTestArchiveDataView;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.service.cryption.CryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private static final DateTimeFormatter BIRTHDAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter IDENTIFIER_FORMATTER = DateTimeFormatter.ofPattern("ddMM");

    private final ArchiveProperties properties;

    private final KeyProvider keyProvider;

    private final ArchiveRepository longTermArchiveRepository;

    private final QuickTestArchiveRepository shortTermArchiveRepository;

    private final ConversionService converter;

    private final ObjectMapper mapper;

    private final CryptionService cryptionService;

    /**
     * Moves the entries to the archive.
     */
    @Transactional
    public void moveToArchive() {
        final long olderThanInSeconds = properties.getMoveToArchiveJob().getOlderThanInSeconds();
        final int chunkSize = properties.getMoveToArchiveJob().getChunkSize();
        if (olderThanInSeconds > 0) {
            final LocalDateTime beforeDateTime = LocalDateTime.now().minusSeconds(olderThanInSeconds);
            List<QuickTestArchiveDataView> shortTermArchiveEntities =
                    shortTermArchiveRepository.findAllByUpdatedAtBefore(beforeDateTime, PageRequest.of(0, chunkSize))
                            .filter(this::checkIsNotOnIgnoreListOrDelete)
                            .collect(Collectors.toList());

            if (shortTermArchiveEntities.isEmpty()) {
                log.debug("No ShortTermArchive Data to move.");
                return;
            }

            List<String> hashedGuids = shortTermArchiveEntities.stream()
                    .map(QuickTestArchiveDataView::getHashedGuid)
                    .collect(Collectors.toList());

            List<String> existingHashedGuids = longTermArchiveRepository.findAllHashedGuids(hashedGuids);
            if (!existingHashedGuids.isEmpty()) {
                log.warn("Found {} QuickTest entities which are both in short-term and long-term archive: {}",
                        existingHashedGuids.size(), String.join(", ", existingHashedGuids));
                log.warn("Deleting not properly cleaned up entities.");
                existingHashedGuids.forEach(shortTermArchiveRepository::deleteByHashedGuid);
            }

            shortTermArchiveEntities.stream()
                    .filter(entity -> !existingHashedGuids.contains(entity.getHashedGuid()))
                    .map(this::convertQuickTest)
                    .map(this::buildArchive)
                    .map(longTermArchiveRepository::save)
                    .map(Archive::getHashedGuid)
                    .forEach(shortTermArchiveRepository::deleteById);
        } else {
            log.error("Property 'archive.moveToArchiveJob.older-than-in-seconds' not set.");
        }
        log.info("Finished move to longterm archive.");
    }

    /**
     * Moves all entries to the archive for a given tenantId.
     */
    @Transactional
    public void moveToArchiveByTenantId(String tenantId) {
        final int chunkSize = properties.getCancellationArchiveJob().getChunkSize();

        List<QuickTestArchiveDataView> shortTermArchiveEntities =
                shortTermArchiveRepository.findAllByTenantId(tenantId, PageRequest.of(0, chunkSize))
                        .filter(this::checkIsNotOnIgnoreListOrDelete)
                        .collect(Collectors.toList());

        if (shortTermArchiveEntities.isEmpty()) {
            log.debug("No (more) ShortTermArchive Data to move.");
            return;
        }

        List<String> hashedGuids = shortTermArchiveEntities.stream()
                .map(QuickTestArchiveDataView::getHashedGuid)
                .collect(Collectors.toList());

        List<String> existingHashedGuids = longTermArchiveRepository.findAllHashedGuids(hashedGuids);
        if (!existingHashedGuids.isEmpty()) {
            log.warn("Found {} QuickTest entities which are both in short-term and long-term archive: {}",
                    existingHashedGuids.size(), String.join(", ", existingHashedGuids));
            log.warn("Deleting not properly cleaned up entities.");
            existingHashedGuids.forEach(shortTermArchiveRepository::deleteByHashedGuid);
        }

        shortTermArchiveEntities.stream()
                .filter(entity -> !existingHashedGuids.contains(entity.getHashedGuid()))
                .map(this::convertQuickTest)
                .map(this::buildArchive)
                .map(longTermArchiveRepository::save)
                .map(Archive::getHashedGuid)
                .forEach(shortTermArchiveRepository::deleteById);
        log.info("Chunk Finished move to longterm archive.");

        // Continue with next chunk (Recursion will be stopped when no entities are left)
        moveToArchiveByTenantId(tenantId);
    }

    /**
     * Get longterm archives by pocId.
     */
    public List<ArchiveCipherDtoV1> getQuicktestsFromLongterm(final String pocId, final String tenantId)
            throws JsonProcessingException {
        List<Archive> allByPocId = longTermArchiveRepository.findAllByPocId(createHash(pocId));
        List<ArchiveCipherDtoV1> dtos = new ArrayList<>(allByPocId.size());
        for (Archive archive : allByPocId) {
            try {
                final String decrypt = keyProvider.decrypt(archive.getSecret(), tenantId);
                final String json = cryptionService.getAesCryption().decrypt(decrypt, archive.getCiphertext());
                final ArchiveCipherDtoV1 dto = this.mapper.readValue(json, ArchiveCipherDtoV1.class);
                dtos.add(dto);
            } catch (final Exception e) {
                log.warn("Could not decrypt archive {}", archive.getHashedGuid());
                log.warn("Cause: {}", e.getLocalizedMessage());
            }
        }
        return dtos;
    }

    /**
     * Get longterm archives by tenantId.
     */
    public List<ArchiveCipherDtoV1> getQuicktestsFromLongtermByTenantId(final String tenantId) {
        List<Archive> allByPocId = longTermArchiveRepository.findAllByTenantId(createHash(tenantId));
        List<ArchiveCipherDtoV1> dtos = new ArrayList<>(allByPocId.size());
        for (Archive archive : allByPocId) {
            try {
                final String decrypt = keyProvider.decrypt(archive.getSecret(), tenantId);
                final String json = cryptionService.getAesCryption().decrypt(decrypt, archive.getCiphertext());
                final ArchiveCipherDtoV1 dto = this.mapper.readValue(json, ArchiveCipherDtoV1.class);
                dtos.add(dto);
            } catch (final Exception e) {
                log.warn("Could not decrypt archive {}", archive.getHashedGuid());
                log.warn("Cause: {}", e.getLocalizedMessage());
            }
        }
        return dtos;
    }

    /**
     * Checks whether the PartnerId of given Test is on ignore-list for longterm archive and directly deletes entry
     * from archive.
     *
     * @param archiveData entity to check
     * @return true if entity's partner ID is NOT on ignore list.
     */
    private boolean checkIsNotOnIgnoreListOrDelete(QuickTestArchiveDataView archiveData) {
        List<String> excludedPartners = Stream.of(properties.getExcludedPartners().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        if (excludedPartners.stream().anyMatch(partnerId -> partnerId.equals(archiveData.getTenantId()))) {
            shortTermArchiveRepository.deleteById(archiveData.getHashedGuid());
            return false;
        } else {
            return true;
        }
    }

    public void deleteByTenantId(String partnerId) {
        longTermArchiveRepository.deleteAllByTenantId(createHash(partnerId));
    }

    private ArchiveCipherDtoV1 convertQuickTest(final QuickTestArchiveDataView quickTestArchive) {
        final ArchiveCipherDtoV1 archive = new ArchiveCipherDtoV1();

        archive.setShortHashedGuid(quickTestArchive.getShortHashedGuid());
        archive.setHashedGuid(quickTestArchive.getHashedGuid());
        archive.setTenantId(quickTestArchive.getTenantId());
        archive.setPocId(quickTestArchive.getPocId());
        archive.setCreatedAt(quickTestArchive.getCreatedAt());
        archive.setUpdatedAt(quickTestArchive.getUpdatedAt());
        archive.setVersion(quickTestArchive.getVersion());
        archive.setConfirmationCwa(quickTestArchive.getConfirmationCwa());
        archive.setTestResult(quickTestArchive.getTestResult());
        archive.setPrivacyAgreement(quickTestArchive.getPrivacyAgreement());
        archive.setFirstName(quickTestArchive.getFirstName());
        archive.setLastName(quickTestArchive.getLastName());
        archive.setEmail(quickTestArchive.getEmail());
        archive.setPhoneNumber(quickTestArchive.getPhoneNumber());
        archive.setSex(quickTestArchive.getSex());
        archive.setStreet(quickTestArchive.getStreet());
        archive.setHouseNumber(quickTestArchive.getHouseNumber());
        archive.setZipCode(quickTestArchive.getZipCode());
        archive.setCity(quickTestArchive.getCity());
        archive.setBirthday(quickTestArchive.getBirthday());
        archive.setTestBrandId(quickTestArchive.getTestBrandId());
        archive.setTestBrandName(quickTestArchive.getTestBrandName());
        archive.setTestResultServerHash(quickTestArchive.getTestResultServerHash());
        archive.setDcc(quickTestArchive.getDcc());
        archive.setAdditionalInfo(quickTestArchive.getAdditionalInfo());
        archive.setGroupName(quickTestArchive.getGroupName());

        return archive;
    }

    private Archive buildArchive(final ArchiveCipherDtoV1 dto) {
        final LocalDateTime now = LocalDateTime.now();
        final String secret = cryptionService.generateRandomSecret();

        final Archive archive = new Archive();
        archive.setHashedGuid(dto.getHashedGuid());
        archive.setIdentifier(buildIdentifier(dto));
        archive.setTenantId(createHash(dto.getTenantId()));
        archive.setPocId(createHash(dto.getPocId()));
        archive.setCiphertext(buildCiphertext(secret, dto));
        archive.setSecret(encryptSecret(secret, dto.getTenantId()));
        archive.setAlgorithmAes(cryptionService.getAesCryption().getAlgorithm());
        archive.setCreatedAt(now);
        archive.setUpdatedAt(now);
        return archive;
    }

    private String encryptSecret(final String secret, final String context) {
        return keyProvider.encrypt(secret, context);
    }

    private String buildCiphertext(final String secret, final ArchiveCipherDtoV1 dto) {
        try {
            final String json = mapper.writeValueAsString(dto);
            return cryptionService.getAesCryption().encrypt(secret, json);
        } catch (JsonProcessingException e) {
            throw new UncheckedJsonProcessingException(e);
        }
    }

    private String buildIdentifier(final ArchiveCipherDtoV1 dto) {
        return buildIdentifier(dto.getBirthday(), dto.getLastName());
    }

    String buildIdentifier(final String birthday, final String lastname) {
        final String lastnameId = StringUtils.rightPad(lastname, 2, 'X');
        final String identifier = String.format("%s%s",
                LocalDate.parse(birthday, BIRTHDAY_FORMATTER).format(IDENTIFIER_FORMATTER),
                lastnameId.substring(0, 2).toUpperCase());
        return createHash(identifier);
    }

    String createHash(String in) {
        if (StringUtils.isBlank(in)) {
            return "";
        }
        final MessageDigest digest = buildMessageDigest(properties.getHash().getAlgorithm());
        digest.reset();
        digest.update(keyProvider.getPepper());
        byte[] identifierHashed = digest.digest(in.getBytes(StandardCharsets.UTF_8));
        return Hex.toHexString(identifierHashed);
    }

    private MessageDigest buildMessageDigest(final String alg) {
        try {
            return MessageDigest.getInstance(alg);
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedNoSuchAlgorithmException(e);
        }
    }
}

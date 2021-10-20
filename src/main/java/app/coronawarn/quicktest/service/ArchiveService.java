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
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.exception.UncheckedJsonProcessingException;
import app.coronawarn.quicktest.exception.UncheckedNoSuchAlgorithmException;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.service.cryption.CryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.core.convert.ConversionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private static final  DateTimeFormatter BIRTHDAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter IDENTIFIER_FORMATTER = DateTimeFormatter.ofPattern("ddMM");

    private final QuickTestConfig properties;

    private final KeyProvider keyProvider;

    private final ArchiveRepository repository;

    private final QuickTestArchiveRepository quickTestArchiveRepository;

    private final ConversionService converter;

    private final ObjectMapper mapper;

    private final CryptionService cryptionService;

    @Transactional
    @Scheduled(cron = "${quicktest.archive.moveToArchiveJob.cron}")
    @SchedulerLock(name = "MoveToArchiveJob", lockAtLeastFor = "PT0S", 
        lockAtMostFor = "${quicktest.archive.moveToArchiveJob.locklimit}")
    public void moveToArchiveJob() {
        log.debug("Starting Job: moveToArchiveJob");
        this.moveToArchive();
    }
    
    /**
     * Moves the entries to the archive.
     */
    @Transactional
    public void moveToArchive() {
        final long olderThanInSeconds = this.properties.getArchive().getMoveToArchiveJob().getOlderThanInSeconds();
        if (olderThanInSeconds > 0) {
            final LocalDateTime beforeDateTime = LocalDateTime.now().minusSeconds(olderThanInSeconds);
            this.quickTestArchiveRepository.findAllByUpdatedAtBefore(beforeDateTime)
                    .map(this::convertQuickTest)
                    .map(this::buildArchive)
                    .map(this.repository::save)
                    .map(archive -> archive.getHashedGuid())
                    .forEach(this.quickTestArchiveRepository::deleteById);
        } else {
            log.error("Property 'quicktest.archive.moveToArchiveJob.older-than-in-seconds' not set.");
        }
    }

    private ArchiveCipherDtoV1 convertQuickTest(final QuickTestArchive quickTestArchive) {
        return this.converter.convert(quickTestArchive, ArchiveCipherDtoV1.class);
    }

    private Archive buildArchive(final ArchiveCipherDtoV1 dto) {
        final LocalDateTime now = LocalDateTime.now();
        final PublicKey publicKey = this.keyProvider.getPublicKey();
        final String secret = cryptionService.generateRandomSecret();

        final Archive archive = new Archive();
        archive.setHashedGuid(dto.getHashedGuid());
        archive.setIdentifier(this.buildIdentifier(dto));
        archive.setCiphertext(this.buildCiphertext(secret, dto));
        archive.setSecret(this.encryptSecret(publicKey, secret));
        archive.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        archive.setAlgorithmAes(this.cryptionService.getAesCryption().getAlgorithm());
        archive.setAlgorithmRsa(this.cryptionService.getRsaCryption().getAlgorithm());
        archive.setCreatedAt(now);
        archive.setUpdatedAt(now);
        return archive;
    }

    private String encryptSecret(final PublicKey publicKey, final String secret) {
        return this.cryptionService.getRsaCryption().encrypt(publicKey, secret);
    }

    private String buildCiphertext(final String secret, final ArchiveCipherDtoV1 dto) {
        try {
            final String json = this.mapper.writeValueAsString(dto);
            return this.cryptionService.getAesCryption().encrypt(secret, json);
        } catch (JsonProcessingException e) {
            throw new UncheckedJsonProcessingException(e);
        }
    }

    private String buildIdentifier(final ArchiveCipherDtoV1 dto) {
        return this.buildIdentifier(dto.getBirthday(), dto.getLastName());
    }
    
    String buildIdentifier(final String birthday, final String lastname) {
        final String identifier = String.format("%s%s",
                LocalDate.parse(birthday, BIRTHDAY_FORMATTER).format(IDENTIFIER_FORMATTER),
                lastname.substring(0, 2).toUpperCase());

        final MessageDigest digest = buildMessageDigest(this.properties.getArchive().getHash().getAlgorithm());
        digest.reset();
        digest.update(this.keyProvider.getPepper());
        byte[] identifierHashed = digest.digest(identifier.getBytes(StandardCharsets.UTF_8));
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

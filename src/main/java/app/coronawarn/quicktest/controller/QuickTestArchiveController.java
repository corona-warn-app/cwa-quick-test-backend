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

package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.service.QuickTestArchiveService;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(value = "/api/quicktestarchive")
@RequiredArgsConstructor
public class QuickTestArchiveController {

    private final QuickTestService quickTestService;
    private final QuickTestArchiveService quickTestArchiveService;

    private final QuickTestArchiveRepository quickTestArchiveRepository;

    /**
     * Endpoint for storing archive. The record in QuickTest will be moved unchanged to archive.
     *
     * @param shortHashedGuid containing the short hashed guid.
     * @param pdf containing the generated pdf to store.
     * @return Empty Response
     */
    @Operation(
            summary = "Creates a quicktest archive record",
            description = "Creates a quicktest archive and move exists quicktest data unchanged to it."
                    + "Note: quicktest is deleted if successful."
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Quicktest archive is created"),
      @ApiResponse(responseCode = "409", description = "Quicktest archive with hash already exists"),
      @ApiResponse(responseCode = "500", description = "Inserting failed because of internal error.")})
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<String> createQuickTestArchive(
            @RequestParam String shortHashedGuid,
            @RequestParam MultipartFile pdf) {
        if (pdf.isEmpty()) {
            return ResponseEntity.badRequest().body("Error Message: PDF is empty");
        }
        if (!pdf.getContentType().equals(MediaType.APPLICATION_PDF_VALUE)) {
            return ResponseEntity.badRequest().body("Error Message: Only PDF content type supported");
        }
        log.debug("Persisting new file: {}", pdf.getOriginalFilename());
        try {
            quickTestArchiveService.createNewQuickTestArchive(shortHashedGuid, pdf);
        } catch (QuickTestServiceException e) {
            if (e.getReason() == QuickTestServiceException.Reason.INSERT_CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("Exception = {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @Secured(ROLE_LAB)
    public byte[] getQuickTestArchivePDF() {
        Optional<QuickTestArchive> qta = quickTestArchiveRepository.findById(
            "8fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        return qta.get().getPdf();
    }

}

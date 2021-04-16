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
import app.coronawarn.quicktest.service.QuickTestServiceException;
import app.coronawarn.quicktest.utils.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/quicktestarchive")
@RequiredArgsConstructor
public class QuickTestArchiveController {

    private final QuickTestArchiveService quickTestArchiveService;

    /**
     * Endpoint for receiving pdf.
     *
     * @param hashedGuid containing the full hashed guid.
     * @return PDF
     */
    @Operation(
            summary = "Response quicktest as PDF",
            description = "PDF stored in DB will be responsed for download if found."
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "PDF found"),
      @ApiResponse(responseCode = "404", description = "Quicktest not found"),
      @ApiResponse(responseCode = "500", description = "Inserting failed because of internal error.")})
    @RequestMapping(path = "/pdf", method = RequestMethod.GET)
    @Secured(ROLE_LAB)
    public ResponseEntity<byte[]> createQuickTestArchive(
            @RequestParam String hashedGuid) {
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                            + "Schnelltest_" + hashedGuid + "\"")
                    .body(quickTestArchiveService.getPdf(hashedGuid));
        } catch (QuickTestServiceException e) {
            if (e.getReason() == QuickTestServiceException.Reason.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("Couldn't prepare stored pdf for download. Message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

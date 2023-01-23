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

package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ARCHIVE_OPERATOR;

import app.coronawarn.quicktest.service.ArchiveService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequestMapping(value = "/api/archive")
@RequiredArgsConstructor
@Profile("archive_export")
public class ArchiveExportController {

    private final ArchiveService archiveService;

    /**
     * Endpoint for downloading archived entities.
     *
     * @return CSV with all archived data.
     */
    @Operation(
        summary = "Download Archive CSV-File",
        description = "Creates a CSV-File with all archived data for whole Partner or just one POC ID.",
        parameters = {
          @Parameter(
                in = ParameterIn.PATH,
                name = "partnerId",
                description = "Partner ID of the PArtner to download data of",
                required = true),
          @Parameter(
                in = ParameterIn.QUERY,
                name = "pocId",
                description = "Filter for entities with given pocId")
        }
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful")
    })
    @GetMapping(value = "/{partnerId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Secured({ROLE_ARCHIVE_OPERATOR})
    public ResponseEntity<byte[]> exportArchive(@PathVariable("partnerId") String partnerId,
                                                Authentication authentication) {

        try {
            ArchiveService.CsvExportFile csv = archiveService.createCsv(partnerId);

            log.info("Archive Export triggered for PartnerId: {} by {}, FileSize: {}",
                partnerId, authentication.getName(), csv.getCsvBytes().length);

            return ResponseEntity
                .status(HttpStatus.OK)
                .contentLength(csv.getCsvBytes().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quicktest_export.csv")
                .body(csv.getCsvBytes());

        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("Failed to create CSV: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create CSV.");
        }
    }
}

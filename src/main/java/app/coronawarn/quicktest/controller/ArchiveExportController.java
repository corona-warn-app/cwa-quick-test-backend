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
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ARCHIVE_ZIP_CREATOR;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ARCHIVE_ZIP_DOWNLOADER;

import app.coronawarn.quicktest.model.cancellation.ZipRequest;
import app.coronawarn.quicktest.service.ArchiveService;
import app.coronawarn.quicktest.service.ArchiveZipService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequestMapping(value = "/api/archive")
@RequiredArgsConstructor
@Profile("archive_export")
@Validated
public class ArchiveExportController {

    private final ArchiveService archiveService;

    private final ArchiveZipService archiveZipService;

    private static final String zipFileNameRegex = "^\\w{0,20}.zip$";

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

    /**
     * Endpoint for creating a zip file with multiple CSV-files.
     *
     * @return Status Code.
     */
    @Operation(
        summary = "Create Archive ZIP-File",
        description = "Creates a ZIP-File with all CSV-Files of provided partner ids."
            + "ZIP File will be stored in OBS bucket"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "ZIP Created")
    })
    @PostMapping(value = "/zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Secured({ROLE_ARCHIVE_ZIP_CREATOR})
    public ResponseEntity<Void> createZip(@Valid @RequestBody ZipRequest zipRequest) {

        try {
            List<String> partnerIds = zipRequest.getPartnerIds();
            partnerIds.add(zipRequest.getPartnerId());

            archiveZipService.createZip(
                zipRequest.getPartnerId() + ".zip",
                zipRequest.getPartnerIds(),
                zipRequest.getPassword());

        } catch (ArchiveZipService.ArchiveZipServiceException e) {
            if (e.getReason() == ArchiveZipService.ArchiveZipServiceException.Reason.CSV_FILE_NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CSV File " + e.getMessage() + " not found");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unexpected Error while creating ZIP File.");
            }
        } catch (IOException e) {
            log.error("Failed to create ZIP (IOException): {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .build();
    }

    /**
     * Endpoint for receiving download link for Archive ZIP File.
     *
     * @return Download link of zip
     */
    @Operation(
        summary = "Request Download Link (ZIP)",
        description = "Returns a presigned URL to download the generated ZIP File from Bucket."
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful"),
      @ApiResponse(responseCode = "404", description = "No ZIP file found")
    })
    @GetMapping(value = "/zip/download", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ROLE_ARCHIVE_ZIP_DOWNLOADER})
    public ResponseEntity<URL> getZipLink(
        @Valid @Pattern(regexp = zipFileNameRegex) @RequestParam("filename") String filename) {

        Date expiration = Date.from(Instant.now().plus(5, ChronoUnit.MINUTES));
        URL url;
        try {
            url = archiveZipService.getDownloadUrl(filename, expiration);
        } catch (ArchiveZipService.ArchiveZipServiceException e) {
            if (e.getReason() == ArchiveZipService.ArchiveZipServiceException.Reason.ZIP_FILE_NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ZIP File doesn't exist in Bucket");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }

        return ResponseEntity.ok(url);
    }
}

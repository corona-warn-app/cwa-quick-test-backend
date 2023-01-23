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

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;

import app.coronawarn.quicktest.model.quicktest.QuickTestArchiveResponse;
import app.coronawarn.quicktest.model.quicktest.QuickTestArchiveResponseList;
import app.coronawarn.quicktest.repository.QuickTestArchiveView;
import app.coronawarn.quicktest.service.QuickTestArchiveService;
import app.coronawarn.quicktest.utils.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(value = "/api/quicktestarchive")
@RequiredArgsConstructor
@Validated
public class QuickTestArchiveController {

    private final QuickTestArchiveService quickTestArchiveService;
    private final ModelMapper modelMapper;
    private final Utilities utilities;

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
    @RequestMapping(path = "/{hashedGuid}/pdf", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
    @Secured({ROLE_COUNTER, ROLE_LAB})
    public ResponseEntity<byte[]> getQuickTestPdf(
            @PathVariable("hashedGuid") String hashedGuid) {
        try {
            ResponseEntity<byte[]> responseEntity = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""
                    + "Schnelltest_" + hashedGuid + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(quickTestArchiveService.getPdf(hashedGuid));
            log.info("pdf successfully downloaded.");
            return responseEntity;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't prepare stored pdf for download.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * TODO: Maybe add custom ROLE
     * Endpoint for getting quicktests in archive table by query parameters.
     *
     * @return QuickTestArchiveListResponse with all found archives
     */
    @Operation(
            summary = "Find quicktests in archive",
            description = "Returns all found quicktests in archive for search parameters"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful"),
      @ApiResponse(responseCode = "500", description = "Query failed because of an internal server error")
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ROLE_COUNTER, ROLE_LAB})
    public ResponseEntity<QuickTestArchiveResponseList> findArchivesByTestResultAndUpdatedAtBetween(
            @RequestParam(required = false) @Min(0) @Max(8) Short testResult,
            @RequestParam("dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime zonedDateFrom,
            @RequestParam("dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime zonedDateTo) {
        try {
            LocalDateTime utcDateFrom = LocalDateTime.ofInstant(zonedDateFrom.toInstant(), ZoneOffset.UTC);
            LocalDateTime utcDateTo = LocalDateTime.ofInstant(zonedDateTo.toInstant(), ZoneOffset.UTC);
            List<QuickTestArchiveView> archives = quickTestArchiveService.findByTestResultAndUpdatedAtBetween(
                utilities.getIdsFromToken(),
                testResult,
                utcDateFrom,
                utcDateTo);
            TypeToken<List<QuickTestArchiveResponse>> typeToken = new TypeToken<>(){};
            List<QuickTestArchiveResponse> quickTestArchiveResponses = modelMapper.map(
                    archives,
                    typeToken.getType()
            );
            QuickTestArchiveResponseList response = new QuickTestArchiveResponseList();
            response.setQuickTestArchives(quickTestArchiveResponses);
            log.info("quicktest found successfully.");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Couldn't execute findArchivesByTestResultAndUpdatedAtBetween."
                    + " Message: {}", e.getMessage());
            log.error("Couldn't execute findArchivesByTestResultAndUpdatedAtBetween.");
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "trying to find quicktests failed");
        }
    }

}

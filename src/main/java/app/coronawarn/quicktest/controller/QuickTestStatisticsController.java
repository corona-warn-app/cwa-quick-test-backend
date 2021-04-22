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

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;

import app.coronawarn.quicktest.model.QuickTestStatisticsResponse;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import app.coronawarn.quicktest.service.QuickTestStatisticsService;
import app.coronawarn.quicktest.utils.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(value = "/api/quickteststatistics")
@RequiredArgsConstructor
public class QuickTestStatisticsController {

    private final QuickTestStatisticsService quickTestStatisticsService;

    private final ModelMapper modelMapper;

    private final Utilities utilities;


    //TODO check role
    /**
     * Endpoint for get statistic for QuickTest.
     *
     * @return QuickTestStatisticsResponse with total and positive count
     */
    @Operation(
        summary = "Get quicktest statistics",
        description = "Returns the statistics for total and positive counts for today"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Get statistic data"),
      @ApiResponse(responseCode = "500", description = "Inserting failed because of internal error.")})
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_COUNTER)
    public ResponseEntity<QuickTestStatisticsResponse> getQuicktestStatistics(
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    ZonedDateTime zonedDateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    ZonedDateTime zonedDateTo
    ) {
        try {
            if (zonedDateFrom == null) {
                zonedDateFrom = Utilities.getStartTimeForLocalDateInGermany();
            }
            if (zonedDateTo == null) {
                zonedDateTo = Utilities.getEndTimeForLocalDateInGermany();
            }
            LocalDateTime utcDateFrom = LocalDateTime.ofInstant(zonedDateFrom.toInstant(), ZoneOffset.UTC);
            LocalDateTime utcDateTo = LocalDateTime.ofInstant(zonedDateTo.toInstant(), ZoneOffset.UTC);
            QuickTestStatisticsResponse quickTestStatisticsResponse = modelMapper.map(
                quickTestStatisticsService.getStatistics(utilities.getIdsFromToken(), utcDateFrom, utcDateTo),
                QuickTestStatisticsResponse.class);

            return ResponseEntity.ok(quickTestStatisticsResponse);
        } catch (QuickTestServiceException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, e.getReason().toString());
        } catch (Exception e) {
            log.error("Couldn't execute getQuicktestStatistics."
                    + " Message: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "trying to get statistics failed");
        }

    }

}

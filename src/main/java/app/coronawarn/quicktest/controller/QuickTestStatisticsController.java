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
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_TENANT_COUNTER;

import app.coronawarn.quicktest.model.Aggregation;
import app.coronawarn.quicktest.model.QuickTestStatisticsResponse;
import app.coronawarn.quicktest.model.QuickTestTenantStatistics;
import app.coronawarn.quicktest.model.QuickTestTenantStatisticsResponse;
import app.coronawarn.quicktest.model.QuickTestTenantStatisticsResponseList;
import app.coronawarn.quicktest.service.QuickTestStatisticsService;
import app.coronawarn.quicktest.utils.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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

    /**
     * Endpoint for get statistic for QuickTest.
     *
     * @return QuickTestStatisticsResponse with total and positive count
     */
    @Operation(
        summary = "Get quicktest statistics",
        description = "Returns the statistics for total and positive counts (default for today)"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Get statistic data"),
      @ApiResponse(responseCode = "500", description = "Inserting failed because of internal error.")})
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ROLE_COUNTER, ROLE_LAB})
    public ResponseEntity<QuickTestStatisticsResponse> getQuicktestStatistics(
        @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime zonedDateFrom,
        @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime zonedDateTo) {
        try {
            if (zonedDateFrom == null) {
                zonedDateFrom = Utilities.getStartTimeForLocalDateInGermanyInUtc();
            }
            if (zonedDateTo == null) {
                zonedDateTo = Utilities.getEndTimeForLocalDateInGermanyInUtc();
            }
            LocalDateTime utcDateFrom = LocalDateTime.ofInstant(zonedDateFrom.toInstant(), ZoneOffset.UTC);
            LocalDateTime utcDateTo = LocalDateTime.ofInstant(zonedDateTo.toInstant(), ZoneOffset.UTC);
            QuickTestStatisticsResponse quickTestStatisticsResponse = modelMapper.map(
                quickTestStatisticsService.getStatistics(utilities.getIdsFromToken(), utcDateFrom, utcDateTo),
                QuickTestStatisticsResponse.class);
            return ResponseEntity.ok(quickTestStatisticsResponse);
        } catch (Exception e) {
            log.error("Couldn't execute getQuicktestStatistics.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint for get statistic for QuickTest.
     *
     * @return QuickTestStatisticsResponse with total and positive count
     */
    @Operation(
        summary = "Get aggregated statistic data for tenant",
        description = "Returns the aggregated statistics for tenant with total and positive counts"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Get aggregated statistic data for tenant"),
      @ApiResponse(responseCode = "500", description = "Inserting failed because of internal error.")})
    @GetMapping(value = "/tenant", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_TENANT_COUNTER)
    public ResponseEntity<QuickTestTenantStatisticsResponseList> getQuicktestStatisticsForTenantWithAggregation(
        @RequestParam(value = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime zonedDateFrom,
        @RequestParam(value = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            ZonedDateTime zonedDateTo,
        @RequestParam(value = "aggregation")
            Aggregation aggregation) {
        try {
            LocalDateTime utcDateFrom = LocalDateTime.ofInstant(zonedDateFrom.toInstant(), ZoneOffset.UTC);
            LocalDateTime utcDateTo = LocalDateTime.ofInstant(zonedDateTo.toInstant(), ZoneOffset.UTC);

            List<QuickTestTenantStatistics> quickTestTenantStatistics =
                quickTestStatisticsService.getStatisticsForTenant(
                    utilities.getTenantIdFromToken(),
                    utcDateFrom,
                    utcDateTo,
                    aggregation);

            TypeToken<List<QuickTestTenantStatisticsResponse>> typeToken = new TypeToken<>() {
            };
            List<QuickTestTenantStatisticsResponse> quickTestTenantStatisticsResponses = modelMapper.map(
                quickTestTenantStatistics,
                typeToken.getType()
            );

            QuickTestTenantStatisticsResponseList response = new QuickTestTenantStatisticsResponseList();
            response.setQuickTestTenantStatistics(quickTestTenantStatisticsResponses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Couldn't execute getQuicktestStatistics.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

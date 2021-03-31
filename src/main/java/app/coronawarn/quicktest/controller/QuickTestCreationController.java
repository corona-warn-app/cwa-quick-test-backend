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

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.QuickTestUpdateRequest;
import app.coronawarn.quicktest.model.QuicktestCreationRequest;
import app.coronawarn.quicktest.service.QuickTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/quicktest")
@RequiredArgsConstructor
public class QuickTestCreationController {

    @Autowired
    QuickTestService quickTestService;

    @Operation(
        summary = "Creates a quicktest",
        description = "Creates a quicktest and a pending testresult"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Quicktest is created"),
        @ApiResponse(responseCode = "409", description = "Quicktest already exists")})
    @PostMapping(value = "/",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Secured(ROLE_COUNTER)
    public ResponseEntity<Resource> createQuickTest(QuicktestCreationRequest quicktestCreationRequest) {
        if( quickTestService.saveQuickTest(quicktestCreationRequest) != null) {
            return ResponseEntity.status(200).build();
        }
        return ResponseEntity.status(409).build();
    }

    /**
     * Endpoint for updating a Quicktest result.
     *
     * @param quickTestUpdateRequest contains the the new test value for the quicktest.
     * @return ResponseEntity with binary data.
     */
    @Operation(
        summary = "Updates the testresult of an quicktest",
        description = "Updates the testresult of an quicktest"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "204 ", description = "Update successful"),
      @ApiResponse(responseCode = "404", description = "Short Hash doesn't exists")})
    @PutMapping(value = "/",
             consumes = MediaType.APPLICATION_JSON_VALUE,
             produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Secured(ROLE_LAB)
    public ResponseEntity<?> updateQuickTestStatus(QuickTestUpdateRequest quickTestUpdateRequest) {
//        return quickTestService.updateQuickTest(quickTestUpdateRequest);
        return ResponseEntity.status(204).build();
    }
}

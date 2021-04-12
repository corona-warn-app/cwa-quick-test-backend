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
import app.coronawarn.quicktest.model.KeyCloakConfigFile;
import app.coronawarn.quicktest.model.QuickTestCreationRequest;
import app.coronawarn.quicktest.model.QuickTestPersonalDataRequest;
import app.coronawarn.quicktest.model.QuickTestUpdateRequest;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import app.coronawarn.quicktest.utils.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/quicktest")
@RequiredArgsConstructor
public class QuickTestCreationController {

    private final QuickTestService quickTestService;

    private final ModelMapper modelMapper;

    private final Utilities utilities;

    /**
     * Endpoint for creating new QuickTest entities.
     *
     * @param quicktestCreationRequest containing the hashed guid.
     * @return Empty Response
     */
    @Operation(
        summary = "Creates a quicktest",
        description = "Creates a quicktest and a pending testresult"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Quicktest is created"),
      @ApiResponse(responseCode = "409", description = "Quicktest with short hash already exists"),
      @ApiResponse(responseCode = "500", description = "Inserting failed because of internal error.")})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_COUNTER)
    public ResponseEntity<Void> createQuickTest(@Valid @RequestBody QuickTestCreationRequest quicktestCreationRequest) {
        try {
            quickTestService.createNewQuickTest(utilities.getIdsFromToken(),
              quicktestCreationRequest.getHashedGuid());
        } catch (QuickTestServiceException e) {
            if (e.getReason() == QuickTestServiceException.Reason.INSERT_CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Endpoint for updating a Quicktest result.
     *
     * @param quickTestUpdateRequest contains the the new test value for the quicktest.
     * @return ResponseEntity with binary data.
     */
    @Operation(
        summary = "Updates the test result of a quicktest",
        description = "Updates the test result of a quicktest"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "204 ", description = "Update successful"),
      @ApiResponse(responseCode = "404", description = "Short Hash doesn't exists")})
    @PutMapping(value = "/{shortHash}/testResult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<Void> updateQuickTestStatus(
            @PathVariable String shortHash,
            @Valid @RequestBody QuickTestUpdateRequest quickTestUpdateRequest) {
        try {
            quickTestService.updateQuickTest(
              utilities.getIdsFromToken(),
                    shortHash, quickTestUpdateRequest.getResult());
        } catch (QuickTestServiceException e) {
            if (e.getReason() == QuickTestServiceException.Reason.UPDATE_NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Endpoint for updating personal data for a quicktest.
     *
     * @param quickTestPersonalDataRequest contains the the personal data for the quicktest.
     * @return ResponseEntity with binary data.
     */
    @Operation(
            summary = "Updates the test result of a quicktest",
            description = "Updates the test result of a quicktest"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "204 ", description = "Update successful"),
      @ApiResponse(responseCode = "404", description = "Short Hash doesn't exists")})
    @PutMapping(value = "/{shortHash}/personalData", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_COUNTER)
    public ResponseEntity<Void> updateQuickTestWithPersonalData(
            @PathVariable String shortHash,
            @Valid @RequestBody QuickTestPersonalDataRequest quickTestPersonalDataRequest) {
        try {
            quickTestService.updateQuickTestWithPersonalData(
              utilities.getIdsFromToken(),shortHash,
                    modelMapper.map(quickTestPersonalDataRequest, QuickTest.class));
        } catch (QuickTestServiceException e) {
            if (e.getReason() == QuickTestServiceException.Reason.UPDATE_NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping(value = "/{shortHash}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuickTest> getQuickTest(@PathVariable String shortHash) {
        try {
            return ResponseEntity.ok(quickTestService.getQuickTest(utilities.getIdsFromToken().get("poc_id"),shortHash));
        } catch (QuickTestServiceException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

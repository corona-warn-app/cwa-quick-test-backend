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
import app.coronawarn.quicktest.model.quicktest.QuickTestCreationRequest;
import app.coronawarn.quicktest.model.quicktest.QuickTestDccConsent;
import app.coronawarn.quicktest.model.quicktest.QuickTestPersonalDataRequest;
import app.coronawarn.quicktest.model.quicktest.QuickTestResponse;
import app.coronawarn.quicktest.model.quicktest.QuickTestResponseList;
import app.coronawarn.quicktest.model.quicktest.QuickTestUpdateRequest;
import app.coronawarn.quicktest.repository.QuicktestView;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.utils.TestTypeUtils;
import app.coronawarn.quicktest.utils.Utilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(value = "/api/quicktest")
@RequiredArgsConstructor
public class QuickTestController {

    private final QuickTestService quickTestService;
    private final ModelMapper modelMapper;
    private final Utilities utilities;
    
    private final CancellationUtils cancellationUtils;

    /**
     * Endpoint for getting pending (registration with name, etc. completed) quicktests for poc and tenant.
     */
    @Operation(
      summary = "Get poc specific quicktests",
      description = "Returns all found (pending) quicktests containing personal data for a specific poc"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "403", description =
          "Cancellation already started, endpoint is not available anymore."),
      @ApiResponse(responseCode = "500", description = "Query failed due to an internal server error")
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<QuickTestResponseList> getQuickTestsForTenantIdAndPocId() {
        if (cancellationUtils.isCancellationStartedAndTestResultSubmittingDenied()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              "Cancellation already started, endpoint is not available anymore.");
        }
        try {
            List<QuickTestResponse> quickTests =
              quickTestService.findAllPendingQuickTestsByTenantIdAndPocId(utilities.getIdsFromToken())
                .stream()
                .map(this::mapViewToResponse)
                .collect(Collectors.toList());
            QuickTestResponseList response = new QuickTestResponseList();
            response.setQuickTests(quickTests);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.debug("Respondstatus error information getQuickTests: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to find pending quicktests");
            log.debug("Extended error information getQuickTests: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
      @ApiResponse(responseCode = "403", description =
          "Cancellation already started, endpoint is not available anymore."),
      @ApiResponse(responseCode = "409", description = "Quicktest with short hash already exists"),
      @ApiResponse(responseCode = "500", description = "Inserting failed because of internal error.")})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_COUNTER)
    public ResponseEntity<Void> createQuickTest(@Valid @RequestBody QuickTestCreationRequest quicktestCreationRequest) {
        if (cancellationUtils.isCancellationStarted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              "Cancellation already started, endpoint is not available anymore.");
        }
        try {
            quickTestService.createNewQuickTest(utilities.getIdsFromToken(),
              quicktestCreationRequest.getHashedGuid());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't execute createQuickTest.");
            log.debug("Extended error information createQuickTest: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Endpoint for deleting a Quicktest result.
     *
     * @param shortHash contains the the new test value for the quicktest.
     * @return ResponseEntity with status code.
     */
    @Operation(
      summary = "Deletes a quicktest",
      description = "Deletes a quicktest"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200 ", description = "Deletion successful."),
      @ApiResponse(responseCode = "403", description = "Deletion of updated Quicktests not allowed."),
      @ApiResponse(responseCode = "403", description =
          "Cancellation started more then 24 hours ago, endpoint is not available anymore."),
      @ApiResponse(responseCode = "500", description = "Updating failed because of internal error.")})
    @DeleteMapping(value = "/{shortHash}")
    @Secured(ROLE_COUNTER)
    public ResponseEntity<Void> deleteEmptyQuickTest(@Valid @PathVariable("shortHash") String shortHash) {
        if (cancellationUtils.isCancellationStartedAndTestResultSubmittingDenied()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              "Cancellation started more then 24 hours ago, endpoint is not available anymore.");
        }
        quickTestService.deleteQuicktest(utilities.getIdsFromToken(), shortHash, utilities.getUserNameFromToken());
        return ResponseEntity.status(HttpStatus.OK).build();
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
      @ApiResponse(responseCode = "403", description =
          "Cancellation started more then 24 hours ago, endpoint is not available anymore."),
      @ApiResponse(responseCode = "404", description = "Short Hash doesn't exists"),
      @ApiResponse(responseCode = "500", description = "Updating failed because of internal error.")})
    @PutMapping(value = "/{shortHash}/testResult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<Void> updateQuickTestStatus(
      @PathVariable("shortHash") String shortHash,
      @Valid @RequestBody QuickTestUpdateRequest quickTestUpdateRequest) {
        if (cancellationUtils.isCancellationStartedAndTestResultSubmittingDenied()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              "Cancellation started more then 24 hours ago, endpoint is not available anymore.");
        }
        try {
            quickTestService.updateQuickTest(utilities.getIdsFromToken(), shortHash, quickTestUpdateRequest,
              utilities.getPocInformationFromToken(), utilities.getUserNameFromToken()
            );
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't execute updateQuickTestStatus.");
            log.debug("Extended error information updateQuickTestStatus: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Endpoint for quering the dcc consent for pending tests.
     *
     * @param shortHash also called processId in front end.
     * @return ResponseEntity with dcc status.
     */
    @Operation(
      summary = "Updates the test result of a quicktest",
      description = "Updates the test result of a quicktest"
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "test found"),
      @ApiResponse(responseCode = "403", description =
          "Cancellation started more then 24 hours ago, endpoint is not available anymore."),
      @ApiResponse(responseCode = "404", description = "test not found"),
      @ApiResponse(responseCode = "500", description = "internal error.")})
    @GetMapping(value = "/{shortHash}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<QuickTestDccConsent> getDccConsent(
      @PathVariable("shortHash") String shortHash) {
        if (cancellationUtils.isCancellationStartedAndTestResultSubmittingDenied()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              "Cancellation started more then 24 hours ago, endpoint is not available anymore.");
        }
        try {
            return ResponseEntity.ok(quickTestService.getDccConsent(utilities.getIdsFromToken(), shortHash));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't execute updateQuickTestStatus.");
            log.debug("Extended error information getDccConsent: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
      @ApiResponse(responseCode = "403", description =
          "Cancellation already started, endpoint is not available anymore."),
      @ApiResponse(responseCode = "404", description = "Short Hash doesn't exists"),
      @ApiResponse(responseCode = "500", description = "Updating failed because of internal error.")})
    @PutMapping(value = "/{shortHash}/personalData", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_COUNTER)
    public ResponseEntity<Void> updateQuickTestWithPersonalData(
      @PathVariable("shortHash") String shortHash,
      @Valid @RequestBody QuickTestPersonalDataRequest quickTestPersonalDataRequest) {
        if (cancellationUtils.isCancellationStarted()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
              "Cancellation already started, endpoint is not available anymore.");
        }
        if (quickTestPersonalDataRequest.getConfirmationCwa()
            && quickTestPersonalDataRequest.getTestResultServerHash() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (TestTypeUtils.isPcr(quickTestPersonalDataRequest.getTestType()) && !utilities.checkPocNatPermission()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not allowed to create PoC NAT tests.");
        }
        try {
            quickTestService.updateQuickTestWithPersonalData(
              utilities.getIdsFromToken(), shortHash,
              modelMapper.map(quickTestPersonalDataRequest, QuickTest.class));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't execute updateQuickTestStatus.");
            log.debug("Extended error information updateQuickTestStatus: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private QuickTestResponse mapViewToResponse(QuicktestView quicktestView) {
        QuickTestResponse response = new QuickTestResponse();
        response.setShortHashedGuid(quicktestView.getShortHashedGuid());
        return response;
    }

}

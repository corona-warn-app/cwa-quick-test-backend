package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.model.QuickTestUpdateRequest;
import app.coronawarn.quicktest.model.QuicktestCreationRequest;
import app.coronawarn.quicktest.model.TestResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping(value = "/quicktest/")
@RequiredArgsConstructor
public class QuickTestCreationController {

  /**
   * Endpoint for creating a quicktest and a testresult.
   *
   * @param quicktestCreationRequest contains the data to set up a new test.
   * @return ResponseEntity with status code.
   */
  @Operation(
    summary = "Creates a quicktest",
    description = "Creates a quicktest and a pending testresult"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Quicktest is created"),
    @ApiResponse(responseCode = "500", description = "Failed to create quicktest")})
  @PostMapping(value = "/",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Resource> createQuickTest(QuicktestCreationRequest quicktestCreationRequest) {
    return null;
  }

  /**
   * Endpoint for updating a Quicktest result.
   *
   * @param quickTestUploadRequest contains the the new test value for the quicktest.
   * @return ResponseEntity with binary data.
   */
  @Operation(
    summary = "Updates the testresult of an quicktest",
    description = "Updates the testresult of an quicktest"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204 ", description = "Update successful"),
    @ApiResponse(responseCode = "500", description = "Failed to update testresult")})
  @PostMapping(value = "/",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Resource> updateQuickTestStatus(QuickTestUpdateRequest quickTestUploadRequest) {
    return null;
  }

  /**
   * Endpoint for downloading a log file.
   *
   * @param guid contains the guid to recive the testresult for.
   * @return ResponseEntity with the test result.
   */
  @Operation(
    summary = "Queries the testresult and personal data hash of an quicktest",80
    description = "Updates the testresult of an quicktest"
  )
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200 ", description = "Returns the Testresult"),
    @ApiResponse(responseCode = "500", description = "Failed to update testresult")})
  @GetMapping("/{guid}")
  public ResponseEntity<TestResult> validate(@PathVariable("guid") String guid) {
    return null;
  }
}

package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_TERMINATOR;

import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.model.cancellation.CancellationRequest;
import app.coronawarn.quicktest.service.CancellationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping(value = "/api/cancellation")
@RequiredArgsConstructor
public class CancellationController {


    private final CancellationService cancellationService;

    /**
     * Endpoint for receiving canceled PartnerIds.
     *
     * @return List of successfully reported PartnerIds
     */
    @Operation(
      summary = "Creates a cancellation entry for each given PartnerId",
      description = "Creates a cancellation entry for each given PartnerId and returns a list of them."
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful")
    })
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ROLE_TERMINATOR})
    public ResponseEntity<List<Cancellation>> createCancellations(@RequestBody CancellationRequest request) {
        List<Cancellation> cancellations = new ArrayList<>();
        for (String partnerId : request.getPartnerIds()) {
            cancellations.add(cancellationService.createCancellation(partnerId,request.getFinalDeletion()));
        }
        return ResponseEntity.ok(cancellations);
    }
}

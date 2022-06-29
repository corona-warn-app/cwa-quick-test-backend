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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequestMapping(value = "/api/cancellation")
@RequiredArgsConstructor
public class CancellationController {


    private final CancellationService cancellationService;

    private final UserManagementControllerUtils utils;

    /**
     * Endpoint for creating new cancellations.
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
            cancellations.add(cancellationService.createCancellation(partnerId, request.getFinalDeletion()));
        }
        return ResponseEntity.ok(cancellations);
    }

    /**
     * Endpoint for receiving information about cancellations.
     *
     * @return Cancellation information for users PartnerId
     */
    @Operation(
      summary = "Returns information about a cancellation",
      description = "Returns Information about a cancellation for the partnerId associated to the requesting user."
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful"),
      @ApiResponse(responseCode = "404", description = "No cancellation found for given PartnerId.")
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cancellation> getCancellation(KeycloakAuthenticationToken token) {
        GroupRepresentation groupRepresentation = utils.checkUserRootGroup();
        Optional<Cancellation> cancellation = cancellationService.getByPartnerId(groupRepresentation.getId());
        if (cancellation.isPresent()) {
            return ResponseEntity.ok(cancellation.get());
        } else {
            throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "No cancellation found for given PartnerId.");
        }
    }
}

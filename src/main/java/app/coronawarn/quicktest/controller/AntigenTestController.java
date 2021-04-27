package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.domain.AntigenTest;
import app.coronawarn.quicktest.service.AntigenTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;

@Slf4j
@RestController
@RequestMapping(value = "/api/antigentests")
@RequiredArgsConstructor
public class AntigenTestController {

    private final AntigenTestService antigenTestService;

    /**
     * Endpoint for receiving antigen tests.
     *
     * @return AntigenTests
     */
    @Operation(
            summary = "Response antigen tests as JSON array",
            description = ""
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "antigen tests found"),
      @ApiResponse(responseCode = "404", description = "antigen tests empty")})
    @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<List<AntigenTest>> getAntigenTests() {
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.LAST_MODIFIED, antigenTestService.getLastUpdate().toString())
                    .body(antigenTestService.antigenTests());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't prepare antigen tests response.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

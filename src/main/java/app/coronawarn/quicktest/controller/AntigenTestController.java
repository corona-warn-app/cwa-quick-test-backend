package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;

import app.coronawarn.quicktest.service.AntigenTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
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
    @RequestMapping(path = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured(ROLE_LAB)
    public ResponseEntity<List<List<String>>> getAntigenTests() {
        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.LAST_MODIFIED, antigenTestService.getLastUpdate().toString())
                    .body(antigenTestService.antigenTests());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Couldn't prepare stored pdf for download.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

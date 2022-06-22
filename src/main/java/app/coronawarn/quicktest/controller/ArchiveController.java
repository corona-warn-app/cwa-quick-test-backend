package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;

import app.coronawarn.quicktest.archive.domain.ArchiveCipherDtoV1;
import app.coronawarn.quicktest.service.ArchiveService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping(value = "/api/longtermarchive")
@RequiredArgsConstructor
public class ArchiveController {


    private final ArchiveService archiveService;

    /**
     * Endpoint for getting quicktests in longterm archive table by pocId.
     *
     * @return QuickTestArchiveListResponse with all found archives
     */
    @Operation(
            summary = "Find quicktests in longterm archive",
            description = "Returns all found quicktests in longterm archive for search parameters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful"),
            @ApiResponse(responseCode = "500", description = "Query failed because of an internal server error")
    })
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured({ROLE_COUNTER, ROLE_LAB})
    public ResponseEntity<List<ArchiveCipherDtoV1>> findLongtermArchiveByPocId(@RequestParam String pocId) {
        try {
            return ResponseEntity.ok(archiveService.getQuicktestsFromLongterm(pocId));
        } catch (JsonProcessingException e) {
            log.error("Couldn't parse DB entry.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

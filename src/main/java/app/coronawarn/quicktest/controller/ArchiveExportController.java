package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ARCHIVE_DOWNLOADER;

import app.coronawarn.quicktest.archive.domain.ArchiveCipherDtoV1;
import app.coronawarn.quicktest.service.ArchiveService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequestMapping(value = "/api/archive")
@RequiredArgsConstructor
public class ArchiveExportController {

    private final ArchiveService archiveService;

    /**
     * Endpoint for downloading archived entities.
     *
     * @return CSV with all archived data.
     */
    @Operation(
      summary = "Download Archive CSV-File",
      description = "Creates a CSV-File with all archived data for whole Partner or just one POC ID.",
      parameters = {
        @Parameter(
                in = ParameterIn.PATH,
                name = "partnerId",
                description = "Partner ID of the PArtner to download data of",
                required = true),
        @Parameter(
                 in = ParameterIn.QUERY,
                 name = "pocId",
                 description = "Filter for entities with given pocId")
      }
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful")
    })
    @GetMapping(value = "/{partnerId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Secured({ROLE_ARCHIVE_DOWNLOADER})
    public ResponseEntity<byte[]> exportArchive(
            @PathVariable("partnerId") String partnerId,
            @RequestParam(value = "pocId", required = false) String pocId
    ) {

        List<ArchiveCipherDtoV1> quicktests = archiveService.getQuicktestsFromLongterm(pocId, partnerId);
        try {
            byte[] csv = archiveService.createCsv(quicktests);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentLength(csv.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quicktest_export.csv")
                    .body(csv);

        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("Failed to create CSV: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create CSV.");
        }
    }
}

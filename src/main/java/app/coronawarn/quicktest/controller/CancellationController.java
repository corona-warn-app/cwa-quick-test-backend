package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_TERMINATOR;

import app.coronawarn.quicktest.config.CsvUploadConfig;
import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.model.cancellation.CancellationRequest;
import app.coronawarn.quicktest.service.CancellationService;
import app.coronawarn.quicktest.utils.Utilities;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final CsvUploadConfig s3Config;

    private final AmazonS3 s3Client;

    private final CancellationService cancellationService;

    private final Utilities utils;

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
            cancellations.add(cancellationService.createCancellation(
              partnerId, request.getCancellationDate().atZone(ZoneId.of("UTC"))));
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
    public ResponseEntity<Cancellation> getCancellation() {
        Optional<Cancellation> cancellation = cancellationService.getByPartnerId(utils.getTenantIdFromToken());

        if (cancellation.isPresent()) {
            return ResponseEntity.ok(cancellation.get());
        } else {
            throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "No cancellation found for given PartnerId.");
        }
    }

    /**
     * Endpoint for receiving download link of tenant.
     *
     * @return Download link of csv for tenant
     */
    @Operation(
      summary = "Returns Download link of csv for tenant",
      description = "Returns Download link of csv with all quicktests for tenant."
    )
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successful"),
      @ApiResponse(responseCode = "400", description = "Download Link for given PartnerId not yet available, "
        + "cancellation might not have been processed yet."),
      @ApiResponse(responseCode = "404", description = "No cancellation found for given PartnerId.")
    })
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<URL> getCsvLink() {
        Optional<Cancellation> cancellation = cancellationService.getByPartnerId(utils.getTenantIdFromToken());

        if (cancellation.isPresent() && ZonedDateTime.now().isAfter(cancellation.get().getCancellationDate())) {

            if (cancellation.get().getCsvCreated() != null && cancellation.get().getBucketObjectId() != null) {
                long expTimeMillis = Instant.now().toEpochMilli();
                expTimeMillis += s3Config.getExpiration();
                Date expiration = new Date();
                expiration.setTime(expTimeMillis);
                GeneratePresignedUrlRequest generatePresignedUrlRequest =
                  new GeneratePresignedUrlRequest(s3Config.getBucketName(), cancellation.get().getBucketObjectId())
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
                cancellationService.updateDownloadLinkRequested(
                  cancellation.get(), ZonedDateTime.now(), utils.getUserNameFromToken());
                return ResponseEntity.ok(s3Client.generatePresignedUrl(generatePresignedUrlRequest));
            } else {
                throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST,
                  "Download Link for given PartnerId not yet available, "
                    + "cancellation might not have been processed yet.");
            }
        } else {
            throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, "No cancellation found for given PartnerId.");
        }
    }
}

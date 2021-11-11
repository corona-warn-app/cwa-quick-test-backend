package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.DemisServerValuesConfig;
import app.coronawarn.quicktest.model.demis.DemisStatus;
import app.coronawarn.quicktest.model.demis.DemisToken;
import app.coronawarn.quicktest.model.demis.NotificationResponse;
import app.coronawarn.quicktest.model.demis.TokenEndpointResponse;
import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemisServerClientImpl implements DemisServerClient {

    private final DemisServerValuesConfig demisConfig;
    private final RestTemplate restTemplate;
    private final FhirContext context = FhirContext.forR4();

    private DemisToken token;
    private boolean authenticated;

    @PostConstruct
    private void initialize() {
        authenticated = authenticate();
    }

    private boolean authenticate() {
        if (demisConfig.isEnabled()) {
            MultiValueMap<String, String> auth = getAuth(
              this.token != null && this.token.isRefreshable()
                ? Optional.ofNullable(this.token.getRefreshToken()) : Optional.empty()
            );
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(auth, getHttpHeaders());

            try {
                log.info("Trying to authenticate at Demis");
                Instant now = Instant.now();
                ResponseEntity<TokenEndpointResponse> response = restTemplate.postForEntity(
                  demisConfig.getAuthUrl(), request, TokenEndpointResponse.class);
                log.debug("Got: {}", response);
                if (response.getBody() != null) {
                    final TokenEndpointResponse tokenEndpointResponse = response.getBody();
                    this.token = DemisToken.builder()
                      .accessToken(tokenEndpointResponse.getAccessToken())
                      .expiresAt(now.plus(tokenEndpointResponse.getExpiresIn(), ChronoUnit.SECONDS))
                      .refreshToken(tokenEndpointResponse.getRefreshToken())
                      .refreshTokenExpiresAt(now.plus(tokenEndpointResponse.getRefreshExpiresIn(), ChronoUnit.SECONDS))
                      .build();
                }
                return true;
            } catch (Exception e) {
                log.error("Could not authenticate at Demis: {} ", e.getLocalizedMessage());
            }
        }
        return false;
    }

    private MultiValueMap<String, String> getAuth(Optional<String> refreshToken) {
        refreshToken.ifPresentOrElse(
          token -> log.info("Trying to refresh token."),
          () -> log.info("Getting a new access token."));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", demisConfig.getClientId());
        map.add("client_secret", demisConfig.getClientSecret());
        map.add("username", demisConfig.getUsername());
        if (refreshToken.isEmpty()) {
            map.add("grant_type", "password");
        } else {
            map.add("grant_type", "refresh_token");
            map.add("refresh_token", refreshToken.get());
        }
        return map;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    @Override
    public NotificationResponse sendNotification(Bundle notification) {
        if (!this.authenticated || this.token.expiresSoon()) {
            this.authenticated = authenticate();
        }
        if (this.authenticated) {
            final Parameters parameters = new Parameters();
            parameters.addParameter().setName("content").setResource(notification);
            String paramJson = context.newJsonParser().encodeResourceToString(parameters);
            log.info("Sending: {}", paramJson);
            HttpEntity<String> request = createRequest(paramJson);
            ResponseEntity<String> response;
            try {
                response = this.restTemplate.postForEntity(
                  demisConfig.getFhirBasepath() + "$process-notification", request, String.class);
            } catch (HttpStatusCodeException ex) {
                response =
                  new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getResponseHeaders(), ex.getStatusCode());
            }

            return processResponse(response);
        } else {
            log.warn("Can not authenticate at Demis Server.");
            NotificationResponse unauthenticated = new NotificationResponse();
            unauthenticated.setStatus(DemisStatus.SENDING_FAILED);
            return unauthenticated;
        }

    }

    private HttpEntity<String> createRequest(String paramJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + this.token.getAccessToken());

        return new HttpEntity<>(paramJson, headers);
    }

    private NotificationResponse processResponse(ResponseEntity<String> response) {
        NotificationResponse notificationResponse = new NotificationResponse();
        HttpStatus statusCode = response.getStatusCode();
        if (statusCode.isError()) {
            log.error("Sending Demis notification failed with code: {}", statusCode);
            if (statusCode.is4xxClientError()) {
                notificationResponse.setStatus(DemisStatus.SENDING_FAILED);
            } else {
                notificationResponse.setStatus(DemisStatus.ZIP_NOT_SUPPORTED);
            }
            return notificationResponse;
        }

        IParser parser = context.newJsonParser();
        log.info("Demis response bundle: {}", response.getBody());
        Parameters parameters = parser.parseResource(Parameters.class, response.getBody());
        Parameters.ParametersParameterComponent component = DemisUtils.retrieveParameter(parameters, "bundle");
        if (component  == null) {
            notificationResponse.setStatus(DemisStatus.INVALID_RESPONSE_BODY);
        } else {
            notificationResponse.setResultBundle((Bundle) component.getResource());
            component = DemisUtils.retrieveParameter(parameters, "operationOutcome");
            if (component == null) {
                notificationResponse.setStatus(DemisStatus.INVALID_RESPONSE_BODY);
            } else {
                notificationResponse.setStatus(DemisStatus.OK);
                notificationResponse.setOperationOutcome((OperationOutcome) component.getResource());
            }
        }

        return notificationResponse;
    }
}

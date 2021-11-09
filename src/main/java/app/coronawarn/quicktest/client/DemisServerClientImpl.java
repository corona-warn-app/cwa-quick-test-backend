package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.DemisServerValuesConfig;
import app.coronawarn.quicktest.model.demis.DemisStatus;
import app.coronawarn.quicktest.model.demis.NotificationResponse;
import app.coronawarn.quicktest.model.demis.TokenEndpointResponse;
import app.coronawarn.quicktest.utils.DemisUtils;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.util.List;
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

    private String token;

    @PostConstruct
    private void initialize() {
        if (demisConfig.isEnabled()) {
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(getAuth(), getHttpHeaders());

            try {
                log.info("Trying to authenticate at Demis");
                ResponseEntity<TokenEndpointResponse> response = restTemplate.postForEntity(
                  demisConfig.getAuthUrl(), request, TokenEndpointResponse.class);
                log.debug("Got: {}", response);
                this.token = response.getBody().getAccessToken();
            } catch (Exception e) {
                log.error("Could not authenticate at Demis: {} ", e.getLocalizedMessage());
            }
        }
    }

    private MultiValueMap<String, String> getAuth() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", demisConfig.getClientId());
        map.add("client_secret", demisConfig.getClientSecret());
        map.add("username", demisConfig.getUsername());
        map.add("grant_type", "password");
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
        final Parameters parameters = new Parameters();
        parameters.addParameter().setName("content").setResource(notification);
        String paramJson = context.newJsonParser().encodeResourceToString(parameters);
        log.info("Sending: {}", paramJson);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + this.token);

        HttpEntity<String> request = new HttpEntity<>(paramJson, headers);
        ResponseEntity<String> response;
        try {
            response = this.restTemplate.postForEntity(
              demisConfig.getFhirBasepath() + "$process-notification", request, String.class);
        } catch (HttpStatusCodeException ex) {
            response = new ResponseEntity<>(ex.getResponseBodyAsString(), ex.getResponseHeaders(), ex.getStatusCode());
        }

        return processResponse(response);
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

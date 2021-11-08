package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.DemisServerValuesConfig;
import app.coronawarn.quicktest.model.demis.NotificationResponse;
import app.coronawarn.quicktest.model.demis.TokenEndpointResponse;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemisServerClientImpl implements DemisServerClient {

    private final DemisServerValuesConfig demisConfig;
    private final RestTemplate restTemplate;
    private final FhirContext context = FhirContext.forR4();
    private IGenericClient fhirClient;

    private String token;

    @PostConstruct
    private void initialize() {
        if (demisConfig.isEnabled()) {
            this.fhirClient = this.context.newRestfulGenericClient(demisConfig.getFhirBasepath());

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

        String s = this.restTemplate.postForObject(demisConfig.getFhirBasepath() + "$process-notification", request,
          String.class);
        // TODO Map to NotificationResponse
        IParser parser = context.newJsonParser();
        Parameters para = parser.parseResource(Parameters.class, s);
        return new NotificationResponse();
    }
}

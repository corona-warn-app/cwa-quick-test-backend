package app.coronawarn.quicktest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("demisserver")
public class DemisServerValuesConfig {

    private boolean enabled;
    private boolean hostnameVerify;
    private String keyStorePath;
    private char[] keyStorePassword;
    private String trustStorePath;
    private char[] trustStorePassword;
    private String authUrl;
    private String fhirBasepath;
    private String clientId;
    private String clientSecret;
    private String username;
}

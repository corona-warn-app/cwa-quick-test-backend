package app.coronawarn.quicktest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("dccserver")
public class DccServerValuesConfig {

    private boolean enabled;
    private boolean oneWay;
    private boolean twoWay;
    private boolean hostnameVerify;
    private String keyStorePath;
    private char[] keyStorePassword;
    private String trustStorePath;
    private char[] trustStorePassword;
}

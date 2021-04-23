package app.coronawarn.quicktest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("quicktest")
public class QuickTestConfig {

    private String pointOfCareIdName;
    private String tenantIdKey;
    private String tenantPointOfCareIdKey;

    private String pointOfCareInformationName;
    private String pointOfCareInformationDelimiter;

    private String dbEncryptionPassword;

}

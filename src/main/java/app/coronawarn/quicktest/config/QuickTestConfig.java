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

    private CleanUpSettings cleanUpSettings;
    private String pointOfCareIdName;
    private String tenantIdKey;
    private String tenantPointOfCareIdKey;
    private String pointOfCareInformationName;
    private String pointOfCareInformationDelimiter;
    private String pointOfCareZipcodeKey;
    private String dbEncryptionKey;
    private String labId;
    private String healthDepartmentDownloadUrl;
    private String healthDepartmentDownloadCron;

    @Getter
    @Setter
    public static class CleanUpSettings {
        private String cron;
        private int maxAgeInMinutes;
        private int locklimit;
    }
}

package app.coronawarn.quicktest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("email")
public class EmailConfig {

    private HealthDepartment healthDepartment;
    private TestedPerson testedPerson;

    @Getter
    @Setter
    public static class HealthDepartment {
        private boolean enabled = false;
        private String subject;
        private String text;
        private String attachmentFilename;
    }

    @Getter
    @Setter
    public static class TestedPerson {
        private boolean enabled = false;
        private String subject;
        private String text;
        private String attachmentFilename;
    }

}

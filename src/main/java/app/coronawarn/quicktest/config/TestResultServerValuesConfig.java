package app.coronawarn.quicktest.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties
public class TestResultServerValuesConfig {

    private TestResultServer testResultServer;
    /**
     * Configure the TestResultServerValues with build property values and return the configured parameters.
     */
    @Getter
    @Setter
    public static class TestResultServer {
        private boolean enabled = false;
        private boolean oneWay = false;
        private boolean twoWay = false;
        private boolean hostnameVerify = false;
        private String  keyStorePath = "";
        private char[]  keyStorePassword = new char[0];
        private String  trustStorePath = "";
        private char[]  trustStorePassword = new char[0];
    }

}

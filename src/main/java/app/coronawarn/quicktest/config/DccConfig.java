package app.coronawarn.quicktest.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("dcc")
public class DccConfig {
    private int algId;
    private String keyId;
    private String issuer;
    private String cwtIssuer;
    private String country;
    @DurationUnit(ChronoUnit.DAYS)
    @NotNull
    private Duration expired;
    private String dgciPrefix;

    private SearchPublicKeysJob searchPublicKeysJob;
    private UploadDccJob uploadDccJob;

    @Getter
    @Setter
    public static class SearchPublicKeysJob {
        private String fixedDelayString;
        private int lockLimit;
    }

    @Getter
    @Setter
    public static class UploadDccJob {
        private String fixedDelayString;
        private int lockLimit;
    }

}

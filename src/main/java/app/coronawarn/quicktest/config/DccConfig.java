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
    @DurationUnit(ChronoUnit.DAYS)
    @NotNull
    private Duration expired;
}

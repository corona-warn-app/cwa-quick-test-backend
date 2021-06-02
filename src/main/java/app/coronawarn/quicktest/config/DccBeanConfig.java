package app.coronawarn.quicktest.config;

import eu.europa.ec.dgc.DgcCryptedPublisher;
import eu.europa.ec.dgc.DgcGenerator;
import eu.europa.ec.dgc.DgciGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DccBeanConfig {
    private final DccConfig dccConfig;

    @Bean
    DgcGenerator dgcGenerator() {
        return new DgcGenerator();
    }

    @Bean
    DgcCryptedPublisher dgcCryptedPublisher() {
        return new DgcCryptedPublisher();
    }
}

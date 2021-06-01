package app.coronawarn.quicktest.config;

import eu.europa.ec.dgc.DgciGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DccBeanConfig {
    private final DccConfig dccConfig;

    @Bean
    DgciGenerator createDgciGenerator() {
        return new DgciGenerator(dccConfig.getDgciPrefix());
    }
}

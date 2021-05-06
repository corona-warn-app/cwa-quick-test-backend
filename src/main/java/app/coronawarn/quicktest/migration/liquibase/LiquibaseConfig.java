package app.coronawarn.quicktest.migration.liquibase;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.Getter;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Getter
@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
public class LiquibaseConfig {

    private DataSource dataSource;

    private LiquibaseProperties properties;

    private ResourceLoader resourceLoader;

    /**
     * Constructor for liquibase-config.
     */
    public LiquibaseConfig(DataSource dataSource, LiquibaseProperties properties, ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Create liquibasebean with BeanAwareSpringLiquibase, to get beans for repositories in migration-skript.
     *
     * @return springliquibase
     */
    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new BeanAwareSpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(this.properties.getChangeLog());
        liquibase.setContexts(this.properties.getContexts());
        liquibase.setDefaultSchema(this.properties.getDefaultSchema());
        liquibase.setDropFirst(this.properties.isDropFirst());
        liquibase.setShouldRun(this.properties.isEnabled());
        liquibase.setLabels(this.properties.getLabels());
        liquibase.setChangeLogParameters(this.properties.getParameters());
        liquibase.setRollbackFile(this.properties.getRollbackFile());
        return liquibase;
    }
}

/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.quicktest.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class LiquibaseConfig {

    @Bean(name = "masterLiquibaseProperties")
    @Primary
    @ConfigurationProperties(prefix = "jdbc.master.liquibase")
    public LiquibaseProperties masterLiquibaseProperties() {
        log.info("Creating master LiquibaseProperties");
        return new LiquibaseProperties();
    }

    @Bean(name = "archiveLiquibaseProperties")
    @ConfigurationProperties(prefix = "jdbc.archive.liquibase")
    public LiquibaseProperties archiveLiquibaseProperties() {
        log.info("Creating archive LiquibaseProperties");
        return new LiquibaseProperties();
    }

    @Bean(name = "masterLiquibase")
    @Autowired
    public SpringLiquibase masterLiquibase(@Qualifier("masterDataSource") final DataSource masterDataSource) {
        log.info("Creating master SpringLiquibase");
        return this.buildSpringLiquibase(masterDataSource, this.masterLiquibaseProperties());
    }

    @Bean(name = "archiveLiquibase")
    @Autowired
    public SpringLiquibase archiveLiquibase(@Qualifier("archiveDataSource") final DataSource archiveDataSource) {
        log.info("Creating archive SpringLiquibase");
        return this.buildSpringLiquibase(archiveDataSource, this.archiveLiquibaseProperties());
    }

    private SpringLiquibase buildSpringLiquibase(final DataSource dataSource, final LiquibaseProperties properties) {
        final SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setLabels(properties.getLabels());
        liquibase.setChangeLogParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        return liquibase;
    }
}

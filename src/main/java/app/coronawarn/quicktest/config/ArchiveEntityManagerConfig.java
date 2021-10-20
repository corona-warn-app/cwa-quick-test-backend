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

import app.coronawarn.quicktest.archive.repository.ArchiveRepository;
import java.util.Arrays;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Slf4j
@Configuration
public class ArchiveEntityManagerConfig {

    private static final String[] ENTITY_PACKAGES = { "app.coronawarn.quicktest.archive.domain" };

    private final DataSource archiveDataSource;

    private Environment env;

    @Autowired
    public ArchiveEntityManagerConfig(
            @Qualifier("archiveDataSource") final DataSource archiveDataSource,
            final Environment env) {
        this.archiveDataSource = archiveDataSource;
        this.env = env;
    }

    @Bean
    public ArchiveRepository archiveRepository() {
        log.info("Creating ArchiveRepository");
        return new ArchiveRepository(this.archiveEntityManager());
    }

    private EntityManager archiveEntityManager() {
        log.debug("Creating Archive EntityManager");
        return this.archiveJpaTransactionManager()
                .getEntityManagerFactory()
                .createEntityManager();
    }

    private JpaTransactionManager archiveJpaTransactionManager() {
        log.debug("Creating Archive JpaTransactionManager");
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(archiveEntityManagerFactoryBean().getObject());
        return transactionManager;
    }

    private LocalContainerEntityManagerFactoryBean archiveEntityManagerFactoryBean() {
        log.debug("Creating Archive LocalContainerEntityManagerFactoryBean");
        final LocalContainerEntityManagerFactoryBean emFactory = new LocalContainerEntityManagerFactoryBean();
        emFactory.setJpaVendorAdapter(this.vendorAdaptor());
        emFactory.setDataSource(this.archiveDataSource);
        emFactory.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        emFactory.setPackagesToScan(ENTITY_PACKAGES);
        emFactory.setJpaProperties(this.jpaHibernateProperties());
        emFactory.afterPropertiesSet();
        return emFactory;
    }

    private HibernateJpaVendorAdapter vendorAdaptor() {
        log.debug("Creating Archive HibernateJpaVendorAdapter");
        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        return vendorAdapter;
    }

    private Properties jpaHibernateProperties() {
        log.debug("Creating Archive Properties (JPA Hibernate)");
        final Properties properties = new Properties();

        final String dialect = env.getProperty("spring.jpa.properties.hibernate.dialect");
        if (dialect != null) {
            properties.put(AvailableSettings.DIALECT, dialect);
        }
        final String maxFetchDepth = env.getProperty("spring.jpa.properties.hibernate.max_fetch_depth");
        if (maxFetchDepth != null) {
            properties.put(AvailableSettings.MAX_FETCH_DEPTH, maxFetchDepth);
        }
        final String fetchSize = env.getProperty("spring.jpa.properties.hibernate.jdbc.fetch_size");
        if (fetchSize != null) {
            properties.put(AvailableSettings.STATEMENT_FETCH_SIZE, fetchSize);
        }
        final String batchSize = env.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size");
        if (batchSize != null) {
            properties.put(AvailableSettings.STATEMENT_BATCH_SIZE, batchSize);
        }
        final String showSql = env.getProperty("spring.jpa.properties.hibernate.show_sql");
        if (showSql != null) {
            properties.put(AvailableSettings.SHOW_SQL, showSql);
        }

        // disable validation for testing (H2 DB)
        if (Arrays.stream(this.env.getActiveProfiles()).anyMatch(profile -> (!profile.equalsIgnoreCase("test")))) {
            properties.put(AvailableSettings.HBM2DDL_DATABASE_ACTION,
                    env.getProperty("spring.jpa.hibernate.ddl-auto", "none"));
        }

        return properties;
    }
}

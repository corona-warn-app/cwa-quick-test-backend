/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
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
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Slf4j
@Configuration
public class ArchiveEntityManagerConfig {

    private static final String[] ENTITY_PACKAGES = { "app.coronawarn.quicktest.archive.domain" };

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private final DataSource archiveDataSource;

    private final JpaHibernateProperties jpaHibernateProperties;

    @Autowired
    public ArchiveEntityManagerConfig(
            @Qualifier("archiveDataSource") final DataSource archiveDataSource,
            final JpaHibernateProperties jpaHibernateProperties) {
        this.archiveDataSource = archiveDataSource;
        this.jpaHibernateProperties = jpaHibernateProperties;
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
        emFactory.setJpaProperties(this.buildJpaHibernateProperties());
        emFactory.afterPropertiesSet();
        return emFactory;
    }

    private HibernateJpaVendorAdapter vendorAdaptor() {
        log.debug("Creating Archive HibernateJpaVendorAdapter");
        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        return vendorAdapter;
    }

    private Properties buildJpaHibernateProperties() {
        log.debug("Creating Archive Properties (JPA Hibernate)");
        final Properties properties = new Properties();
        if (this.jpaHibernateProperties.getDialect() != null) {
            properties.put(AvailableSettings.DIALECT, jpaHibernateProperties.getDialect());
        }
        if (this.jpaHibernateProperties.getMaxFetchDepth() != null) {
            properties.put(AvailableSettings.MAX_FETCH_DEPTH, this.jpaHibernateProperties.getMaxFetchDepth());
        }
        if (this.jpaHibernateProperties.getJdbc().getFetchSize() != null) {
            properties.put(AvailableSettings.STATEMENT_FETCH_SIZE,
                    this.jpaHibernateProperties.getJdbc().getFetchSize());
        }
        if (this.jpaHibernateProperties.getJdbc().getBatchSize() != null) {
            properties.put(AvailableSettings.STATEMENT_BATCH_SIZE,
                    this.jpaHibernateProperties.getJdbc().getBatchSize());
        }
        if (this.jpaHibernateProperties.getShowSql() != null) {
            properties.put(AvailableSettings.SHOW_SQL, this.jpaHibernateProperties.getShowSql());
        }
        // disable validation for testing (H2 DB)
        if (this.activeProfile.contains("test")) {
            properties.put(AvailableSettings.HBM2DDL_DATABASE_ACTION, "none");
        } else {
            properties.put(AvailableSettings.HBM2DDL_DATABASE_ACTION, jpaHibernateProperties.getDdlAuto());
        }
        return properties;
    }
}

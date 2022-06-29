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

import app.coronawarn.quicktest.datasource.DataSourceType;
import app.coronawarn.quicktest.datasource.TransactionRoutingDatasource;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Configuration
@EnableTransactionManagement
public class TransactionRoutingConfiguration {

    @Value("${jdbc.min-pool}")
    private int minPool;

    @Value("${jdbc.max-pool}")
    private int maxPool;

    @Value("${jdbc.master.url}")
    private String masterUrl;

    @Value("${jdbc.master.driver-class-name}")
    private String masterDriverClassName;

    @Value("${jdbc.replica.url}")
    private String replicaUrl;

    @Value("${jdbc.replica.driver-class-name}")
    private String replicaDriverClassName;
    
    @Value("${jdbc.archive.url}")
    private String archiveUrl;

    @Value("${jdbc.archive.driver-class-name}")
    private String archiveDriverClassName;

    /**
     * Routing Datasource between read-only replica and read-write master.
     */
    @Bean
    @Primary
    public TransactionRoutingDatasource dataSource() {
        log.info("Creating TransactionRoutingDatasource");
        TransactionRoutingDatasource routingDatasource = new TransactionRoutingDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.READ_ONLY, replicaDataSource());
        targetDataSources.put(DataSourceType.READ_WRITE, masterDataSource());
        routingDatasource.setTargetDataSources(targetDataSources);

        routingDatasource.setDefaultTargetDataSource(masterDataSource());
        return routingDatasource;
    }

    /**
     * Replication datasource.
     * @return replica
     */
    public DataSource replicaDataSource() {

        log.info("Creating Replica Datasource");
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(replicaDriverClassName);
        hikariDataSource.setJdbcUrl(replicaUrl);
        hikariDataSource.setMaximumPoolSize(maxPool);
        hikariDataSource.setMinimumIdle(minPool);
        return hikariDataSource;
    }

    /**
     * Master datasource.
     * @return master
     */
    @Bean(name = "masterDataSource", destroyMethod = "close")
    public DataSource masterDataSource() {

        log.info("Creating Master Datasource");
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(masterDriverClassName);
        hikariDataSource.setJdbcUrl(masterUrl);
        hikariDataSource.setMaximumPoolSize(maxPool);
        hikariDataSource.setMinimumIdle(minPool);
        return hikariDataSource;
    }
    
    /**
     * Archive datassource.
     * 
     * @return archive
     */
    @Bean(name = "archiveDataSource", destroyMethod = "close")
    public DataSource archiveDataSource() {
        log.info("Creating Archive Datasource");
        final HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(this.archiveDriverClassName);
        hikariDataSource.setJdbcUrl(this.archiveUrl);
        hikariDataSource.setMaximumPoolSize(this.maxPool);
        hikariDataSource.setMinimumIdle(this.minPool);
        return hikariDataSource;
    }
}

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TransactionRoutingConfiguration {

    @Value("${jdbc.master.url}")
    private String masterUrl;

    @Value("${jdbc.master.driver-class-name}")
    private String masterDriverClassName;

    @Value("${jdbc.replica.url}")
    private String replicaUrl;

    @Value("${jdbc.replica.driver-class-name}")
    private String replicaDriverClassName;


    /**
     * Routing Datasource between read-only replica and read-write master.
     */
    @Bean
    public DataSource dataSource() {
        TransactionRoutingDatasource routingDatasource = new TransactionRoutingDatasource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceType.READ_WRITE, masterDataSource());
        targetDataSources.put(DataSourceType.READ_ONLY, replicaDataSource());
        routingDatasource.setTargetDataSources(targetDataSources);

        routingDatasource.setDefaultTargetDataSource(masterDataSource());
        return routingDatasource;
    }

    /**
     * Replication datasource.
     * @return replica
     */
    public DataSource replicaDataSource() {

        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(replicaDriverClassName);
        hikariDataSource.setJdbcUrl(replicaUrl);
        return hikariDataSource;
    }

    /**
     * Master datasource.
     * @return master
     */
    public DataSource masterDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName(masterDriverClassName);
        hikariDataSource.setJdbcUrl(masterUrl);
        return hikariDataSource;
    }
}

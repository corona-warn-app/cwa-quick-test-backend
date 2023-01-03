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

package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.DccServerValuesConfig;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;
import org.springframework.web.server.ResponseStatusException;


@Configuration
@RequiredArgsConstructor
@Slf4j
public class DccServerClientConfig {

    private final DccServerValuesConfig config;

    /**
     * HttpClient for connection to Test-Result-Server.
     *
     * @return Instance of HttpClient
     */
    @Bean
    public Client dccClient() {
        if (config.isEnabled()) {
            return new ApacheHttpClient(
                    HttpClientBuilder
                            .create()
                            .setSSLContext(getSslContext())
                            .setSSLHostnameVerifier(getSslHostnameVerifier())
                            .build()
            );
        }
        return new ApacheHttpClient(HttpClientBuilder.create()
                .setSSLHostnameVerifier(getSslHostnameVerifier())
                .build());
    }

    private SSLContext getSslContext() {
        try {
            SSLContextBuilder builder = SSLContextBuilder
                    .create();
            if (config.isOneWay()) {
                builder.loadTrustMaterial(ResourceUtils.getFile(config.getTrustStorePath()),
                        config.getTrustStorePassword());
            }
            if (config.isTwoWay()) {
                builder.loadKeyMaterial(ResourceUtils.getFile(config.getKeyStorePath()),
                        config.getKeyStorePassword(),
                        config.getKeyStorePassword());
            }
            return builder.build();
        } catch (IOException | GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The SSL context could not be loaded.");
        }
    }

    private HostnameVerifier getSslHostnameVerifier() {
        return config.isHostnameVerify() ? new DefaultHostnameVerifier() : new NoopHostnameVerifier();
    }
}

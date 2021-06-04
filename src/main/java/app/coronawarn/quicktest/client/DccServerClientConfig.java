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

package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.DccApplicationConfig;
import app.coronawarn.quicktest.exception.DccException;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;





@Configuration
@RequiredArgsConstructor
@Slf4j
public class DccServerClientConfig {

    private final DccApplicationConfig config;

    /**
     * Configure the client depending on the ssl properties.
     *
     * @return an Apache Http Client with or without SSL features
     */
    @Bean
    public Client signingApiClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        if (config.getSigningApiServer().isEnableMtls()) {
            httpClientBuilder.setSSLContext(getSslContext());
            httpClientBuilder.setSSLHostnameVerifier(getSslHostnameVerifier());
        }

        if (config.getSigningApiServer().getProxy().isEnabled()) {
            httpClientBuilder.setProxy(new HttpHost(
                    config.getSigningApiServer().getProxy().getHost(),
                    config.getSigningApiServer().getProxy().getPort()
            ));
        }

        if (config.getSigningApiServer().getApiKey() != null) {
            httpClientBuilder.setDefaultHeaders(Collections.singletonList(
                    new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getSigningApiServer().getApiKey())
            ));
        }

        return new ApacheHttpClient(httpClientBuilder.build());
    }

    private SSLContext getSslContext() {
        try {
            SSLContextBuilder builder = SSLContextBuilder
                    .create();

            builder.loadTrustMaterial(ResourceUtils.getFile(config.getSigningApiServer().getTrustStorePath()),
                    config.getSigningApiServer().getTrustStorePassword());


            builder.loadKeyMaterial(ResourceUtils.getFile(config.getSigningApiServer().getKeyStorePath()),
                    config.getSigningApiServer().getKeyStorePassword(),
                    config.getSigningApiServer().getKeyStorePassword());

            return builder.build();
        } catch (IOException | GeneralSecurityException e) {
            log.error("The SSL context for Signing Server could not be loaded. Exception: {} {}",
                    e.getClass().getSimpleName(), e.getMessage());

            throw new DccException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "The SSL context for Signing Server could not be loaded.");
        }
    }

    private HostnameVerifier getSslHostnameVerifier() {
        return config.getSigningApiServer().isVerifyHostnames()
                ? new DefaultHostnameVerifier() : new NoopHostnameVerifier();
    }

}

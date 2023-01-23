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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class S3StorageClientConfig {

    private final CsvUploadConfig s3Config;

    /**
     * Creates a Bean for accessing S3 storage depending on application configuration.
     *
     * @return Preconfigured AmazonS3 instance.
     */
    @Bean
    public AmazonS3 getStorage() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setSignerOverride("AWSS3V4SignerType");

        if (s3Config.getProxy().getEnabled()) {
            log.info("Setting proxy for S3 connection.");
            clientConfig.setProxyHost(s3Config.getProxy().getHost());
            clientConfig.setProxyPort(s3Config.getProxy().getPort());
        }

        AWSCredentials credentials = new BasicAWSCredentials(s3Config.getAccessKey(), s3Config.getSecretKey());

        return AmazonS3ClientBuilder.standard()
          .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
            s3Config.getRegion().getEndpoint(), s3Config.getRegion().getName()))
          .withPathStyleAccessEnabled(true)
          .withClientConfiguration(clientConfig)
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .build();
    }
}

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

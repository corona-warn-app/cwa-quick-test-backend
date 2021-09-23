package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.MapServerValuesConfig;
import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
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
public class QuicktestMapClientConfig {
    private final MapServerValuesConfig config;

    /**
     * HttpClient for connection to Map-Server.
     *
     * @return Instance of HttpClient
     */
    @Bean
    public Client mapClient() {
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
                .setProxy(new HttpHost("sia-lb.telekom.de",8080))
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

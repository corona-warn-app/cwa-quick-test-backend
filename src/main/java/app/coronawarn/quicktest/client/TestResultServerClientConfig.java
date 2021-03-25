package app.coronawarn.quicktest.client;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
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
public class TestResultServerClientConfig {

    //@Value("${cwa-testresult-server.ssl.enabled}")
    private final boolean enabled = false;
    //@Value("${cwa-testresult-server.ssl.one-way}")
    private final boolean oneWay = false;
    //@Value("${cwa-testresult-server.ssl.two-way}")
    private final boolean twoWay = false;
    //@Value("${cwa-testresult-server.ssl.hostname-verify}")
    private final boolean hostnameVerify = false;
    //@Value("${cwa-testresult-server.ssl.key-store}")
    private final String keyStorePath = "";
    //@Value("${cwa-testresult-server.ssl.key-store-password}")
    private final char[] keyStorePassword = new char[0];
    //@Value("${cwa-testresult-server.ssl.trust-store}")
    private final String trustStorePath = "";
    //@Value("${cwa-testresult-server.ssl.trust-store-password}")
    private final char[] trustStorePassword = new char[0];

    /**
     * Configure the client dependent on the ssl properties.
     *
     * @return an Apache Http Client with or without SSL features
     */
    @Bean
    public Client client() {
        if (enabled) {
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
            if (oneWay) {
                builder.loadTrustMaterial(ResourceUtils.getFile(trustStorePath),
                    trustStorePassword);
            }
            if (twoWay) {
                builder.loadKeyMaterial(ResourceUtils.getFile(keyStorePath),
                    keyStorePassword,
                    keyStorePassword);
            }
            return builder.build();
        } catch (IOException | GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "The SSL context could not be loaded.");
        }
    }

    private HostnameVerifier getSslHostnameVerifier() {
        return hostnameVerify ? new DefaultHostnameVerifier() : new NoopHostnameVerifier();
    }

}

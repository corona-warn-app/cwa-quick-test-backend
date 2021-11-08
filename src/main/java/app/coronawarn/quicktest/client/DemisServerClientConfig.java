package app.coronawarn.quicktest.client;

import app.coronawarn.quicktest.config.DemisServerValuesConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DemisServerClientConfig {

    private final DemisServerValuesConfig config;

    /**
     * RestTemplate used for calls to the Demis backend.
     * @return ssl enabled RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
          .requestFactory(this::clientHttpRequestFactory)
          .build();
    }

    /**
     * Apache HttpClient used for communication with Demis.
     * @return client
     */
    @Bean
    public HttpClient demisServerClient() {
        if (config.isEnabled()) {
            return HttpClientBuilder
              .create()
              .setSSLContext(getSslContext())
              .setSSLHostnameVerifier(getSslHostnameVerifier())
              .build();
        }
        return HttpClientBuilder.create()
          .setSSLHostnameVerifier(getSslHostnameVerifier())
          .build();
    }

    private SSLContext getSslContext() {
        try {
            SSLContextBuilder builder = SSLContextBuilder.create();

            builder.loadTrustMaterial(initializeTrustStore(config.getTrustStorePath(), config.getTrustStorePassword()),
                null);
            builder.setProtocol("TLSv1.2");


            builder.loadKeyMaterial(initializeKeyStore(config.getKeyStorePath(), config.getKeyStorePassword()),
                config.getKeyStorePassword());

            return builder.build();
        } catch (IOException | GeneralSecurityException e) {
            log.error("The SSL context for Demis Server could not be loaded. Exception: {} {}",
              e.getClass().getSimpleName(), e.getMessage());

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "The SSL context for Demis Server could not be loaded.");
        }
    }

    private HostnameVerifier getSslHostnameVerifier() {
        return config.isHostnameVerify() ? new DefaultHostnameVerifier() : new NoopHostnameVerifier();
    }

    /**
     * HttpClient factory.
     * @return the factory
     */
    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(demisServerClient());
        return clientHttpRequestFactory;
    }

    private KeyStore initializeKeyStore(final String path, final char[] pass) throws KeyStoreException, IOException,
      NoSuchAlgorithmException, CertificateException {
        try {
            final InputStream keyStoreIs = new FileInputStream(ResourceUtils.getFile(path));
            final KeyStore ksJks = KeyStore.getInstance("jks");
            ksJks.load(keyStoreIs, pass);
            return ksJks;
        } catch (Exception ex) {
            throw ex;
        }
    }

    private KeyStore initializeTrustStore(final String path, final char[] pass) throws KeyStoreException, IOException,
      NoSuchAlgorithmException, CertificateException {
        try {
            final InputStream trustStoreIs = new FileInputStream(ResourceUtils.getFile(path));
            final KeyStore tsJks = KeyStore.getInstance("jks");
            tsJks.load(trustStoreIs, pass);
            return tsJks;
        } catch (Exception ex) {
            throw ex;
        }
    }
}

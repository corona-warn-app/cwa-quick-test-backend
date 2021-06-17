package app.coronawarn.quicktest.client;

import feign.Client;
import feign.httpclient.ApacheHttpClient;
import lombok.RequiredArgsConstructor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RkiToolClientConfig {

    /**
     * HttpClient for connection to Rki Server.
     *
     * @return Instance of HttpClient
     */
    @Bean(name = "RkiHttpClient")
    public Client client() {
        return new ApacheHttpClient(HttpClientBuilder.create()
          .build());
    }
}

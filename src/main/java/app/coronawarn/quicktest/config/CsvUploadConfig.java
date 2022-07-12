package app.coronawarn.quicktest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("s3")
@Data
public class CsvUploadConfig {
    private String accessKey;
    private String secretKey;
    private String bucketName;

    private Region region;

    private ProxyConfig proxy;

    @Data
    public static class Region {
        private String name = "";
        private String endpoint;
    }

    @Data
    public static class ProxyConfig {
        private Boolean enabled = Boolean.FALSE;
        private String host;
        private Integer port;
    }
}

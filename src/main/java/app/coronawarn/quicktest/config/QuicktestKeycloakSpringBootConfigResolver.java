package app.coronawarn.quicktest.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.SneakyThrows;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuicktestKeycloakSpringBootConfigResolver extends KeycloakSpringBootConfigResolver {
    private final AdapterConfig adapterConfig;

    public QuicktestKeycloakSpringBootConfigResolver(KeycloakSpringBootProperties properties) {
        this.adapterConfig = properties;
    }

    @SneakyThrows
    @Override
    public KeycloakDeployment resolve(HttpFacade.Request request) {
        AdapterConfig tenantConfig = this.adapterConfig;
        JSONObject jwtBodyAsJson = null;
        String realm = null;
        if (
            request.getHeader("Authorization") != null
                && request.getHeader("Authorization").split("Bearer ").length > 1
                && request.getHeader("Authorization").split("Bearer ")[1].split("\\.").length > 1
        ) {
            //Remove Bearer and split in three parts => take the second with the body information
            String jwtBody = request.getHeader("Authorization").split("Bearer ")[1].split("\\.")[1];
            //Decode and convert in Json
            jwtBodyAsJson = new JSONObject(new String(Base64.getDecoder().decode(jwtBody),
                StandardCharsets.UTF_8));
        }
        if (
            jwtBodyAsJson != null
                && jwtBodyAsJson.get("iss") != null
                && jwtBodyAsJson.get("iss").toString().split("/").length > 0) {
            //get issuerUri from body and split url by /
            String[] issuerUriElements = jwtBodyAsJson.get("iss").toString().split("/");
            //get last element from issuerUriElements => realm name
            realm = issuerUriElements[issuerUriElements.length - 1];
        }
        if (realm != null) {
            tenantConfig.setRealm(realm);
        }
        return KeycloakDeploymentBuilder.build(tenantConfig);
    }

}

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuicktestKeycloakSpringBootConfigResolver extends KeycloakSpringBootConfigResolver {

    private final AdapterConfig adapterConfig;

    private final Map<String, KeycloakDeployment> deploymentMap = new HashMap<>();
    private final KeycloakDeployment defaultDeployment;

    public QuicktestKeycloakSpringBootConfigResolver(KeycloakSpringBootProperties properties) {
        this.adapterConfig = properties;
        this.defaultDeployment = KeycloakDeploymentBuilder.build(properties);
    }

    @SneakyThrows
    @Override
    public KeycloakDeployment resolve(HttpFacade.Request request) {
        ObjectMapper objectMapper = new ObjectMapper();
        AdapterConfig tenantConfig = this.adapterConfig;
        JsonNode jwtBodyAsJson = null;
        String realm = null;

        if (
            request.getHeader("Authorization") != null
                && request.getHeader("Authorization").split("Bearer ").length > 1
                && request.getHeader("Authorization").split("Bearer ")[1].split("\\.").length > 1
        ) {
            //Remove Bearer and split in three parts => take the second with the body information
            String jwtBody = request.getHeader("Authorization").split("Bearer ")[1].split("\\.")[1];
            //Decode and convert in Json
            jwtBodyAsJson = objectMapper.readTree((new String(Base64.getUrlDecoder().decode(jwtBody),
                StandardCharsets.UTF_8)));
        }

        if (
            jwtBodyAsJson != null
                && jwtBodyAsJson.get("iss") != null
                && jwtBodyAsJson.get("iss").toString().split("/").length > 0) {
            //get issuerUri from body and split url by /
            String[] issuerUriElements = jwtBodyAsJson.get("iss").toString().split("/");
            //get last element from issuerUriElements => realm name
            realm = StringUtils.strip(issuerUriElements[issuerUriElements.length - 1], "\"");
        }

        if (realm == null) {
            return defaultDeployment;
        } else {
            return deploymentMap.computeIfAbsent(realm, r -> {
                tenantConfig.setRealm(r);
                return KeycloakDeploymentBuilder.build(tenantConfig);
            });
        }
    }

}

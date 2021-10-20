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

package app.coronawarn.quicktest.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.keycloak.KeyCloakConfigFile;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackageClasses = {QuickTestConfig.class, KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
@RequiredArgsConstructor
class ConfigControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    @Autowired
    private QuickTestConfig quickTestConfig;

    @Test
    void getKeyCloakConfig() throws Exception {

        MvcResult mvcResult = mockMvc().perform(MockMvcRequestBuilders
            .get("/api/config/keycloak.json")
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk()).andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        responseBody = responseBody.replace("auth-server-url", "authServerUrl");
        responseBody = responseBody.replace("ssl-required", "sslRequired");
        responseBody = responseBody.replace("public-client", "publicClient");
        KeyCloakConfigFile response = new Gson().fromJson(responseBody, KeyCloakConfigFile.class);
        assertNotNull(response.getAuthServerUrl());
        assertNotNull(response.getResource());
        assertNotNull(response.getSslRequired());
        assertTrue(response.isPublicClient());
    }

    @Test
    void getContextConfig() throws Exception {

        final String dummyUrl = "https://example.org";

        quickTestConfig.getFrontendContextConfig().setRulesServerUrl(dummyUrl);

        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/config/context.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rules-server-url").value(dummyUrl));
    }
}

package app.coronawarn.quicktest.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.KeyCloakConfigFile;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@WebMvcTest(KeyCloakConfigController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class KeyCloakConfigControllerTest extends ServletKeycloakAuthUnitTestingSupport {

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
}

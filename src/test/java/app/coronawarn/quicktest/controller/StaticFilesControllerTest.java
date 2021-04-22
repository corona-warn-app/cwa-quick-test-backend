package app.coronawarn.quicktest.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StaticFilesController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class StaticFilesControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    @Test
    void redirect() throws Exception {
        mockMvc().perform(MockMvcRequestBuilders
            .get("/test1234/test1234")
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk()).andExpect(forwardedUrl("/"));
    }
}

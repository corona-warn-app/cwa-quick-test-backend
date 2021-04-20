package app.coronawarn.quicktest.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(QuickTestCreationController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class QuickTestCreationControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    @MockBean
    private QuickTestService quickTestService;
    @MockBean
    private ModelMapper modelMapper;
    @MockBean
    private Utilities utilities;

    @Test
    void createQuickTest() {
        try {
            when(quickTestService.createNewQuickTest( any(), any()));

        } catch (QuickTestServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    void updateQuickTestStatus() {
    }

    @Test
    void updateQuickTestWithPersonalData() {
    }
}

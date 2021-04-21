package app.coronawarn.quicktest.controller;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.model.QuickTestCreationRequest;
import app.coronawarn.quicktest.model.QuickTestStatisticsResponse;
import app.coronawarn.quicktest.service.QuickTestArchiveService;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import javassist.bytecode.ByteArray;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@WebMvcTest(QuickTestArchiveController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class QuickTestArchiveControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    @MockBean
    private QuickTestArchiveService quickTestArchiveService;

    @Test
    void createQuickTestArchive() throws Exception {
        String output = "test output";
        when(quickTestArchiveService.getPdf(any())).thenReturn(output.getBytes());

        MvcResult result = mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4/pdf")
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk()).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        Assertions.assertThat(responseBody)
            .isEqualTo(output);

    }

    @Test
    void findArchivesByTestResultAndUpdatedAtBetween() {
    }
}

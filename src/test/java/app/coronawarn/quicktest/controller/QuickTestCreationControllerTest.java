package app.coronawarn.quicktest.controller;


import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;
import static org.h2.util.IntIntHashMap.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.QuickTestCreationRequest;
import app.coronawarn.quicktest.model.QuickTestUpdateRequest;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.service.QuickTestServiceException;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

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
    void createQuickTest() throws Exception {
        QuickTestCreationRequest quicktestCreationRequest = new QuickTestCreationRequest();
        quicktestCreationRequest.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");

        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .post("/api/quicktest/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isCreated());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .post("/api/quicktest/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isForbidden());

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .post("/api/quicktest/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isForbidden());

        doThrow(new QuickTestServiceException(QuickTestServiceException.Reason.INSERT_CONFLICT))
            .when(quickTestService).createNewQuickTest(any(), any());
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .post("/api/quicktest/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isConflict())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("409 CONFLICT \"Quicktest with short hash already exists\"",
                result.getResolvedException().getMessage()));

        doThrow(new QuickTestServiceException(QuickTestServiceException.Reason.SAVE_FAILED))
            .when(quickTestService).createNewQuickTest(any(), any());
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .post("/api/quicktest/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("500 INTERNAL_SERVER_ERROR \"SAVE_FAILED\"",
                result.getResolvedException().getMessage()));

        quicktestCreationRequest.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c");

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .post("/api/quicktest/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isBadRequest());

        quicktestCreationRequest.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c55");

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .post("/api/quicktest/")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateQuickTestStatus() throws Exception {
        QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
        quickTestUpdateRequest.setTestBrandId("brandId");
        quickTestUpdateRequest.setTestBrandName("brandName");

        for(short result =0; result <= 10; result++){
            quickTestUpdateRequest.setResult((short) result);

            if(result>5 && result < 9) {
                mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                    .put("/api/quicktest/6fa4dcec/testResult")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(new Gson().toJson(quickTestUpdateRequest)))
                    .andExpect(status().isNoContent());
            }
            else{
                mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                    .put("/api/quicktest/6fa4dcec/testResult")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(new Gson().toJson(quickTestUpdateRequest)))
                    .andExpect(status().isBadRequest());
            }
        }
        quickTestUpdateRequest.setResult((short) 6);

        quickTestUpdateRequest.setTestBrandName(null);
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName("1");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName("");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName(
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName(
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isBadRequest());

        quickTestUpdateRequest.setTestBrandId(null);
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isBadRequest());


        quickTestUpdateRequest.setResult((short) 6);
        quickTestUpdateRequest.setTestBrandId("brandId");
        quickTestUpdateRequest.setTestBrandName(null);
        doThrow(new QuickTestServiceException(QuickTestServiceException.Reason.UPDATE_NOT_FOUND))
            .when(quickTestService).updateQuickTest(any(),any(),anyShort(),any(),any(),any(),any());
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("404 NOT_FOUND \"Short Hash doesn't exists\"",
                result.getResolvedException().getMessage()));

        doThrow(new QuickTestServiceException(QuickTestServiceException.Reason.INSERT_CONFLICT))
            .when(quickTestService).updateQuickTest(any(),any(),anyShort(),any(),any(),any(),any());
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put("/api/quicktest/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("500 INTERNAL_SERVER_ERROR \"INSERT_CONFLICT\"",
                result.getResolvedException().getMessage()));

    }

    @Test
    void updateQuickTestWithPersonalData() {
    }
}

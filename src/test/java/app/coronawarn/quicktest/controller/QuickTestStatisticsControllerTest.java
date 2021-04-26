package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.QuickTestStatistics;
import app.coronawarn.quicktest.model.QuickTestStatisticsResponse;
import app.coronawarn.quicktest.service.QuickTestStatisticsService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(QuickTestStatisticsController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class QuickTestStatisticsControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    @MockBean
    private QuickTestStatisticsService quickTestStatisticsService;

    @MockBean
    private Utilities utilities;

    @InjectMocks
    private QuickTestStatisticsController quickTestStatisticsController;

    @Test
    void getQuicktestStatistics() throws Exception {
        QuickTestStatisticsResponse quickTestStatisticsResponse = new QuickTestStatisticsResponse();
        quickTestStatisticsResponse.setPositiveTestCount(1);
        quickTestStatisticsResponse.setTotalTestCount(2);

        QuickTestStatistics quickTestStatistics = new QuickTestStatistics();
        quickTestStatistics.setPositiveTestCount(1);
        quickTestStatistics.setTotalTestCount(2);
        when(quickTestStatisticsService.getStatistics(any(), any(), any())).thenReturn(quickTestStatistics);

        MvcResult result = mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/"))
            .andExpect(status().isOk())
            .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        QuickTestStatisticsResponse response
            = new Gson().fromJson(responseBody, QuickTestStatisticsResponse.class);

        Assertions.assertThat(response.getPositiveTestCount())
            .isEqualTo(quickTestStatisticsResponse.getPositiveTestCount());
        Assertions.assertThat(response.getTotalTestCount()).isEqualTo(quickTestStatisticsResponse.getTotalTestCount());


        result = mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .andExpect(status().isOk())
            .andReturn();
        responseBody = result.getResponse().getContentAsString();
        response
            = new Gson().fromJson(responseBody, QuickTestStatisticsResponse.class);

        Assertions.assertThat(response.getPositiveTestCount())
            .isEqualTo(quickTestStatisticsResponse.getPositiveTestCount());
        Assertions.assertThat(response.getTotalTestCount()).isEqualTo(quickTestStatisticsResponse.getTotalTestCount());


        result = mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/")
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .andExpect(status().isOk())
            .andReturn();
        responseBody = result.getResponse().getContentAsString();
        response
            = new Gson().fromJson(responseBody, QuickTestStatisticsResponse.class);

        Assertions.assertThat(response.getPositiveTestCount())
            .isEqualTo(quickTestStatisticsResponse.getPositiveTestCount());
        Assertions.assertThat(response.getTotalTestCount()).isEqualTo(quickTestStatisticsResponse.getTotalTestCount());


        result = mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .andExpect(status().isOk())
            .andReturn();
        responseBody = result.getResponse().getContentAsString();
        response
            = new Gson().fromJson(responseBody, QuickTestStatisticsResponse.class);

        Assertions.assertThat(response.getPositiveTestCount())
            .isEqualTo(quickTestStatisticsResponse.getPositiveTestCount());
        Assertions.assertThat(response.getTotalTestCount()).isEqualTo(quickTestStatisticsResponse.getTotalTestCount());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/"))
            .andExpect(status().isForbidden());

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/"))
            .andExpect(status().isForbidden());

        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/"))
            .andExpect(status().isUnauthorized());

        when(utilities.getIdsFromToken())
            .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).get("/api/quickteststatistics/")
            .andExpect(status().isInternalServerError())
            .andExpect(resultException -> assertTrue(
                resultException.getResolvedException() instanceof ResponseStatusException))
            .andExpect(resultException -> assertEquals(
                "500 INTERNAL_SERVER_ERROR",
                resultException.getResolvedException().getMessage()));

    }

    @Test
    void getQuicktestStatisticsFail() {
        ModelMapper modelMapper = mock(ModelMapper.class);
        when(modelMapper.map(any(), any())).thenReturn(null);
        try {
            quickTestStatisticsController.getQuicktestStatistics(ZonedDateTime.now(), ZonedDateTime.now());
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        } catch (Exception e) {
            fail("catch exception and convert to ResponseStatusException failed");
        }


    }
}

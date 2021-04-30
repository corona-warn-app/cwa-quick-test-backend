package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.model.Aggregation;
import app.coronawarn.quicktest.model.QuickTestStatistics;
import app.coronawarn.quicktest.model.QuickTestStatisticsResponse;
import app.coronawarn.quicktest.model.QuickTestTenantStatistics;
import app.coronawarn.quicktest.model.QuickTestTenantStatisticsResponse;
import app.coronawarn.quicktest.model.QuickTestTenantStatisticsResponseList;
import app.coronawarn.quicktest.service.QuickTestStatisticsService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_TENANT_COUNTER;
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

        QuickTestStatistics quickTestStatistics = QuickTestStatistics.builder()
        .positiveTestCount(1)
        .totalTestCount(2).build();
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


    @Test
    void getQuickteTenantStatistics() throws Exception {
        Gson gson = new GsonBuilder().
            setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").
            registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString())).
            registerTypeAdapter(ZonedDateTime.class, (JsonSerializer<ZonedDateTime>) (zonedDateTime, type, jsonSerializationContext) -> new JsonPrimitive(zonedDateTime != null ? zonedDateTime.withZoneSameInstant(
                ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null)).
            create();

        ZonedDateTime time = ZonedDateTime.now();


        QuickTestStatisticsResponse quickTestStatisticsResponse = new QuickTestStatisticsResponse();
        quickTestStatisticsResponse.setPositiveTestCount(1);
        quickTestStatisticsResponse.setTotalTestCount(2);

        QuickTestTenantStatisticsResponse quickTestTenantStatisticsResponse = new QuickTestTenantStatisticsResponse();
        quickTestTenantStatisticsResponse.setQuickTestStatistics(quickTestStatisticsResponse);
        quickTestTenantStatisticsResponse.setAggregation(Aggregation.DAY);
        quickTestTenantStatisticsResponse.setPocId("pocId");
        quickTestTenantStatisticsResponse.setTimestamp(time);

        QuickTestTenantStatisticsResponseList quickTestTenantStatisticsResponseList =
            new QuickTestTenantStatisticsResponseList();

        quickTestTenantStatisticsResponseList.setQuickTestTenantStatistics(Collections.singletonList(quickTestTenantStatisticsResponse));


        QuickTestStatistics quickTestStatistics = QuickTestStatistics.builder()
            .positiveTestCount(1)
            .totalTestCount(2).build();

        QuickTestTenantStatistics quickTestTenantStatistics = QuickTestTenantStatistics.builder().build();
        quickTestTenantStatistics.setQuickTestStatistics(quickTestStatistics);
        quickTestTenantStatistics.setAggregation(Aggregation.DAY);
        quickTestTenantStatistics.setPocId("pocId");
        quickTestTenantStatistics.setTimestamp(time);

        when(quickTestStatisticsService.getStatisticsForTenant(any(), any(), any(), any())).thenReturn(Collections.singletonList(quickTestTenantStatistics));

        MvcResult result =
            mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.NONE.toString()))
            .andExpect(status().isOk())
            .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        QuickTestTenantStatisticsResponseList response
            = gson.fromJson(responseBody, QuickTestTenantStatisticsResponseList.class);

        response.getQuickTestTenantStatistics().forEach(tenantResponse -> {
            Assertions.assertThat(tenantResponse.getAggregation())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getAggregation());
            Assertions.assertThat(tenantResponse.getPocId())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getPocId());
            Assertions.assertThat(tenantResponse.getTimestamp())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getTimestamp());
            Assertions.assertThat(tenantResponse.getQuickTestStatistics().getPositiveTestCount())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getQuickTestStatistics().getPositiveTestCount());
            Assertions.assertThat(tenantResponse.getQuickTestStatistics().getTotalTestCount()).
                isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getQuickTestStatistics().getTotalTestCount());

        });

        result = mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.HOUR.toString()))
            .andExpect(status().isOk())
            .andReturn();
        responseBody = result.getResponse().getContentAsString();
        response
            = gson.fromJson(responseBody, QuickTestTenantStatisticsResponseList.class);

        response.getQuickTestTenantStatistics().forEach(tenantResponse -> {
            Assertions.assertThat(tenantResponse.getAggregation())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getAggregation());
            Assertions.assertThat(tenantResponse.getPocId())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getPocId());
            Assertions.assertThat(tenantResponse.getTimestamp())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getTimestamp());
            Assertions.assertThat(tenantResponse.getQuickTestStatistics().getPositiveTestCount())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getQuickTestStatistics().getPositiveTestCount());
            Assertions.assertThat(tenantResponse.getQuickTestStatistics().getTotalTestCount()).
                isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getQuickTestStatistics().getTotalTestCount());

        });

        result = mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isOk())
            .andReturn();
        responseBody = result.getResponse().getContentAsString();
        response
            = gson.fromJson(responseBody, QuickTestTenantStatisticsResponseList.class);

        response.getQuickTestTenantStatistics().forEach(tenantResponse -> {
            Assertions.assertThat(tenantResponse.getAggregation())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getAggregation());
            Assertions.assertThat(tenantResponse.getPocId())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getPocId());
            Assertions.assertThat(tenantResponse.getTimestamp())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getTimestamp());
            Assertions.assertThat(tenantResponse.getQuickTestStatistics().getPositiveTestCount())
                .isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getQuickTestStatistics().getPositiveTestCount());
            Assertions.assertThat(tenantResponse.getQuickTestStatistics().getTotalTestCount()).
                isEqualTo(quickTestTenantStatisticsResponseList.getQuickTestTenantStatistics().get(0).getQuickTestStatistics().getTotalTestCount());

        });

        mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", "test"))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isForbidden());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isForbidden());

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isForbidden());

        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isUnauthorized());

        when(utilities.getIdsFromToken())
            .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        mockMvc().with(authentication().authorities(ROLE_TENANT_COUNTER)).perform(MockMvcRequestBuilders.get("/api/quickteststatistics/tenant/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("aggregation", Aggregation.DAY.toString()))
            .andExpect(status().isInternalServerError())
            .andExpect(resultException -> assertTrue(
                resultException.getResolvedException() instanceof ResponseStatusException))
            .andExpect(resultException -> assertEquals(
                "500 INTERNAL_SERVER_ERROR",
                resultException.getResolvedException().getMessage()));

    }

    @Test
    void getQuicktestTenantStatisticsFail() {
        ModelMapper modelMapper = mock(ModelMapper.class);
        when(modelMapper.map(any(), any())).thenReturn(null);
        try {
            quickTestStatisticsController.getQuicktestStatisticsForTenantWithAggregation(ZonedDateTime.now(),
                ZonedDateTime.now(), Aggregation.DAY);
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        } catch (Exception e) {
            fail("catch exception and convert to ResponseStatusException failed");
        }


    }

    private static final class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write( final JsonWriter jsonWriter, final LocalDate localDate ) throws IOException {
            jsonWriter.value(localDate.toString());
        }

        @Override
        public LocalDate read( final JsonReader jsonReader ) throws IOException {
            return LocalDate.parse(jsonReader.nextString());
        }
    }
}

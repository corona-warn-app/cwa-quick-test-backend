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

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.quicktest.QuickTestArchiveResponseList;
import app.coronawarn.quicktest.model.quicktest.QuickTestArchiveResponse;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.service.QuickTestArchiveService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.NestedServletException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(QuickTestArchiveController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class QuickTestArchiveControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    @MockBean
    private QuickTestArchiveService quickTestArchiveService;

    @InjectMocks
    private QuickTestArchiveController quickTestArchiveController;

    @MockBean
    private Utilities utilities;

    @Test
    void createQuickTestArchive() throws Exception {
        String output = "test output";
        when(quickTestArchiveService.getPdf(any())).thenReturn(output.getBytes());

        MvcResult mvcResult = mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4/pdf")
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk()).andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertThat(responseBody)
            .isEqualTo(output);


        mvcResult = mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4/pdf"))
            .andExpect(status().isOk()).andReturn();
        responseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertThat(responseBody)
            .isEqualTo(output);

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4/pdf"))
            .andExpect(status().isForbidden());

        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4/pdf"))
            .andExpect(status().isUnauthorized());

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
            .when(quickTestArchiveService).getPdf(any());
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4/pdf")
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("404 NOT_FOUND",
                result.getResolvedException().getMessage()));

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
            .when(quickTestArchiveService).getPdf(any());
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4/pdf")
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("500 INTERNAL_SERVER_ERROR",
                result.getResolvedException().getMessage()));
    }

    @Test
    void findArchivesByTestResultAndUpdatedAtBetween() throws Exception {

        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");
        quickTestArchive.setShortHashedGuid("6fa4dcec");
        quickTestArchive.setTenantId("tenant");
        quickTestArchive.setCreatedAt(LocalDateTime.now());
        quickTestArchive.setUpdatedAt(LocalDateTime.now());
        quickTestArchive.setConfirmationCwa(true);
        quickTestArchive.setTestResult((short) 6);
        quickTestArchive.setPrivacyAgreement(true);
        quickTestArchive.setLastName("1");
        quickTestArchive.setFirstName("1");
        quickTestArchive.setEmail("v@e.o");
        quickTestArchive.setPhoneNumber("+490000");
        quickTestArchive.setSex(Sex.DIVERSE);
        quickTestArchive.setStreet("f");
        quickTestArchive.setHouseNumber("a");
        quickTestArchive.setZipCode("11111");
        quickTestArchive.setCity("f");
        quickTestArchive.setTestBrandId("testbrand");
        quickTestArchive.setTestBrandName("brandname");
        quickTestArchive.setBirthday(LocalDate.now().toString());
        quickTestArchive.setPdf("test output".getBytes());
        when(quickTestArchiveService.findByTestResultAndUpdatedAtBetween(any(), anyShort(), any(), any())).thenReturn(
            Collections.singletonList(quickTestArchive));

        MvcResult mvcResult = mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("testResult", "6")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk()).andReturn();
        String responseBody = mvcResult.getResponse().getContentAsString();

        QuickTestArchiveResponseList response
            = new Gson().fromJson(responseBody, QuickTestArchiveResponseList.class);
        checkResponse(response.getQuickTestArchives().get(0), quickTestArchive);

        mvcResult = mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk()).andReturn();
        responseBody = mvcResult.getResponse().getContentAsString();

        response
            = new Gson().fromJson(responseBody, QuickTestArchiveResponseList.class);
        assertEquals(response.getQuickTestArchives().size(), 0);


        mvcResult = mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("testResult", "6")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isOk()).andReturn();
        responseBody = mvcResult.getResponse().getContentAsString();

        response = new Gson().fromJson(responseBody, QuickTestArchiveResponseList.class);
        checkResponse(response.getQuickTestArchives().get(0), quickTestArchive);

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("testResult", "6")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isForbidden());

        mockMvc().perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("testResult", "6")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isUnauthorized());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("testResult", "6")
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("testResult", "6")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isBadRequest());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .get("/api/quicktestarchive/")
            .param("testResult", "6")
            .param("dateFrom",
                ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .contentType(MediaType.APPLICATION_PDF_VALUE))
            .andExpect(status().isBadRequest());


        assertThatExceptionOfType(NestedServletException.class).isThrownBy(() -> {
            mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                .get("/api/quicktestarchive/")
                .param("testResult", "4")
                .param("dateFrom",
                    ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(status().isBadRequest());
        }).matches(e ->
            e.getRootCause().getMessage().equals("findArchivesByTestResultAndUpdatedAtBetween.testResult: " +
                "must be greater than or equal to 5")
        );

        assertThatExceptionOfType(NestedServletException.class).isThrownBy(() -> {
            mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                .get("/api/quicktestarchive/")
                .param("testResult", "9")
                .param("dateFrom",
                    ZonedDateTime.now().minusDays(1).withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .param("dateTo", ZonedDateTime.now().withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .contentType(MediaType.APPLICATION_PDF_VALUE))
                .andExpect(status().isBadRequest());
        }).matches(e ->
            e.getRootCause().getMessage().equals("findArchivesByTestResultAndUpdatedAtBetween.testResult: " +
                "must be less than or equal to 8")
        );
    }

    @Test
    void getQuicktestStatisticsFail() {
        try {
            quickTestArchiveController.getQuickTestPdf(null);
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(),HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        } catch (Exception e) {
            fail("catch exception and convert to ResponseStatusException failed");
        }

        ModelMapper modelMapper = mock(ModelMapper.class);
        when(modelMapper.map(any(), any())).thenReturn(null);
        try {
            quickTestArchiveController.findArchivesByTestResultAndUpdatedAtBetween(
                (short) 6,
                ZonedDateTime.now(),
                ZonedDateTime.now());
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(),HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        } catch (Exception e) {
            fail("catch exception and convert to ResponseStatusException failed");
        }

    }

    private void checkResponse(QuickTestArchiveResponse response, QuickTestArchive quickTestArchive) {
        assertEquals(quickTestArchive.getHashedGuid(), response.getHashedGuid());
        assertEquals(quickTestArchive.getLastName(), response.getLastName());
        assertEquals(quickTestArchive.getFirstName(), response.getFirstName());
        assertEquals(quickTestArchive.getEmail(), response.getEmail());
        assertEquals(quickTestArchive.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(quickTestArchive.getSex(), response.getSex());
        assertEquals(quickTestArchive.getStreet(), response.getStreet());
        assertEquals(quickTestArchive.getHouseNumber(), response.getHouseNumber());
        assertEquals(quickTestArchive.getZipCode(), response.getZipCode());
        assertEquals(quickTestArchive.getCity(), response.getCity());
        assertEquals(quickTestArchive.getBirthday(), response.getBirthday());
        assertEquals(quickTestArchive.getTestResult().toString(), response.getTestResult());
    }
}

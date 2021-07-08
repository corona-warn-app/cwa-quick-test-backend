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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.QuickTestCreationRequest;
import app.coronawarn.quicktest.model.QuickTestPersonalDataRequest;
import app.coronawarn.quicktest.model.QuickTestResponseList;
import app.coronawarn.quicktest.model.QuickTestUpdateRequest;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(QuickTestController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class QuickTestControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    private final String API_BASE_PATH = "/api/quicktest";
    @MockBean
    private QuickTestService quickTestService;
    @MockBean
    private Utilities utilities;
    @InjectMocks
    private QuickTestController quickTestController;

    @Test
    void createQuickTest() throws Exception {
        QuickTestCreationRequest quicktestCreationRequest = new QuickTestCreationRequest();
        quicktestCreationRequest.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");

        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isCreated());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isForbidden());

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isForbidden());

        mockMvc().perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isUnauthorized());

        doThrow(new ResponseStatusException(HttpStatus.CONFLICT))
            .when(quickTestService).createNewQuickTest(any(), any());
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isConflict())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("409 CONFLICT",
                result.getResolvedException().getMessage()));

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
            .when(quickTestService).createNewQuickTest(any(), any());
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("500 INTERNAL_SERVER_ERROR",
                result.getResolvedException().getMessage()));

        quicktestCreationRequest.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c");

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isBadRequest());

        quicktestCreationRequest.setHashedGuid("6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c55");

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .post(API_BASE_PATH)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quicktestCreationRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void catchAndConvertExceptionTests() {
        QuickTestCreationRequest quicktestCreationRequest = null;
        QuickTestUpdateRequest quickTestUpdateRequest = null;
        QuickTestPersonalDataRequest quickTestPersonalDataRequest = new QuickTestPersonalDataRequest();
        quickTestPersonalDataRequest.setConfirmationCwa(false);

        try {
            quickTestController.createQuickTest(quicktestCreationRequest);
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(),HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        } catch (Exception e) {
            fail("catch exception and convert to ResponseStatusException failed");
        }

        try {
            quickTestController.updateQuickTestStatus(
                    "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c55",
                    quickTestUpdateRequest);
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(),HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        } catch (Exception e) {
            fail("catch exception and convert to ResponseStatusException failed");
        }

        try {
            quickTestController.updateQuickTestWithPersonalData(
                    "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c55",
                    quickTestPersonalDataRequest);
            fail("has to throw exception");
        } catch (ResponseStatusException e) {
            assertEquals(e.getStatus(),HttpStatus.INTERNAL_SERVER_ERROR, "wrong status");
        } catch (Exception e) {
            fail("catch exception and convert to ResponseStatusException failed");
        }
    }

    @Test
    void updateQuickTestStatus() throws Exception {
        QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
        quickTestUpdateRequest.setTestBrandId("brandId");
        quickTestUpdateRequest.setTestBrandName("brandName");

        for (short result = 0; result <= 10; result++) {
            quickTestUpdateRequest.setResult((short) result);

            if (result > 5 && result < 9) {
                mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                    .put(API_BASE_PATH + "/6fa4dcec/testResult")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(new Gson().toJson(quickTestUpdateRequest)))
                    .andExpect(status().isNoContent());
            } else {
                mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                    .put(API_BASE_PATH + "/6fa4dcec/testResult")
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(new Gson().toJson(quickTestUpdateRequest)))
                    .andExpect(status().isBadRequest());
            }
        }
        quickTestUpdateRequest.setResult((short) 6);

        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isForbidden());

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isForbidden());

        mockMvc().perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isUnauthorized());

        quickTestUpdateRequest.setTestBrandName(null);
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName("a1");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName("");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName(
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        quickTestUpdateRequest.setTestBrandName(
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789");
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNoContent());

        /* TODO validation changed due to dcc
        quickTestUpdateRequest.setTestBrandId(null);
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isBadRequest());
        */

        quickTestUpdateRequest.setResult((short) 6);
        quickTestUpdateRequest.setTestBrandId("brandId");
        quickTestUpdateRequest.setTestBrandName(null);
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
            .when(quickTestService).updateQuickTest(any(), any(), any(QuickTestUpdateRequest.class), any(), any());
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("404 NOT_FOUND",
                result.getResolvedException().getMessage()));

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
            .when(quickTestService).updateQuickTest(any(), any(), any(QuickTestUpdateRequest.class), any(), any());
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/testResult")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(new Gson().toJson(quickTestUpdateRequest)))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("500 INTERNAL_SERVER_ERROR",
                result.getResolvedException().getMessage()));

    }

    @Test
    void updateQuickTestWithPersonalData() throws Exception {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

        QuickTestPersonalDataRequest quickTestPersonalDataRequest = new QuickTestPersonalDataRequest();
        quickTestPersonalDataRequest.setConfirmationCwa(true);
        quickTestPersonalDataRequest.setPrivacyAgreement(true);
        quickTestPersonalDataRequest.setLastName("Lastnäme");
        quickTestPersonalDataRequest.setStandardisedFamilyName("TR");
        quickTestPersonalDataRequest.setFirstName("FirstNamè");
        quickTestPersonalDataRequest.setStandardisedGivenName("ARTUR");
        quickTestPersonalDataRequest.setEmail("v@e.o");
        quickTestPersonalDataRequest.setPhoneNumber("+490000");
        quickTestPersonalDataRequest.setSex(Sex.DIVERSE);
        quickTestPersonalDataRequest.setStreet("Street test");
        quickTestPersonalDataRequest.setHouseNumber("12 b");
        quickTestPersonalDataRequest.setZipCode("11111");
        quickTestPersonalDataRequest.setCity("Testcity");
        quickTestPersonalDataRequest.setDiseaseAgentTargeted("t");
        quickTestPersonalDataRequest.setBirthday(LocalDate.now());
        quickTestPersonalDataRequest.setStandardisedFamilyName("standardisedFamily");
        quickTestPersonalDataRequest.setStandardisedGivenName("standaärdisedGivenName");
        quickTestPersonalDataRequest.setDiseaseAgentTargeted("diseaseAgentTargeted");
        quickTestPersonalDataRequest.setTestResultServerHash(
            "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4");

        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isForbidden());

        mockMvc().with(authentication()).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isForbidden());

        mockMvc().perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isUnauthorized());

        // test cwa
        boolean oldCwaValue = quickTestPersonalDataRequest.getConfirmationCwa();
        quickTestPersonalDataRequest.setConfirmationCwa(false);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setConfirmationCwa(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());
        quickTestPersonalDataRequest.setConfirmationCwa(oldCwaValue);

        // test privacyAgreement
        boolean privacyAgreement = quickTestPersonalDataRequest.getPrivacyAgreement();
        quickTestPersonalDataRequest.setPrivacyAgreement(false);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setPrivacyAgreement(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());
        quickTestPersonalDataRequest.setPrivacyAgreement(privacyAgreement);

        // test lastName
        String lastName = quickTestPersonalDataRequest.getLastName();
        quickTestPersonalDataRequest.setLastName(
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setLastName(
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setLastName("");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setLastName(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setLastName(lastName);

        // test firstName
        String firstName = quickTestPersonalDataRequest.getFirstName();
        quickTestPersonalDataRequest.setFirstName(
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setFirstName(
            "01234567890123456789012345678901234567890123456789012345678901234567890123456789");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setFirstName("");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setFirstName(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());
        quickTestPersonalDataRequest.setFirstName(firstName);

        //Test email
        String email = quickTestPersonalDataRequest.getEmail();
        quickTestPersonalDataRequest.setEmail("a@be");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setEmail(
            "extralooooooooooooooooooooooooolongemail555555555577777778888999" +
                "@extralooooooooooooooooooooooooolongemail55555555557777777888899" +
                ".extralooooooooooooooooooooooooolongemail55555555557777777888899");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setEmail(
            "extralooooooooooooooooooooooooolongemail5555555555777777788889990" +
                "@extralooooooooooooooooooooooooolongemail55555555557777777888899" +
                ".extralooooooooooooooooooooooooolongemail55555555557777777888899");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setEmail(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
        //    .andExpect(status().isBadRequest());
            .andExpect(status().isNoContent());
        quickTestPersonalDataRequest.setEmail(email);


        //Test email
        String phone = quickTestPersonalDataRequest.getPhoneNumber();
        quickTestPersonalDataRequest.setPhoneNumber("0100000");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setPhoneNumber(
            "0100000012345678901234567890123456789012345678901234567890123456789012345678900");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setPhoneNumber(
            "+491000000123456789012345678901234567890123456789012345678901234567890123456789");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setPhoneNumber(
            "01000000123456789012345678901234567890123456789012345678901234567890123456789000");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setPhoneNumber(
            "+4910000001234567890123456789012345678901234567890123456789012345678901234567890");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setPhoneNumber(
            "010000");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setPhoneNumber(
            "+49100");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setPhoneNumber(
            "123456789");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setPhoneNumber(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());
        quickTestPersonalDataRequest.setPhoneNumber(phone);

        //test sex
        Sex sex = quickTestPersonalDataRequest.getSex();
        quickTestPersonalDataRequest.setSex(Sex.MALE);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setSex(Sex.DIVERSE);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setSex(Sex.FEMALE);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setSex(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setSex(sex);

        //test street
        String street = quickTestPersonalDataRequest.getStreet();
        quickTestPersonalDataRequest.setStreet(
            "extraloooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooooooooooooooooooooooooooooooooooooooongstreetname");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setStreet(
            "extraloooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooooooooooooooooooooooooongstreetname");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setStreet("");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setStreet(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setStreet(street);

        //test housenumber
        String housenumber = quickTestPersonalDataRequest.getHouseNumber();
        quickTestPersonalDataRequest.setHouseNumber("012345678901234");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setHouseNumber("0123456789012345");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setHouseNumber("");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setHouseNumber(housenumber);

        //test zipcode
        String zipcode = quickTestPersonalDataRequest.getZipCode();
        quickTestPersonalDataRequest.setZipCode("01111");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setZipCode("10111");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setZipCode("1011");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setZipCode("101111");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setZipCode("00111");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setZipCode(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());
        quickTestPersonalDataRequest.setZipCode(zipcode);

        //test city
        String city = quickTestPersonalDataRequest.getCity();
        quickTestPersonalDataRequest.setCity(
            "extraloooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooooooooooooooooooooooooooooooooooooooooooongcityname");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setCity(
            "extraloooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo"
                + "oooooooooooooooooooooooooooooooooooooooooooongcityname");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setCity("");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setCity(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setCity(city);

        //test birthday
        LocalDate birthday = quickTestPersonalDataRequest.getBirthday();
        quickTestPersonalDataRequest.setBirthday(LocalDate.of(1900, 1, 1));
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNoContent());

        quickTestPersonalDataRequest.setBirthday(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setBirthday(birthday);

        //test testResultServerHash
        String testResultServerHash = quickTestPersonalDataRequest.getTestResultServerHash();
        quickTestPersonalDataRequest.setTestResultServerHash(
            "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setTestResultServerHash(
            "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c55");
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setTestResultServerHash(null);
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isBadRequest());

        quickTestPersonalDataRequest.setTestResultServerHash(testResultServerHash);

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
            .when(quickTestService).updateQuickTestWithPersonalData(any(), any(), any());
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("404 NOT_FOUND",
                result.getResolvedException().getMessage()));

        doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
            .when(quickTestService).updateQuickTestWithPersonalData(any(), any(), any());
        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
            .put(API_BASE_PATH + "/6fa4dcec/personalData")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(quickTestPersonalDataRequest)))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResponseStatusException))
            .andExpect(result -> assertEquals("500 INTERNAL_SERVER_ERROR",
                result.getResolvedException().getMessage()));


    }

    @Test
    void getPendingQuickTests() throws Exception {
        QuickTest quickTest = new QuickTest();
        quickTest.setShortHashedGuid("00000000");
        quickTest.setPrivacyAgreement(true);
        quickTest.setFirstName("firstName");
        when(quickTestService.findAllPendingQuickTestsByTenantIdAndPocId(any()))
                .thenReturn(Collections.singletonList(quickTest));

        MvcResult result = mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                .get(API_BASE_PATH)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        QuickTestResponseList response = new Gson().fromJson(responseBody, QuickTestResponseList.class);

        Assertions.assertThat(response.getQuickTests().get(0).getShortHashedGuid())
                .isEqualTo(quickTest.getShortHashedGuid());

        try {
            Method method1 = response.getQuickTests().get(0).getClass().getMethod("getPrivacyAgreement");
            Method method2 = response.getQuickTests().get(0).getClass().getMethod("getFirstName");
            fail("Only short hash must be returned");
        } catch (NoSuchMethodException e){
        }

        mockMvc().with(authentication().authorities(ROLE_COUNTER)).perform(MockMvcRequestBuilders
                .get(API_BASE_PATH)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPendingQuickFailedTest() throws Exception {
        when(quickTestService.findAllPendingQuickTestsByTenantIdAndPocId(any()))
                .thenThrow();
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                .get(API_BASE_PATH)
                .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError());
    }

    class LocalDateAdapter implements JsonSerializer<LocalDate> {

        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)); // "yyyy-mm-dd"
        }
    }
}

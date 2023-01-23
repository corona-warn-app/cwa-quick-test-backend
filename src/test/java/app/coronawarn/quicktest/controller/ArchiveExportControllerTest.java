/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2022 T-Systems International GmbH and all other contributors
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

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ARCHIVE_OPERATOR;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.service.ArchiveSchedulingService;
import app.coronawarn.quicktest.utils.Utilities;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.RandomUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "keycloak-admin.realm=REALM")
@ActiveProfiles({"test", "archive_export"})
@AutoConfigureMockMvc
@Import({UserManagementControllerUtils.class, Utilities.class})
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class ArchiveExportControllerTest extends ServletKeycloakAuthUnitTestingSupport {

    @Autowired
    private QuickTestArchiveRepository shortTermArchiveRepository;

    @Autowired
    private ArchiveSchedulingService archiveSchedulingService;

    private final static String userId = "user-id";

    public static final String PARTNER_ID_1 = "P10000";

    public static final String PARTNER_ID_2 = "P10001";

    public static final String POC_ID_1 = "Poc_42";

    public static final String POC_ID_2 = "Poc_43";

    @Test
    @WithMockKeycloakAuth(
      authorities = ROLE_ARCHIVE_OPERATOR,
      claims = @OpenIdClaims(sub = userId)
    )
    void downloadArchiveByPartnerId() throws Exception {

        shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID_1, POC_ID_1));
        shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID_1, POC_ID_1));
        shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID_1, POC_ID_1));
        shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID_1, POC_ID_2));

        shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID_2, POC_ID_1));
        shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID_2, POC_ID_1));
        shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID_2, POC_ID_2));

        archiveSchedulingService.moveToArchiveJob();

        MvcResult mvcResult = mockMvc().perform(MockMvcRequestBuilders
            .get("/api/archive/" + PARTNER_ID_1))
          .andReturn();
        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_TYPE));
        Assertions.assertEquals("attachment; filename=quicktest_export.csv", mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        checkCsv(mvcResult.getResponse().getContentAsByteArray(), 27, 5);

        mvcResult = mockMvc().perform(MockMvcRequestBuilders
                        .get("/api/archive/" + PARTNER_ID_2))
                .andReturn();
        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_TYPE));
        Assertions.assertEquals("attachment; filename=quicktest_export.csv", mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        checkCsv(mvcResult.getResponse().getContentAsByteArray(), 27, 4);

        mvcResult = mockMvc().perform(MockMvcRequestBuilders
                        .get("/api/archive/randomPartnerId"))
                .andReturn();
        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_TYPE));
        Assertions.assertEquals("attachment; filename=quicktest_export.csv", mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        checkCsv(mvcResult.getResponse().getContentAsByteArray(), 0, 0);

    }

    @Test
    @WithMockKeycloakAuth(
            authorities = ROLE_COUNTER,
            claims = @OpenIdClaims(sub = userId)
    )
    void downloadArchiveWrongRole() throws Exception {

        mockMvc().perform(MockMvcRequestBuilders
                        .get("/api/archive/" + PARTNER_ID_1))
                .andExpect(status().isForbidden());
    }

    private void checkCsv(byte[] csvBytes, int expectedCols, int expectedRows) throws IOException, CsvException {
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        CSVParser csvParser = new CSVParserBuilder()
                .withSeparator('\t')
                .build();

        try (CSVReader csvReader = new CSVReaderBuilder(new StringReader(csv))
                .withCSVParser(csvParser)
                .build()
        ) {
            List<String[]> csvEntries = csvReader.readAll();
            Assertions.assertEquals(expectedRows, csvEntries.size());
            if (expectedRows > 0) {
                Assertions.assertEquals(expectedCols, csvEntries.get(0).length);
            }
        }
    }

    private QuickTestArchive buildQuickTestArchive(String tenantId, String pocId) {
        QuickTestArchive qta = new QuickTestArchive();
        qta.setShortHashedGuid(HexUtils.toHexString(RandomUtils.nextBytes(4)));
        qta.setHashedGuid(HexUtils.toHexString(RandomUtils.nextBytes(32)));
        qta.setTenantId(tenantId);
        qta.setPocId(pocId);
        qta.setCreatedAt(LocalDateTime.now().minusMonths(3));
        qta.setUpdatedAt(LocalDateTime.now().minusMonths(2));
        qta.setConfirmationCwa(Boolean.TRUE);
        qta.setTestResult(Short.valueOf("6"));
        qta.setPrivacyAgreement(Boolean.TRUE);
        qta.setLastName("last_name");
        qta.setFirstName("first_name");
        qta.setEmail("email");
        qta.setPhoneNumber("phone_number");
        qta.setSex(Sex.MALE);
        qta.setStreet("street");
        qta.setHouseNumber("house_number");
        qta.setZipCode("zip_code");
        qta.setCity("city");
        qta.setTestBrandId("test_brand_id");
        qta.setTestBrandName("test_brand_name, Ltd, another_part_of_test_brand_name");
        qta.setBirthday("2000-01-01");
        qta.setPdf("PDF".getBytes());
        qta.setTestResultServerHash("test_result_server_hash");
        qta.setDcc("dcc");
        qta.setAdditionalInfo("additional_info");
        qta.setGroupName("group_name");
        return qta;
    }
}

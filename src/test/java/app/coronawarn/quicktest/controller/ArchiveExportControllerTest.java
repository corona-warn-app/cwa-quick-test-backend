/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
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
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ARCHIVE_ZIP_CREATOR;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_ARCHIVE_ZIP_DOWNLOADER;
import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_COUNTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.coronawarn.quicktest.config.CsvUploadConfig;
import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.model.cancellation.ZipRequest;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import app.coronawarn.quicktest.service.ArchiveSchedulingService;
import app.coronawarn.quicktest.utils.Utilities;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.c4_soft.springaddons.security.oauth2.test.annotations.OpenIdClaims;
import com.c4_soft.springaddons.security.oauth2.test.annotations.keycloak.WithMockKeycloakAuth;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.lang3.RandomUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StreamUtils;

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

    @MockBean
    private AmazonS3 s3Client;

    @Autowired
    CsvUploadConfig csvUploadConfig;

    @Autowired
    ObjectMapper objectMapper;

    private final static String userId = "user-id";

    public static final String PARTNER_ID_1 = "P10000";

    public static final String PARTNER_ID_2 = "P10001";

    public static final String POC_ID_1 = "Poc_42";

    public static final String POC_ID_2 = "Poc_43";

    public static final String ZIP_PASSWORD = "abcdefghijklmnopqrstuvwqyz";

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
        checkCsv(mvcResult.getResponse().getContentAsByteArray(), 24, 5);

        mvcResult = mockMvc().perform(MockMvcRequestBuilders
                .get("/api/archive/" + PARTNER_ID_2))
            .andReturn();
        Assertions.assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_TYPE));
        Assertions.assertEquals("attachment; filename=quicktest_export.csv", mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        checkCsv(mvcResult.getResponse().getContentAsByteArray(), 24, 4);

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

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ARCHIVE_ZIP_CREATOR,
        claims = @OpenIdClaims(sub = userId)
    )
    void testCreateZip() throws Exception {
        ZipRequest zipRequest = new ZipRequest();
        zipRequest.setPartnerId(PARTNER_ID_1);
        zipRequest.setPartnerIds(List.of(PARTNER_ID_2));
        zipRequest.setPassword(ZIP_PASSWORD);

        byte[] csv1 = RandomUtils.nextBytes(1000);
        byte[] csv2 = RandomUtils.nextBytes(1000);

        S3Object csv1Object = new S3Object();
        csv1Object.setObjectContent(new ByteArrayInputStream(csv1));

        S3Object csv2Object = new S3Object();
        csv2Object.setObjectContent(new ByteArrayInputStream(csv2));

        when(s3Client.getObject(csvUploadConfig.getBucketName(), PARTNER_ID_1 + ".csv"))
            .thenReturn(csv1Object);
        when(s3Client.getObject(csvUploadConfig.getBucketName(), PARTNER_ID_2 + ".csv"))
            .thenReturn(csv2Object);

        ArgumentCaptor<InputStream> zipUploadStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<ObjectMetadata> zipUploadMetaCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

        when(s3Client.putObject(
            eq(csvUploadConfig.getBucketName()),
            eq(PARTNER_ID_1 + ".zip"),
            zipUploadStreamCaptor.capture(),
            zipUploadMetaCaptor.capture()))
            .thenReturn(null);

        mockMvc().perform(MockMvcRequestBuilders
                .post("/api/archive/zip")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(zipRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().bytes(new byte[0]));

        csv1Object.close();
        csv2Object.close();

        Assertions.assertNotNull(zipUploadMetaCaptor.getValue());
        Assertions.assertNotNull(zipUploadStreamCaptor.getValue());

        byte[] uploadedZip = StreamUtils.copyToByteArray(zipUploadStreamCaptor.getValue());

        ObjectMetadata uploadedMetadata = zipUploadMetaCaptor.getValue();
        Assertions.assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, uploadedMetadata.getContentType());
        Assertions.assertEquals(uploadedZip.length, uploadedMetadata.getContentLength());

        ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(uploadedZip), ZIP_PASSWORD.toCharArray());

        for (byte i = 0; i < 2; i++) {
            System.out.println(i);
            LocalFileHeader zipFile = zipStream.getNextEntry();
            Assertions.assertEquals(EncryptionMethod.AES, zipFile.getEncryptionMethod());

            if (zipFile.getFileName().equals(PARTNER_ID_1 + ".csv")) {
                Assertions.assertArrayEquals(csv1, zipStream.readAllBytes());
            } else if (zipFile.getFileName().equals(PARTNER_ID_2 + ".csv")) {
                Assertions.assertArrayEquals(csv2, zipStream.readAllBytes());
            } else {
                Assertions.fail();
            }
        }

        Assertions.assertNull(zipStream.getNextEntry());

        zipStream.close();
    }

    @Test
    @WithMockKeycloakAuth(
        authorities = ROLE_ARCHIVE_ZIP_DOWNLOADER,
        claims = @OpenIdClaims(sub = userId)
    )
    void testRequestPresignedUrl() throws Exception {
        URL presignedUrl = new URL("https://example.org");

        when(s3Client.doesObjectExist(csvUploadConfig.getBucketName(), PARTNER_ID_1 + ".zip"))
            .thenReturn(true);
        when(s3Client.doesObjectExist(csvUploadConfig.getBucketName(), PARTNER_ID_2 + ".zip"))
            .thenReturn(false);

        ArgumentCaptor<GeneratePresignedUrlRequest> presignedUrlRequestArgumentCaptor = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        when(s3Client.generatePresignedUrl(presignedUrlRequestArgumentCaptor.capture()))
            .thenReturn(presignedUrl);

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/archive/zip/download?filename=" + PARTNER_ID_2 + ".zip"))
            .andExpect(status().isNotFound())
            .andExpect(content().bytes(new byte[0]));

        verify(s3Client, never()).generatePresignedUrl(any());

        mockMvc().perform(MockMvcRequestBuilders
                .get("/api/archive/zip/download?filename=" + PARTNER_ID_1 + ".zip"))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(presignedUrl)));

        long now = Instant.now().toEpochMilli();
        long expiration = presignedUrlRequestArgumentCaptor.getValue().getExpiration().toInstant().toEpochMilli();
        Assertions.assertTrue(expiration - now > 299_000);
        Assertions.assertTrue(expiration - now < 301_000);
        Assertions.assertEquals(csvUploadConfig.getBucketName(), presignedUrlRequestArgumentCaptor.getValue().getBucketName());
        Assertions.assertEquals(PARTNER_ID_1 + ".zip", presignedUrlRequestArgumentCaptor.getValue().getKey());
        Assertions.assertEquals(HttpMethod.GET, presignedUrlRequestArgumentCaptor.getValue().getMethod());
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

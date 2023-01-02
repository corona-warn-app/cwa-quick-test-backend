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

package app.coronawarn.quicktest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.archive.repository.ArchiveRepository;
import app.coronawarn.quicktest.domain.Cancellation;
import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.repository.CancellationRepository;
import app.coronawarn.quicktest.repository.QuickTestArchiveRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.RandomUtils;
import org.apache.tomcat.util.buf.HexUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class CancellationCsvTest {

    @Autowired
    private CancellationSchedulingService cancellationSchedulingService;

    @Autowired
    private QuickTestArchiveRepository shortTermArchiveRepository;

    @Autowired
    private ArchiveRepository longTermArchiveRepository;

    @Autowired
    private CancellationRepository cancellationRepository;

    @MockBean
    private AmazonS3 s3Client;

    @BeforeEach
    void setUp() {
        shortTermArchiveRepository.deleteAll();
        longTermArchiveRepository.deleteAllByTenantId(PARTNER_ID);
        cancellationRepository.deleteAll();
    }

    public static final ZonedDateTime CANCELLATION_DATE = ZonedDateTime.now().minusHours(49).truncatedTo(ChronoUnit.MINUTES);
    public static final String PARTNER_ID = "P10000";
    public static final String PARTNER_ID_HASH = "212e58b487b6d6b486b71c6ebb3fedc0db3c69114f125fb3cd2fbc72e6ffc25f";

    @ParameterizedTest
    @ValueSource(ints = {1, 5_000})
    @Transactional
    void testCsvExport(int n) throws IOException, NoSuchAlgorithmException, CsvException {
        Cancellation cancellation = new Cancellation();
        cancellation.setPartnerId(PARTNER_ID);
        cancellation.setCancellationDate(CANCELLATION_DATE);
        cancellationRepository.save(cancellation);

        for (int i = 0; i < n; i++) {
            shortTermArchiveRepository.save(buildQuickTestArchive(PARTNER_ID));
        }

        Assertions.assertEquals(n, shortTermArchiveRepository.findAllByTenantId(PARTNER_ID, Pageable.unpaged()).count());
        Assertions.assertEquals(0, longTermArchiveRepository.findAllByTenantId(PARTNER_ID_HASH, PageRequest.of(0, Integer.MAX_VALUE)).size());

        cancellationSchedulingService.cancellationArchiveJob();

        Assertions.assertEquals(0, shortTermArchiveRepository.findAllByTenantId(PARTNER_ID, Pageable.unpaged()).count());
        Assertions.assertEquals(n, longTermArchiveRepository.findAllByTenantId(PARTNER_ID_HASH, PageRequest.of(0, Integer.MAX_VALUE)).size());

        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        String expectedFileName = PARTNER_ID + ".csv";
        when(s3Client.putObject(anyString(), eq(expectedFileName), inputStreamArgumentCaptor.capture(), any()))
          .thenReturn(new PutObjectResult());

        cancellationSchedulingService.csvUploadJob();

        verify(s3Client).putObject(anyString(), eq(expectedFileName), any(), any());

        byte[] csvBytes = inputStreamArgumentCaptor.getValue().readAllBytes();
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        cancellation = cancellationRepository.findById(PARTNER_ID).orElseThrow();

        Assertions.assertEquals(n, cancellation.getCsvEntityCount());
        Assertions.assertEquals(csvBytes.length, cancellation.getCsvSize());
        Assertions.assertEquals(getHash(csvBytes), cancellation.getCsvHash());

        CSVParser csvParser = new CSVParserBuilder()
                .withEscapeChar('\\')
                .withSeparator('\t')
                .withQuoteChar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                .build();

        try (CSVReader csvReader = new CSVReaderBuilder(new StringReader(csv))
                .withCSVParser(csvParser)
                .build()
        ) {
            List<String[]> csvEntries = csvReader.readAll();
            Assertions.assertEquals(n + 1, csvEntries.size());
            Assertions.assertEquals(27, csvEntries.get(0).length);
        }

        longTermArchiveRepository.deleteAllByTenantId(PARTNER_ID_HASH);
    }

    private String getHash(byte[] bytes) throws NoSuchAlgorithmException {
        return Hex.toHexString(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
    private QuickTestArchive buildQuickTestArchive(String tenantId) {
        QuickTestArchive qta = new QuickTestArchive();
        qta.setShortHashedGuid(HexUtils.toHexString(RandomUtils.nextBytes(4)));
        qta.setHashedGuid(HexUtils.toHexString(RandomUtils.nextBytes(32)));
        qta.setTenantId(tenantId);
        qta.setPocId("poc_id");
        qta.setCreatedAt(LocalDateTime.now().minusMonths(3));
        qta.setUpdatedAt(LocalDateTime.now().minusMonths(2));
        qta.setConfirmationCwa(Boolean.TRUE);
        qta.setTestResult(Short.valueOf("6"));
        qta.setPrivacyAgreement(Boolean.TRUE);
        qta.setLastName("last\n_na\"me");
        qta.setFirstName("fir\rst_name");
        qta.setEmail("email");
        qta.setPhoneNumber("phone_number");
        qta.setSex(Sex.MALE);
        qta.setStreet("street");
        qta.setHouseNumber("house_number");
        qta.setZipCode("zip_code");
        qta.setCity("Düßeldorf");
        qta.setTestBrandId("test_brand_id");
        qta.setTestBrandName("test_brand_name, \"Ltd\", another_part_of_test_brand_name");
        qta.setBirthday("2000-01-01");
        qta.setPdf("PDF".getBytes());
        qta.setTestResultServerHash("test_result_server_hash");
        qta.setDcc("dcc");
        qta.setAdditionalInfo("additional_info");
        qta.setGroupName("group_name");
        return qta;
    }
}

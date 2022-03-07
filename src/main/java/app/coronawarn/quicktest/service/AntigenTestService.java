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

import app.coronawarn.quicktest.domain.AntigenTest;
import app.coronawarn.quicktest.utils.Utilities;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AntigenTestService {

    private List<AntigenTest> antigenTests = new ArrayList<>();

    @Getter
    private LocalDateTime lastUpdate = Utilities.getCurrentLocalDateTimeUtc();

    /**
     * Method for receiving antigen tests.
     *
     * @return antigen tests
     */
    public List<AntigenTest> getAntigenTests() throws ResponseStatusException {
        log.debug("Response antigenTests");
        if (antigenTests.isEmpty()) {
            init();
        }
        if (antigenTests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return antigenTests;
        }
    }

    /**
     * Method to update antigen tests by CSV file.
     */
    public void updateAntigenTestsByCsv(MultipartFile multipartFile) {
        try {
            setAntigenTests(multipartFile.getInputStream());
        } catch (IOException e) {
            log.error("Could not read MultipartFile in updateAntigenTestsByCSV");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected void setAntigenTests(InputStream inputStreamCsv) {
        List<List<String>> unFilteredAntigentest = createAntigenTestListFromCsvInputStream(inputStreamCsv);
        antigenTests = mapToAntigenTest(filterAntigenTests(unFilteredAntigentest));
        lastUpdate = Utilities.getCurrentLocalDateTimeUtc();
    }

    private void init()  {
        final ClassPathResource classPathResource = new ClassPathResource("antigentests.csv");
        InputStream inputStreamCsv = (Objects.requireNonNull(
                Objects.requireNonNull(classPathResource.getClassLoader())
                        .getResourceAsStream("antigentests.csv")));
        setAntigenTests(inputStreamCsv);
    }

    private List<AntigenTest> mapToAntigenTest(List<List<String>> rawAntigenTests) {
        List<AntigenTest> mappedAntigenTests = new ArrayList<>();
        for (List<String> row : rawAntigenTests) {
            mappedAntigenTests.add(new AntigenTest(row.get(0), row.get(1)));
        }
        return mappedAntigenTests;
    }

    private List<List<String>> filterAntigenTests(List<List<String>> unFilteredAntigentest) {
        // TODO check with scoping: unFilteredAntigentest.removeIf(st -> !st.get(2).equalsIgnoreCase("Ja"));
        return unFilteredAntigentest;
    }

    private List<List<String>> createAntigenTestListFromCsvInputStream(InputStream inputStreamCsv) {
        List<List<String>> rawAntigenTests = new ArrayList<>();
        try {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .withIgnoreQuotations(true)
                    .build();
            CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStreamCsv, "windows-1252"))
                    .withSkipLines(2)
                    .withCSVParser(parser)
                    .build();
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                rawAntigenTests.add(Arrays.asList(values));
            }
        } catch (IOException | CsvValidationException exception) {
            log.debug("loadAntigenTestsFromFile failed: {}", exception.getMessage());
            log.error("loadAntigenTestsFromFile failed");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return rawAntigenTests;
    }
}

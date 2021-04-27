package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.domain.AntigenTest;
import app.coronawarn.quicktest.utils.Utilities;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AntigenTestService {

    private List<AntigenTest> antigenTests = new ArrayList();
    @Getter
    private LocalDateTime lastUpdate = Utilities.getCurrentLocalDateTimeUtc();

    /**
     * Method for receiving antigen tests.
     *
     * @return antigen tests
     */
    public List<AntigenTest> getAntigenTests() throws ResponseStatusException, IOException {
        log.debug("Response antigenTests");
        if (antigenTests.isEmpty()) {
            loadAntigenTests();
        }
        if (antigenTests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return antigenTests;
        }
    }

    protected void loadAntigenTests() throws IOException {
        File file = loadAntigenTestsFromBfArM();
        List<List<String>> unFilteredAntigentest = loadAntigenTestsFromFile(file);
        antigenTests = mapToAntigenTest(filterAntigenTests(unFilteredAntigentest));
        lastUpdate = Utilities.getCurrentLocalDateTimeUtc();
    }

    private List<AntigenTest> mapToAntigenTest(List<List<String>> filterAntigenTests) {
        List<AntigenTest> antigenTests = new ArrayList();
        for (List<String> raw : filterAntigenTests) {
            antigenTests.add(new AntigenTest(raw.get(0), raw.get(1)));
        }
        return antigenTests;
    }

    private List<List<String>> filterAntigenTests(List<List<String>> unFilteredAntigentest) {
        // TODO check with scoping: unFilteredAntigentest.removeIf(st -> !st.get(2).equalsIgnoreCase("Ja"));
        return unFilteredAntigentest;
    }

    private List<List<String>> loadAntigenTestsFromFile(File file) throws IOException {
        FileReader reader;
        if (file == null) {
            String fileName = "antigentests.csv";
            ClassLoader classLoader = getClass().getClassLoader();
            reader = new FileReader(
                    Objects.requireNonNull(classLoader.getResource(fileName)).getPath(), Charset.defaultCharset());
        } else {
            reader = new FileReader(file, Charset.defaultCharset());
        }

        List<List<String>> records = new ArrayList<>();
        try {
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .withIgnoreQuotations(true)
                    .build();
            CSVReader csvReader = new CSVReaderBuilder(reader)
                    .withSkipLines(1)
                    .withCSVParser(parser)
                    .build();
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
        } catch (IOException | CsvValidationException exception) {
            log.debug("loadAntigenTestsFromFile failed: {}", exception.getMessage());
            log.error("loadAntigenTestsFromFile failed");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return records;
    }

    private File loadAntigenTestsFromBfArM() {
        // TODO impl
        return null;
    }
}

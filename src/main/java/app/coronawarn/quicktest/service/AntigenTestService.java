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
import java.nio.charset.Charset;
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

    private List<AntigenTest> antigenTests = new ArrayList();
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

    protected void setAntigenTests(InputStream inputStreamCSV) {
        List<List<String>> unFilteredAntigentest = createAntigenTestListFromCsvInputStream(inputStreamCSV);
        antigenTests = mapToAntigenTest(filterAntigenTests(unFilteredAntigentest));
        lastUpdate = Utilities.getCurrentLocalDateTimeUtc();
    }

    private void init() {
        InputStream inputStreamCSV = loadAntigenTestsFromBfArM();
        if (inputStreamCSV == null) {
            final ClassPathResource classPathResource = new ClassPathResource("antigentests.csv");
            inputStreamCSV = Objects.requireNonNull(
                    Objects.requireNonNull(classPathResource.getClassLoader())
                            .getResourceAsStream("antigentests.csv"));
        }
        setAntigenTests(inputStreamCSV);
    }

    private List<AntigenTest> mapToAntigenTest(List<List<String>> rawAntigenTests) {
        List<AntigenTest> antigenTests = new ArrayList();
        for (List<String> row : rawAntigenTests) {
            antigenTests.add(new AntigenTest(row.get(0), row.get(1)));
        }
        return antigenTests;
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
            CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStreamCsv, Charset.defaultCharset()))
                    .withSkipLines(1)
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

    private InputStream loadAntigenTestsFromBfArM() {
        // TODO impl
        return null;
    }
}

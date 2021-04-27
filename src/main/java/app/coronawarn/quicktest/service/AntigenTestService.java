package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.domain.AntigenTest;
import app.coronawarn.quicktest.utils.Utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AntigenTestService {

    private List<List<String>> antigenTests = new ArrayList();

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    private LocalDateTime lastUpdate = Utilities.getCurrentLocalDateTimeUtc();

    /**
     * Method for receiving antigen tests.
     *
     * @return antigen tests
     */
    public List<List<String>> antigenTests() throws ResponseStatusException {
        log.debug("Response antigenTests");
        if (antigenTests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return antigenTests;
        }
    }

    protected void loadAntigenTests() {
        // TODO change to loadAntigenTestsFromBfArM
        List<List<String>> unFilteredAntigentest = loadAntigenTestsFromFile();
        antigenTests = filterAntigenTests(unFilteredAntigentest);
        lastUpdate = Utilities.getCurrentLocalDateTimeUtc();
    }

    private List<List<String>> filterAntigenTests(List<List<String>> unFilteredAntigentest) {
        return null;
    }

    private List<List<String>> loadAntigenTestsFromFile() {
        List<List<String>> records = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader("antigenttest.csv"));) {
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

    private ArrayList<AntigenTest> loadAntigenTestsFromBfArM() {
        // TODO impl
        return null;
    }
}

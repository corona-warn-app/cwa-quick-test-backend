package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.client.RkiToolClient;
import app.coronawarn.quicktest.model.TransmittingSites;
import app.coronawarn.quicktest.model.TransmittingSites.SearchText;
import app.coronawarn.quicktest.model.TransmittingSites.TransmittingSite;
import app.coronawarn.quicktest.utils.Utilities;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import feign.FeignException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthDepartmentService {

    private final RkiToolClient rkiToolClient;

    private final Map<String, String> map = new HashMap<>();
    private List<TransmittingSite> healthDepartments = new ArrayList<>();
    private LocalDateTime lastUpdated = Utilities.getCurrentLocalDateTimeUtc();

    /**
     * Loads health departments (on app start) from RKI. Using local backup file as fallback if download fails.
     * TODO: refresh
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            InputStream inputStream = downloadFileFromRki();
            if (inputStream == null) {
                inputStream = loadFallbackFile();
            }
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry ze = zis.getNextEntry();
            if (ze.isDirectory()) {
                throw new Exception();
            }
            XmlMapper xmlMapper = new XmlMapper();
            healthDepartments = xmlMapper.readValue(zis, TransmittingSites.class).getTransmittingSites();
            lastUpdated = Utilities.getCurrentLocalDateTimeUtc();
        } catch (Exception e) {
            log.error("Could not create healthDepartment list");
            System.out.println(e.toString());
        }
    }

    private InputStream downloadFileFromRki() {
        try {
            return rkiToolClient.downloadFile().body().asInputStream();
        } catch (FeignException | IOException e) {
            log.error("Could not fetch new health department data, using fallback data file");
            return null;
        }
    }

    private InputStream loadFallbackFile() throws FileNotFoundException {
        InputStream inputStream = Objects.requireNonNull(new ClassPathResource("departments.zip").getClassLoader())
                .getResourceAsStream("departments.zip");
        if (inputStream == null) {
            log.error("Could not load health departments from data file");
            throw new FileNotFoundException();
        }
        return inputStream;
    }

    /**
     * Finds responsible health department email address by zip code in xml document provided by RKI.
     * @param zipCode Zip code of PoC
     * @return Email address of responsible health department
     */
    public String findHealthDepartmentEmailByZipCode(String zipCode) {
        if (map.containsKey(zipCode)) {
            return map.get(zipCode);
        }
        for (TransmittingSite healthDepartment : healthDepartments) {
            for (SearchText searchText : healthDepartment.getSearchTexts()) {
                if (searchText.getValue().equals(zipCode)) {
                    String email = StringUtils.isBlank(healthDepartment.getCovid19EMail())
                            ? healthDepartment.getEmail() : healthDepartment.getCovid19EMail();
                    map.put(zipCode, email);
                    return email;
                }
            }
        }
        return "";
    }

}

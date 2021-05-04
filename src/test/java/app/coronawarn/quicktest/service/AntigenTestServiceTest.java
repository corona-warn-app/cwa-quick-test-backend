package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.domain.AntigenTest;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
class AntigenTestServiceTest {

    @InjectMocks
    private AntigenTestService antigenTestService;

    @Test
    void getAntigenTest() {
        List<AntigenTest> antigenTests = antigenTestService.getAntigenTests();
        assertEquals(13, antigenTests.size());
        assertEquals(expectedResult(), antigenTests.toString());
    }

    @Test
    void updateAntigenTestsByCsv() throws IOException {
        final ClassPathResource classPathResource = new ClassPathResource("antigentestsupdate.csv");
        InputStream inputStreamCSV = Objects.requireNonNull(
                Objects.requireNonNull(classPathResource.getClassLoader())
                        .getResourceAsStream("antigentestsupdate.csv"));
        MultipartFile multipartFile = new MockMultipartFile("file",
                "Unittest", "text/csv", IOUtils.toByteArray(inputStreamCSV));
        List<AntigenTest> antigenTests = antigenTestService.getAntigenTests();
        assertEquals(13, antigenTests.size());
        antigenTestService.updateAntigenTestsByCsv(multipartFile);
        antigenTests = antigenTestService.getAntigenTests();
        assertEquals(4, antigenTests.size());
    }

    private String expectedResult() {
        return "[AntigenTest(testBrandId=AT116/21, testBrandName=Panbio (TM) Covid-19 Ag Rapid Test Device "
                + "(Nasal)), AntigenTest(testBrandId=AT018/21, testBrandName=Cora Gentest-19), "
                + "AntigenTest(testBrandId=AT810/21, testBrandName=ACCU-TELL SARS-CoV-2-Ag Cassette (Nasal Swab)),"
                + " AntigenTest(testBrandId=AT084/21, testBrandName=ACCU-TELL SARS-CoV-2-Ag Cassette), "
                + "AntigenTest(testBrandId=AT579/21, testBrandName=ACCU-TELL SARS-CoV-2-Ag Cassette Saliva), "
                + "AntigenTest(testBrandId=AT155/20, testBrandName=SARS-CoV-2-Antigenschnelltest), "
                + "AntigenTest(testBrandId=AT434/21, testBrandName=FIAflex SARS-CoV-2 Antigen FIA), "
                + "AntigenTest(testBrandId=AT495/20, testBrandName=Swiss Point of Care SARS-CoV-2 Antigen Rapid Test)"
                + ", AntigenTest(testBrandId=AT874/21, testBrandName=Flowflex SARS-CoV-2 Antigen Rapid Test "
                + "(Nasal/Saliva)), AntigenTest(testBrandId=AT261/21, testBrandName=Flowflex SARS-CoV-2 "
                + "Antigen Rapid Test), AntigenTest(testBrandId=AT594/21, testBrandName=FIAflex SARS-CoV-2"
                + " Antigen FIA), AntigenTest(testBrandId=AT503/20, testBrandName=SARS-CoV-2-Antigenschnelltest),"
                + " AntigenTest(testBrandId=AT542/21, testBrandName=LITUO COVID-19 Antigen Detection Kit (CG))]";
    }
}
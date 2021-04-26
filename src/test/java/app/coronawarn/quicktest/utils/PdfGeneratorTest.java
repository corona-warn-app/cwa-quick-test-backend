package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class PdfGeneratorTest {

    @InjectMocks
    private PdfGenerator pdfGenerator;
    @Mock
    private PdfConfig pdfConfig;

    @Test
    void generatePdfTest() throws IOException {
        when(pdfConfig.getLogoPath()).thenReturn("/logo");
        when(pdfConfig.getAuthorPdfPropertiesText()).thenReturn("Unittest");
        when(pdfConfig.getQuickTestHeadlineText()).thenReturn("Unittest");
        when(pdfConfig.getCreatorPdfPropertiesText()).thenReturn("Rapid Test");
        when(pdfConfig.getFurtherDataAboutThePersonText()).thenReturn("Mehr Informationen");
        when(pdfConfig.getFurtherDataAboutTestDescriptionText()).thenReturn("Mehr Informationen");
        when(pdfConfig.getSignatureText()).thenReturn("MFG");
        List<String> pocInformation = new ArrayList();
        pocInformation.add("PoC Unittest");
        pocInformation.add("Unittest Way 15");
        pocInformation.add("10101 Unittest City");
        pocInformation.add("Call: 0123-7890-0");
        QuickTest quicktest = getQuickTest();
        String user = "Mr. Unittest";
        ByteArrayOutputStream file = pdfGenerator.generatePdf(pocInformation, quicktest, user);

        assertEquals(firstPartPdf(), file.toString().substring(0, 427).replaceAll("\\p{C}", "?"));
        assertEquals(middlePartPdf(), file.toString().substring(772, 1313).replaceAll("\\p{C}", "?"));
        assertEquals(lastPartPdf(), file.toString().substring(1380).replaceAll("\\p{C}", "?"));
        assertEquals(streamPartOnePdf(), file.toString().substring(421, 468).replaceAll("\\p{C}", "?"));
        assertEquals(streamPartTwoPdf(), file.toString().substring(470, 496).replaceAll("\\p{C}", "?"));
        assertEquals(streamPartTreePdf(), file.toString().substring(505, 522).replaceAll("\\p{C}", "?"));
        assertEquals(streamPartFourPdf(), file.toString().substring(528, 781).replaceAll("\\p{C}", "?"));

    }

    private QuickTest getQuickTest() {
        QuickTest quicktest = new QuickTest();
        quicktest.setZipCode("12345");
        quicktest.setTestResult(Short.parseShort("5"));
        quicktest.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quicktest.setCity("oyvkpigcga");
        quicktest.setConfirmationCwa(Boolean.TRUE);
        quicktest.setShortHashedGuid("cjfybkfn");
        quicktest.setPhoneNumber("00491777777777777");
        quicktest.setEmail("test@test.test");
        quicktest.setTenantId("4711");
        quicktest.setPocId("4711-A");
        quicktest.setTestBrandId("AT116/21");
        quicktest.setTestBrandName("Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)");
        quicktest.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        quicktest.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quicktest.setFirstName("Joe");
        quicktest.setLastName("Miller");
        quicktest.setStreet("Boe");
        quicktest.setHouseNumber("11");
        quicktest.setPrivacyAgreement(Boolean.FALSE);
        quicktest.setSex(Sex.DIVERSE);
        return quicktest;
    }

    private String firstPartPdf() {
        return "%PDF-1.4?%����?1 0 obj?<<?/Type /Catalog?/Version /1.4?/Pages 2 0 R?>>?endobj" +
                "?3 0 obj?<<?/Author (Unittest)?/Title (Unittest)?/Creator (Rapid Test)?/Creation" +
                "Date (D:20210426000000+02'00')?>>?endobj?2 0 obj?<<?/Type /Pages?/Kids [4 0 R]?/Cou" +
                "nt 1?>>?endobj?4 0 obj?<<?/Type /Page?/MediaBox [0.0 0.0 595.27563 841.8898]?/Parent 2" +
                " 0 R?/Contents 5 0 R?/Resources 6 0 R?>>?endobj?5 0 obj?<<?/Length 363?/Filter /FlateDe" +
                "code?>>?stream";
    }
    private String streamPartOnePdf() {
        return "stream??x��R]O�0?}�W�GfB�[�G���h�,�Ɨ�ԍ�*��p����";
    }
    private String streamPartTwoPdf() {
        return "�&?%i�9��sҙ��s?d 7?r⁼�(?.�";
    }
    private String streamPartTreePdf() {
        return "��g@뭽�#x�tY&�r?��";
    }
    private String streamPartFourPdf() {
        return "�?/?��pZ<<�#���LPMC��B�nCCZ��0�tyl�&�D??5Ri:?��u�PP��D1 ^Kk�O���?���o?f" +
                "��6����z��wy?�NӤ8�I��:B축6�A~<���v�U]!�LSJ����?�Z�e�#���?v�ͣ}m�?!��0�*" +
                "'t�8A6��G�?���T�{�??'�?�f���P�γ$3�g�?�?W�-L%�?fh?.T��sX�2^� �?z��??�N�A" +
                "�w�*9�UR�?�^��QOM���W?�x~Ӌ�?Ƭ�???endstream";

}
    private String middlePartPdf() {
        return "endstream?endobj?6 0 obj?<<?/Font 7 0 R?>>?endobj?7 0 obj?<<?/F1 8 0 R?/F2 9 0 R?" +
                ">>?endobj?8 0 obj?<<?/Type /Font?/Subtype /Type1?/BaseFont /Helvetica?/Encoding " +
                "/WinAnsiEncoding?>>?endobj?9 0 obj?<<?/Type /Font?/Subtype /Type1?/BaseFont /Helvet" +
                "ica-Bold?/Encoding /WinAnsiEncoding?>>?endobj?xref?0 10?0000000000 65535 f??0000000015" +
                " 00000 n??0000000198 00000 n??0000000078 00000 n??0000000255 00000 n??0000000374 00000" +
                " n??0000000811 00000 n??0000000844 00000 n??0000000885 00000 n??0000000982 00000 n??tra" +
                "iler?<<?/Root 1 0 R?/Info 3 0 R?/ID [<";
    }
    private String lastPartPdf() {
        return ">]?/Size 10?>>?startxref?1084?%%EOF?";
    }
}

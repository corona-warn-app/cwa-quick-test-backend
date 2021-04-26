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
        assertTrue(file.toString().contains(firstPartPdf()));

        // assertTrue(file.toString().contains(streamPartOnePdf()));
        // assertTrue(file.toString().contains(streamPartTwoPdf()));
        // assertTrue(file.toString().contains(streamPartTreePdf()));
        // assertTrue(file.toString().contains(streamPartFourPdf()));
        log.info(file.toString());
        log.info(middlePartPdf());
        assertTrue(file.toString().contains(middlePartPdf()));
        assertTrue(file.toString().contains(lastPartPdf()));


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
        return "%PDF-1.4\n" +
                "%����\n" +
                "1 0 obj\n" +
                "<<\n" +
                "/Type /Catalog\n" +
                "/Version /1.4\n" +
                "/Pages 2 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<<\n" +
                "/Author (Unittest)\n" +
                "/Title (Unittest)\n" +
                "/Creator (Rapid Test)\n" +
                "/CreationDate (D:20210426000000+02'00')\n" +
                ">>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<<\n" +
                "/Type /Pages\n" +
                "/Kids [4 0 R]\n" +
                "/Count 1\n" +
                ">>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<<\n" +
                "/Type /Page\n" +
                "/MediaBox [0.0 0.0 595.27563 841.8898]\n" +
                "/Parent 2 0 R\n" +
                "/Contents 5 0 R\n" +
                "/Resources 6 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "5 0 obj\n" +
                "<<\n" +
                "/Length 363\n" +
                "/Filter /FlateDecode\n" +
                ">>\n" +
                "stream";
    }
    private String streamPartOnePdf() {
        return "stream\n" +
                "x��R]O�0}�W�GfB�[�G���h�,�Ɨ�ԍ�*��p����";
    }
    private String streamPartTwoPdf() {
        return "�&%i�9��sҙ��sd 7r⁼�(.�";
    }
    private String streamPartTreePdf() {
        return "��g@뭽�#x�tY&�r��";
    }
    private String streamPartFourPdf() {
        return "�/��pZ<<�#���LPMC��B�nCCZ��0�tyl�&�D5Ri:��u�PP��D1 ^Kk�O������of��6����z��wy�NӤ8�I��:B축6�A~<���v�U]!�LSJ�����Z�e�#���v�ͣ}m�!��0�*'t�8A6��G����T�{�'��f���P�γ$3�g��゘W�-L%�?fh.T��sX�2^� �z���N�A�w�*9�UR��^��QOM���W?�x~Ӌ�Ƭ�\n" +
                "endstream\n";

}
    private String middlePartPdf() {
        return "endstream\n" +
                "endobj\n" +
                "6 0 obj\n" +
                "<<\n" +
                "/Font 7 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "7 0 obj\n" +
                "<<\n" +
                "/F1 8 0 R\n" +
                "/F2 9 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "8 0 obj\n" +
                "<<\n" +
                "/Type /Font\n" +
                "/Subtype /Type1\n" +
                "/BaseFont /Helvetica\n" +
                "/Encoding /WinAnsiEncoding\n" +
                ">>\n" +
                "endobj\n" +
                "9 0 obj\n" +
                "<<\n" +
                "/Type /Font\n" +
                "/Subtype /Type1\n" +
                "/BaseFont /Helvetica-Bold\n" +
                "/Encoding /WinAnsiEncoding\n" +
                ">>\n" +
                "endobj\n" +
                "xref\n" +
                "0 10\n" +
                "0000000000 65535 f\n" +
                "0000000015 00000 n\n" +
                "0000000198 00000 n\n" +
                "0000000078 00000 n\n" +
                "0000000255 00000 n\n" +
                "0000000374 00000 n\n" +
                "0000000811 00000 n\n" +
                "0000000844 00000 n\n" +
                "0000000885 00000 n\n" +
                "0000000982 00000 n\n" +
                "trailer\n" +
                "<<\n" +
                "/Root 1 0 R\n" +
                "/Info 3 0 R\n" +
                "/ID [<";
    }
    private String lastPartPdf() {
        return ">]\n" +
                "/Size 10\n" +
                ">>\n" +
                "startxref\n" +
                "1084\n" +
                "%%EOF";
    }

    private String fullLog() {
        return "%PDF-1.4\n" +
                "%����\n" +
                "1 0 obj\n" +
                "<<\n" +
                "/Type /Catalog\n" +
                "/Version /1.4\n" +
                "/Pages 2 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "3 0 obj\n" +
                "<<\n" +
                "/Author (Unittest)\n" +
                "/Title (Unittest)\n" +
                "/Creator (Rapid Test)\n" +
                "/CreationDate (D:20210426000000+02'00')\n" +
                ">>\n" +
                "endobj\n" +
                "2 0 obj\n" +
                "<<\n" +
                "/Type /Pages\n" +
                "/Kids [4 0 R]\n" +
                "/Count 1\n" +
                ">>\n" +
                "endobj\n" +
                "4 0 obj\n" +
                "<<\n" +
                "/Type /Page\n" +
                "/MediaBox [0.0 0.0 595.27563 841.8898]\n" +
                "/Parent 2 0 R\n" +
                "/Contents 5 0 R\n" +
                "/Resources 6 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "5 0 obj\n" +
                "<<\n" +
                "/Length 363\n" +
                "/Filter /FlateDecode\n" +
                ">>\n" +
                "stream\n" +
                "x��R]O�0\u0014}�W�GfB�[�G���h�,�Ɨ�ԍ�*��p���� R�&\u0002%i�9��sҙ��s\u0004d 7\u0016r⁼�(\u0004.�0\u0014A rmq\u001E\u0010��g@뭽�#x�tY&�r\u0004��ryaq�\u0012/\u0010��pZ<<�#���LPMC��B�nCCZ��0�tyl�&�D\u0015\u00065Ri:\u0001��u�PP��D1 ^Kk�O���\u007F���o\u001Bf��6����z��wy\u0002�NӤ8�I��:B축6�A~<���v�U]!�LSJ����\u0018�Z�e�#���\u0015v�ͣ}m�\u0001!��0�*'t�8A6��G�\u000F���T�{�\u001A\u0012'�\u0005�f���P�γ$3�g�\u007F�\u3098W�-L%�?fh\u001C.T��sX�2^� �\u000Fz��\u0016\u001E�N�A�w�*9�UR�\u001E�^��QOM���W?�x~Ӌ�\u000BƬ�\n" +
                "endstream\n" +
                "endobj\n" +
                "6 0 obj\n" +
                "<<\n" +
                "/Font 7 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "7 0 obj\n" +
                "<<\n" +
                "/F1 8 0 R\n" +
                "/F2 9 0 R\n" +
                ">>\n" +
                "endobj\n" +
                "8 0 obj\n" +
                "<<\n" +
                "/Type /Font\n" +
                "/Subtype /Type1\n" +
                "/BaseFont /Helvetica\n" +
                "/Encoding /WinAnsiEncoding\n" +
                ">>\n" +
                "endobj\n" +
                "9 0 obj\n" +
                "<<\n" +
                "/Type /Font\n" +
                "/Subtype /Type1\n" +
                "/BaseFont /Helvetica-Bold\n" +
                "/Encoding /WinAnsiEncoding\n" +
                ">>\n" +
                "endobj\n" +
                "xref\n" +
                "0 10\n" +
                "0000000000 65535 f\n" +
                "0000000015 00000 n\n" +
                "0000000198 00000 n\n" +
                "0000000078 00000 n\n" +
                "0000000255 00000 n\n" +
                "0000000374 00000 n\n" +
                "0000000811 00000 n\n" +
                "0000000844 00000 n\n" +
                "0000000885 00000 n\n" +
                "0000000982 00000 n\n" +
                "trailer\n" +
                "<<\n" +
                "/Root 1 0 R\n" +
                "/Info 3 0 R\n" +
                "/ID [<277247B225235DFBDCCF85B16B3B08BA> <277247B225235DFBDCCF85B16B3B08BA>]\n" +
                "/Size 10\n" +
                ">>\n" +
                "startxref\n" +
                "1084\n" +
                "%%EOF";
    }
}

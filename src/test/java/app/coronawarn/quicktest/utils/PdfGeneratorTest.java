package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PdfGeneratorTest {

    @InjectMocks
    private PdfGenerator pdfGenerator;
    @Mock
    private PdfConfig pdfConfig;

    @BeforeAll
    void beforeAll(){
        when(pdfConfig.getLogoPath()).thenReturn("/logo");
        when(pdfConfig.getAuthorPdfPropertiesText()).thenReturn("Unittest");
        when(pdfConfig.getQuickTestHeadlineText()).thenReturn("Unittest");
        when(pdfConfig.getCreatorPdfPropertiesText()).thenReturn("Rapid Test");
        when(pdfConfig.getFurtherDataAboutThePersonText()).thenReturn("Mehr Informationen zur Person");
        when(pdfConfig.getFurtherDataAboutTestDescriptionText()).thenReturn("Mehr Informationen zum Test");
        when(pdfConfig.getSignatureText()).thenReturn("MFG");
        when(pdfConfig.getPersonEmailDescriptionText()).thenReturn("Email: ");
        when(pdfConfig.getPersonPhoneDescriptionText()).thenReturn("Telefon: ");
        when(pdfConfig.getQuickTestOfDateText()).thenReturn("Erstellt am: ");
        when(pdfConfig.getTestResultDescriptionText()).thenReturn("Ergebnis: ");
        when(pdfConfig.getNegativeInstructionText()).thenReturn("Sie sind nagativ getestet worden.");
        when(pdfConfig.getExecutedByDescriptionText()).thenReturn("Ausgeführt am: ");
        when(pdfConfig.getExecutedFromDescriptionText()).thenReturn("Durchgeführt von: ");
        when(pdfConfig.getGenderDescriptionText()).thenReturn("Geschlecht: ");
        when(pdfConfig.getTestBrandNameDescriptionText()).thenReturn("Test Marke: ");
        when(pdfConfig.getTestBrandIdDescriptionText()).thenReturn("Test ID: ");
        when(pdfConfig.getDiverseText()).thenReturn("divers");
        when(pdfConfig.getTestResultNegativeText()).thenReturn("NEGATIV");
        when(pdfConfig.getBirthDateDescriptionText()).thenReturn("Geburtsdatum: ");
    }

    @Test
    void generatePdfTest() throws IOException {
        List<String> pocInformation = new ArrayList<>();
        pocInformation.add("PoC Unittest");
        pocInformation.add("Unittest Way 15");
        pocInformation.add("10101 Unittest City");
        pocInformation.add("Call: 0123-7890-0");
        QuickTest quicktest = getQuickTest();
        String user = "Mr. Unittest";
        ByteArrayOutputStream file = pdfGenerator.generatePdf(pocInformation, quicktest, user);
        PDDocument pdfDocument = PDDocument.load(file.toByteArray());
        try {
            String pdfText = new PDFTextStripper().getText(pdfDocument);
            assertTrue(pdfText.contains("Unittest"));
            assertTrue(pdfText.contains("PoC Unittest"));
            assertTrue(pdfText.contains("Unittest Way 15"));
            assertTrue(pdfText.contains("10101 Unittest City"));
            assertTrue(pdfText.contains("Call: 0123-7890-0"));
            assertTrue(pdfText.contains("Joe Miller"));
            assertTrue(pdfText.contains("Boe 11"));
            assertTrue(pdfText.contains("12345 oyvkpigcga"));
            assertTrue(pdfText.contains("Telefon: 00491777777777777"));
            assertTrue(pdfText.contains("Email: test@test.test"));
            assertTrue(pdfText.contains("Erstellt am: 08.04.2021 10:11:12"));
            assertTrue(pdfText.contains("Ergebnis: NEGATIV"));
            assertTrue(pdfText.contains("Ausgeführt am: 08.04.2021 10:11:12"));
            assertTrue(pdfText.contains("Mehr Informationen zur Person"));
            assertTrue(pdfText.contains("Geschlecht: divers"));
            assertTrue(pdfText.contains("Geburtsdatum: 11.11.1911"));
            assertTrue(pdfText.contains("Mehr Informationen zum Test"));
            assertTrue(pdfText.contains("Durchgeführt von: Mr. Unittest"));
            assertTrue(pdfText.contains("Test ID: AT116/21"));
            assertTrue(pdfText.contains("Test Marke: Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)"));
            assertTrue(pdfText.contains("Sie sind nagativ getestet worden."));
            assertTrue(pdfText.contains("MFG"));
            assertEquals("Unittest", pdfDocument.getDocumentInformation().getAuthor());
            assertEquals("Rapid Test", pdfDocument.getDocumentInformation().getCreator());
        } finally {
            pdfDocument.close();
        }


    }

    @Test
    void encryptPdfTest() throws IOException {
        byte[] pdf = pdfGenerator.generatePdf(Collections.emptyList(), getQuickTest(), "user").toByteArray();
        byte[] encryptedPdf = pdfGenerator.encryptPdf(pdf, getQuickTest().getZipCode()).toByteArray();
        PDDocument encPdfDoc = PDDocument.load(encryptedPdf, "12345");
        assertTrue(encPdfDoc.isEncrypted());
        assertEquals(encPdfDoc.getEncryption().getLength(), 256);
        assertTrue(encPdfDoc.getEncryption().isEncryptMetaData());
        AccessPermission ap = encPdfDoc.getCurrentAccessPermission();
        assertTrue(ap.canPrint());
        assertFalse(ap.canModify());
        assertFalse(ap.canAssembleDocument());
        assertFalse(ap.canFillInForm());
        assertFalse(ap.canModify());
        assertFalse(ap.canModifyAnnotations());
        assertFalse(ap.canExtractForAccessibility());
        assertFalse(ap.canPrintDegraded());
        assertFalse(ap.isOwnerPermission());
    }

    private QuickTest getQuickTest() {
        QuickTest quicktest = new QuickTest();
        quicktest.setZipCode("12345");
        quicktest.setTestResult(Short.parseShort("6"));
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
        quicktest.setBirthday("1911-11-11");
        return quicktest;
    }
}

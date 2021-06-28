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

package app.coronawarn.quicktest.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
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

        List<String> pocInformation = new ArrayList();
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
            PDPage page = pdfDocument.getPage(1);
            generateQRCode(pdfDocument, page,
                    "HC1:6BF-606A0T9WTWGSLKC 4X7923S%CA.48Y+6TAB3XK2F310RT012F3LMQ1001JC X8Y50.FK8ZKO/EZKEZ967L6C56." +
                            ".DU%DLPCG/DS2DHIA5Y8GY8JPCT3E5JDOA73467463W5207ZWERIL9WEQDD+Q6TW6FA7C464KCCWE6T9OF6:/6NA76W5." +
                            "JC2EC+96-Q63KCZPCNF6OF63W59%6PF6.SA*479L61G73564KC*KETF6A46.96646B565WET.D6$CBWE3/DO341$CKWEY " +
                            "CUPC1JC%N9+EDIPDCECRTCWH8.KEZEDWJC0FD6A5AIA%G7X+AQB9F+ALG7$X85G6+%6UB8AY8VS8VNAJ*8A1A*" +
                            "CBYB9UY9UB8%6A27BT3DC6CRHQ:FQSBG6X2MQE PIUIJ+Q83%3.KBJD7N5T+GUIIJT-MFWT*$0CQ7P5C4UQHF8F." +
                            "EC4D78J.2K$KQDIDIQRVS8A4KF5QM:D",
                    page.getCropBox().getWidth() / 4 * 3 - (150 / 2), page.getCropBox().getHeight() - 200);
        } finally {
            pdfDocument.save("C:/tmp/pdf/test.pdf");
            pdfDocument.close();
        }


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

    private static void generateQRCode(PDDocument document, PDPage page, String text, float x, float y) {
        try {
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(text.getBytes("UTF-8"), "UTF-8"),
                    BarcodeFormat.QR_CODE, 150, 150, hintMap);

            MatrixToImageConfig config = new MatrixToImageConfig(0xFF000001, 0xFFFFFFFF);
            BufferedImage bImage = MatrixToImageWriter.toBufferedImage(matrix, config);
            PDImageXObject image = JPEGFactory.createFromImage(document, bImage);
            contentStream.drawImage(image, x, y, 150, 150);
            contentStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

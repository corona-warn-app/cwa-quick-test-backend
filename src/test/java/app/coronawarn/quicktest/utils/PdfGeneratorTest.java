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

import static app.coronawarn.quicktest.utils.QuicktestUtils.getQuickTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
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
        PdfConfig pdc = new PdfConfig();
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
        ByteArrayOutputStream file1 = pdfGenerator.generatePdf(pocInformation, quicktest, user);

        PDDocument pdfDocument = PDDocument.load(file1.toByteArray());
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
}

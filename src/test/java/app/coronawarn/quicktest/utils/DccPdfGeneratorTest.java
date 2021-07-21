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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class DccPdfGeneratorTest {

    @InjectMocks
    private DccPdfGenerator pdfGenerator;
    @Mock
    private PdfConfig pdfConfig;

    @Test
    void appendDccPageTest() throws IOException {
        PdfConfig pdc = new PdfConfig();

        when(pdfConfig.getCertQrDescription()).thenReturn(pdc.getCertQrDescription());
        when(pdfConfig.getCertMemberStateDescriptionDe()).thenReturn(pdc.getCertMemberStateDescriptionDe());
        when(pdfConfig.getCertMemberStateDescriptionEn()).thenReturn(pdc.getCertMemberStateDescriptionEn());
        when(pdfConfig.getCertMemberStateFurtherDescription()).thenReturn(pdc.getCertMemberStateFurtherDescription());
        when(pdfConfig.getCertDiseaseAgentDe()).thenReturn(pdc.getCertDiseaseAgentDe());
        when(pdfConfig.getCertDiseaseAgentEn()).thenReturn(pdc.getCertDiseaseAgentEn());
        when(pdfConfig.getCertLineSeparator()).thenReturn(pdc.getCertLineSeparator());
        when(pdfConfig.getCertCertlogoPath()).thenReturn(pdc.getCertCertlogoPath());
        when(pdfConfig.getCertFlagPath()).thenReturn(pdc.getCertFlagPath());
        when(pdfConfig.getCertFlagSeparatorPath()).thenReturn(pdc.getCertFlagSeparatorPath());
        when(pdfConfig.getCertHeaderTestEn()).thenReturn(pdc.getCertHeaderTestEn());
        when(pdfConfig.getCertHeaderTestDe()).thenReturn(pdc.getCertHeaderTestDe());
        when(pdfConfig.getCertTestNameEn()).thenReturn(pdc.getCertTestNameEn());
        when(pdfConfig.getCertTestNameDe()).thenReturn(pdc.getCertTestNameDe());
        when(pdfConfig.getCertDateSampleCollectionEn()).thenReturn(pdc.getCertDateSampleCollectionEn());
        when(pdfConfig.getCertDateSampleCollectionDe()).thenReturn(pdc.getCertDateSampleCollectionDe());
        when(pdfConfig.getCertTestManufacturerEn()).thenReturn(pdc.getCertTestManufacturerEn());
        when(pdfConfig.getCertTestManufacturerDe()).thenReturn(pdc.getCertTestManufacturerDe());
        when(pdfConfig.getCertTestResultEn()).thenReturn(pdc.getCertTestResultEn());
        when(pdfConfig.getCertTestResultDe()).thenReturn(pdc.getCertTestResultDe());
        when(pdfConfig.getCertTestingCentreEn()).thenReturn(pdc.getCertTestingCentreEn());
        when(pdfConfig.getCertTestingCentreDe()).thenReturn(pdc.getCertTestingCentreDe());
        when(pdfConfig.getCertStateOfTestEn()).thenReturn(pdc.getCertStateOfTestEn());
        when(pdfConfig.getCertStateOfTestDe()).thenReturn(pdc.getCertStateOfTestDe());
        when(pdfConfig.getCertIssuerEn()).thenReturn(pdc.getCertIssuerEn());
        when(pdfConfig.getCertIssuerDe()).thenReturn(pdc.getCertIssuerDe());
        when(pdfConfig.getCertTestTypeEn()).thenReturn(pdc.getCertTestTypeEn());
        when(pdfConfig.getCertTestTypeDe()).thenReturn(pdc.getCertTestTypeDe());
        when(pdfConfig.getCertCertIdDe()).thenReturn(pdc.getCertCertIdDe());
        when(pdfConfig.getCertCertIdEn()).thenReturn(pdc.getCertCertIdEn());
        when(pdfConfig.getCertNameDe()).thenReturn(pdc.getCertNameDe());
        when(pdfConfig.getCertNameEn()).thenReturn(pdc.getCertNameEn());
        when(pdfConfig.getCertBirthdayDe()).thenReturn(pdc.getCertBirthdayDe());
        when(pdfConfig.getCertBirthdayEn()).thenReturn(pdc.getCertBirthdayEn());
        when(pdfConfig.getCertTestType()).thenReturn(pdc.getCertTestType());
        when(pdfConfig.getCertIssuerState()).thenReturn(pdc.getCertIssuerState());
        when(pdfConfig.getCertDiseaseAgentTargeted()).thenReturn(pdc.getCertDiseaseAgentTargeted());


        QuickTest quicktest = getQuickTest();
        String dcc = "HC1:6BF-606A0T9WTWGSLKC 4X7923S%CA.48Y+6TAB3XK2F310RT012F3LMQ1001JC X8Y50.FK8ZKO/EZKEZ967L6C56." +
          ".DU%DLPCG/DS2DHIA5Y8GY8JPCT3E5JDOA73467463W5207ZWERIL9WEQDD+Q6TW6FA7C464KCCWE6T9OF6:/6NA76W5." +
          "JC2EC+96-Q63KCZPCNF6OF63W59%6PF6.SA*479L61G73564KC*KETF6A46.96646B565WET.D6$CBWE3/DO341$CKWEY " +
          "CUPC1JC%N9+EDIPDCECRTCWH8.KEZEDWJC0FD6A5AIA%G7X+AQB9F+ALG7$X85G6+%6UB8AY8VS8VNAJ*8A1A*" +
          "CBYB9UY9UB8%6A27BT3DC6CRHQ:FQSBG6X2MQE PIUIJ+Q83%3.KBJD7N5T+GUIIJT-MFWT*$0CQ7P5C4UQHF8F." +
          "EC4D78J.2K$KQDIDIQRVS8A4KF5QM:D";

        ByteArrayOutputStream pdf = createFirstPagePdf();
        ByteArrayOutputStream file = pdfGenerator.appendCertificatePage(pdf.toByteArray(), quicktest, dcc);

        PDDocument pdfDocument = PDDocument.load(file.toByteArray());
        try {
            String pdfText = new PDFTextStripper().getText(pdfDocument);
            assertTrue(pdfText.contains(pdc.getCertDiseaseAgentDe()));
            assertTrue(pdfText.contains(pdc.getCertDiseaseAgentEn()));
            assertTrue(pdfText.contains(pdc.getCertHeaderTestEn()));
            assertTrue(pdfText.contains(pdc.getCertNameDe()));
            assertTrue(pdfText.contains(pdc.getCertNameEn()));
            assertTrue(pdfText.contains(pdc.getCertIssuerEn()));
            assertTrue(pdfText.contains(pdc.getCertIssuerDe()));
            assertTrue(pdfText.contains(quicktest.getFirstName()));
            assertTrue(pdfText.contains(quicktest.getLastName()));
            assertTrue(pdfText.contains(pdc.getCertDiseaseAgentTargeted()));
        } finally {
            pdfDocument.close();
        }
    }

    private ByteArrayOutputStream createFirstPagePdf() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page1 = new PDPage(PDRectangle.A4);
        document.addPage(page1);
        page1.setMediaBox(PDRectangle.A4);
        PDPageContentStream cos = new PDPageContentStream(document, page1);
        cos.setFont(PDType1Font.HELVETICA, 12f);
        cos.beginText();
        cos.showText("Anschreiben Test");
        cos.endText();
        cos.close();
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        document.save(pdf);
        document.close();
        return pdf;
    }


}
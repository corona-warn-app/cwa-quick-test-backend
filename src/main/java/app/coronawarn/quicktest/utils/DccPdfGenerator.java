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

import static app.coronawarn.quicktest.utils.PdfUtils.splitStringToParagraph;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.dgc.DccDecodeResult;
import app.coronawarn.quicktest.dgc.DccDecoder;
import app.coronawarn.quicktest.domain.QuickTest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DccPdfGenerator {

    private final PdfConfig pdfConfig;

    private final DccDecoder dccDecoder;

    private final int pending = 5;
    private final int negativeRat = 6;
    private final int negativePcr = 11;
    private final int positiveRat = 7;
    private final int positivePcr = 12;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss '(UTC' X')'");
    private final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final float leading = 14.5f;

    private PDFont fontArial;
    private PDFont fontArialBold;
    private PDFont fontArialItalic;

    private final Color pantoneReflexBlue = Color.decode("#003399");
    private final Color pantoneYellow = Color.decode("#FFCC00");


    /**
     * Appends the EU certificate including a QR code to the pdf.
     *
     * @param quicktest      Quicktest
     * @param dcc            certificate data
     * @throws IOException   when creating pdf went wrong
     */
    public ByteArrayOutputStream appendCertificatePage(byte[] pdf, QuickTest quicktest, String dcc) throws IOException {
        PDDocument document = PDDocument.load(pdf);
        configCertPage(document);
        generateCertPage(document, quicktest, dcc);
        generateCertPageFoldable(document, quicktest, dcc);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        close(document, out);
        return out;
    }

    private void configCertPage(PDDocument document) {
        // Add Arial fonts to pdfbox
        final ClassPathResource cs = new ClassPathResource("pdf/fonts/arial.ttf");
        try {
            this.fontArial = PDType0Font.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/arial.ttf"));
            this.fontArialBold = PDType0Font.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/arialbd.ttf"));
            this.fontArialItalic = PDType0Font.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/ariali.ttf"));
        } catch (IOException e) {
            log.error("Could not load font");
        }
    }

    private void generateCertPage(PDDocument document, QuickTest quicktest, String dcc)
      throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        page.setMediaBox(PDRectangle.A4);
        PDPageContentStream cos = new PDPageContentStream(document, page);
        PDRectangle rect = page.getMediaBox();

        DccDecodeResult dccDecodeResult = dccDecoder.decodeDcc(dcc);
        generateHeadlinePage(document, cos, rect, false);
        generatePersonalInfoPage(document, cos, rect, quicktest, dccDecodeResult, false);
        generateQrCode(document, cos, rect, dcc, false);
        generateMemberStateInfoPage(document, cos, rect, false);
        generateCertificateInfoPage(cos, rect, quicktest, dccDecodeResult, false);
        cos.close();
    }

    private void generateCertPageFoldable(PDDocument document, QuickTest quicktest, String dcc)
      throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        page.setMediaBox(PDRectangle.A4);
        PDPageContentStream cos = new PDPageContentStream(document, page);
        PDRectangle rect = page.getMediaBox();

        DccDecodeResult dccDecodeResult = dccDecoder.decodeDcc(dcc);
        generateFoldings(document, cos, rect);
        generateHeadlinePage(document, cos, rect, true);
        generatePersonalInfoPage(document, cos, rect, quicktest, dccDecodeResult, true);
        generateQrCode(document, cos, rect, dcc, true);

        // Rotate upcoming pages by 180 degrees
        cos.transform(Matrix.getRotateInstance(Math.toRadians(180), rect.getWidth(), rect.getHeight() + 420));

        generateMemberStateInfoPage(document, cos, rect, true);
        generateCertificateInfoPage(cos, rect, quicktest, dccDecodeResult, true);
        cos.close();
    }

    private void generateFoldings(PDDocument document, PDPageContentStream cos, PDRectangle rect) {
        try {
            cos.setLineDashPattern(new float[]{3, 1}, 0);
            float length = 15f;
            float middleX = rect.getWidth() / 2;
            float middleY = rect.getHeight() / 2;
            float left = 5f;
            float right = rect.getWidth() - 5f;
            float top = rect.getHeight() - 5f;
            float bottom = 5f;

            cos.moveTo(left, middleY);
            cos.lineTo(left + length, middleY);
            cos.stroke();

            cos.moveTo(right, middleY);
            cos.lineTo(right - length, middleY);
            cos.stroke();

            cos.moveTo(middleX, top);
            cos.lineTo(middleX, top - length);
            cos.stroke();

            cos.moveTo(middleX, bottom);
            cos.lineTo(middleX, bottom + length);
            cos.stroke();

            cos.moveTo(middleX - length / 2, middleY);
            cos.lineTo(middleX + length / 2, middleY);
            cos.stroke();

            cos.moveTo(middleX, middleY + length / 2);
            cos.lineTo(middleX, middleY - length / 2);
            cos.stroke();

            cos.setLineDashPattern(new float[]{}, 0);
        } catch (IOException exception) {
            log.warn("Could not draw dotted folding lines");
        }
    }

    private void generateQrCode(PDDocument document, PDPageContentStream cos, PDRectangle rect, String text,
                                boolean foldable) {
        try {
            // Print QR Code on the personal info page, aligning to the center of the whole page
            float x = foldable ? mm2Point(45f) : rect.getWidth() / 2;
            float y = foldable ? mm2Point(85f) : rect.getHeight() / 2 + mm2Point(85f);
            // Set QR Code size to 6 cm in px
            // 708 -> 6cm at 300 dpi
            // 226 -> 6cm at 96 dpi
            int qrCodeSizePx = 708;
            float qrCodeImageSizePt = mm2Point(60f);

            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix matrix = new MultiFormatWriter().encode(
              new String(text.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
              BarcodeFormat.QR_CODE, qrCodeSizePx, qrCodeSizePx, hintMap);

            MatrixToImageConfig config = new MatrixToImageConfig(0xFF000001, 0xFFFFFFFF);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix, config);
            PDImageXObject image = JPEGFactory.createFromImage(document, bufferedImage);
            cos.drawImage(image, x, y, qrCodeImageSizePt, qrCodeImageSizePt);
        } catch (Exception e) {
            log.error("Could not create QR code.", e);
        }
    }

    private void generateHeadlinePage(PDDocument document, PDPageContentStream cos, PDRectangle rect, boolean foldable)
      throws IOException {
        // Top left on single page, bottom right on folding page
        float offsetX = foldable ? rect.getWidth() / 2 : 0;
        float offsetY = foldable ? 0 : rect.getHeight() / 2;

        cos.beginText();
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX + 40f, offsetY + mm2Point(85f));
        cos.setFont(fontArialBold, 21);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("EU DIGITALES COVID-");
        cos.newLineAtOffset(15f, -25f);
        cos.showText("TESTZERTIFIKAT");
        cos.endText();

        cos.setNonStrokingColor(Color.yellow);
        cos.addRect(offsetX + leading, offsetY + mm2Point(65f), rect.getWidth() / 2 - leading * 2, 8);
        cos.fillAndStroke();

        cos.beginText();
        cos.newLineAtOffset(offsetX + 55f, offsetY + mm2Point(52f));
        cos.setFont(fontArialBold, 21);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("EU DIGITAL COVID");
        cos.newLineAtOffset(2f, -25f);
        cos.showText("TEST CERTIFICATE");
        cos.endText();

        try {
            // Show flag with German country code

            String flag = pdfConfig.getCertFlagPath();
            final ClassPathResource classPathResource = new ClassPathResource(flag);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(flag)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "flag");

            float flagWidth = 113f;
            float flagHeight = 75f;
            float flagX = offsetX + rect.getWidth() / 4 - flagWidth / 2;
            float flagY = offsetY + mm2Point(10f);
            cos.drawImage(pdImage, flagX, flagY, flagWidth, flagHeight);

            cos.beginText();
            String country = "DE";
            float coutryFontSize = 16f;
            float textWidth = fontArial.getStringWidth(country) / 1000 * coutryFontSize;
            cos.newLineAtOffset(flagX + flagWidth / 2 - textWidth / 2, flagY + flagHeight / 2 - coutryFontSize / 2);
            cos.setNonStrokingColor(Color.WHITE);
            cos.setFont(fontArial, coutryFontSize);
            cos.showText(country);
            cos.endText();
        } catch (IOException | NullPointerException e) {
            log.error("Flag image not found!");
        }
    }

    private void generatePersonalInfoPage(PDDocument document, PDPageContentStream cos,
                                          PDRectangle rect, QuickTest quicktest,
                                          DccDecodeResult dccDecodeResult, boolean foldable)
      throws IOException {
        // Top right on single page, bottom left on foldable page
        float offsetX = foldable ? 0 : rect.getWidth() / 2;
        float offsetY = foldable ? 0 : rect.getHeight() / 2;

        cos.beginText();
        cos.setLeading(leading);
        // Set text to the outside of the page, left fot foldable page, right for single page
        if (foldable) {
            cos.newLineAtOffset(offsetX + mm2Point(10f), offsetY + mm2Point(138f));
        } else {
            cos.newLineAtOffset(offsetX + mm2Point(62f), offsetY + mm2Point(138f));
        }

        float textsize = 9f;
        cos.setFont(fontArial, textsize);
        cos.setNonStrokingColor(Color.BLACK);
        for (String qrLine : pdfConfig.getCertQrDescription().split(pdfConfig.getCertLineSeparator())) {
            cos.showText(qrLine);
            cos.newLineAtOffset(0f, -textsize);
        }

        cos.newLine();
        cos.newLine();
        cos.showText("Mehr Informationen");
        cos.newLineAtOffset(0f, -textsize);
        cos.showText("unter:");
        cos.newLineAtOffset(0f, -textsize);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("https://www.digitaler-");
        cos.newLineAtOffset(0f, -textsize);
        cos.showText("impfnachweis-app.de");
        cos.endText();

        try {
            String cert = pdfConfig.getCertCertlogoPath();
            final ClassPathResource classPathResource = new ClassPathResource(cert);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(cert)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "cert");
            cos.drawImage(pdImage, offsetX + mm2Point(5f), offsetY + mm2Point(60f), 295f - leading * 2,
                70f - 7f);
        } catch (IOException | NullPointerException e) {
            log.error("Certificate image not found!");
        }

        LocalDate birthday = LocalDate.parse(quicktest.getBirthday(), dtf);
        cos.beginText();
        cos.setLeading(leading);
        float newlineX = foldable ? 10f : 5f;
        cos.newLineAtOffset(offsetX + mm2Point(newlineX), offsetY + mm2Point(50f));

        // Print personal info
        List<List<String>> data = List.of(
          List.of(pdfConfig.getCertNameDe(), pdfConfig.getCertNameEn(),
            quicktest.getLastName().concat(", ".concat(quicktest.getFirstName()))),
          List.of(pdfConfig.getCertBirthdayDe(), pdfConfig.getCertBirthdayEn(), formatterDate.format(birthday)),
          List.of(pdfConfig.getCertCertIdDe(), pdfConfig.getCertCertIdEn(),dccDecodeResult.getCi())
        );

        data.forEach(entry -> printQrPagePersonalInfo(cos, entry));
        cos.endText();
    }

    private void printQrPagePersonalInfo(PDPageContentStream cos, List<String> data) {
        int boldSize = 11;
        int italicSize = 10;
        int normalSize = 10;

        try {
            cos.setFont(fontArialBold, boldSize);
            cos.setNonStrokingColor(pantoneReflexBlue);
            cos.showText(data.get(0));
            cos.setNonStrokingColor(Color.BLACK);
            cos.newLineAtOffset(0, -italicSize);
            cos.setFont(fontArialItalic, italicSize);
            cos.showText(data.get(1));
            cos.newLine();
            cos.setFont(fontArial, normalSize);
            cos.setNonStrokingColor(pantoneReflexBlue);
            cos.showText(data.get(2));
            cos.setNonStrokingColor(Color.BLACK);
            cos.newLineAtOffset(0, 2f * -normalSize);
        } catch (IOException ex) {
            log.warn("Could not create QR personal data page.");
        }
    }

    private void generateMemberStateInfoPage(PDDocument document, PDPageContentStream cos,
                                             PDRectangle rect, boolean foldable) throws IOException {
        // If foldable, page is rotated by 180 degrees
        // Bottom left for single page, top right for foldable page
        float offsetX = 0;
        float offsetY = foldable ? rect.getHeight() / 2 : 0;

        try {
            String flagSep = pdfConfig.getCertFlagSeparatorPath();
            final ClassPathResource classPathResource = new ClassPathResource(flagSep);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(flagSep)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "flagSep");
            cos.drawImage(pdImage, offsetX + mm2Point(5f),offsetY + mm2Point(125f), 295f - 2 * leading, 63f);
        } catch (IOException | NullPointerException e) {
            log.error("Flag seperator image not found!");
        }

        cos.beginText();

        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX + mm2Point(5f), offsetY + mm2Point(120f));
        float textsize = 9f;

        printDescriptionBlock(cos, textsize, pdfConfig.getCertMemberStateDescriptionDe());

        cos.newLine();
        if (!foldable) {
            cos.newLine();
        }

        printDescriptionBlock(cos, textsize, pdfConfig.getCertMemberStateDescriptionEn());

        cos.newLine();
        if (!foldable) {
            cos.newLine();
        }

        printDescriptionBlock(cos, textsize, pdfConfig.getCertMemberStateFurtherDescription());

        cos.endText();

        if (foldable) {
            // Show folding instructions

            cos.moveTo(offsetX + mm2Point(5f), offsetY + mm2Point(40f));
            cos.lineTo(offsetX + mm2Point(80f), offsetY + mm2Point(40f));
            cos.stroke();

            try {
                String foldingInstruction = pdfConfig.getCertFoldingInstruction();
                final ClassPathResource classPathResource = new ClassPathResource(foldingInstruction);
                final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
                  Objects.requireNonNull(classPathResource.getClassLoader())
                    .getResourceAsStream(foldingInstruction)));
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                  document, sampleBytes, "foldingInstruction");
                cos.drawImage(pdImage, offsetX + mm2Point(15f),offsetY + mm2Point(10f), 208f, 60f);
            } catch (IOException | NullPointerException e) {
                log.error("Folding instruction image not found!");
            }
        }
    }

    /**
     * Print a block consisting of multiple lines. Split at <br> and color the first 2 words in the first line in blue.
     * @param cos pdfbox page content stream
     * @param textsize fontsize
     * @param text the text to print
     * @throws IOException thrown from content stream
     */
    private void printDescriptionBlock(PDPageContentStream cos, float textsize, String text) throws IOException {
        String[] splittedText = text.split(pdfConfig.getCertLineSeparator());
        for (int i = 0, deTextLength = splittedText.length; i < deTextLength; i++) {
            String currentText = splittedText[i];
            if (i == 0) {
                cos.setFont(fontArialBold, textsize);
                cos.setNonStrokingColor(pantoneReflexBlue);
                String[] words = currentText.split(" ");
                cos.showText(words[0] + " " + words[1] + " ");
                cos.setNonStrokingColor(Color.BLACK);
                cos.setFont(fontArial, textsize);
                cos.showText(Arrays.stream(words).skip(2).collect(Collectors.joining(" ")));
            } else {
                if (currentText.contains("https://")) {
                    cos.setNonStrokingColor(pantoneReflexBlue);
                }
                cos.showText(currentText);
                cos.setNonStrokingColor(Color.BLACK);
            }
            cos.newLineAtOffset(0f, -textsize);
        }
    }

    private void generateCertificateInfoPage(PDPageContentStream cos, PDRectangle rect, QuickTest quickTest,
                                             DccDecodeResult dccDecodeResult, boolean foldable) throws IOException {
        // If foldable, page is rotated by 180 degrees
        // Bottom right for single page, top left for foldable page

        float offsetX = rect.getWidth() / 2;
        float offsetY = foldable ? rect.getHeight() / 2 : 0;

        cos.beginText();
        cos.setLeading(leading);
        cos.setFont(fontArialBold, 13f);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.newLineAtOffset(offsetX + mm2Point(5f), offsetY + mm2Point(135f));
        cos.showText(pdfConfig.getCertHeaderTestDe());
        cos.newLineAtOffset(0, -15f);
        cos.setFont(fontArialBold, 13f);
        cos.showText(pdfConfig.getCertHeaderTestEn());
        cos.newLineAtOffset(0f, -30f);

        cos.setNonStrokingColor(Color.BLACK);

        final String testTypeDesc =
                TestTypeUtils.isRat(quickTest.getTestType())
                        ? pdfConfig.getCertTestTypeRat() : pdfConfig.getCertTestTypePcr();

        List<List<String>> data = List.of(
          List.of(pdfConfig.getCertDiseaseAgentDe(), pdfConfig.getCertDiseaseAgentEn(),
            pdfConfig.getCertDiseaseAgentTargeted()),
          List.of(pdfConfig.getCertTestTypeDe(), pdfConfig.getCertTestTypeEn(), testTypeDesc),
          List.of(pdfConfig.getCertTestManufacturerDe(), pdfConfig.getCertTestManufacturerEn(),
            quickTest.getTestBrandName()),
          List.of(pdfConfig.getCertDateSampleCollectionDe(), pdfConfig.getCertDateSampleCollectionEn(),
            PdfUtils.getFormattedTime(quickTest.getUpdatedAt(), formatter)),
          List.of(pdfConfig.getCertTestResultDe(), pdfConfig.getCertTestResultEn(),
            getTestResultText(quickTest.getTestResult())),
          List.of(pdfConfig.getCertTestingCentreDe(), pdfConfig.getCertTestingCentreEn(), quickTest.getGroupName()),
          List.of(pdfConfig.getCertStateOfTestDe(), pdfConfig.getCertStateOfTestEn(), pdfConfig.getCertIssuerState()),
          List.of(pdfConfig.getCertIssuerDe(), pdfConfig.getCertIssuerEn(), dccDecodeResult.getIssuer())
        );

        float spacingParagraph = -13f;
        float spacingText = -9f;

        data.forEach(entry ->
          printCertData(cos, spacingParagraph, spacingText, entry.get(0), entry.get(1), entry.get(2)));
        cos.endText();
    }

    private String getTestResultText(Short testResultValue) {
        String testResult;
        switch (testResultValue != null ? testResultValue : -1) {
          case positivePcr:
          case positiveRat:
              testResult = "Detected";
              break;
          case negativePcr:
          case negativeRat:
              testResult = "Not detected";
              break;
          default:
              testResult = "Fehler";
              break;
        }

        return testResult;
    }

    private void printCertData(PDPageContentStream cos, float spacingParagraph, float spacingText, String textOriginal,
                               String translation, String value) {
        try {
            cos.setNonStrokingColor(pantoneReflexBlue);
            cos.setFont(fontArialBold, 8);
            // split text at a configured line break and count lines on the left side of the row to be able to
            // reset the offset of the next row correctly
            int leftLines = 0;
            for (String line : textOriginal.split(pdfConfig.getCertLineSeparator())) {
                cos.showText(line);
                cos.newLineAtOffset(0, spacingText);
                leftLines++;
            }
            cos.setNonStrokingColor(Color.BLACK);
            cos.setFont(fontArial, 8);
            for (String lineItalic : translation.split(pdfConfig.getCertLineSeparator())) {
                cos.showText(lineItalic);
                cos.newLineAtOffset(0, spacingText);
                leftLines++;
            }
            cos.setFont(fontArial, 8);
            cos.setNonStrokingColor(pantoneReflexBlue);

            float paragraphIndent = 150f;
            cos.newLineAtOffset(paragraphIndent, leftLines * -spacingText);

            List<String> textParagraph = splitStringToParagraph(value, 30);
            int rightLines = 0;
            for (String n : textParagraph) {
                cos.showText(n);
                cos.newLineAtOffset(0, spacingText);
                rightLines++;
            }

            cos.newLineAtOffset(-paragraphIndent,
                (rightLines * -spacingText) + (leftLines * spacingText + spacingParagraph));
        } catch (IOException exception) {
            log.warn("Could not create certificate data page.");
        }
    }

    private float mm2Point(float mm) {
        return mm * 2.83465f;
    }

    private void close(PDDocument document, ByteArrayOutputStream output) throws IOException {
        document.save(output);
        document.close();
    }
}

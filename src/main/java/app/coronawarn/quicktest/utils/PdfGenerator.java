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

import static app.coronawarn.quicktest.model.Sex.DIVERSE;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import eu.europa.ec.dgc.DccDecodeResult;
import eu.europa.ec.dgc.DccDecoder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfGenerator {

    private final PdfConfig pdfConfig;
    private final DccDecoder dccDecoder = new DccDecoder();

    private final int pending = 5;
    private final int negative = 6;
    private final int positive = 7;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter utcParserFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private int offsetX = 70;
    private float leading = 14.5f;
    private int fontSize = 12;
    private PDType1Font fontType = PDType1Font.HELVETICA;

    private PDTrueTypeFont fontArial;
    private PDTrueTypeFont fontArialBold;
    private PDTrueTypeFont fontArialItalic;

    private final Color pantoneReflexBlue = Color.decode("#003399");
    private final Color pantoneYellow = Color.decode("#FFCC00");

    /**
     * Generates a PDF file for rapid test result to print.
     *
     * @param pocInformation point of care data used in pdf
     * @param quicktest      Quicktest
     * @param user           carried out by user
     * @throws IOException when creating pdf went wrong
     */
    public ByteArrayOutputStream generatePdf(List<String> pocInformation, QuickTest quicktest,
                                             String user) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page1 = new PDPage(PDRectangle.A4);
        document.addPage(page1);
        page1.setMediaBox(PDRectangle.A4);
        PDPageContentStream cos = new PDPageContentStream(document, page1);
        config(document);
        PDRectangle rect1 = page1.getMediaBox();
        write(document, cos, rect1, pocInformation, quicktest, user);
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        close(document, pdf);
        return pdf;
    }

    /**
     * GAppends the QR code to the pdf.
     *
     * @param quicktest      Quicktest
     * @param dcc            certificate data
     * @throws IOException   when creating pdf went wrong
     */
    public ByteArrayOutputStream appendCertificatePage(byte[] pdf, QuickTest quicktest, String dcc) throws IOException {
        PDDocument document = PDDocument.load(pdf);
        configCertPage(document);
        generateCertPage(document, quicktest, dcc);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        close(document, out);
        return out;
    }

    private void config(PDDocument document) {
        PDDocumentInformation pdd = document.getDocumentInformation();
        pdd.setAuthor(pdfConfig.getAuthorPdfPropertiesText());
        pdd.setTitle(pdfConfig.getQuickTestHeadlineText());
        pdd.setCreator(pdfConfig.getCreatorPdfPropertiesText());
        LocalDate date = LocalDate.now();
        GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        pdd.setCreationDate(gcal);
    }

    private void configCertPage(PDDocument document) {
        final ClassPathResource cs = new ClassPathResource("pdf/fonts/arial.ttf");
        try {
            this.fontArial = PDTrueTypeFont.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/arial.ttf"), WinAnsiEncoding.INSTANCE);
            this.fontArialBold = PDTrueTypeFont.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/arialbd.ttf"), WinAnsiEncoding.INSTANCE);
            this.fontArialItalic = PDTrueTypeFont.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/ariali.ttf"), WinAnsiEncoding.INSTANCE);
        } catch (IOException e) {
            log.error("Could not load font");
        }
    }

    private void write(PDDocument document, PDPageContentStream cos, PDRectangle rect,
                       List<String> pocInformation,
                       QuickTest quicktest,
                       String user) throws IOException {
        generatePoCAddress(cos, rect, pocInformation);
        addCoronaAppIcon(document, cos, rect);
        generatePersonAddress(cos, rect, quicktest);
        generateSubject(cos, rect, quicktest);
        generateText(cos, rect, quicktest, user);
        generateEnd(cos, rect);
        cos.close();
    }

    private void generatePoCAddress(PDPageContentStream cos, PDRectangle rect, List<String> pocInformation)
        throws IOException {
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(0, rect.getHeight() - 110);
        pocInformation.forEach(s -> {
            try {
                rightAlignment(cos, rect, s);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        cos.endText();
    }

    private void addCoronaAppIcon(PDDocument document, PDPageContentStream cos, PDRectangle rect) throws IOException {
        try {
            final ClassPathResource classPathResource = new ClassPathResource(pdfConfig.getLogoPath());
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
                Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(pdfConfig.getLogoPath())));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "logo");
            cos.drawImage(pdImage, 280, rect.getHeight() - offsetX, 50, 50);
        } catch (IOException | NullPointerException e) {
            log.error("Logo not found!");
        }
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.newLineAtOffset(230, rect.getHeight() - 85);
        cos.showText(pdfConfig.getQuickTestHeadlineText());
        cos.endText();
    }

    private void generatePersonAddress(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest)
        throws IOException {
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 220);
        cos.showText(quicktest.getFirstName() + " " + quicktest.getLastName());
        cos.newLine();
        cos.showText(quicktest.getStreet() + " "
                + (quicktest.getHouseNumber() == null ? "" : quicktest.getHouseNumber()));
        cos.newLine();
        cos.showText(quicktest.getZipCode() + " " + quicktest.getCity());
        cos.newLine();
        cos.showText(pdfConfig.getPersonPhoneDescriptionText() + quicktest.getPhoneNumber());
        cos.newLine();
        cos.showText(pdfConfig.getPersonEmailDescriptionText() + quicktest.getEmail());
        cos.newLine();
        cos.endText();

    }

    private void rightAlignment(PDPageContentStream cos, PDRectangle rect, String text) throws IOException {
        float pagewidth;
        float textWidth;
        float padding;
        pagewidth = rect.getWidth();
        textWidth = (fontType.getStringWidth(text) / 1000.0f) * fontSize;
        padding = pagewidth - ((40 * 2) + textWidth);

        cos.newLineAtOffset(padding, 0);
        cos.showText(text);
        cos.newLineAtOffset(-padding, 0);
        cos.newLine();
    }

    private void generateSubject(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest) throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 340);
        String dateAndTimeInGermany;
        if (quicktest.getUpdatedAt() != null) {
            dateAndTimeInGermany =
                ZonedDateTime.of(quicktest.getUpdatedAt(), ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(formatter);
        } else {
            dateAndTimeInGermany = "-";
        }
        cos.showText(pdfConfig.getQuickTestOfDateText() + dateAndTimeInGermany);
        cos.newLine();
        cos.endText();

    }

    private void generateText(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest, String user)
        throws IOException {
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 380);
        switch (quicktest.getTestResult() != null ? quicktest.getTestResult() : -1) {
          case pending:
              cos.showText(pdfConfig.getTestResultDescriptionText() + pdfConfig.getTestResultPendingText());
              cos.newLine();
              break;
          case negative:
              cos.showText(pdfConfig.getTestResultDescriptionText() + pdfConfig.getTestResultNegativeText());
              cos.newLine();
              break;
          case positive:
              cos.showText(pdfConfig.getTestResultDescriptionText() + pdfConfig.getTestResultPositiveText());
              cos.newLine();
              break;
          default:
              cos.showText(pdfConfig.getTestResultDescriptionText() + pdfConfig.getTestResultDefaultText());
              cos.newLine();
              break;
        }

        String dateAndTimeInGermany;
        if (quicktest.getUpdatedAt() != null) {
            dateAndTimeInGermany =
                ZonedDateTime.of(quicktest.getUpdatedAt(), ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(formatter);
        } else {
            dateAndTimeInGermany = "-";
        }
        cos.showText(pdfConfig.getExecutedByDescriptionText() + dateAndTimeInGermany);
        cos.newLine();
        cos.newLine();
        cos.showText(pdfConfig.getFurtherDataAboutThePersonText());
        cos.newLine();

        switch (quicktest.getSex() != null ? quicktest.getSex() : DIVERSE) {
          case MALE:
              cos.showText(pdfConfig.getGenderDescriptionText() + pdfConfig.getMaleText());
              cos.newLine();
              break;
          case FEMALE:
              cos.showText(pdfConfig.getGenderDescriptionText() + pdfConfig.getFemaleText());
              cos.newLine();
              break;
          default:
              cos.showText(pdfConfig.getGenderDescriptionText() + pdfConfig.getDiverseText());
              cos.newLine();
              break;
        }
        if (quicktest.getBirthday() != null) {
            LocalDate datetime = LocalDate.parse(quicktest.getBirthday(), dtf);
            cos.showText(pdfConfig.getBirthDateDescriptionText() + datetime.format(formatterDate));
        } else {
            cos.showText(pdfConfig.getBirthDateDescriptionText() + "-");
        }
        cos.newLine();
        cos.newLine();
        cos.showText(pdfConfig.getFurtherDataAboutTestDescriptionText());
        cos.newLine();
        cos.showText(pdfConfig.getExecutedFromDescriptionText() + user);
        cos.newLine();
        cos.showText(pdfConfig.getTestBrandIdDescriptionText() + quicktest.getTestBrandId());
        cos.newLine();
        if (quicktest.getTestBrandName() == null) {
            cos.showText(pdfConfig.getTestBrandNameDescriptionText() + pdfConfig.getTradeNameEmptyText());
        } else {
            cos.showText(pdfConfig.getTestBrandNameDescriptionText() + quicktest.getTestBrandName());
        }
        cos.newLine();
        String useText = "";
        if (quicktest.getTestResult() != null && quicktest.getTestResult() == positive) {
            useText = pdfConfig.getPositiveInstructionText();
        } else if (quicktest.getTestResult() != null && quicktest.getTestResult() == negative) {
            useText = pdfConfig.getNegativeInstructionText();
        }
        cos.newLine();
        cos.newLine();
        List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (useText.length() > 0) {
            int spaceIndex = useText.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0) {
                spaceIndex = useText.length();
            }
            String subString = useText.substring(0, spaceIndex);
            float size = fontSize * fontType.getStringWidth(subString) / 1000;
            if (size > rect.getWidth() - 150) {
                if (lastSpace < 0) {
                    lastSpace = spaceIndex;
                }
                subString = useText.substring(0, lastSpace);
                lines.add(subString);
                useText = useText.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == useText.length()) {
                lines.add(useText);
                useText = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        for (String line : lines) {
            cos.showText(line);
            cos.newLineAtOffset(0, -1.5f * fontSize);
        }
        cos.newLine();
        cos.newLine();
        cos.endText();

    }

    private void generateCertPage(PDDocument document, QuickTest quicktest, String dcc)
      throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        page.setMediaBox(PDRectangle.A4);
        PDPageContentStream cos = new PDPageContentStream(document, page);
        PDRectangle rect = page.getMediaBox();

        DccDecodeResult dccDecodeResult = dccDecoder.decodeDcc(dcc);
        generateCertTextPage1(document, cos, rect);
        generateCertTextPage2(document, cos, rect, quicktest, dccDecodeResult);
        generateQrCode(document, cos, rect, dcc);
        generateCertTextPage3(document, cos, rect);
        generateCertTextPage4(document, cos, rect, quicktest, dccDecodeResult);
        cos.close();
    }

    private void generateQrCode(PDDocument document, PDPageContentStream cos, PDRectangle rect, String text) {
        try {
            int qrCodeSize = 150;
            float qrCodeImageSize = 175f;

            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            BitMatrix matrix = new MultiFormatWriter().encode(
              new String(text.getBytes("UTF-8"), "UTF-8"),
              BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);

            MatrixToImageConfig config = new MatrixToImageConfig(0xFF000001, 0xFFFFFFFF);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix, config);
            PDImageXObject image = JPEGFactory.createFromImage(document, bufferedImage);
            cos.drawImage(image,
                rect.getWidth() / 4 * 3 - (qrCodeImageSize / 2),
                rect.getHeight() - 200,
                qrCodeImageSize,
                qrCodeImageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateCertTextPage1(PDDocument document, PDPageContentStream cos, PDRectangle rect)
      throws IOException {

        cos.beginText();
        cos.setLeading(leading);
        cos.newLineAtOffset(rect.getWidth() / 2 / 3, rect.getHeight() - 100);
        cos.setFont(fontArialBold, 21);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("EU Digital");
        cos.newLineAtOffset(-35f, -25f);
        cos.showText("COVID Certificate");
        cos.endText();

        cos.setNonStrokingColor(Color.yellow);
        cos.addRect(leading, rect.getHeight() - 160, rect.getWidth() / 2 - leading * 2, 10);
        cos.fillAndStroke();

        cos.beginText();
        cos.newLineAtOffset(50f, rect.getHeight() - 200);
        cos.setFont(fontArialBold, 21);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("Certificat numérique");
        cos.newLineAtOffset(15f, -25f);
        cos.showText("européen COVID");
        cos.endText();

        try {
            String flag = pdfConfig.getCertFlagPath();
            final ClassPathResource classPathResource = new ClassPathResource(flag);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(flag)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "flag");

            float flagWidth = 113f;
            float flagHeight = 75f;
            float flagX = rect.getWidth() / 4 - flagWidth / 2;
            float flagY = rect.getHeight() / 2 + 80;
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

    private void generateCertTextPage2(PDDocument document, PDPageContentStream cos,
                                       PDRectangle rect, QuickTest quicktest,
                                       DccDecodeResult dccDecodeResult)
      throws IOException {

        try {
            String cert = pdfConfig.getCertCertlogoPath();
            final ClassPathResource classPathResource = new ClassPathResource(cert);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(cert)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "cert");
            cos.drawImage(pdImage, rect.getWidth() / 2 + leading,
              rect.getHeight() - (rect.getHeight() / 4) - 75, 295 - leading * 2, 70f - 7f);
        } catch (IOException | NullPointerException e) {
            log.error("Certificate image not found!");
        }

        LocalDate birthday = LocalDate.parse(quicktest.getBirthday(), dtf);
        cos.beginText();
        cos.setLeading(leading);
        cos.newLineAtOffset(rect.getWidth() / 2 + 20, rect.getHeight() / 2 + 120);

        List<List<String>> data = List.of(
          List.of("Surname(s) and forename(s)", "Nom(s) de familie et prénom(s)",
            quicktest.getLastName().concat(", ".concat(quicktest.getFirstName()))),
          List.of("Date of birth", "Date de naissance", formatterDate.format(birthday)),
          List.of("Unique certificate identifier", "Identifiant unique du certificat",dccDecodeResult.getCi())
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
            cos.setNonStrokingColor(Color.BLACK);
            cos.showText(data.get(0));
            cos.newLineAtOffset(0, -italicSize);
            cos.setFont(fontArialItalic, italicSize);
            cos.showText(data.get(1));
            cos.newLineAtOffset(0, -italicSize);
            cos.setFont(fontArial, normalSize);
            cos.setNonStrokingColor(pantoneReflexBlue);
            cos.showText(data.get(2));
            cos.setNonStrokingColor(Color.BLACK);
            cos.newLine();
        } catch (IOException ex) {
            log.warn("Could not create QR personal data page.");
        }
    }

    private void generateCertTextPage3(PDDocument document, PDPageContentStream cos,
                                       PDRectangle rect) throws IOException {

        try {
            String flagSep = pdfConfig.getCertFlagSeparatorPath();
            final ClassPathResource classPathResource = new ClassPathResource(flagSep);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(flagSep)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "flagSep");
            cos.drawImage(pdImage, leading,rect.getHeight() / 2 - 100, 295 - 2 * leading, 63f);
        } catch (IOException | NullPointerException e) {
            log.error("Flag seperator image not found!");
        }

        cos.beginText();
        cos.setLeading(leading);
        cos.setFont(fontArialBold, 14);
        cos.newLineAtOffset(rect.getWidth() / 4 - 85, rect.getHeight() / 2 - 125);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("Member State Placeholder");
        cos.endText();

        cos.beginText();
        cos.setLeading(leading);
        cos.setFont(fontArial, 8);
        cos.setNonStrokingColor(Color.BLACK);
        cos.newLineAtOffset(30, rect.getHeight() / 4 - 50);

        float spacing = -10f;
        cos.showText("This certificate is not a travel document.  The scientific evidence");
        cos.newLineAtOffset(5, spacing);
        cos.showText("on COVID-19 vaccination, testing and recovering continues to");
        cos.newLineAtOffset(2, spacing);
        cos.showText("evolve, also in view of new variants of concern of the virus.");
        cos.newLineAtOffset(0, spacing);
        cos.showText("Before travelling, please check the applicable public health");
        cos.newLineAtOffset(2, spacing);
        cos.showText("measures and related restrictions applied at the point of");
        cos.newLineAtOffset(75, spacing);
        cos.showText("destination.");
        cos.newLineAtOffset(-50f, spacing);
        cos.showText("Relevant information can be found here:");
        cos.newLineAtOffset(20f, spacing);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("https://reopen.europa.eu/en");
        cos.setNonStrokingColor(Color.BLACK);
        cos.endText();
    }

    private void generateCertTextPage4(PDDocument document, PDPageContentStream cos,
                                       PDRectangle rect, QuickTest quickTest,
                                       DccDecodeResult dccDecodeResult) throws IOException {

        cos.beginText();
        cos.setLeading(leading);
        cos.setFont(fontArialBold, 15);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.newLineAtOffset(rect.getWidth() / 2 + 100, rect.getHeight() / 2 - 30);
        cos.showText(pdfConfig.getCertHeaderTestEn());
        cos.newLineAtOffset(0, -15f);
        cos.setFont(fontArialBold, 14);
        cos.showText(pdfConfig.getCertHeaderTestFr());
        cos.newLineAtOffset(-80f, -30f);

        cos.setNonStrokingColor(Color.BLACK);
        LocalDateTime dt = LocalDateTime.parse(dccDecodeResult.getDccJsonNode().get("t").get(0).get("sc").asText(),
          utcParserFormatter);

        List<List<String>> data = List.of(
          List.of("Disease or agent targeted", "Maladie ou agent cible", "COVID-19"),
          List.of(pdfConfig.getCertTestTypeEn(), pdfConfig.getCertTestTypeFr(), "RAT"),
          List.of(pdfConfig.getCertTestNameEn(), pdfConfig.getCertTestNameFr(), quickTest.getTestBrandName()),
          List.of(pdfConfig.getCertDateSampleCollectionEn(), pdfConfig.getCertDateSampleCollectionFr(),
            dt.format(formatter)),
          List.of(pdfConfig.getCertDateTestResultEn(), pdfConfig.getCertDateTestResultFr(),
            quickTest.getUpdatedAt().format(formatter)),
          List.of(pdfConfig.getCertTestResultEn(), pdfConfig.getCertTestResultFr(),
            getTestResultText(quickTest.getTestResult())),
          List.of(pdfConfig.getCertTestingCentreEn(), pdfConfig.getCertTestingCentreFr(), quickTest.getPocId()),
          List.of(pdfConfig.getCertStateOfTestEn(), pdfConfig.getCertStateOfTestFr(), "DE"),
          List.of(pdfConfig.getCertIssuerEn(), pdfConfig.getCertIssuerFr(), dccDecodeResult.getIssuer())
        );

        float spacingParagraph = -13f;
        float spacingText = -9f;

        data.forEach(entry ->
          printCertData(cos, spacingParagraph, spacingText, entry.get(0), entry.get(1), entry.get(2)));
        cos.endText();
    }

    private String getTestResultText(Short testResultValue) {
        String testResult = "";
        switch (testResultValue != null ? testResultValue : -1) {
          case positive:
              testResult = "Positiv";
              break;
          case negative:
              testResult = "Negativ";
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
            cos.setNonStrokingColor(Color.BLACK);
            cos.setFont(fontArialBold, 8);
            // split text at a configured line break and count lines on the left side of the row to be able to
            // reset the offset of the next row correctly
            int leftLines = 0;
            for (String line : textOriginal.split(pdfConfig.getCertLineSeparator())) {
                cos.showText(line);
                cos.newLineAtOffset(0, spacingText);
                leftLines++;
            }
            cos.setFont(fontArialItalic, 8);
            for (String lineItalic : translation.split(pdfConfig.getCertLineSeparator())) {
                cos.showText(lineItalic);
                cos.newLineAtOffset(0, spacingText);
                leftLines++;
            }
            cos.setFont(fontArialBold, 8);
            cos.setNonStrokingColor(pantoneReflexBlue);

            float paragraphIndent = 150f;
            cos.newLineAtOffset(paragraphIndent, leftLines * -spacingText);

            int limit = 25;
            List<String> textParagraph = new ArrayList<>();
            StringBuilder lineBuilder = new StringBuilder();
            String[] split = value.split(" ");
            for (int i = 0, splitLength = split.length; i < splitLength; i++) {
                String word = split[i];
                lineBuilder.append(word).append(" ");
                if (lineBuilder.length() + word.length() > limit || i == splitLength - 1) {
                    textParagraph.add(lineBuilder.toString());
                    lineBuilder = new StringBuilder();
                }
            }
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

    private void generateEnd(PDPageContentStream cos, PDRectangle rect) throws IOException {
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 800);
        cos.showText(pdfConfig.getSignatureText());
        cos.newLine();
        cos.endText();

    }

    private void close(PDDocument document, ByteArrayOutputStream output) throws IOException {
        document.save(output);
        document.close();
    }

}

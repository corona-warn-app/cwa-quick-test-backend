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
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
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

    private final int pending = 5;
    private final int negative = 6;
    private final int positive = 7;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

    private void config(PDDocument document) {
        PDDocumentInformation pdd = document.getDocumentInformation();
        pdd.setAuthor(pdfConfig.getAuthorPdfPropertiesText());
        pdd.setTitle(pdfConfig.getQuickTestHeadlineText());
        pdd.setCreator(pdfConfig.getCreatorPdfPropertiesText());
        LocalDate date = LocalDate.now();
        GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        pdd.setCreationDate(gcal);
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
        //generateQrCode(document, page2, "Qr code cose text", 50f, 50f);
        generateEnd(cos, rect);
        generateCertPage(document,  quicktest);
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

    private void generateQrCode(PDDocument document, PDPage page, String text, float x, float y) {
        try {
            PDPageContentStream contentStream = new PDPageContentStream(
              document, page, PDPageContentStream.AppendMode.APPEND, true);

            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(text.getBytes("UTF-8"), "UTF-8"),
                    BarcodeFormat.QR_CODE, 150, 150, hintMap);

            MatrixToImageConfig config = new MatrixToImageConfig(0xFF000001, 0xFFFFFFFF);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix, config);
            PDImageXObject image = JPEGFactory.createFromImage(document, bufferedImage);
            contentStream.drawImage(image, x, y, 175, 175);
            contentStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateCertPage(PDDocument document, QuickTest quicktest)
      throws IOException {

        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        page.setMediaBox(PDRectangle.A4);
        PDPageContentStream cos = new PDPageContentStream(document, page);
        PDRectangle rect = page.getMediaBox();

        cos.drawLine(rect.getWidth() / 2, 0, rect.getWidth() / 2, rect.getHeight());
        cos.drawLine(0, rect.getHeight() / 2, rect.getWidth(), rect.getHeight() / 2);

        generateCertTextPage1(document, cos, rect);
        generateCertTextPage2(document, cos, rect, quicktest);
        generateCertTextPage3(document, cos, rect);
        generateCertTextPage4(document, cos, rect, quicktest);
        cos.close();
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
        cos.addRect(14.5f, rect.getHeight() - 160, rect.getWidth() / 2 - 29f, 10);
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
            String flag = "pdf/eu_flag.png";
            final ClassPathResource classPathResource = new ClassPathResource(flag);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(flag)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "flag");
            cos.drawImage(pdImage, rect.getWidth() / 4 - 58, (rect.getHeight() / 2) + 80, 113, 75);
        } catch (IOException | NullPointerException e) {
            log.error("Flag image not found!");
        }
    }

    private void generateCertTextPage2(PDDocument document, PDPageContentStream cos,
                                       PDRectangle rect, QuickTest quicktest)
      throws IOException {

        try {
            String cert = "pdf/certificate.png";
            final ClassPathResource classPathResource = new ClassPathResource(cert);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(cert)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "cert");
            cos.drawImage(pdImage, rect.getWidth() / 2 + 14.5f,
              rect.getHeight() - (rect.getHeight() / 4) - 75, 295 - 29.5f, 70 - 7f);
        } catch (IOException | NullPointerException e) {
            log.error("Certificate image not found!");
        }

        cos.beginText();
        cos.setFont(fontArialBold, 11);
        cos.setNonStrokingColor(Color.BLACK);
        cos.setLeading(leading);
        cos.newLineAtOffset(rect.getWidth() / 2 + 20, rect.getHeight() / 2 + 120);
        cos.showText("Surname(s) and forename(s)");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontArialItalic, 10);
        cos.showText("Nom(s) de familie et prénom(s)");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontArial, 11);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText(quicktest.getLastName() + ", " + quicktest.getFirstName());
        cos.setNonStrokingColor(Color.BLACK);
        cos.newLine();
        cos.setFont(fontArialBold, 11);
        cos.showText("Date of birth");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontArialItalic, 10);
        cos.showText("Date de naissance");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontArial, 11);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText(quicktest.getBirthday());
        cos.setNonStrokingColor(Color.BLACK);
        cos.newLine();
        cos.setFont(fontArialBold, 11);
        cos.showText("Unique certificate identifier");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontArialItalic, 10);
        cos.showText("Identifiant unique du certificat");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontArial, 11);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.showText("XXXXXXXXXXXXXXXXXXX");
        cos.setNonStrokingColor(Color.BLACK);
        cos.newLine();
        cos.endText();
    }

    private void generateCertTextPage3(PDDocument document, PDPageContentStream cos,
                                       PDRectangle rect) throws IOException {

        try {
            String flagSep = "pdf/flag_seperator.png";
            final ClassPathResource classPathResource = new ClassPathResource(flagSep);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(flagSep)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "flagSep");
            cos.drawImage(pdImage, 14.5f,rect.getHeight() / 2 - 100, 295 - 29.5f, 70 - 7f);
        } catch (IOException | NullPointerException e) {
            log.error("Flag seperator image not found!");
        }

        cos.beginText();
        cos.setLeading(leading);
        cos.setFont(fontArial, 8);
        cos.newLineAtOffset(30, rect.getHeight() / 4 - 50);
        cos.showText("This certificate is not a travel document.  The scientific evidence");
        cos.newLineAtOffset(5, -10f);
        cos.showText("on COVID-19 vaccination, testing and recovering continues to");
        cos.newLineAtOffset(2, -10f);
        cos.showText("evolve, also in view of new variants of concern of the virus.");
        cos.newLineAtOffset(0, -10f);
        cos.showText("Before travelling, please check the applicable public health");
        cos.newLineAtOffset(2, -10f);
        cos.showText("measures and related restrictions applied at the point of");
        cos.newLineAtOffset(75, -10f);
        cos.showText("destination.");
        cos.newLineAtOffset(-50f, -10f);
        cos.showText("Relevant information can be found here:");
        cos.endText();
    }

    private void generateCertTextPage4(PDDocument document, PDPageContentStream cos,
                                       PDRectangle rect, QuickTest quickTest) throws IOException {

        float spacing = -15f;

        cos.beginText();
        cos.setLeading(leading);
        cos.setFont(fontArialBold, 15);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.newLineAtOffset(rect.getWidth() / 2 + 100, rect.getHeight() / 2 - 60);
        cos.showText("Test Certificate");
        cos.newLineAtOffset(0, -15f);
        cos.setFont(fontArialBold, 14);
        cos.showText("Certificat de Test");
        cos.newLineAtOffset(-80f, -30f);

        cos.setNonStrokingColor(Color.BLACK);
        cos.setFont(fontArialBold, 8);
        cos.showText("Disease or agent targeted");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Maladie ou agent cible");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Test name (optional for NAAT)");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Nom du test");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Test manufacturer (optional for NAAT)");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Fabricant du test");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Date and time of the test sample");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Date et heure");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Date and time of the test result");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Date et heure");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Result of the test");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Resultat du test");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Testing centre or facility");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Centre ou installation de test");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Member state of test");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("État membre du test");

        cos.newLineAtOffset(0, spacing);
        cos.setFont(fontArialBold, 8);
        cos.showText("Certificate issuer");
        cos.newLineAtOffset(0, -9f);
        cos.setFont(fontArialItalic, 8);
        cos.showText("Émetteur du certificat");
        cos.endText();

        cos.beginText();
        cos.setLeading(leading);
        cos.setFont(fontArialBold, 8);
        cos.setNonStrokingColor(pantoneReflexBlue);
        cos.newLineAtOffset(rect.getWidth() / 2 + 200, rect.getHeight() / 2 - 105);
        cos.showText("COVID-19");
        cos.newLineAtOffset(0, -24f);
        //TODO
        cos.showText(quickTest.getTestBrandName());
        cos.newLineAtOffset(0, -24f);
        cos.showText(quickTest.getTestBrandName());
        cos.newLineAtOffset(0, -24f);
        cos.showText(quickTest.getCreatedAt().format(formatter));
        cos.newLineAtOffset(0, -24f);
        cos.showText(quickTest.getUpdatedAt().format(formatter));
        cos.newLineAtOffset(0, -24f);
        //TODO
        cos.showText(quickTest.getTestResult() == 7 ? "Positive" : "Negative");
        cos.newLineAtOffset(0, -24f);
        cos.showText(quickTest.getPocId());
        cos.newLineAtOffset(0, -24f);
        //TODO
        cos.showText("DE");
        cos.newLineAtOffset(0, -24f);
        cos.showText("DE");

        cos.endText();

        cos.close();
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

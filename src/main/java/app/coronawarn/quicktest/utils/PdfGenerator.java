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
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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
        PDPage page2 = new PDPage(PDRectangle.A6);
        document.addPage(page2);
        page2.setMediaBox(PDRectangle.A6);
        PDPageContentStream cos = new PDPageContentStream(document, page1);
        config(document);
        PDRectangle rect1 = page1.getMediaBox();
        write(document, cos, rect1, page2, pocInformation, quicktest, user);
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
    }

    private void write(PDDocument document, PDPageContentStream cos, PDRectangle rect, PDPage page2,
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
        generateCertTextPage1(document, new PDPageContentStream(document, page2), page2.getMediaBox(), quicktest);
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

    private void generateCertTextPage1(PDDocument document,
                                       PDPageContentStream cos, PDRectangle rect, QuickTest quicktest)
      throws IOException {
        try {
            String cert = "Certificate.png";
            final ClassPathResource classPathResource = new ClassPathResource(cert);
            final byte[] sampleBytes = IOUtils.toByteArray(Objects.requireNonNull(
              Objects.requireNonNull(classPathResource.getClassLoader())
                .getResourceAsStream(cert)));
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, sampleBytes, "logo");
            cos.drawImage(pdImage, rect.getWidth() / 2 - 30, rect.getHeight() / 2, 60, 60);
        } catch (IOException | NullPointerException e) {
            log.error("Logo not found!");
        }
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA_BOLD, 11);
        cos.setLeading(leading);
        cos.newLineAtOffset(20, rect.getHeight() / 2 - 30);
        cos.showText("Surname(s) and forename(s)");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(PDType1Font.TIMES_ITALIC, 10);
        cos.showText("Nom(s) de familie et prénom(s)");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontType, 11);
        cos.setNonStrokingColor(0,51,153);
        cos.showText(quicktest.getLastName() + ", " + quicktest.getFirstName());
        cos.setNonStrokingColor(0,0,0);
        cos.newLine();
        cos.newLine();
        cos.setFont(PDType1Font.HELVETICA_BOLD, 11);
        cos.showText("Date of birth");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(PDType1Font.TIMES_ITALIC, 10);
        cos.showText("Date de naissance");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontType, 11);
        cos.setNonStrokingColor(0,51,153);
        cos.showText(quicktest.getBirthday());
        cos.setNonStrokingColor(0,0,0);
        cos.newLine();
        cos.newLine();
        cos.setFont(PDType1Font.HELVETICA_BOLD, 11);
        cos.showText("Unique certificate identifier");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(PDType1Font.TIMES_ITALIC, 10);
        cos.showText("Identifiant unique du certificat");
        cos.newLineAtOffset(0, -.9f * 10);
        cos.setFont(fontType, 11);
        cos.setNonStrokingColor(0,51,153);
        cos.showText("XXXXXXXXXXXXXXXXXXX");
        cos.setNonStrokingColor(0,0,0);
        cos.newLine();
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

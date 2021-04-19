package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.domain.QuickTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

@Service
public class PdfGenerator {

    private final int pending = 5;
    private final int negative = 6;
    private final int positive = 7;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        PDRectangle rect = page1.getMediaBox();
        PDPageContentStream cos = new PDPageContentStream(document, page1);
        config(document, cos);
        write(document, cos, rect, pocInformation, quicktest, user);
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        close(document, pdf);
        return pdf;
    }

    private void config(PDDocument document, PDPageContentStream cos) throws IOException {
        PDDocumentInformation pdd = document.getDocumentInformation();
        pdd.setAuthor("Schnelltestportal");
        pdd.setTitle("Schnelltest-Ergebnis");
        pdd.setCreator("Schnelltestportal");
        LocalDate date = LocalDate.now();
        GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        pdd.setCreationDate(gcal);
    }

    private void write(PDDocument document, PDPageContentStream cos, PDRectangle rect, List<String> pocInformation,
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
        cos.setFont(PDType1Font.HELVETICA, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(70, rect.getHeight() - 100);
        pocInformation.forEach(s -> {
            try {
                cos.showText(s);
                cos.newLine();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        cos.endText();
    }

    private void addCoronaAppIcon(PDDocument document, PDPageContentStream cos, PDRectangle rect) throws IOException {
        PDImageXObject pdImage =
            PDImageXObject.createFromFile(PdfGenerator.class.getResource("/logo.png").getPath(), document);
        cos.drawImage(pdImage, 280, rect.getHeight() - 70, 50, 50);
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA, 8);
        cos.newLineAtOffset(260, rect.getHeight() - 77);
        cos.showText("Corona-Antigen-Schnelltest");
        cos.endText();
    }

    private void generatePersonAddress(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest)
        throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(0, rect.getHeight() - 220);
        rightAlignment(cos, rect, quicktest.getFirstName() + " " + quicktest.getLastName());
        rightAlignment(cos, rect, quicktest.getStreet() + " " + quicktest.getHouseNumber());
        rightAlignment(cos, rect, quicktest.getZipCode() + " " + quicktest.getCity());
        rightAlignment(cos, rect, "Tel.: " + quicktest.getPhoneNumber());
        rightAlignment(cos, rect, "E-mail: " + quicktest.getEmail());
        cos.endText();

    }

    private void rightAlignment(PDPageContentStream cos, PDRectangle rect, String text) throws IOException {
        float pagewidth;
        float textWidth;
        float padding;
        pagewidth = rect.getWidth();
        textWidth = (PDType1Font.HELVETICA.getStringWidth(text) / 1000.0f) * 12;
        padding = pagewidth - ((40 * 2) + textWidth);

        cos.newLineAtOffset(padding, 0);
        cos.showText(text);
        cos.newLineAtOffset(-padding, 0);
        cos.newLine();
    }

    private void generateSubject(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest) throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(70, rect.getHeight() - 340);
        String dateAndTimeInGermany =
            ZonedDateTime.of(quicktest.getUpdatedAt(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(formatter);
        cos.showText("Schnelltestergebnis vom " + dateAndTimeInGermany);
        cos.newLine();
        cos.endText();

    }

    private void generateText(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest, String user)
        throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(70, rect.getHeight() - 380);
        switch (quicktest.getTestResult()) {
          case pending:
              cos.showText("Testergebnis: ausstehend");
              cos.newLine();
              break;
          case negative:
              cos.showText("Testergebnis: negativ");
              cos.newLine();
              break;
          case positive:
              cos.showText("Testergebnis: positiv");
              cos.newLine();
              break;
          default:
              cos.showText("Testergebnis: fehlgeschlagen");
              cos.newLine();
              break;
        }

        String dateAndTimeInGermany =
            ZonedDateTime.of(quicktest.getUpdatedAt(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(formatter);
        cos.showText("Durchgeführt: " + dateAndTimeInGermany);
        cos.newLine();
        cos.newLine();
        cos.showText("Weitere Angaben zu der Person: ");
        cos.newLine();

        switch (quicktest.getSex()) {
          case MALE:
              cos.showText("Geschlecht: männlich");
              cos.newLine();
              break;
          case FEMALE:
              cos.showText("Geschlecht: weiblich");
              cos.newLine();
              break;
          default:
              cos.showText("Geschlecht: divers");
              cos.newLine();
              break;
        }

        LocalDate datetime = LocalDate.parse(quicktest.getBirthday(), dtf);
        cos.showText("Geburtstag: " + datetime.format(formatterDate));
        cos.newLine();
        cos.newLine();
        cos.showText("Weitere Angaben zum Test: ");
        cos.newLine();
        cos.showText("Durchgeführt durch : " + user);
        cos.newLine();
        cos.showText("Hersteller-ID: " + quicktest.getTestBrandId());
        cos.newLine();
        if (quicktest.getTestBrandName() == null) {
            cos.showText("Handelsname: Unbekannt");
        } else {
            cos.showText("Handelsname: " + quicktest.getTestBrandName());
        }
        cos.newLine();
        if (quicktest.getTestResult() == positive) {
            cos.newLine();
            cos.newLine();
            String text = "Ihr Antigen-Schnelltests ist positiv ausgefallen, begeben Sie sich "
                + "bitte unverzüglich in die häusliche Quarantäne und informieren Sie Hausstandsangehörige und "
                + "weitere nahe Kontaktpersonen. Kontaktieren Sie umgehend Ihren Hausarzt oder die Leitstelle des "
                + "Ärztlichen Bereitschaftsdienstes unter der Nummern 116117 für weitere Verhaltensregeln und zur nun"
                + " benötigten Durchführung eines PCR-Tests. Bitte beachten Sie, dass auch ein negatives Ergebnis "
                + "eine mögliche Infektion nicht vollständig ausschließen kann und lediglich eine Momentaufnahme "
                + "darstellt.";

            List<String> lines = new ArrayList<>();
            int lastSpace = -1;
            while (text.length() > 0) {
                int spaceIndex = text.indexOf(' ', lastSpace + 1);
                if (spaceIndex < 0) {
                    spaceIndex = text.length();
                }
                String subString = text.substring(0, spaceIndex);
                float size = 12 * PDType1Font.HELVETICA.getStringWidth(subString) / 1000;
                if (size > rect.getWidth() - 150) {
                    if (lastSpace < 0) {
                        lastSpace = spaceIndex;
                    }
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();
                    lastSpace = -1;
                } else if (spaceIndex == text.length()) {
                    lines.add(text);
                    text = "";
                } else {
                    lastSpace = spaceIndex;
                }
            }
            for (String line : lines) {
                cos.showText(line);
                cos.newLineAtOffset(0, -1.5f * 12);
            }
            cos.newLine();
            cos.newLine();
        }
        cos.endText();

    }

    private void generateEnd(PDPageContentStream cos, PDRectangle rect) throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(70, rect.getHeight() - 800);
        cos.showText("Dieses Schreiben wurde maschinell erstellt und bedarf keiner Unterschrift.");
        cos.newLine();
        cos.endText();

    }

    private void close(PDDocument document, ByteArrayOutputStream output) throws IOException {
        document.save(output);
        document.close();
    }

}

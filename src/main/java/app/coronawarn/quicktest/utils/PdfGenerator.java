package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.domain.QuickTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PdfGenerator {

    private final int pending = 5;
    private final int negative = 6;
    private final int positive = 7;
    private final int failed = 8;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String pocName;
    private String pocStreet;
    private String pocHouseNumber;
    private String pocZip;
    private String pocCity;
    private String pocPhone;
    private String responsiblePersonFullName;
    private QuickTest quicktest;
    private PDDocument document;
    private PDRectangle rect;
    private PDPageContentStream cos;
    private ByteArrayOutputStream output = new ByteArrayOutputStream();

    /**
     * Generates a PDF file for rapid test result to print.
     *
     * @param pocName                   point of care data used in pdf
     * @param pocStreet                 point of care data used in pdf
     * @param pocHouseNumber            point of care data used in pdf
     * @param pocZip                    point of care data used in pdf
     * @param pocCity                   point of care data used in pdf
     * @param pocPhone                  point of care data used in pdf
     * @param quicktest                 Quicktest
     * @param responsiblePersonFullName responsible doctor
     * @throws IOException when creating pdf went wrong
     */
    public PdfGenerator(String pocName, String pocStreet, String pocHouseNumber,
                        String pocZip, String pocCity, String pocPhone, String responsiblePersonFullName,
                        QuickTest quicktest) throws IOException {
        this.pocName = pocName;
        this.pocStreet = pocStreet;
        this.pocHouseNumber = pocHouseNumber;
        this.pocZip = pocZip;
        this.pocCity = pocCity;
        this.pocPhone = pocPhone;
        this.quicktest = quicktest;
        this.responsiblePersonFullName = responsiblePersonFullName;
        config();
        write();
        close();
    }

    public ByteArrayOutputStream get() throws IOException {
        return output;
    }

    private void config() throws IOException {
        document = new PDDocument();
        PDPage page1 = new PDPage(PDRectangle.LETTER);
        document.addPage(page1);
        rect = page1.getMediaBox();
        cos = new PDPageContentStream(document, page1);
        PDDocumentInformation pdd = document.getDocumentInformation();
        pdd.setAuthor("Schnelltestportal");
        pdd.setTitle("Schnelltest-Ergebnis");
        pdd.setCreator("Schnelltestportal");
        LocalDate date = LocalDate.now();
        GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        pdd.setCreationDate(gcal);
    }

    private void write() throws IOException {
        generatePoCAddress();
        generatePersonAddress();
        generateSubject();
        generateText();
        generateEnd();
        cos.close();
    }

    private void generatePoCAddress() throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.TIMES_ROMAN, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(100, rect.getHeight() - 100);
        cos.showText(pocName);
        cos.newLine();
        cos.showText(pocStreet + " " + pocHouseNumber);
        cos.newLine();
        cos.showText(pocZip + " " + pocCity);
        cos.newLine();
        cos.showText("Tel.: " + pocPhone);
        cos.newLine();
        cos.endText();
    }

    private void generatePersonAddress() throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.TIMES_ROMAN, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(400, rect.getHeight() - 100);
        cos.showText(this.quicktest.getFirstName() + " " + this.quicktest.getLastName());
        cos.newLine();
        cos.showText(this.quicktest.getStreet() + " " + this.quicktest.getHouseNumber());
        cos.newLine();
        cos.showText(this.quicktest.getZipCode() + " " + this.quicktest.getCity());
        cos.newLine();
        cos.showText("Tel.: " + this.quicktest.getPhoneNumber());
        cos.newLine();
        cos.showText("E-mail: " + this.quicktest.getEmail());
        cos.newLine();
        cos.endText();

    }

    private void generateSubject() throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.TIMES_BOLD, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(100, rect.getHeight() - 250);
        cos.showText("Schnelltestergebnis von " + quicktest.getUpdatedAt().format(formatter) + " UTC");
        cos.newLine();
        cos.endText();

    }

    private void generateText() throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.TIMES_ROMAN, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(100, rect.getHeight() - 350);
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

        cos.showText("Durchgeführt: " + quicktest.getUpdatedAt().format(formatter) + " UTC");
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
        cos.showText("Durchgeführt durch : " + responsiblePersonFullName);
        cos.newLine();
        cos.showText("HerstellerID: " + quicktest.getTestBrandId());
        cos.newLine();
        if (quicktest.getTestBrandName() != null) {
            cos.showText("HerstellerName: " + quicktest.getTestBrandName());
            cos.newLine();
        }
        cos.endText();

    }

    private void generateEnd() throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.TIMES_ROMAN, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(100, rect.getHeight() - 550);
        cos.showText("Dieses Schreiben wurde maschinell erstellt und bedarf keiner Unterschrift.");
        cos.newLine();
        cos.endText();

    }

    private void close() throws IOException {
        document.save(output);
        document.close();
    }

}

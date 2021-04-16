package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.domain.QuickTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class PdfGenerator {

    private String pocName;
    private String pocStreet;
    private String pocHouseNumber;
    private String pocZip;
    private String pocCity;
    private String pocPhone;
    private String responsiblePersonFullName;

    private QuickTest quicktest;

    private LocalDate time;
    private PDDocument document;
    private PDRectangle rect;
    private PDPageContentStream cos;
    private PDFont fontPlain;
    private PDFont fontBold;
    private PDFont fontItalic;
    private PDFont fontMono;
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
     * @throws IOException              when creating pdf went wrong
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
        this.time = LocalDate.now();
        config();
        write();
        close();
    }

    public ByteArrayOutputStream get() throws IOException {
        return output;
    }

    private void config() throws IOException {
        fontPlain = PDType1Font.HELVETICA;
        fontBold = PDType1Font.HELVETICA_BOLD;
        fontItalic = PDType1Font.HELVETICA_OBLIQUE;
        fontMono = PDType1Font.COURIER;
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

        cos.close();
    }

    private void generatePoCAddress() throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.TIMES_ROMAN, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(25, 700);
        cos.showText(pocName);
        cos.newLine();
        cos.showText(pocStreet + " " + pocHouseNumber);
        cos.newLine();
        cos.showText(pocZip + " " + pocCity);
        cos.newLine();
        cos.showText("Tel.: " + pocPhone);
        cos.newLine();
        cos.endText();
        cos.moveTo(0,0);
    }

    private void generatePersonAddress() throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.TIMES_ROMAN, 12);
        cos.setLeading(14.5f);
        cos.newLineAtOffset(325, 700);
        cos.showText(this.quicktest.getFirstName() + " " + this.quicktest.getLastName());
        cos.newLine();
        cos.showText(this.quicktest.getStreet() + " " + this.quicktest.getHouseNumber());
        cos.newLine();
        cos.showText(this.quicktest.getZipCode() + " " + this.quicktest.getCity());
        cos.newLine();
        cos.showText("Tel.: " + this.quicktest.getPhoneNumber());
        cos.newLine();
        cos.endText();
        cos.moveTo(5000,5000);
    }

    private void close() throws IOException {
        document.save(output);
        document.close();
    }

}

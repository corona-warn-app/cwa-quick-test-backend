package app.coronawarn.quicktest.utils;

import java.awt.Color;
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
    private String personFirstName;
    private String personLastName;
    private String personBirthday;
    private String personStreet;
    private String personHouseNumber;
    private String personZip;
    private String personCity;
    private String responsiblePersonFullName;
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
     * @param personFirstName           tested person
     * @param personLastName            tested person
     * @param personBirthday            tested person
     * @param personStreet              tested person
     * @param personHouseNumber         tested person
     * @param personZip                 tested person
     * @param personCity                tested person
     * @param responsiblePersonFullName responsible doctor
     * @throws IOException              when creating pdf went wrong
     */
    public PdfGenerator(String pocName, String pocStreet, String pocHouseNumber,
                        String pocZip, String pocCity, String pocPhone,
                        String personFirstName, String personLastName, String personBirthday,
                        String personStreet, String personHouseNumber, String personZip,
                        String personCity, String responsiblePersonFullName) throws IOException {
        this.pocName = pocName;
        this.pocStreet = pocStreet;
        this.pocHouseNumber = pocHouseNumber;
        this.pocZip = pocZip;
        this.pocCity = pocCity;
        this.pocPhone = pocPhone;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.personBirthday = personBirthday;
        this.personStreet = personStreet;
        this.personHouseNumber = personHouseNumber;
        this.personZip = personZip;
        this.personCity = personCity;
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
        pdd.setAuthor("Tutorialspoint");
        pdd.setTitle("Sample document");
        pdd.setCreator("PDF Examples");
        pdd.setSubject("Example document");
        LocalDate date = LocalDate.now();
        GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        pdd.setCreationDate(gcal);
    }

    private void write() throws IOException {
        int line = 0;
        cos.beginText();
        cos.setFont(fontPlain, 12);
        cos.newLineAtOffset(70, rect.getHeight() - 50 * (++line));
        cos.showText(pocName);
        cos.newLine();
        cos.showText(pocStreet + " " + pocHouseNumber);
        cos.newLine();
        cos.showText(pocZip + " " + pocCity);
        cos.newLine();
        cos.showText("Tel.: " + pocPhone);
        cos.newLine();
        cos.endText();
        cos.setLineWidth(1);
        cos.moveTo(100, 100);
        cos.lineTo(500, 100);


        cos.close();
    }

    private void close() throws IOException {
        document.save(output);
        document.close();
    }

}

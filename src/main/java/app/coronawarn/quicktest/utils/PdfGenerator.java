/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
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
import static app.coronawarn.quicktest.utils.PdfUtils.getFormattedTime;
import static app.coronawarn.quicktest.utils.PdfUtils.splitStringToParagraph;

import app.coronawarn.quicktest.config.PdfConfig;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.domain.QuickTest;
import java.awt.Color;
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
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfGenerator {

    private final PdfConfig pdfConfig;
    private final QuickTestConfig quickTestConfig;

    private final int pendingPcr = 10;
    private final int negativePcr = 11;
    private final int positivePcr = 12;
    private final int pending = 5;
    private final int negative = 6;
    private final int positive = 7;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final DateTimeFormatter formatterEn = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final int offsetX = 70;
    private final float leading = 14.5f;
    private final int fontSize = 12;
    private PDFont fontType;
    private PDFont fontTypeBold;

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
        write(document, cos, rect1, pocInformation, quicktest, user, false);

        PDPage page2 = new PDPage(PDRectangle.A4);
        document.addPage(page2);
        page2.setMediaBox(PDRectangle.A4);
        PDPageContentStream cos2 = new PDPageContentStream(document, page2);
        write(document, cos2, page2.getMediaBox(), pocInformation, quicktest, user, true);
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        close(document, pdf);
        return pdf;
    }

    private void config(PDDocument document) {

        final ClassPathResource cs = new ClassPathResource("pdf/fonts/arial.ttf");
        try {
            this.fontType = PDType0Font.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/arial.ttf"));
            this.fontTypeBold = PDType0Font.load(document,
              Objects.requireNonNull(cs.getClassLoader())
                .getResourceAsStream("pdf/fonts/arialbd.ttf"));
        } catch (IOException e) {
            log.error("Could not load font");
        }

        PDDocumentInformation pdd = document.getDocumentInformation();
        pdd.setAuthor(pdfConfig.getAuthorPdfPropertiesText());
        pdd.setTitle(pdfConfig.getQuickTestHeadlineText());
        pdd.setCreator(pdfConfig.getCreatorPdfPropertiesText());
        LocalDate date = LocalDate.now();
        GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        pdd.setCreationDate(gcal);
    }

    private void write(PDDocument document, PDPageContentStream cos, PDRectangle rect,
                       List<String> pocInformation,
                       QuickTest quicktest,
                       String user,
                       boolean english) throws IOException {
        generatePoCAddress(cos, rect, pocInformation);
        addCoronaAppIcon(document, cos, rect);
        generatePersonAddress(cos, rect, quicktest);
        generateSubject(cos, rect, quicktest, english);
        generateText(cos, rect, quicktest, user, english);
        generateEnd(cos, rect, english);
        if (!isEnvironmentNameEmpty()) {
            generateTrainingText(cos, rect, english);
        }
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
            cos.drawImage(pdImage, 280f, rect.getHeight() - offsetX, 50f, 50f);
        } catch (IOException | NullPointerException e) {
            log.error("Logo not found!");
        }
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.newLineAtOffset(230f, rect.getHeight() - 85f);
        cos.showText(pdfConfig.getQuickTestHeadlineText());
        cos.endText();
    }

    private void generatePersonAddress(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest)
      throws IOException {
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 220f);
        cos.showText(quicktest.getFirstName() + " " + quicktest.getLastName());
        cos.newLine();
        cos.showText(quicktest.getStreet() + " "
            + (quicktest.getHouseNumber() == null ? "" : quicktest.getHouseNumber()));
        cos.newLine();
        cos.showText(quicktest.getZipCode() + " " + quicktest.getCity());
        cos.newLine();
        cos.showText(pdfConfig.getPersonPhoneDescriptionText() + quicktest.getPhoneNumber());
        cos.newLine();
        
        if (quicktest.getEmail() != null) {
            cos.showText(pdfConfig.getPersonEmailDescriptionText() + quicktest.getEmail());
        }
        
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

    private void generateSubject(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest, boolean english)
      throws IOException {
        cos.beginText();
        cos.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 340f);
        if (english) {
            if (TestTypeUtils.isRat(quicktest.getTestType())) {
                cos.showText(pdfConfig.getQuickTestOfDateTextEn() + getFormattedTime(quicktest.getUpdatedAt(),
                        formatterEn));
            } else {
                cos.showText(pdfConfig.getPcrTestOfDateTextEn() + getFormattedTime(quicktest.getUpdatedAt(),
                        formatterEn));
            }
        } else {
            if (TestTypeUtils.isRat(quicktest.getTestType())) {
                cos.showText(
                        pdfConfig.getQuickTestOfDateText() + getFormattedTime(quicktest.getUpdatedAt(), formatter));
            } else {
                cos.showText(
                        pdfConfig.getPcrTestOfDateText() + getFormattedTime(quicktest.getUpdatedAt(), formatter));
            }
        }
        cos.newLine();
        cos.endText();

    }

    private void generateText(PDPageContentStream cos, PDRectangle rect, QuickTest quicktest, String user,
                              boolean english)
      throws IOException {
        cos.beginText();
        cos.setFont(fontTypeBold, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 380);

        switch (quicktest.getTestResult() != null ? quicktest.getTestResult() : -1) {
          case pending:
          case pendingPcr:
              if (english) {
                  cos.showText(pdfConfig.getTestResultDescriptionTextEn() + pdfConfig.getTestResultPendingTextEn());
              } else {
                  cos.showText(pdfConfig.getTestResultDescriptionText() + pdfConfig.getTestResultPendingText());
              }
              cos.newLine();
              break;
          case negative:
          case negativePcr:
              if (english) {
                  cos.showText(pdfConfig.getTestResultDescriptionTextEn() + pdfConfig.getTestResultNegativeTextEn());
                  cos.newLine();
              } else {
                  cos.showText(pdfConfig.getTestResultDescriptionText());
                  for (String line : splitStringToParagraph(pdfConfig.getTestResultNegativeText(), 70)) {
                      cos.showText(line);
                      cos.newLine();
                  }
              }
              break;
          case positive:
          case positivePcr:
              if (english) {
                  cos.showText(pdfConfig.getTestResultDescriptionTextEn() + pdfConfig.getTestResultPositiveTextEn());
              } else {
                  cos.showText(pdfConfig.getTestResultDescriptionText() + pdfConfig.getTestResultPositiveText());
              }
              cos.newLine();
              break;
          default:
              if (english) {
                  cos.showText(pdfConfig.getTestResultDescriptionTextEn() + pdfConfig.getTestResultDefaultTextEn());
              } else {
                  cos.showText(pdfConfig.getTestResultDescriptionText() + pdfConfig.getTestResultDefaultText());
              }
              cos.newLine();
              break;
        }
        cos.setFont(fontType, fontSize);

        String dateAndTimeInGermany;
        if (quicktest.getUpdatedAt() != null) {
            dateAndTimeInGermany =
              ZonedDateTime.of(quicktest.getUpdatedAt(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Europe/Berlin")).format(english ? formatterEn : formatter);
        } else {
            dateAndTimeInGermany = "-";
        }
        if (english) {
            cos.showText(pdfConfig.getExecutedByDescriptionTextEn() + dateAndTimeInGermany);
        } else {
            cos.showText(pdfConfig.getExecutedByDescriptionText() + dateAndTimeInGermany);
        }
        cos.newLine();
        cos.newLine();
        if (english) {
            cos.showText(pdfConfig.getFurtherDataAboutThePersonTextEn());
        } else {
            cos.showText(pdfConfig.getFurtherDataAboutThePersonText());
        }
        cos.newLine();

        switch (quicktest.getSex() != null ? quicktest.getSex() : DIVERSE) {
          case MALE:
              if (english) {
                  cos.showText(pdfConfig.getGenderDescriptionTextEn() + pdfConfig.getMaleTextEn());
              } else {
                  cos.showText(pdfConfig.getGenderDescriptionText()  + pdfConfig.getMaleText());
              }
              cos.newLine();
              break;
          case FEMALE:
              if (english) {
                  cos.showText(pdfConfig.getGenderDescriptionTextEn() + pdfConfig.getFemaleTextEn());
              } else {
                  cos.showText(pdfConfig.getGenderDescriptionText()  + pdfConfig.getFemaleText());
              }
              cos.newLine();
              break;
          default:
              if (english) {
                  cos.showText(pdfConfig.getGenderDescriptionTextEn() + pdfConfig.getDiverseTextEn());
              } else {
                  cos.showText(pdfConfig.getGenderDescriptionText() + pdfConfig.getDiverseText());
              }
              cos.newLine();
              break;
        }
        String birthdayDescText =
          english ? pdfConfig.getBirthDateDescriptionTextEn() : pdfConfig.getBirthDateDescriptionText();
        if (quicktest.getBirthday() != null) {
            LocalDate datetime = LocalDate.parse(quicktest.getBirthday(), dtf);
            cos.showText(birthdayDescText + datetime.format(english ? dtf : formatterDate));
        } else {
            cos.showText(birthdayDescText + "-");
        }
        cos.newLine();
        if (quicktest.getAdditionalInfo() != null) {
            cos.newLine();
            if (english) {
                cos.showText(pdfConfig.getAdditionalInfoDescriptionTextEn());
            } else {
                cos.showText(pdfConfig.getAdditionalInfoDescriptionText());
            }
            cos.newLine();
            for (String line : splitStringToParagraph(quicktest.getAdditionalInfo(), 80)) {
                cos.showText(line);
                cos.newLine();
            }
        }
        cos.newLine();
        cos.newLine();
        if (english) {
            cos.showText(pdfConfig.getFurtherDataAboutTestDescriptionTextEn());
        } else {
            cos.showText(pdfConfig.getFurtherDataAboutTestDescriptionText());
        }
        cos.newLine();
        if (english) {
            cos.showText(pdfConfig.getExecutedFromDescriptionTextEn() + user);
        } else {
            cos.showText(pdfConfig.getExecutedFromDescriptionText() + user);
        }
        cos.newLine();
        if (TestTypeUtils.isRat(quicktest.getTestType())) {
            cos.showText(pdfConfig.getTestBrandIdDescriptionText() + quicktest.getTestBrandId());
            cos.newLine();
        } else {
            cos.showText(pdfConfig.getPcrTestSystemDescriptionText());
            for (String line : splitStringToParagraph(quicktest.getTestBrandName(), 60)) {
                cos.showText(line);
                cos.newLine();
            }
        }
        if (TestTypeUtils.isRat(quicktest.getTestType())) {
            if (quicktest.getTestBrandName() == null) {
                if (english) {
                    cos.showText(pdfConfig.getTestBrandNameDescriptionTextEn() + pdfConfig.getTradeNameEmptyTextEn());
                } else {
                    cos.showText(pdfConfig.getTestBrandNameDescriptionText() + pdfConfig.getTradeNameEmptyText());
                }
            } else {
                if (english) {
                    cos.showText(pdfConfig.getTestBrandNameDescriptionTextEn());
                } else {
                    cos.showText(pdfConfig.getTestBrandNameDescriptionText());
                }
                for (String line : splitStringToParagraph(quicktest.getTestBrandName(), 60)) {
                    cos.showText(line);
                    cos.newLine();
                }
            }
        }
        String useText = "";
        final Short testResult = quicktest.getTestResult();
        if (testResult != null && (testResult == positive || testResult == positivePcr)) {
            useText = english ? pdfConfig.getPositiveInstructionTextEn() : pdfConfig.getPositiveInstructionText();
        } else if (testResult != null && (testResult == negative || testResult == negativePcr)) {
            useText = english ? pdfConfig.getNegativeInstructionTextEn() : pdfConfig.getNegativeInstructionText();
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

    private void generateTrainingText(PDPageContentStream cos, PDRectangle rect,
                              boolean english) throws IOException {
        PDFont font = PDType1Font.HELVETICA_BOLD;
        int trainingFontSize = 28;
        float trainingFontWidth = 0.0f;

        cos.beginText();
        cos.setFont(font, trainingFontSize);
        cos.setNonStrokingColor(Color.ORANGE);
        if (english) {
            trainingFontWidth = font.getStringWidth(pdfConfig.getCertForTrainingEn()) / 1000 * trainingFontSize;
            float tx = (trainingFontWidth / 2) / (float)Math.sqrt(2);
            cos.transform(Matrix.getRotateInstance(
                    Math.toRadians(45), rect.getWidth() / 2 - tx, rect.getHeight() / 2 - tx));
            cos.showText(pdfConfig.getCertForTrainingEn());
        } else {
            trainingFontWidth = font.getStringWidth(pdfConfig.getCertForTrainingDe()) / 1000 * trainingFontSize;
            //Horizontal zentrierte Schrift
            //cos.newLineAtOffset(rect.getWidth() / 2 - trainingFontWidth /2, (rect.getHeight() / 2));
            float tx = (trainingFontWidth / 2) / (float)Math.sqrt(2);
            cos.transform(Matrix.getRotateInstance(
                    Math.toRadians(45), rect.getWidth() / 2 - tx, rect.getHeight() / 2 - tx));
            cos.showText(pdfConfig.getCertForTrainingDe());
        }
        cos.endText();
    }

    private void generateEnd(PDPageContentStream cos, PDRectangle rect, boolean english) throws IOException {
        cos.beginText();
        cos.setFont(fontType, fontSize);
        cos.setLeading(leading);
        cos.newLineAtOffset(offsetX, rect.getHeight() - 800);
        if (english) {
            cos.showText(pdfConfig.getSignatureTextEn());
        } else {
            cos.showText(pdfConfig.getSignatureText());
        }
        cos.newLine();
        cos.endText();
    }

    private void close(PDDocument document, ByteArrayOutputStream output) throws IOException {
        document.save(output);
        document.close();
    }

    private boolean isEnvironmentNameEmpty() {
        if (quickTestConfig != null
                && quickTestConfig.getFrontendContextConfig() != null
                && quickTestConfig.getFrontendContextConfig().getEnvironmentName() != null
                && !quickTestConfig.getFrontendContextConfig().getEnvironmentName().isEmpty()) {
            return false;
        }

        return true;
    }
}

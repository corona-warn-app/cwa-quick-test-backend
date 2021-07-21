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

package app.coronawarn.quicktest.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("pdf")
public class PdfConfig {
    private String signatureText = "Dieses Schreiben wurde maschinell erstellt und bedarf keiner Unterschrift.";
    private String positiveInstructionText = "Ihr Antigen-Schnelltests ist positiv ausgefallen, begeben Sie sich "
      + "bitte unverzüglich in die häusliche Quarantäne und informieren Sie Hausstandsangehörige und "
      + "weitere nahe Kontaktpersonen. Kontaktieren Sie umgehend Ihren Hausarzt oder die Leitstelle des "
      + "Ärztlichen Bereitschaftsdienstes unter der Nummern 116117 für weitere Verhaltensregeln und zur nun"
      + " benötigten Durchführung eines PCR-Tests. ";
    private String negativeInstructionText = "Bitte beachten Sie, dass ein negatives Ergebnis eine mögliche "
      + "Infektion nicht vollständig ausschließen kann und lediglich eine Momentaufnahme darstellt.";
    private String testBrandNameDescriptionText = "Handelsname: ";
    private String tradeNameEmptyText = "nicht angegeben";
    private String testBrandIdDescriptionText = "Test-ID: ";
    private String quickTestHeadlineText = "Corona-Antigen-Schnelltest";
    private String authorPdfPropertiesText = "Schnelltestportal";
    private String creatorPdfPropertiesText = "Schnelltestportal";
    private String personPhoneDescriptionText = "Tel.: ";
    private String logoPath = "logo.png";
    private String quickTestOfDateText = "Schnelltestergebnis vom ";
    private String personEmailDescriptionText = "E-mail: ";
    private String testResultDescriptionText = "Testergebnis: ";
    private String testResultPendingText = "ausstehend";
    private String testResultNegativeText = "negativ";
    private String testResultPositiveText = "positiv";
    private String testResultDefaultText = "fehlgeschlagen";
    private String executedByDescriptionText = "Durchgeführt: ";
    private String furtherDataAboutThePersonText = "Weitere Angaben zu der Person: ";
    private String genderDescriptionText = "Geschlecht: ";
    private String maleText = "männlich";
    private String femaleText = "weiblich";
    private String diverseText = "divers";
    private String birthDateDescriptionText = "Geburtstag: ";
    private String furtherDataAboutTestDescriptionText = "Weitere Angaben zum Test: ";
    private String executedFromDescriptionText = "Durchgeführt durch: ";

    private String certFlagPath = "pdf/eu_flag.png";
    private String certCertlogoPath = "pdf/certificate.png";
    private String certFlagSeparatorPath = "pdf/flag_seperator.png";
    private String certLineSeparator = " <br> ";
    private String certNameDe = "Name, Vorname";
    private String certNameEn = "Surname(s) and forename(s)";
    private String certBirthdayDe = "Geburtsdatum";
    private String certBirthdayEn = "Date of birth";
    private String certCertIdDe = "Eindeutige Zertifikatkennung";
    private String certCertIdEn = "Unique certificate identifier";
    private String certDiseaseAgentDe = "Erreger";
    private String certDiseaseAgentEn = "Disease or agent targeted";
    private String certDiseaseAgentTargeted = "COVID-19";
    private String certHeaderTestEn = "TEST CERTIFICATE";
    private String certHeaderTestDe = "TESTZERTIFIKAT";
    private String certTestTypeEn = "Type of test";
    private String certTestTypeDe = "Testart";
    private String certTestNameEn = "Test name (optional for NAAT)";
    private String certTestNameDe = "Name des Tests <br> (beim NAAT-Test fakultativ)";
    private String certTestManufacturerEn = "Test manufacturer <br> (optional for NAAT)";
    private String certTestManufacturerDe = "Testhersteller <br> (beim NAAT-Test fakultativ)";
    private String certDateSampleCollectionEn = "Date and time of the test sample <br> collection";
    private String certDateSampleCollectionDe = "Datum und Uhreit der Probenahme";
    private String certTestResultEn = "Result of the test";
    private String certTestResultDe = "Testergebnis";
    private String certTestingCentreEn = "Testing centre or facility";
    private String certTestingCentreDe = "Testzentrum oder -einrichtung";
    private String certStateOfTestEn = "Member State of test";
    private String certStateOfTestDe = "Mitgliedstaat";
    private String certIssuerEn = "Certificate issuer";
    private String certIssuerDe = "Zertifikataussteller";
    private String certTestType = "Rapid immunoassay";
    private String certIssuerState = "DE";

    private String certQrDescription = "Scannen Sie den <br> nebenstehenden <br> QR-Code mit der <br> "
      + "CovPass-App oder der <br> Corona-Warn-App, um <br> Ihren digitalen Nachweis <br> zu erstellen. Laden Sie <br> "
      + "dazu die CovPass-App <br> oder die Corona-Warn- <br> App in Ihrem App Store <br> herunter.";

    private String certMemberStateDescriptionDe =
      "Diese Bescheinigung ist kein Reisedokument. Die wissenschaft- <br> lichen Erkentnisse zu COVID-19 in den "
        + "Bereichen Impfung, <br> Testung und Genesung entwickeln sich fortlaufend weiter, auch <br> "
        + "im Hinblick auf neue  besorgniserregende Virusvarianten. Bitte <br> informieren Sie sich vor Reiseantritt "
        + "über die am Zielort <br> geltenden Gesundheitsmaßnahmen und entrsprechenenden <br> Beschränkungen.";

    private String certMemberStateDescriptionEn =
      "This certificate is not a travel document. The scientific evidence <br> on COVID-19 vaccination, testing and  "
        + "recovery continues to <br> evolve, also in view of new variants of concern of the virus. <br> "
        + "Before traveling, please check the applicable public health <br> measures and related restrictions applied "
        + "at the point of <br> destination.";

    private String certMemberStateFurtherDescription =
      "Zusammengefasste Informationen über die in den europäi- <br> schen Ländern jeweils geltenden Corona-Maßnahmen "
        + "und <br> Reisebeschränkungen finden Sie unter anderem auf der Seite: <br> "
        + "https://reopen.europa.eu/de <br> Die Aktualisierung der Informationen obliegt den jeweiligen <br> "
        + "europäischen Ländern. Dort findedn Sie auch Hinweise, welche <br> Informationen und Dokumente Sie bei der "
        + "Einreise vorlegen <br> müssen. Die Datenverarbeitung unterliegt den Vorschriften des <br> "
        + "jeweiligen Einreiselandes";
}

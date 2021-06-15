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
}

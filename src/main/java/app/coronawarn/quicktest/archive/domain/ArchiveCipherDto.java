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

package app.coronawarn.quicktest.archive.domain;

import app.coronawarn.quicktest.model.Sex;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvIgnore;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public abstract class ArchiveCipherDto {

    @CsvBindByName(column = "41_Test-ID")
    private String hashedGuid;

    @CsvBindByName(column = "40_Proben-ID")
    private String shortHashedGuid;

    @CsvBindByName(column = "10_Partner-ID")
    private String tenantId;

    @CsvBindByName(column = "11_Testzentrums-ID")
    private String pocId;

    @CsvBindByName(column = "13_Datum Patientendatenerfassung")
    private LocalDateTime createdAt;

    @CsvBindByName(column = "14_Datum Testergebniseingabe")
    private LocalDateTime updatedAt;

    // Persistence (Hibernate) version from 'quick_test_archive' table
    @CsvIgnore
    private Integer version;

    @CsvBindByName(column = "31_Einwilligung zur Testergebnisübertragung (in CWA)")
    private Boolean confirmationCwa;

    @CsvBindByName(column = "44_Testergebnis")
    private Short testResult;

    @CsvBindByName(column = "32_Datenschutzhinweis übergeben")
    private Boolean privacyAgreement;

    @CsvBindByName(column = "20_Nachname")
    private String lastName;

    @CsvBindByName(column = "21_Vorname")
    private String firstName;

    @CsvBindByName(column = "27_eMail-Adresse")
    private String email;

    @CsvBindByName(column = "28_Telefon-Nr")
    private String phoneNumber;

    @CsvBindByName(column = "22_Geschlecht")
    private Sex sex;

    @CsvBindByName(column = "24_Strasse und Hausnummer")
    private String street;

    @CsvIgnore
    private String houseNumber;

    @CsvBindByName(column = "25_Postleitzahl")
    private String zipCode;

    @CsvBindByName(column = "26_Ort")
    private String city;

    @CsvBindByName(column = "42_Testkit-ID")
    private String testBrandId;

    @CsvBindByName(column = "43_Testkit-Name")
    private String testBrandName;

    @CsvBindByName(column = "23_Geburtsdatum")
    private String birthday;

    @CsvBindByName(column = "45_CWA-Test-ID")
    private String testResultServerHash;

    @CsvBindByName(column = "30_EU-Zertifikat (DCC)")
    private String dcc;

    @CsvBindByName(column = "29_zusätzliche Informationen")
    private String additionalInfo;

    @CsvBindByName(column = "12_Name Testzentrum")
    private String groupName;
}

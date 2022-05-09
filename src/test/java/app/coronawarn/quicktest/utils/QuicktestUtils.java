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

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import java.time.LocalDateTime;

public class QuicktestUtils {

    public static QuickTest getQuickTest() {
        return getQuickTest("LP217198-3", 6);
    }

    public static QuickTest getQuickTest(String testType, int result) {
        QuickTest quicktest = new QuickTest();
        quicktest.setZipCode("12345");
        quicktest.setTestResult((short) result);
        quicktest.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quicktest.setCity("oyvkpigcga");
        quicktest.setConfirmationCwa(Boolean.TRUE);
        quicktest.setShortHashedGuid("cjfybkfn");
        quicktest.setPhoneNumber("00491777777777777");
        quicktest.setEmail("test@test.test");
        quicktest.setTenantId("4711");
        quicktest.setPocId("4711-A");
        quicktest.setTestBrandId("AT116/21");
        quicktest.setTestBrandName("PerGrande BioTech Development Co., Ltd., SARS-CoV-2 Antigen Detection Kit " +
          "(Colloidal Gold Immunochromatographic assay)");
        quicktest.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        quicktest.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quicktest.setFirstName("Joę");
        quicktest.setLastName("Miller");
        quicktest.setStreet("Boe");
        quicktest.setHouseNumber("11");
        quicktest.setPrivacyAgreement(Boolean.FALSE);
        quicktest.setSex(Sex.DIVERSE);
        quicktest.setBirthday("1911-11-11");
        quicktest.setDiseaseAgentTargeted("COVID-19");
        quicktest.setAdditionalInfo("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod " +
          "tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et " +
          "justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea tak");
        quicktest.setGroupName("Mein Testcenter mit fünfzig Zeichen Testzertifikat");
        quicktest.setTestType(testType);
        return quicktest;
    }
}

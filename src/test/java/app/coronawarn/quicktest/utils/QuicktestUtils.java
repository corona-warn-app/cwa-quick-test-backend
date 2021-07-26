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
        QuickTest quicktest = new QuickTest();
        quicktest.setZipCode("12345");
        quicktest.setTestResult(Short.parseShort("6"));
        quicktest.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quicktest.setCity("oyvkpigcga");
        quicktest.setConfirmationCwa(Boolean.TRUE);
        quicktest.setShortHashedGuid("cjfybkfn");
        quicktest.setPhoneNumber("00491777777777777");
        quicktest.setEmail("test@test.test");
        quicktest.setTenantId("4711");
        quicktest.setPocId("4711-A");
        quicktest.setTestBrandId("AT116/21");
        quicktest.setTestBrandName("Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)");
        quicktest.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        quicktest.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quicktest.setFirstName("Joe");
        quicktest.setLastName("Miller");
        quicktest.setStreet("Boe");
        quicktest.setHouseNumber("11");
        quicktest.setPrivacyAgreement(Boolean.FALSE);
        quicktest.setSex(Sex.DIVERSE);
        quicktest.setBirthday("1911-11-11");
        quicktest.setDiseaseAgentTargeted("COVID-19");
        return quicktest;
    }
}

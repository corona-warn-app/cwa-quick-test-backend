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

package app.coronawarn.quicktest.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import app.coronawarn.quicktest.domain.QuickTestArchive;
import app.coronawarn.quicktest.model.Sex;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class QuickTestArchiveTest {

    @Test
    void constructorTest() {
        QuickTestArchive quickTestArchive = new QuickTestArchive();
        quickTestArchive.setZipCode("12345");
        quickTestArchive.setTestResult(Short.parseShort("5"));
        quickTestArchive.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quickTestArchive.setCity("oyvkpigcga");
        quickTestArchive.setConfirmationCwa(Boolean.TRUE);
        quickTestArchive.setShortHashedGuid("cjfybkfn");
        quickTestArchive.setPhoneNumber("00491777777777777");
        quickTestArchive.setEmail("test@test.test");
        quickTestArchive.setTenantId("4711");
        quickTestArchive.setPocId("4711-A");
        quickTestArchive.setTestBrandId("AT116/21");
        quickTestArchive.setTestBrandName("Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)");
        quickTestArchive.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        quickTestArchive.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quickTestArchive.setFirstName("Joe");
        quickTestArchive.setLastName("Miller");
        quickTestArchive.setBirthday("01.01.1954");
        quickTestArchive.setStreet("Boe");
        quickTestArchive.setHouseNumber("11");
        quickTestArchive.setPrivacyAgreement(Boolean.FALSE);
        quickTestArchive.setSex(Sex.DIVERSE);
        quickTestArchive.setPdf("Hello".getBytes());
        quickTestArchive.setAdditionalInfo("Hello");
        quickTestArchive.setGroupName("Barmen");
        quickTestArchive.setTestType("LP217198-3");
        assertEquals("QuickTestArchive(hashedGuid=mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht, "
                        + "shortHashedGuid=cjfybkfn, tenantId=4711, pocId=4711-A, createdAt=2021-04-08T08:11:11, "
                        + "updatedAt=2021-04-08T08:11:12, version=null, confirmationCwa=true, testResult=5, "
                        + "privacyAgreement=false, lastName=Miller, firstName=Joe, email=test@test.test, "
                        + "phoneNumber=00491777777777777, sex=DIVERSE, street=Boe, houseNumber=11, zipCode=12345, "
                        + "city=oyvkpigcga, testBrandId=AT116/21, testBrandName=Panbio (TM) Covid-19 Ag Rapid Test "
                        + "Device (Nasal), birthday=01.01.1954, pdf=[72, 101, 108, 108, 111], testResultServerHash=null," +
                        " dcc=null, additionalInfo=Hello, groupName=Barmen, testType=LP217198-3)",
                quickTestArchive.toString());
    }

    @Test
    void checkVersionNotHasSetter() {
        try {
            Method method = QuickTestArchive.class.getDeclaredMethod("setVersion");
            fail("setVersion method not allowed");
        } catch (NoSuchMethodException e) {
        }
    }
}

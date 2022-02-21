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

package app.coronawarn.quicktest.entity;

import app.coronawarn.quicktest.domain.QuickTestLog;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class QuickTestStatisticsTest {

    @Test
    void constructorTest() {
        QuickTestLog quickTestLog = new QuickTestLog();
        quickTestLog.setPocId("testPocId");
        quickTestLog.setTenantId("testTenantId");
        quickTestLog.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quickTestLog.setPositiveTestResult(true);
        quickTestLog.setTestType("LP6464-4");
        assertEquals(
                "QuickTestLog(id=0, pocId=testPocId, tenantId=testTenantId, " +
                        "createdAt=2021-04-08T08:11:12, positiveTestResult=true, testType=LP6464-4)",
                quickTestLog.toString());
    }
}

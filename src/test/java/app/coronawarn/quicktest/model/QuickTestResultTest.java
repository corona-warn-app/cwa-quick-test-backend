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

package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.client.TestResultServerClient;
import app.coronawarn.quicktest.config.QuickTestConfig;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.utils.Utilities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
public class QuickTestResultTest{

    @Autowired
    QuickTestConfig quickTestConfig;
    @MockBean
    private QuickTestService quickTestService;
    @MockBean
    TestResultServerClient testResultServerClient;



    @Test
    public void testResultListSampleCollection() throws Exception {
        Map<String, String> ids = new HashMap<>();
        String id = "b".repeat(64);
        Short resultShort = 5;
        ResponseEntity<Void> responseEntity =  ResponseEntity.noContent().build();
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        QuickTestResult quickTestResult = new QuickTestResult();
        quickTestResult.setId("id");
        quickTestResult.setResult((short) 5);
        quickTestResult.setSampleCollection(System.currentTimeMillis());
        QuickTestResultList resultList = new QuickTestResultList();
        resultList.setTestResults(Collections.singletonList(
                new QuickTestResult().setId(id).setResult(resultShort).setSampleCollection(System.currentTimeMillis())));
        QuickTestUpdateRequest quickTestUpdateRequest = new QuickTestUpdateRequest();
        quickTestUpdateRequest.setTestBrandId("testBrandId");
        quickTestUpdateRequest.setResult((short)6);
        quickTestUpdateRequest.setTestBrandName("TestBrandName");
        quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                quickTestUpdateRequest,
                new ArrayList<>(),
                "User");
        when(testResultServerClient.results(resultList)).thenReturn(responseEntity);
        assertEquals(responseEntity, testResultServerClient.results(resultList));
        verify(testResultServerClient, times(1)).results(resultList);

    }


    @Test
    public void testFeignUpdateQuickTestResultTest(){
        ResponseEntity<Void> responseEntity =  ResponseEntity.noContent().build();
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        QuickTestResult quickTestResult = new QuickTestResult();
        quickTestResult.setId("id");
        quickTestResult.setResult((short) 5);
        quickTestResult.setSampleCollection(System.currentTimeMillis());
        assertNotNull(quickTestResult);
        List<QuickTestResult> testResultList = new ArrayList<>();
        testResultList.add(quickTestResult);
        assertNotNull(testResultList);
        QuickTestResultList quickTestResultList = new QuickTestResultList();
        quickTestResultList.setTestResults(testResultList);
        assertNotNull(quickTestResultList);
        when(testResultServerClient.results(quickTestResultList)).thenReturn(responseEntity);
        assertEquals(responseEntity, testResultServerClient.results(quickTestResultList));
        verify(testResultServerClient, times(1)).results(quickTestResultList);

    }

    @Test
    public void testGetPocInformationFromToken(){
        Stream<String> stringStream = Stream.of("", "b", "c", "d","e");
        String[] stringArray = stringStream.toArray(size -> new String[size]);
           String firstValue = stringArray[0];
        System.out.println("FirstValue_____:"+firstValue);
    }


}

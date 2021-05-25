package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.client.TestResultServerClient;
import app.coronawarn.quicktest.service.QuickTestService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
public class QuickTestResultTest{


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
        quickTestService.updateQuickTest(ids,
                "6fa4dcecf716d8dd96c9e927dda5484f1a8a9da03155aa760e0c38f9bed645c4",
                (short) 6,
                "testBrandId",
                "TestBrandName",
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


}
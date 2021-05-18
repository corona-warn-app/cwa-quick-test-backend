package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.client.TestResultServerClient;

import app.coronawarn.quicktest.utils.Utilities;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class QuickTestResultTest {


    @MockBean
    TestResultServerClient testResultServerClient;



    @Test
    void setLocalDateTime() {

        LocalDateTime localTime = Utilities.getCurrentLocalDateTimeUtc();
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        assertEquals(localTime,localDateTime);



    }

    @Test
    public void testFeignUpdateQuickTestResultTest(){
        ResponseEntity<Void> responseEntity =  ResponseEntity.noContent().build();
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        System.out.println();
        QuickTestResult quickTestResult = new QuickTestResult();
        quickTestResult.setId("id");
        quickTestResult.setResult((short) 5);
        quickTestResult.setLocalDateTime(Utilities.getCurrentLocalDateTimeUtc());
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
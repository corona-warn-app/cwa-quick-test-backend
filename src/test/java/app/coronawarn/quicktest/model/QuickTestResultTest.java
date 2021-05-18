package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.client.TestResultServerClient;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.repository.QuickTestRepository;
import app.coronawarn.quicktest.service.QuickTestService;
import app.coronawarn.quicktest.service.TestResultService;
import app.coronawarn.quicktest.utils.PdfGenerator;
import app.coronawarn.quicktest.utils.Utilities;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
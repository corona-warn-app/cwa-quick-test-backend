package app.coronawarn.quicktest.model;

import app.coronawarn.quicktest.client.TestResultServerClient;

import app.coronawarn.quicktest.utils.Utilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("external")
class QuickTestResultTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    TestResultServerClient testResultServerClient;



//    @Test
//    public void testCollection() throws Exception {
//
//        //pending
//        Long time = System.currentTimeMillis();
//        System.out.println();
//      //
//        String id = "b".repeat(64);
//        Short resultShort = 5;
//
//        QuickTestResult quickTestResult = new QuickTestResult();
//        quickTestResult.setId("id");
//        quickTestResult.setResult((short) 5);
//
//        QuickTestResultList resultList = new QuickTestResultList();
//        resultList.setTestResults(Collections.singletonList(
//                new QuickTestResult().setId(id).setResult(resultShort).setSampleCollection(System.currentTimeMillis())
//        ));
//
//        quickTestResult.setSampleCollection(System.currentTimeMillis());
//
//         mockMvc.perform(MockMvcRequestBuilders
//                .post("/api/v1/quicktest/results")
//                .accept(MediaType.APPLICATION_JSON_VALUE)
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(objectMapper.writeValueAsString(resultList)))
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(MockMvcResultMatchers.status().isNoContent());
//            System.out.println();
//
//
//
//    }

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
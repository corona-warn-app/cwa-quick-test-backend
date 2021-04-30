package app.coronawarn.quicktest.controller;

import app.coronawarn.quicktest.config.QuicktestKeycloakSpringBootConfigResolver;
import app.coronawarn.quicktest.domain.AntigenTest;

import app.coronawarn.quicktest.service.AntigenTestService;
import com.c4_soft.springaddons.security.oauth2.test.mockmvc.keycloak.ServletKeycloakAuthUnitTestingSupport;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static app.coronawarn.quicktest.config.SecurityConfig.ROLE_LAB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AntigenTestController.class)
@ComponentScan(basePackageClasses = {KeycloakSecurityComponents.class, QuicktestKeycloakSpringBootConfigResolver.class})
class AntigenTestContollerTest extends ServletKeycloakAuthUnitTestingSupport {

    @MockBean
    private AntigenTestService antigenTestService;

    @InjectMocks
    private AntigenTestController antigenTestController;

    @Test
    void getAntigenTests() throws Exception {
        LocalDateTime mockTime = LocalDateTime.now();
        when(antigenTestService.getLastUpdate()).thenReturn(mockTime);
        when(antigenTestService.getAntigenTests()).thenReturn(getMockAntigenTests());

        MvcResult mvcResult = mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                .get("/api/antigentests")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk()).andReturn();

        String dateString = mvcResult.getResponse().getHeader("Last-Modified");
        ZonedDateTime zdt = ZonedDateTime.parse(dateString, DateTimeFormatter.RFC_1123_DATE_TIME);

        assertEquals(antigenTestService.getLastUpdate().withNano(0),zdt.toLocalDateTime());

        String responseBody = mvcResult.getResponse().getContentAsString();
        Assertions.assertThat(responseBody)
                .isEqualTo("[{\"testBrandId\":\"TestId-1\",\"testBrandName\":\"TestName-1\"}"
                        + ",{\"testBrandId\":\"TestId-2\",\"testBrandName\":\"TestName-2\"},"
                        + "{\"testBrandId\":\"TestId-3\",\"testBrandName\":\"TestName-3\"}]");

        when(antigenTestService.getAntigenTests()).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                .get("/api/antigentests")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isBadRequest()).andReturn();

        when(antigenTestService.getLastUpdate()).thenThrow(new NullPointerException());
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(MockMvcRequestBuilders
                .get("/api/antigentests")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError()).andReturn();
    }

    @Test
    void updateAntigenTests() throws Exception {
        MockMultipartFile file
                = new MockMultipartFile(
                "file",
                "unittest.csv",
                "text/csv",
                getCscMock().getBytes()
        );
        mockMvc().with(authentication().authorities(ROLE_LAB)).perform(multipart(
                "/api/antigentests").file(file))
                .andExpect(status().isNoContent());
    }

    private String getCscMock() {
        return "AT810/21;ACCU-TELL SARS-CoV-2-Ag Cassette (Nasal Swab);Nein;AccuBioTech Co., Ltd.;Peking;CN;" +
                "Medical Device Safety Service GmbH;Hannover;DE;POC ;93,2;86,5 - 97,2;99,2;97,10 - 99,90\n" +
                "AT084/21;ACCU-TELL SARS-CoV-2-Ag Cassette;Nein;AccuBioTech Co., Ltd.;Peking;CN;Medical " +
                "Device Safety Service GmbH;Hannover;DE;POC ;95,7;90,30 - 98,60;99,2;97,60 - 99,80\n" +
                "AT579/21;ACCU-TELL SARS-CoV-2-Ag Cassette Saliva;Nein;AccuBioTech Co., Ltd.;Peking;CN;" +
                "Medical Device Safety Service GmbH;Hannover;DE;POC ;91,8;86,1 - 95,7;99,9;98,2 - 100\n";
    }

    private List<AntigenTest> getMockAntigenTests() {
        List<AntigenTest> testList = new ArrayList<>();
        testList.add(new AntigenTest("TestId-1", "TestName-1"));
        testList.add(new AntigenTest("TestId-2", "TestName-2"));
        testList.add(new AntigenTest("TestId-3", "TestName-3"));
        return testList;
    }
}

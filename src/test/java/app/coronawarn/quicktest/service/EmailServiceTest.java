package app.coronawarn.quicktest.service;

import app.coronawarn.quicktest.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailConfig emailConfig;

    @Test
    void sendMailToTestedPersonTest() {

    }

    @Test
    void sendMailToHealthDepartmentTest() {

    }

}

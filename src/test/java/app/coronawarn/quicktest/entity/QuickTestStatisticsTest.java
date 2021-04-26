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
        assertEquals(
                "QuickTestLog(id=0, pocId=testPocId, tenantId=testTenantId, " +
                        "createdAt=2021-04-08T08:11:12, positiveTestResult=true)",
                quickTestLog.toString());
    }
}

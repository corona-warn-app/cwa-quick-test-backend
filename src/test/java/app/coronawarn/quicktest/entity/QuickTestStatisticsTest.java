package app.coronawarn.quicktest.entity;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.domain.QuickTestStatistics;
import app.coronawarn.quicktest.model.Sex;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
public class QuickTestStatisticsTest {

    @Test
    void constructorTest() {
        QuickTestStatistics quickTestStatistics = new QuickTestStatistics("testPocId", "testTenantId");
        quickTestStatistics.setCreatedAt(LocalDate.now());
        quickTestStatistics.setPositiveTestCount(1);
        quickTestStatistics.setTotalTestCount(2);
        quickTestStatistics.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        assertEquals(
                "QuickTestStatistics(id=0, pocId=testPocId, tenantId=testTenantId, createdAt=2021-04-22, "
                        + "updatedAt=2021-04-08T08:11:11, totalTestCount=2, positiveTestCount=1)",
                quickTestStatistics.toString());
    }

    @Test
    void checkCountDefault() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = QuickTestStatistics.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        QuickTestStatistics quickTestStatistics = new QuickTestStatistics();
        method.invoke(quickTestStatistics);
        assertEquals(Short.parseShort("0"), quickTestStatistics.getPositiveTestCount());
        assertEquals(Short.parseShort("0"), quickTestStatistics.getTotalTestCount());
    }

    @Test
    void checkOnCreateDate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = QuickTestStatistics.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        QuickTestStatistics quickTestStatistics = new QuickTestStatistics();
        method.invoke(quickTestStatistics);
        LocalDate localDate = LocalDate.now();
        assertTrue(quickTestStatistics.getCreatedAt() instanceof LocalDate);
        log.info(localDate.toString());
        log.info(quickTestStatistics.getCreatedAt().toString());
        assertTrue(localDate.isEqual(quickTestStatistics.getCreatedAt()) ||
                localDate.isAfter(quickTestStatistics.getCreatedAt()));
    }

    @Test
    void checkOnUpdateDate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = QuickTestStatistics.class.getDeclaredMethod("onUpdate");
        method.setAccessible(true);
        QuickTestStatistics quickTestStatistics = new QuickTestStatistics();
        method.invoke(quickTestStatistics);
        LocalDateTime localDateTime = LocalDateTime.now();
        assertTrue(quickTestStatistics.getUpdatedAt() instanceof LocalDateTime);
        log.info(localDateTime.toString());
        log.info(quickTestStatistics.getUpdatedAt().toString());
        assertTrue(localDateTime.isEqual(quickTestStatistics.getUpdatedAt()) ||
                localDateTime.isAfter(quickTestStatistics.getUpdatedAt()));
    }

    @Test
    void checkVersionNotHasSetter() {
        try {
            Method method = QuickTestStatistics.class.getDeclaredMethod("setVersion");
            fail("setVersion method not allowed");
        } catch (NoSuchMethodException e) {
        }
    }
}
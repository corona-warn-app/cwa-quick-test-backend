package app.coronawarn.quicktest.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class QuickTestTest {

    @Test
    void constructorTest() {
        QuickTest quickTest = new QuickTest();
        quickTest.setZipCode("12345");
        quickTest.setTestResult(Short.parseShort("5"));
        quickTest.setHashedGuid("mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht");
        quickTest.setCity("oyvkpigcga");
        quickTest.setConfirmationCwa(Boolean.TRUE);
        quickTest.setShortHashedGuid("cjfybkfn");
        quickTest.setPhoneNumber("00491777777777777");
        quickTest.setEmail("test@test.test");
        quickTest.setTenantId("4711");
        quickTest.setPocId("4711-A");
        quickTest.setTestBrandId("AT116/21");
        quickTest.setTestBrandName("Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal)");
        quickTest.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        quickTest.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quickTest.setFirstName("Joe");
        quickTest.setLastName("Miller");
        quickTest.setStreet("Boe");
        quickTest.setHouseNumber("11");
        quickTest.setPrivacyAgreement(Boolean.FALSE);
        quickTest.setSex(Sex.DIVERSE);
        assertEquals(
            "QuickTest(hashedGuid=mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht, " +
                "shortHashedGuid=cjfybkfn, tenantId=4711, pocId=4711-A, createdAt=2021-04-08T08:11:11, " +
                "updatedAt=2021-04-08T08:11:12, version=null, confirmationCwa=true, testResult=5, " +
                "privacyAgreement=false, lastName=Miller, firstName=Joe, email=test@test.test, " +
                "phoneNumber=00491777777777777, sex=DIVERSE, street=Boe, houseNumber=11, zipCode=12345, " +
                "city=oyvkpigcga, testBrandId=AT116/21, " +
                "testBrandName=Panbio (TM) Covid-19 Ag Rapid Test Device (Nasal), birthday=null)",
            quickTest.toString());
    }

    @Test
    void checkTestResultDefault() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = QuickTest.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        QuickTest quickTest = new QuickTest();
        method.invoke(quickTest);
        assertEquals(Short.parseShort("5"), quickTest.getTestResult());
    }

    @Test
    void checkOnCreateDate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = QuickTest.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        QuickTest quickTest = new QuickTest();
        method.invoke(quickTest);
        LocalDateTime localDateTime = LocalDateTime.now();
        assertTrue(quickTest.getCreatedAt() instanceof LocalDateTime);
        log.info(localDateTime.toString());
        log.info(quickTest.getCreatedAt().toString());
        assertTrue(localDateTime.isEqual(quickTest.getCreatedAt()) || localDateTime.isAfter(quickTest.getCreatedAt()));
    }

    @Test
    void checkOnUpdateDate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = QuickTest.class.getDeclaredMethod("onUpdate");
        method.setAccessible(true);
        QuickTest quickTest = new QuickTest();
        method.invoke(quickTest);
        LocalDateTime localDateTime = LocalDateTime.now();
        assertTrue(quickTest.getUpdatedAt() instanceof LocalDateTime);
        log.info(localDateTime.toString());
        log.info(quickTest.getUpdatedAt().toString());
        assertTrue(localDateTime.isEqual(quickTest.getUpdatedAt()) || localDateTime.isAfter(quickTest.getUpdatedAt()));
    }

    @Test
    void checkVersionNotHasSetter() {
        try {
            Method method = QuickTest.class.getDeclaredMethod("setVersion");
           fail("setVersion method not allowed");
        } catch (NoSuchMethodException e) {
        }
    }
}

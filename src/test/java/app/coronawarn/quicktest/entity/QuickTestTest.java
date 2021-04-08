package app.coronawarn.quicktest.entity;

import app.coronawarn.quicktest.domain.Antigentest;
import app.coronawarn.quicktest.domain.QuickTest;
import app.coronawarn.quicktest.model.Sex;
import app.coronawarn.quicktest.utils.Generators;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
public class QuickTestTest {

    private static Validator validator;
    private final Generators generators = new Generators();

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

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
        quickTest.setTestSpotId("4711-A");
        quickTest.setAntigentest(new Antigentest());
        quickTest.setCreatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 11));
        quickTest.setUpdatedAt(LocalDateTime.of(2021, 4, 8, 8, 11, 12));
        quickTest.setFirstName("Joe");
        quickTest.setLastName("Miller");
        quickTest.setStreet("Boe");
        quickTest.setHouseNumber("11");
        quickTest.setInsuranceBillStatus(Boolean.FALSE);
        quickTest.setSex(Sex.DIVERSE);
        assertEquals("QuickTest(shortHashedGuid=cjfybkfn, hashedGuid=mkamhvdumyvhxeftazravmyrasozuloaghgluvbfjohpofogkylcnsybubamwnht, createdAt=2021-04-08T08:11:11, updatedAt=2021-04-08T08:11:12, confirmationCwa=true, tenantId=4711, testSpotId=4711-A, testResult=5, version=null, insuranceBillStatus=false, antigentest=Antigentest(id=null, manufacturerName=null, tradeName=null), lastName=Miller, firstName=Joe, email=test@test.test, phoneNumber=00491777777777777, sex=DIVERSE, street=Boe, houseNumber=11, zipCode=12345, city=oyvkpigcga)",quickTest.toString());
    }
    @Test
    void checkShortHashedGuidSize() {
        QuickTest quickTest = new QuickTest();
        quickTest.setShortHashedGuid(generators.generateString(13));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setShortHashedGuid(generators.generateString(8));
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkHashedGuidSize() {
        QuickTest quickTest = new QuickTest();
        quickTest.setHashedGuid(generators.generateString(65));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setHashedGuid(generators.generateString(64));
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkTenantIdSize() {
        QuickTest quickTest = new QuickTest();
        quickTest.setTenantId(generators.generateString(260));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setTenantId(generators.generateString(255));
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkTestSpotIdSize() {
        QuickTest quickTest = new QuickTest();
        quickTest.setTestSpotId(generators.generateString(260));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setTestSpotId(generators.generateString(255));
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkTestResultSize() {
        QuickTest quickTest = new QuickTest();
        quickTest.setTestResult(Short.parseShort("1"));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setTestResult(Short.parseShort("10"));
        violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setTestResult(Short.parseShort("6"));
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkTestResultDefault() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = QuickTest.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        QuickTest quickTest = new QuickTest();
        method.invoke(quickTest);
        assertEquals(Short.parseShort("5"),quickTest.getTestResult());
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
            assertTrue(false, "setVersion method not allowed");
        } catch (NoSuchMethodException e) {
            assertTrue(true);
        }
    }
    @Test
    void checkEmailValidator() {
        QuickTest quickTest = new QuickTest();
        quickTest.setEmail(generators.generateString(13));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setEmail("test@test.test");
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkPhoneNumberSize() {
        QuickTest quickTest = new QuickTest();
        quickTest.setPhoneNumber(generators.generateString(130));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setPhoneNumber(generators.generateString(20));
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkZipCodeSize() {
        QuickTest quickTest = new QuickTest();
        quickTest.setZipCode(generators.generateString(12));
        Set<ConstraintViolation<QuickTest>> violations = validator.validate(quickTest);
        assertFalse(violations.isEmpty());
        quickTest.setZipCode(generators.generateString(9));
        violations = validator.validate(quickTest);
        assertTrue(violations.isEmpty());
    }

}

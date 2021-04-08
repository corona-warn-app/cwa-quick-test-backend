package app.coronawarn.quicktest.entity;

import app.coronawarn.quicktest.domain.Antigentest;
import app.coronawarn.quicktest.utils.Generators;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
public class AntigentestTest {

    private static Validator validator;
    private Generators generators = new Generators();

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void constructorTest() {
        Antigentest antigentest = new Antigentest();
        antigentest.setId("12345");
        antigentest.setManufacturerName("Cool Compny");
        antigentest.setTradeName("Nice stuff 2.0");
        assertEquals("Antigentest(id=12345, manufacturerName=Cool Compny, tradeName=Nice stuff 2.0)",antigentest.toString());
    }
    @Test
    void checkIdSize() {
        Antigentest antigentest = new Antigentest();
        antigentest.setId(generators.generateString(13));
        Set<ConstraintViolation<Antigentest>> violations = validator.validate(antigentest);
        assertFalse(violations.isEmpty());
        antigentest.setId(generators.generateString(8));
        violations = validator.validate(antigentest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkManufacturerNameSize() {
        Antigentest antigentest = new Antigentest();
        antigentest.setManufacturerName(generators.generateString(270));
        Set<ConstraintViolation<Antigentest>> violations = validator.validate(antigentest);
        assertFalse(violations.isEmpty());
        antigentest.setManufacturerName(generators.generateString(250));
        violations = validator.validate(antigentest);
        assertTrue(violations.isEmpty());
    }
    @Test
    void checkTradeNameSize() {
        Antigentest antigentest = new Antigentest();
        antigentest.setTradeName(generators.generateString(260));
        Set<ConstraintViolation<Antigentest>> violations = validator.validate(antigentest);
        assertFalse(violations.isEmpty());
        antigentest.setTradeName(generators.generateString(50));
        violations = validator.validate(antigentest);
        assertTrue(violations.isEmpty());
    }
}

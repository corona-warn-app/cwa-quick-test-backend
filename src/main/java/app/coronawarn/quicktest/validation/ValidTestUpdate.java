package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.PersonalDataValidator;
import app.coronawarn.quicktest.validation.validators.TestUpdateValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TestUpdateValidator.class)
public @interface ValidTestUpdate {
    String message () default "Provide either testBrandId or dccTestManufacturerId";
    Class<?>[] groups () default {};
    Class<? extends Payload>[] payload () default {};
}

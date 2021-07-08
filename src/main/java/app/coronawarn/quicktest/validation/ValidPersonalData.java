package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.PersonalDataValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PersonalDataValidator.class)
public @interface ValidPersonalData {
    String message () default "missing standardized names for dcc";
    Class<?>[] groups () default {};
    Class<? extends Payload>[] payload () default {};
}


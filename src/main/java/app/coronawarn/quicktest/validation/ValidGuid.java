package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.GuidValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validation annotation for guid.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GuidValidator.class)
public @interface ValidGuid {

    /**
     * Get the error message for invalid GUID.
     *
     * @return clear text error message
     */
    String message() default "Invalid guid";

}

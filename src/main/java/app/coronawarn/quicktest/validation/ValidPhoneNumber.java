package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.PhoneNumberValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validation annotation for phone number.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface ValidPhoneNumber {

    /**
     * Get the error message for an invalid phone number.
     *
     * @return clear text error message
     */
    String message() default "Invalid phone number";

}

package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.ZipCodeValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ZipCodeValidator.class)
public @interface ValidZipCode {

    /**
     * Get the error message for an invalid short hash.
     * @return clear text error message
     */
    String message() default "Invalid zip code provided";
}

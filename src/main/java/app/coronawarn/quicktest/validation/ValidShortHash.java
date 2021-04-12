package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.ShortHashValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validation annotation for shortHash.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ShortHashValidator.class)
public @interface ValidShortHash {

    /**
     * Get the error message for an invalid short hash.
     *
     * @return clear text error message
     */
    String message() default "Invalid short hash";

    /**
     * groups.
     * @return groups
     */
    Class<?>[] groups() default {};

    /**
     * payload.
     * @return paylaod
     */
    Class<? extends Payload>[] payload() default {};
}

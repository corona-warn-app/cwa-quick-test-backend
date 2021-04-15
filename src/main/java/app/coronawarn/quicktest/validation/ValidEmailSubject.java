package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.EmailSubjectValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validation annotation for email text.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailSubjectValidator.class)
public @interface ValidEmailSubject {

    /**
     * Get the error message for an invalid email subject.
     *
     * @return clear text error message
     */
    String message() default "Invalid email text";

    /**
     * Default groups() method.
     */
    Class<?>[] groups() default {};

    /**
     * Default payload() method.
     */
    Class<? extends Payload>[] payload() default {};

}

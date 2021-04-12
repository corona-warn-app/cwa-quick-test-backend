package app.coronawarn.quicktest.validation;


import app.coronawarn.quicktest.validation.validators.NameValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validation annotation for name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Constraint(validatedBy = NameValidator.class)
public @interface ValidName {

    /**
     * Get the error message for an invalid name.
     *
     * @return clear text error message
     */
    String message() default "Invalid name";

    /**
     * Default groups() method.
     */
    Class<?>[] groups() default {};

    /**
     * Default payload() method.
     */
    Class<? extends Payload>[] payload() default {};

}

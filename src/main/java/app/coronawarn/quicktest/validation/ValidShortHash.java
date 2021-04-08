package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.GuidValidator;
import app.coronawarn.quicktest.validation.validators.ShortHashValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ShortHashValidator.class)
public @interface ValidShortHash {
        String message() default "{Invalid short hash}";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
}

package app.coronawarn.quicktest.validation;

import app.coronawarn.quicktest.validation.validators.GuidValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GuidValidator.class)
public @interface ValidGuid {
        String message() default "{Invalid guid}";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
}

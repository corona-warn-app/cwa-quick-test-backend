package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidZipCode;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class ZipCodeValidator implements ConstraintValidator<ValidZipCode, String> {
    private static final Pattern ZIPCODE_PATTERN =
            Pattern.compile("^([+]{1}[1-9]{1,2}|[0]{1}[1-9]{1})[0-9]{5,}$");

    @Override
    public boolean isValid(String zipCode, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isBlank(zipCode)) {
            return false;
        } else {
            return ZIPCODE_PATTERN.matcher(zipCode).matches();
        }
    }

}

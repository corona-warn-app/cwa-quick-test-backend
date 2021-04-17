package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidPhoneNumber;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    private static final Pattern PHONENUMBER_PATTERN =
            Pattern.compile("^([+]{1}[1-9]{1,2}|[0]{1}[1-9]{1})[0-9]{5,}$");

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(phone)) {
            return false;
        } else {
            return PHONENUMBER_PATTERN.matcher(phone).matches();
        }
    }

}

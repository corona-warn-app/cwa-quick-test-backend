package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidPhoneNumber;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    private static final String PHONENUMBER_PATTERN = "^[0-9]*$";

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(phone)) {
            return false;
        } else {
            return validatePhoneNumber(phone);
        }
    }

    private boolean validatePhoneNumber(String phone) {
        Pattern pattern = Pattern.compile(PHONENUMBER_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
}

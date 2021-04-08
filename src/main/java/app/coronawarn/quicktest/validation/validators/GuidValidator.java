package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidGuid;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuidValidator  implements ConstraintValidator<ValidGuid, String> {
    private static final String GUID_PATTERN = "^([A-Fa-f0-9]{2}){32}$";

    @Override
    public void initialize(ValidGuid constraintAnnotation) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(String guid, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(guid)) {
            return false;
        } else {
            return validateGuid(guid);
        }
    }

    private boolean validateGuid(String guid) {
        Pattern pattern = Pattern.compile(GUID_PATTERN);
        Matcher matcher = pattern.matcher(guid);
        return matcher.matches();
    }
}

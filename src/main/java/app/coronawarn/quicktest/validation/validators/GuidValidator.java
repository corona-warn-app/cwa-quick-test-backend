package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidGuid;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class GuidValidator implements ConstraintValidator<ValidGuid, String> {
    private static final Pattern GUID_PATTERN = Pattern.compile("^([A-Fa-f0-9]{64})$");

    @Override
    public boolean isValid(String guid, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(guid)) {
            return true;
        } else {
            return validateGuid(guid);
        }
    }

    private boolean validateGuid(String guid) {
        Matcher matcher = GUID_PATTERN.matcher(guid);
        return matcher.matches();
    }
}

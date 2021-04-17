package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidShortHash;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class ShortHashValidator implements ConstraintValidator<ValidShortHash, String> {
    private static final Pattern SHORTHASH_PATTERN = Pattern.compile("^[A-Fa-f0-9]{8}$");

    @Override
    public boolean isValid(String shortHash, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(shortHash)) {
            return false;
        } else {
            return validateGuid(shortHash);
        }
    }

    private boolean validateGuid(String shortHash) {
        Matcher matcher = SHORTHASH_PATTERN.matcher(shortHash);
        return matcher.matches();
    }
}

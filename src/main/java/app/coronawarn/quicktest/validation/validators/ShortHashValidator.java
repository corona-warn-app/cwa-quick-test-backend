package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidShortHash;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class ShortHashValidator implements ConstraintValidator<ValidShortHash, String> {
    private static final String SHORTHASH_PATTERN = "^[A-Fa-f0-9]{8}$";

    @Override
    public void initialize(ValidShortHash constraintAnnotation) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(String shortHash, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(shortHash)) {
            return false;
        } else {
            return validateGuid(shortHash);
        }
    }

    private boolean validateGuid(String shortHash) {
        Pattern pattern = Pattern.compile(SHORTHASH_PATTERN);
        Matcher matcher = pattern.matcher(shortHash);
        return matcher.matches();
    }
}

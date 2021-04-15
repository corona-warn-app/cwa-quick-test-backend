package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class NameValidator implements ConstraintValidator<ValidName, String> {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[^- '](?=(?![A-Z]?[A-Z]))(?=(?![a-z]+[A-Z]))"
            + "(?=(?!.*[A-Z][A-Z]))(?=(?!.*[- '][- '.]))(?=(?!.*[.][-'.]))[A-Za-z- '.]{2,79}$");

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(name)) {
            return false;
        } else {
            return validateName(name);
        }
    }

    private boolean validateName(String name) {
        Matcher matcher = NAME_PATTERN.matcher(name);
        return matcher.matches();
    }
}

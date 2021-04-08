package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidName;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameValidator implements ConstraintValidator<ValidName, String> {
    private static final String NAME_PATTERN = "^[^- '](?=(?![A-Z]?[A-Z]))(?=(?![a-z]+[A-Z]))(?=(?!.*[A-Z][A-Z]))(?=(?!.*[- '][- '.]))(?=(?!.*[.][-'.]))[A-Za-z- '.]{2,64}$";

    @Override
    public void initialize(ValidName constraintAnnotation) {
        // nothing to initialize
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(name)) {
            return false;
        } else {
            return validateName(name);
        }
    }

    private boolean validateName(String name) {
        Pattern pattern = Pattern.compile(NameValidator.NAME_PATTERN);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }
}

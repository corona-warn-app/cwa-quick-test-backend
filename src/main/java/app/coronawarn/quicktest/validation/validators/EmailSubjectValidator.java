package app.coronawarn.quicktest.validation.validators;


import app.coronawarn.quicktest.validation.ValidEmailSubject;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class EmailSubjectValidator implements ConstraintValidator<ValidEmailSubject, String> {
    private static final Pattern MAIL_TEXT_PATTERN =
            Pattern.compile("^[\\p{L}\\p{N}\"§$%&/()=?\\\\`´+*~#':.;,\\-_<>|@!°^ ]{0,1024}$");

    @Override
    public boolean isValid(String subject, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isBlank(subject)) {
            return false;
        } else {
            return MAIL_TEXT_PATTERN.matcher(subject).matches();
        }
    }

}

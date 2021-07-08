package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.model.QuickTestPersonalDataRequest;
import app.coronawarn.quicktest.validation.ValidPersonalData;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PersonalDataValidator implements ConstraintValidator<ValidPersonalData, QuickTestPersonalDataRequest> {

    @Override
    public boolean isValid(QuickTestPersonalDataRequest person, ConstraintValidatorContext context) {
        boolean valid;
        if (person.getDccConsent() != null) {
            valid = !person.getDccConsent()
                    || (person.getStandardisedFamilyName() != null || person.getStandardisedGivenName() != null);
        } else {
            valid = true;
        }
        return valid;
    }
}

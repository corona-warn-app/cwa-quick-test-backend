package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.model.quicktest.QuickTestUpdateRequest;
import app.coronawarn.quicktest.validation.ValidTestUpdate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TestUpdateValidator implements ConstraintValidator<ValidTestUpdate, QuickTestUpdateRequest> {

    @Override
    public boolean isValid(QuickTestUpdateRequest testUpdate, ConstraintValidatorContext context) {
        return testUpdate.getTestBrandId() != null || testUpdate.getDccTestManufacturerId() != null
            || testUpdate.getPcrTestName() != null;
    }
}

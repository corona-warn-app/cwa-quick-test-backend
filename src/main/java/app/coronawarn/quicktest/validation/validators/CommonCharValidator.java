/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 - 2023 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package app.coronawarn.quicktest.validation.validators;

import app.coronawarn.quicktest.validation.ValidCommonChar;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class CommonCharValidator implements ConstraintValidator<ValidCommonChar, String> {
    private static final Pattern CONTAINS_SPECIAL_CHARACTERS_EXCEPT_WHITESPACE =
            Pattern.compile("[\\^\\p{IsCommon}\\ ]");

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid;
        if (string == null) {
            isValid = true;
        } else if (StringUtils.isBlank(string)) {
            isValid = false;
        } else {
            isValid = !CONTAINS_SPECIAL_CHARACTERS_EXCEPT_WHITESPACE.matcher(string).matches();
        }
        return isValid;
    }

}

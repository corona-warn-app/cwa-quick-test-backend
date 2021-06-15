/*-
 * ---license-start
 * Corona-Warn-App / cwa-quick-test-backend
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
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

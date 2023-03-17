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


import app.coronawarn.quicktest.validation.ValidEmailSubject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
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

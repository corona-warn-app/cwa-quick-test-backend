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

package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.domain.TestType;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class TestTypeUtils {

    static Map<String, TestType> typeMap = Map.of("LP217198-3", TestType.RAT, "LP6464-4", TestType.NAAT);

    public static boolean isPcr(final String type) {
        return StringUtils.isNotBlank(type) && TestTypeUtils.typeMap.get(type) == TestType.NAAT;
    }

    public static boolean isRat(final String type) {
        return StringUtils.isBlank(type) || TestTypeUtils.typeMap.get(type) == TestType.RAT;
    }

    public static TestType getTestType(final String type) {
        return TestTypeUtils.typeMap.getOrDefault(type, TestType.INVALID);
    }

    public static String getDefaultType() {
        return "LP217198-3";
    }
}

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

package app.coronawarn.quicktest.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TestResult {
    PENDING_PCR("pending_pcr", (short) 10),
    NEGATIVE_PCR("negative_pcr", (short) 11),
    POSITIVE_PCR("positive_pcr", (short) 12),
    FAILED_PCR("failed_pcr", (short) 13),
    PENDING("pending", (short) 5),
    NEGATIVE("negative", (short) 6),
    POSITIVE("positive",  (short) 7),
    FAILED("failed",  (short) 8),
    REDEEMED("redeemed",  (short) 9);

    static final Map<String, TestResult> names = Arrays.stream(TestResult.values())
            .collect(Collectors.toMap(TestResult::getName, Function.identity()));
    static final Map<Short, TestResult> values = Arrays.stream(TestResult.values())
            .collect(Collectors.toMap(TestResult::getValue, Function.identity()));
    private final String name;
    private final short value;

    TestResult(final String name, final short value) {
        this.name = name;
        this.value = value;
    }

    public static TestResult fromName(final String name) {
        return names.get(name);
    }

    public static TestResult fromValue(final int value) {
        return values.get(value);
    }

    public String getName() {
        return name;
    }

    public short getValue() {
        return value;
    }
}

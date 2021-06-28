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

package app.coronawarn.quicktest.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Aggregation {

    NONE("none", 0),
    HOUR("hour", 3600),
    DAY("day", 86400);


    static final Map<String, Aggregation> names = Arrays.stream(Aggregation.values())
        .collect(Collectors.toMap(Aggregation::getName, Function.identity()));
    static final Map<Integer, Aggregation> values = Arrays.stream(Aggregation.values())
        .collect(Collectors.toMap(Aggregation::getValue, Function.identity()));
    private final String name;
    private final int value;

    Aggregation(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    public static Aggregation fromName(final String name) {
        return names.get(name);
    }

    public static Aggregation fromValue(final int value) {
        return values.get(value);
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}

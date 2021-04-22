package app.coronawarn.quicktest.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TestResult {
    PENDING("pending", (short) 5),
    NEGATIVE("negative", (short) 6),
    POSITIVE("positive",  (short) 7);

    TestResult(final String name, final short value) {
        this.name = name;
        this.value = value;
    }

    private final String name;
    private final short value;

    public String getName() {
        return name;
    }

    public short getValue() {
        return value;
    }

    static final Map<String, TestResult> names = Arrays.stream(TestResult.values())
            .collect(Collectors.toMap(TestResult::getName, Function.identity()));
    static final Map<Short, TestResult> values = Arrays.stream(TestResult.values())
            .collect(Collectors.toMap(TestResult::getValue, Function.identity()));

    public static TestResult fromName(final String name) {
        return names.get(name);
    }

    public static TestResult fromValue(final int value) {
        return values.get(value);
    }
}

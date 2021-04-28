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

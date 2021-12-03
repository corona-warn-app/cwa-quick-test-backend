package app.coronawarn.quicktest.utils;

import app.coronawarn.quicktest.domain.TestType;
import java.util.Map;

public class TestTypeUtils {

    static Map<String, TestType> typeMap = Map.of("LP217198-3", TestType.RAT, "LP6464-4", TestType.NAAT);

    public static boolean isPcr(final String type) {
        return TestTypeUtils.typeMap.get(type) == TestType.NAAT;
    }

    public static boolean isRat(final String type) {
        return TestTypeUtils.typeMap.get(type) == TestType.RAT;
    }

    public static TestType getTestType(final String type) {
        return TestTypeUtils.typeMap.getOrDefault(type, TestType.INVALID);
    }

    public static String getDefaultType() {
        return "LP217198-3";
    }
}

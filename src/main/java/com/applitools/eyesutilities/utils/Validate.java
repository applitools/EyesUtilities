package com.applitools.eyesutilities.utils;

import java.util.Arrays;
import java.util.Objects;

public class Validate {
    public static boolean isAllNull(Object... vars) {
        return Arrays.stream(vars).noneMatch(Objects::nonNull);
    }

    public static boolean isExactlyOneNotNull(Object... vars) {
        return Arrays.stream(vars).filter(Objects::nonNull).count() == 1;
    }
}

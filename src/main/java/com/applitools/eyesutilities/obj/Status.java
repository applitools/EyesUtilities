package com.applitools.eyesutilities.obj;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Status {
    Passed,
    Failed,
    Unresolved,
    Running,
    Aborted;

    //Case insensitive lookup, to keep the cli tolerant to 'failed'/'FAILED'/'Failed'
    public static Status parse(String value) {
        if (value != null) {
            String trimmed = value.trim();
            for (Status status : values())
                if (status.name().equalsIgnoreCase(trimmed)) return status;
        }
        throw new IllegalArgumentException(
                String.format("Unknown test status '%s', expected one of: %s", value, names()));
    }

    public static String names() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}

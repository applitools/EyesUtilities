package com.applitools.eyesutilities.utils;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateValidator implements IParameterValidator {
    public static final Pattern DATE_RANGE_REGEX_PATTERN = Pattern.compile("\\[\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}\\s*,\\s*\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}]");

    @Override
    public void validate(String name, String value) throws ParameterException {

        Matcher matcher = DATE_RANGE_REGEX_PATTERN.matcher(value);

        if (value == null || !matcher.matches()) {
            throw new RuntimeException("Date range must follow the pattern \"[month-day-year,month-day-year]\" e.g. [1-5-2022,12-31-2022]");
        }
    }
}


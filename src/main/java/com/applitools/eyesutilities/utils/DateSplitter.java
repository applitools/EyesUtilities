package com.applitools.eyesutilities.utils;

import com.beust.jcommander.converters.IParameterSplitter;

import java.util.Arrays;
import java.util.List;

public class DateSplitter implements IParameterSplitter {
    public List<String> split(String value) {
        return Arrays.asList(
                value.substring(1, value.length() - 1).split(",")
        );
    }
}

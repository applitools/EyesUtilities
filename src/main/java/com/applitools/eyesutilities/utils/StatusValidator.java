package com.applitools.eyesutilities.utils;

import com.applitools.eyesutilities.obj.Status;
import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.StringUtils;

//Validates a semicolon separated list of test statuses, the value arrives here before being split
public class StatusValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (StringUtils.isBlank(value))
            throw new ParameterException(
                    String.format("%s requires at least one test status, one of: %s", name, Status.names()));

        for (String status : new SemiColonSplitter().split(value)) {
            try {
                Status.parse(status);
            } catch (IllegalArgumentException e) {
                throw new ParameterException(String.format("%s - %s", name, e.getMessage()));
            }
        }
    }
}

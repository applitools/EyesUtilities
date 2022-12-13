package com.applitools.eyesutilities.commands;

import com.beust.jcommander.Parameter;

public abstract class BaselineCommand extends CommandBase {
    @Parameter(names = {"-k", "-key"}, description = "Applitools API key", required = true)
    protected String apiKey;

    protected String getFormattedServerUrl() {
        return server.replace("(http|https)://", "");
    }
}

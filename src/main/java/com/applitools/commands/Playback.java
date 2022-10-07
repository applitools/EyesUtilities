package com.applitools.commands;

import com.applitools.obj.serialized.TestInfo;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.IOException;

@Parameters(commandDescription = "Generate playback animation")
public class Playback extends ResultsAPIExtract {
    @Parameter(names = {"-i", "-interval"}, description = "Transition interval between the images")
    private int interval = 1000;

    @Parameter(names = {"-m", "-diffs"}, description = "Add diff marks")
    private boolean withDiffs = false;

    protected void runPerTest(TestInfo testInfo) throws IOException {
        testInfo.getPlaybackAnimation(interval, withDiffs);
    }
}

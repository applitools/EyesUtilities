package com.applitools.commands;

import com.applitools.obj.FailedStep;
import com.applitools.obj.serialized.TestInfo;
import com.beust.jcommander.Parameters;

import java.io.IOException;
import java.util.List;

@Parameters(commandDescription = "Download test diff images of a specific test")
public class DownloadDiffs extends ResultsAPIExtract {


    public DownloadDiffs() {
    }

    public DownloadDiffs(String url, String destination, String viewKey) {
        super(url, viewKey, destination);
    }

    protected void runPerTest(TestInfo ti) {
        List<FailedStep> steps = ti.getFailedSteps();
        steps.parallelStream().forEach(step -> {
            try {
                step.getDiff();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}

package com.applitools.eyesutilities.commands;

import com.applitools.eyesutilities.obj.Status;
import com.applitools.eyesutilities.obj.Step;
import com.applitools.eyesutilities.obj.serialized.TestInfo;
import com.applitools.eyesutilities.utils.SemiColonSplitter;
import com.applitools.eyesutilities.utils.StatusValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Parameters(commandDescription = "Download test steps images of a specific test or of a whole batch")
public class DownloadImages extends ResultsAPIExtract {
    private static final String STATUS_TOKEN = "{test_status}";
    private static final String STATUS_PATH_TMPL = "{workdir_root}/Artifacts/{batch_id}/{test_status}/{test_id}/";

    @Parameter(names = {"-b"}, description = "Save only baselines")
    private boolean onlyBaselines = false;
    @Parameter(names = {"-a"}, description = "Save only actuals")
    private boolean onlyActuals = false;

    @Parameter(
            names = {"-st", "--status"},
            description = "Download only the tests with the given statuses, SemiColon(;) separated. e.g. Failed;Unresolved",
            splitter = SemiColonSplitter.class,
            validateWith = StatusValidator.class
    )
    private List<String> statuses;

    @Parameter(names = {"-gs", "--groupByStatus"}, description = "Bundle the images into a folder per test status")
    private boolean groupByStatus = false;

    private Set<Status> requestedStatuses;

    public DownloadImages() {
    }

    public DownloadImages(String url, String destination, String viewKey, boolean onlyBaselines, boolean onlyActuals) {
        this(url, destination, viewKey, onlyBaselines, onlyActuals, null, false);
    }

    public DownloadImages(String url, String destination, String viewKey, boolean onlyBaselines, boolean onlyActuals,
                          List<String> statuses, boolean groupByStatus) {
        super(url, viewKey, destination);
        this.onlyBaselines = onlyBaselines;
        this.onlyActuals = onlyActuals;
        this.statuses = statuses;
        this.groupByStatus = groupByStatus;
    }

    @Override
    protected boolean accepts(TestInfo testInfo) {
        if (!isFiltering()) return true;
        return getRequestedStatuses().contains(testInfo.getEffectiveStatus());
    }

    @Override
    protected String resolveDestination() {
        String destination = super.resolveDestination();
        if (!isGrouping()) return destination;

        //A destination given by the user is used as is, it may or may not bundle by status
        if (!isDefaultDestination()) {
            if (!destination.contains(STATUS_TOKEN))
                System.out.printf("Note: the given destination has no %s parameter, " +
                        "the images won't be bundled by test status\n", STATUS_TOKEN);
            return destination;
        }

        return String.format("%sfile:%s", STATUS_PATH_TMPL, DEFAULT_FILE_TMPL);
    }

    protected void runPerTest(TestInfo testInfo) throws IOException, InterruptedException {
        List<Step> steps = testInfo.getSteps();
        for (Step step : steps) {
            if (onlyBaselines) saveBaselineImage(step);
            else if (onlyActuals) saveActualImage(step);
            else saveImages(step);
        }
    }

    private boolean isFiltering() {
        return statuses != null && !statuses.isEmpty();
    }

    //Filtering by status implies the results are of interest per status, hence bundled by it
    private boolean isGrouping() {
        return groupByStatus || isFiltering();
    }

    private Set<Status> getRequestedStatuses() {
        if (requestedStatuses == null) {
            requestedStatuses = EnumSet.noneOf(Status.class);
            for (String status : statuses) requestedStatuses.add(Status.parse(status));
        }
        return requestedStatuses;
    }

    private void saveImages(Step step) throws IOException, InterruptedException {
        saveBaselineImage(step);
        saveActualImage(step);
    }

    private void saveActualImage(Step step) throws IOException, InterruptedException {
        step.getActualImage();
    }

    private void saveBaselineImage(Step step) throws IOException, InterruptedException {
        step.getExpectedImage();
    }
}

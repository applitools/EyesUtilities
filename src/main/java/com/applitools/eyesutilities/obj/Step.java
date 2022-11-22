package com.applitools.eyesutilities.obj;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.applitools.eyesutilities.obj.contexts.ResultsAPIContext;
import com.applitools.eyesutilities.obj.serialized.ActualStepResult;
import com.applitools.eyesutilities.obj.serialized.ExpectedStepResult;
import com.applitools.eyesutilities.utils.ResourceDownloader;
import com.applitools.eyesutilities.utils.Utils;

public class Step {
    private static final String ARTIFACT_EXT = "png";
    private static final String BASELINE_ARTIFACT_TYPE = "baseline";
    private static final String ACTUAL_ARTIFACT_TYPE = "actual";
    protected final ResourceDownloader downloader;

    protected String testId;
    protected ExpectedStepResult expected;
    protected ActualStepResult actual;
    protected PathBuilder pathBuilder;
    private int index;
    private ResultsAPIContext context;

    public Step(ResultsAPIContext context, int i, ExpectedStepResult expected, ActualStepResult actual, String testId, PathBuilder pathBuilder) {
        this.context = context;
        this.index = i;
        this.expected = expected;
        this.actual = actual;
        this.testId = testId;
        this.pathBuilder = pathBuilder;
        this.downloader = new ResourceDownloader(context);
    }

    public String getExpectedImage() throws IOException, InterruptedException {
        if (expected == null) return "";
        return saveResourceById(expected.getImageId(), BASELINE_ARTIFACT_TYPE);
    }

    public String getActualImage() throws IOException, InterruptedException {
        if (actual == null) return "";
        return saveResourceById(actual.getImageId(), ACTUAL_ARTIFACT_TYPE);
    }

    public Result result() {
        if (expected == null) {
            return Result.New;
        } else if (actual == null) {
            return Result.Missing;
        } else if (actual.getIsMatching()) {
            return Result.Matched;
        } else {
            return Result.Mismatched;
        }
    }


    public URL getExpectedImageUrl() throws MalformedURLException {
        return context.getImageUrl(expected.getImageId());
    }

    public URL getActualImageUrl() throws MalformedURLException {
        return context.getImageUrl(actual.getImageId());
    }

    public URL getDiffImageUrl() throws MalformedURLException {
        return context.getDiffImageUrl(testId, index);
    }

    private String saveResourceById(String imageId, String artifact_type) throws IOException, InterruptedException {
        Map<String, String> params = getPathParams();
        params.put("artifact_type", artifact_type);
        File destination = pathBuilder.recreate(params).buildFile();
        pathBuilder.ensureTargetFolder();
        URL imageResource = context.getImageResource(imageId);
        BufferedImage image = downloader.getImage(imageResource);
        Utils.saveImage(image, destination);
        return destination.toString();
    }

    protected Map<String, String> getPathParams() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("step_index", String.valueOf(index));
        String tag = "" + UUID.randomUUID();
        if (actual != null && actual.getTag() != null) tag = actual.getTag();
        else if (expected != null && expected.getTag() != null) tag = expected.getTag();
        params.put("step_tag", Utils.toFolderName(tag));
        params.put("file_ext", ARTIFACT_EXT);
        return params;
    }

    public Integer getIndex() {
        return index;
    }


}

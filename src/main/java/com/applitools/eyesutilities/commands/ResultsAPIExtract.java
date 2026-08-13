package com.applitools.eyesutilities.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.applitools.eyesutilities.obj.PathBuilder;
import com.applitools.eyesutilities.obj.ResultUrl;
import com.applitools.eyesutilities.obj.contexts.ResultsAPIContext;
import com.applitools.eyesutilities.obj.serialized.BatchInfo;
import com.applitools.eyesutilities.obj.serialized.TestInfo;
import com.beust.jcommander.Parameter;
import org.apache.commons.lang3.StringUtils;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public abstract class ResultsAPIExtract extends ResultsAPI {
    private static final ObjectMapper mapper = new ObjectMapper();
    protected static final String DEFAULT_PATH_TMPL = "{workdir_root}/Artifacts/{batch_id}/{test_id}/";
    protected static final String DEFAULT_FILE_TMPL = "{step_index}_{step_tag}_{artifact_type}.{file_ext}";
    private static final String DEFAULT_DESTINATION = String.format("%sfile:%s", DEFAULT_PATH_TMPL, DEFAULT_FILE_TMPL);

    @Parameter(names = {"-d", "--destination"}, description = "Destination folder/template to save the results")
    protected String destination = DEFAULT_DESTINATION;

    public ResultsAPIExtract() {
    }

    public ResultsAPIExtract(String resUrl, String viewKey, String destination) {
        super(resUrl, viewKey);
        if (StringUtils.isNotBlank(destination))
            this.destination = destination;
    }

    public void run() throws Exception {
        ResultUrl resultUrl = getUrl();
        ResultsAPIContext ctx = new ResultsAPIContext(resultUrl, viewKey);
        String destTmpl = resolveDestination();
        PathBuilder builder = null;
        if (PathBuilder.isTemplateMatches(destTmpl))
            builder = new PathBuilder(destTmpl);
        else  //we assume destination was set only for path without file
            builder = new PathBuilder(destTmpl, DEFAULT_FILE_TMPL);
        builder = builder.recreate(getParams());
        if (resultUrl.getSessionId() != null) {
            //Just one test
            TestInfo testInfo = mapper.readValue(ctx.getTestApiUrl(), TestInfo.class);
            testInfo.setPathBuilder(builder);
            testInfo.setContext(ctx);
            if (!accepts(testInfo)) {
                System.out.printf("Skipped, test status is %s\n", testInfo.getEffectiveStatusName());
                return;
            }
            runPerTest(testInfo);
        } else if (resultUrl.getBatchId() != null) {
            //Url contains batch
            BatchInfo bi = BatchInfo.get(ctx, builder);
            TestInfo[] tests = bi.getTests();
            int i = 1;
            int total = bi.getTotalTests();
            int skipped = 0;
            System.out.println("Starting batch download...\n");
            for (TestInfo test : tests) {
                System.out.printf("[%s/%s] -->\n", i++, total);
                if (!accepts(test)) {
                    ++skipped;
                    System.out.printf("Skipped, test status is %s\n", test.getEffectiveStatusName());
                    continue;
                }
                runPerTest(test);
                System.out.print("Done\n");
            }
            System.out.printf("Batch download done, %s of %s tests processed\n\n", total - skipped, total);
        } else return;//TODO except
    }

    /**
     * Per test filter, applied on every test of the batch before it's artifacts are extracted.
     * Extending commands may narrow the set of tests to handle, by default all of them are handled.
     */
    protected boolean accepts(TestInfo testInfo) {
        return true;
    }

    /**
     * The destination template to build the artifacts paths with. Extending commands may swap the
     * default template with one of their own, a template provided by the user always wins.
     */
    protected String resolveDestination() {
        return destination;
    }

    protected boolean isDefaultDestination() {
        return DEFAULT_DESTINATION.equals(destination);
    }

    @Override
    protected HashMap<String, String> getParams() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("user_root", new File(System.getProperty("user.dir")).getAbsolutePath());
        params.put("workdir_root", new File("").getAbsolutePath());
        params.put("artifacts", "artifacts");
        return params;
    }

    protected abstract void runPerTest(TestInfo testInfo) throws IOException, InterruptedException;
}

package com.applitools.eyesutilities.obj.contexts;

import java.net.MalformedURLException;

public class BatchesAPIContext extends Context {
    private static final String DATE_RANGE_QUERY_URL_TEMPLATE = "https://%s/api/sessions/batches/query?limit=%s&startedBefore=%s&startedAfter=%s&apiKey=%s";
    private static final String TEST_RESULT_URL_TEMPLATE = "https://%s/app/test-results/%s?ApiKey=%s&format=json";
    private final String serverUrl;
    private final String startedBefore;
    private final String startedAfter;
    private final int limit;

    private static BatchesAPIContext context;

    public BatchesAPIContext(String serverUrl, String viewkey, String startedBefore, String startedAfter, int limit) {
        super(viewkey);
        this.serverUrl = serverUrl;
        this.startedAfter = startedAfter;
        this.startedBefore = startedBefore;
        this.limit = limit;
    }

    public static synchronized BatchesAPIContext Init(
            String serverUrl,
            String viewKey,
            String startedBefore,
            String startedAfter,
            int limit
    ) {
        if (context != null)
            throw new RuntimeException("Invalid call of Context.init(...)");
        context = new BatchesAPIContext(serverUrl, viewKey, startedBefore, startedAfter, limit);
        return context;
    }

    public String getBatchQueryUrl() throws MalformedURLException {
        return String.format(
            DATE_RANGE_QUERY_URL_TEMPLATE,
            serverUrl,
            getLimit(),
            getStartedBefore(),
            getStartedAfter(),
            getKey()
        );
    }

    public String getTestResultUrl(String batchId) {
        return String.format(
            TEST_RESULT_URL_TEMPLATE,
            getServerUrl(),
            batchId,
            getKey()
        );
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getStartedBefore() {
        return startedBefore;
    }

    public String getStartedAfter() {
        return startedAfter;
    }

    public int getLimit() {
        return limit;
    }
}

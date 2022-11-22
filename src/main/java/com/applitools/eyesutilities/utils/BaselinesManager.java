package com.applitools.eyesutilities.utils;

import com.applitools.eyesutilities.commands.MergeBranch;
import com.applitools.eyesutilities.obj.contexts.BranchesAPIContext;
import com.applitools.eyesutilities.obj.serialized.BaselineInfo;
import com.applitools.eyesutilities.obj.serialized.BranchInfo;
import com.applitools.eyesutilities.obj.serialized.MergeBranchResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BaselinesManager {
    private final BranchesAPIContext context;
    protected static ObjectMapper mapper = new ObjectMapper();

    public BaselinesManager(BranchesAPIContext context) {
        this.context = context;
    }

    public MergeBranchResponse mergeBranches(MergeBranch mergeBranch) throws InterruptedException, IOException {
        MergeBranchResponse mergeBranchResponse = null;
        String url = context.getMergedUrl();
        System.out.println("This is the url: " + url);
        String json = mapper.writeValueAsString(mergeBranch);
        System.out.println("This is the json: " + json);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        try (CloseableHttpResponse response = ApiCallHandler.sendPostRequest(url, entity, context)) {
            String resp = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println("Reading Value:");
            System.out.println(resp);
            mergeBranchResponse = mapper.readValue(resp, MergeBranchResponse.class);
        }
        return mergeBranchResponse;
    }

    public boolean deleteBranch(final String sourceBranch, boolean isDeleteBaselines) throws IOException, InterruptedException {
        BranchInfo bi = getBranchInfoByName(sourceBranch);
        if (bi == null) return false;
        bi.setIsDeleted(true);

        final String url = context.getDeleteUrl(bi.getId());
        final String json = mapper.writeValueAsString(bi);
        final StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);

        final List<String> baselineIds = getBaselinesByBranch(bi.getName())
                .stream()
                .map(BaselineInfo::getId)
                .collect(Collectors.toList());

        try (CloseableHttpResponse response = ApiCallHandler.sendPutRequest(url, entity, context)) {
            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:
                case HttpStatus.SC_ACCEPTED:
                    if (isDeleteBaselines && deleteBaselines(baselineIds)) {
                        System.out.println("Successfully deleted baselines");
                    }
                    return true;
                default:
                    throwUnexpectedResponse(response.getStatusLine());
            }
        }
        return false;
    }

    public List<BaselineInfo> getBaselinesByBranch(final String branchName) throws IOException, InterruptedException {
        List<BaselineInfo> baselines = new ArrayList<>();
        BranchInfo bi = getBranchInfoByName(branchName);
        if (bi == null) return baselines;
        String url = context.getBaselinesUrl();

        ObjectNode branch = mapper.createObjectNode();
        branch.put("limit", 1000);
        branch.put("branchId", bi.getId());
        
        //StringEntity stringEntity = new StringEntity(entity.toString(), ContentType.APPLICATION_JSON);
        StringEntity stringEntity = new StringEntity(mapper.writeValueAsString(branch), ContentType.APPLICATION_JSON);

        try (CloseableHttpResponse response = ApiCallHandler.sendPostRequest(url, stringEntity, context)) {
            String resp = EntityUtils.toString(response.getEntity(), "UTF-8");
            JsonNode jsonNode = mapper.readTree(resp);
            String baselinesAsString = jsonNode.get("baselines").toString();
            baselines = mapper.readValue(baselinesAsString, new TypeReference<List<BaselineInfo>>() {
            });
        }
        return baselines;
    }

    public List<BaselineInfo> filterBaselinesByAppName(List<BaselineInfo> baselines, String appName) {
        List<BaselineInfo> filteredBaselines = new ArrayList<>();
        baselines.forEach(baseline -> {
            if (baseline.getAppName().equalsIgnoreCase(appName)) {
                filteredBaselines.add(baseline);
            }
        });
        return filteredBaselines;
    }

    public boolean copyBaselines(String sourceBranch, String targetBranch, List<BaselineInfo> baselines) throws IOException, InterruptedException {
        if (baselines.size() == 0) {
            System.out.println("\nFound 0 baselines to copy.");
            return false;
        }
        String url = context.copyBaselinesUrl();
        StringEntity entity = generateCopyBaselinesEntity(sourceBranch, targetBranch, baselines);

        try (CloseableHttpResponse response = ApiCallHandler.sendPostRequest(url, entity, context)) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) return true;
            return false;
        }
    }

    private StringEntity generateCopyBaselinesEntity(String sourceBranch, String targetBranch, List<BaselineInfo> baselines) throws IOException {
        ObjectNode copyBaselines = mapper.createObjectNode();
        copyBaselines.put("sourceBranch", sourceBranch);
        copyBaselines.put("targetBranch", targetBranch);

        //ArrayNode baselinesIds = mapper.createArrayNode();
        ArrayNode baselinesIds = copyBaselines.putArray("baselineIds");

        baselines.forEach(baseline -> {
            baselinesIds.add(baseline.getId());
        });

        return new StringEntity(mapper.writeValueAsString(copyBaselines), ContentType.APPLICATION_JSON);
    }

    public boolean deleteBaselines(final List<String> baselineIds) throws IOException {
        System.out.println("Deleting the following baselines: " + baselineIds);
        if (baselineIds.isEmpty()) {
            System.out.println("Found 0 baselines to delete");
            return false;
        }
        // OkHttp client will let us make non-standard calls, like passing a body to delete.
        // This 100% needs to move to some sort of dependency injector.
        final OkHttpClient client = new OkHttpClient().newBuilder().build();
        String baselineBody = IntStream
                .range(0, baselineIds.size() - 1)
                .mapToObj(index -> "\"" + baselineIds.get(index) + "\",")
                .collect(
                        Collectors.joining(
                                "",
                                "{\"ids\":[",
                                "\"" + baselineIds.get(baselineIds.size() - 1) + "\"" + ("]}\n")
                        )
                );

        final RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                baselineBody
        );

        final Request request = new Request.Builder()
                .url(context.getDeleteBaselinesUrl())
                .method("DELETE", body)
                .addHeader("Content-Type", "application/json")
                .build();

        int resultStatusCode = client.newCall(request).execute().code();

        return resultStatusCode == HttpStatus.SC_ACCEPTED || resultStatusCode == HttpStatus.SC_NO_CONTENT;
    }

    protected static void throwUnexpectedResponse(StatusLine statusLine) {
        throw new RuntimeException(String.format("Unexpected response from request, code: %s, message: %s \n",
                statusLine.getStatusCode(),
                statusLine.getReasonPhrase()));
    }

    private BranchInfo getBranchInfoByName(String sourceBranch) throws IOException {
        BranchInfo[] branchInfos = mapper.readValue(new URL(context.getBaseUrl()), BranchInfo[].class);
        Optional<BranchInfo> found = Arrays.stream(branchInfos).filter(b -> b.getName().equals(sourceBranch)).findFirst();
        return found.orElse(null);
    }
}

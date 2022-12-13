package com.applitools.eyesutilities.utils;

import com.applitools.eyesutilities.obj.contexts.BatchesAPIContext;
import com.applitools.eyesutilities.obj.serialized.BatchInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BatchesManager {

    private final BatchesAPIContext context;
    private static final ObjectMapper mapper = new ObjectMapper();

    public BatchesManager(BatchesAPIContext context) {
        this.context = context;
    }

    private List<BatchInfo> getListOfBatchinfo() {
        List<BatchInfo> batchInfoList;
        try (CloseableHttpResponse response = ApiCallHandler.sendGetRequest(context.getBatchQueryUrl(), context)) {
            String resp = EntityUtils.toString(response.getEntity(), "UTF-8");
            JsonNode jsonNode = mapper.readTree(resp);
            batchInfoList = mapper.readValue(jsonNode.get("batches").toString(), new TypeReference<List<BatchInfo>>() {});
        } catch (IOException e) {
            throw new Error("IOException - failed to parse batch query URL, or failed to parse  " + e.getMessage());
        } catch (InterruptedException e) {
            throw new Error("InterruptedException: " + e.getMessage());
        }
        return batchInfoList;
    }

    public List<String> getTestResultsUrls() {
        List<String> resultUrls;
        List<BatchInfo> batchInfoList = this.getListOfBatchinfo();
        resultUrls = batchInfoList
                .parallelStream()
                .map(b -> context.getTestResultUrl(b.getId()))
                .collect(Collectors.toList());
        return resultUrls;
    }
}

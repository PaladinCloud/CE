package com.paladincloud.common.search;

import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.util.MapExtras;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

public class ElasticBatch implements AutoCloseable {

    public static int DEFAULT_BATCH_SIZE = 5000;
    private final List<BatchItem> batchItems = new ArrayList<>();
    private final ElasticSearch elasticSearch;
    private int batchSize = DEFAULT_BATCH_SIZE;

    @Inject
    public ElasticBatch(ElasticSearch elasticSearch) {
        this.elasticSearch = elasticSearch;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void add(BatchItem batchData) throws IOException {
        batchItems.add(batchData);
        checkForPush();
    }

    public void add(List<BatchItem> batchData) throws IOException {
        batchItems.addAll(batchData);
        checkForPush();
    }

    @Override
    public void close() throws Exception {
        push();
    }

    private void checkForPush() throws IOException {
        if (batchItems.size() >= batchSize) {
            push();
        }
    }

    private void push() throws IOException {
        if (batchItems.isEmpty()) {
            return;
        }

        // A bulk request comprises two-line pairs; the first line indicates the action & index
        // and the second line is the document
        var payload = new StringBuilder(2048);
        for (var batchData : batchItems) {
            payload.append(STR."""
                { "index": { "_index": "\{batchData.indexName}", "_id": "\{batchData.docId}" } }
                """.trim());
            payload.append("\n");
            payload.append(batchData.document);
            payload.append("\n");
        }

        var response = elasticSearch.invokeAndCheck(ElasticSearch.HttpMethod.POST, "/_bulk",
            payload.toString());
        if (response.hasWarnings()) {
            throw new JobException(STR."ElasticSearch bulk insert has warnings \{response.getWarnings()}");
        }
        batchItems.clear();
    }

    public static class BatchItem {

        public String indexName;
        public String docId;
        public String document;

        public BatchItem(String indexName, String docId, String document) {
            this.indexName = indexName;
            this.docId = docId;
            this.document = document;
        }

        public BatchItem(String indexName, String docId, Map<String, ?> document) {
            this(indexName, docId, MapExtras.toJsonString(document));
        }
    }
}

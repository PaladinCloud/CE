package com.paladincloud.common.search;

import com.paladincloud.common.search.ElasticSearch.HttpMethod;
import com.paladincloud.common.util.MapExtras;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElasticBatch implements AutoCloseable {

    public static int DEFAULT_BATCH_SIZE = 5000;
    private final int batchSize;
    private final List<BatchItem> batchItems = new ArrayList<>();

    public ElasticBatch() {
        this(DEFAULT_BATCH_SIZE);
    }

    public ElasticBatch(int batchSize) {
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

        ElasticSearch.invoke(HttpMethod.POST, "/_bulk", payload.toString());
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

package com.paladincloud.common.search;

import com.paladincloud.common.assets.AssetRepository;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.util.MapHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElasticBatch implements AssetRepository.Batch {

    private static final Logger LOGGER = LogManager.getLogger(ElasticBatch.class);
    private static final int DEFAULT_BATCH_SIZE = 5000;
    private final List<BatchItem> batchItems = new ArrayList<>();
    private final ElasticSearchHelper elasticSearch;
    private int batchSize = DEFAULT_BATCH_SIZE;

    @Inject
    public ElasticBatch(ElasticSearchHelper elasticSearch) {
        this.elasticSearch = elasticSearch;
    }

    public void add(BatchItem batchData) throws IOException {
        batchItems.add(batchData);
        checkForPush();
    }

    public void add(List<BatchItem> batchData) throws IOException {
        batchItems.addAll(batchData);
        checkForPush();
    }

    public void flush() throws IOException {
        push();
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
            payload.append(batchData.actionMetaData);
            payload.append("\n");
            payload.append(batchData.document);
            payload.append("\n");
        }

        var response = elasticSearch.invokeCheckAndConvert(ElasticBulkResponse.class,
            ElasticSearchHelper.HttpMethod.POST, "/_bulk", payload.toString());
        if (response.errors) {
            LOGGER.error("ElasticBulkResponse error: {} for {}", response.items, payload);
            throw new JobException("bulk insert failed");
        }
        batchItems.clear();
    }

    public static class BatchItem {

        public String actionMetaData;
        public String document;

        private BatchItem(String actionMetaData, String document) {
            this.actionMetaData = actionMetaData;
            this.document = document;
        }

        static public BatchItem documentEntry(String indexName, String docId,
            Map<String, ?> document) {
            return documentEntry(indexName, docId, MapHelper.toJsonString(document));
        }

        static public BatchItem documentEntry(String indexName, String docId, String document) {
            var actionInfo = STR."""
                { "index": { "_index": "\{indexName}", "_id": "\{docId}" } }
                """.trim();
            return new BatchItem(actionInfo, document);
        }

        static public BatchItem routingEntry(String indexName, String routingInfo,
            Map<String, ?> document) {
            return routingEntry(indexName, routingInfo, MapHelper.toJsonString(document));
        }

        static public BatchItem routingEntry(String indexName, String routingInfo,
            String document) {
            var actionInfo = STR."""
                { "index": { "_index": "\{indexName}", "routing": "\{routingInfo}" } }
                """.trim();
            return new BatchItem(actionInfo, document);
        }
    }
}

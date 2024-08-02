package com.paladincloud.common.assets;

import com.paladincloud.common.search.ElasticBatch.BatchItem;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AssetRepository {

    Map<String, AssetDTO> getLatestAssets(String indexName, List<String> filters);

    void deleteAssetsWithoutValue(String indexName, String docType, String fieldName,
        String fieldValue) throws IOException;

    Map<String, Object> getTypeRelations(String indexName, String parentType) throws IOException;

    void updateTypeRelations(String indexName, String parentType, Map<String, Object> relations)
        throws IOException;

    void processLoadErrors(String indexName, String type, String loadDate,
        Map<String, List<Map<String, Object>>> typeToError) throws IOException;

    Batch createBatch();

    void createIndex(String index) throws IOException;

    interface Batch extends AutoCloseable {

        void add(BatchItem batchData) throws IOException;

        void add(List<BatchItem> batchData) throws IOException;

        void flush() throws IOException;

        void close() throws Exception;
    }
}

package com.paladincloud.commons.search;

import static org.mockito.Mockito.when;

import com.paladincloud.common.search.ElasticBatch;
import com.paladincloud.common.search.ElasticBatch.BatchItem;
import com.paladincloud.common.search.ElasticSearch;
import com.paladincloud.common.search.ElasticSearch.HttpMethod;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.elasticsearch.client.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ElasticBatchTests {

    @Mock
    ElasticSearch elasticSearch;
    @Mock
    Response elasticResponse;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void batchFormsQueryCorrectly() throws Exception {
        var expectedPayload = """
            { "index": { "_index": "testing", "_id": "id-1" } }
            { "some": "data" }
            """;
        setSuccessfulResponse();
        when(elasticSearch.invoke(HttpMethod.POST, "/_bulk", expectedPayload)).thenReturn(
            elasticResponse);
        try (var batch = new ElasticBatch(elasticSearch)) {
            batch.add(new BatchItem("testing", "id-1", """
                { "some": "data" }
                """.trim()));
        }
    }

    private void setSuccessfulResponse() {
        when(elasticResponse.getStatusLine()).thenReturn(
            new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
    }
}

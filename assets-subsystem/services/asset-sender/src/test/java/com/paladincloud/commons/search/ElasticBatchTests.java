package com.paladincloud.commons.search;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.paladincloud.common.search.ElasticBatch;
import com.paladincloud.common.search.ElasticBatch.BatchItem;
import com.paladincloud.common.search.ElasticSearch;
import com.paladincloud.common.search.ElasticSearch.HttpMethod;
import java.io.IOException;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicStatusLine;
import org.elasticsearch.client.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ElasticBatchTests {

    @Spy
    ElasticSearch spyElasticSearch;
    @Mock
    ElasticSearch mockedElasticSearch;
    @Mock
    Response elasticResponse;

    @Test
    public void batchFormsQueryCorrectly() throws Exception {
        var expectedPayload = """
            { "index": { "_index": "testing", "_id": "id-1" } }
            { "some": "data" }
            """;
        when(mockedElasticSearch.invokeAndCheck(HttpMethod.POST, "/_bulk",
            expectedPayload)).thenReturn(elasticResponse);
        try (var batch = new ElasticBatch(mockedElasticSearch)) {
            batch.add(new BatchItem("testing", "id-1", """
                { "some": "data" }
                """.trim()));
        }
    }

    /**
     * This confirms that invokeAndCheck properly handles a bad request
     */
    @Test
    public void failedInsertThrowsException() throws Exception {
        var payload = """
            { "index": { "_index": "testing", "_id": "id-1" } }
            { "some": "data" }
            """;
        doReturn(setBadParameterResponse()).when(spyElasticSearch)
            .invoke(HttpMethod.POST, "/_bulk", payload);
        assertThrows(IOException.class, () -> {
            try (var batch = new ElasticBatch(spyElasticSearch)) {
                batch.add(new BatchItem("testing", "id-1", """
                    { "some": "data" }
                    """.trim()));
            }
        });
    }

    private Response setBadParameterResponse() {
        when(elasticResponse.getStatusLine()).thenReturn(
            new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 400, "Bad Request"));
        return elasticResponse;
    }
}

package com.paladincloud.commons.search;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.paladincloud.common.search.ElasticBatch;
import com.paladincloud.common.search.ElasticBatch.BatchItem;
import com.paladincloud.common.search.ElasticResponse;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticBatchTests {

    @Spy
    ElasticSearchHelper spyElasticSearch;
    @Mock
    ElasticSearchHelper mockedElasticSearch;

    // KVT TODO: I'm still in the process of working through this test.
    /*
    @Test
    void batchFormsQueryCorrectly() throws Exception {
        var expectedPayload = """
            { "index": { "_index": "testing", "_id": "id-1" } }
            { "some": "data" }
            """;

        var elasticResponse = new ElasticResponse(200, "OK", """
            { "errors": 0, "items": [] }
            """);
        // If this test fails due to 'Strict stubbing argument mismatch', it's due to
        // the generated payload differing from the expectedPayload, which needs to
        // be analyzed.
        when(mockedElasticSearch.invokeAndCheck(HttpMethod.POST, "/_bulk",
            expectedPayload)).thenReturn(elasticResponse);

        assertDoesNotThrow(() -> {
            try (var batch = new ElasticBatch(mockedElasticSearch)) {
                batch.add(BatchItem.documentEntry("testing", "id-1", """
                    { "some": "data" }
                    """.trim()));
            }
        });
    }
     */

    /**
     * This confirms that invokeAndCheck properly handles a bad request
     */
    @Test
    void failedInsertThrowsException() throws Exception {
        var payload = """
            { "index": { "_index": "testing", "_id": "id-1" } }
            { "some": "data" }
            """;
        doReturn(getBadParameterResponse()).when(spyElasticSearch)
            .invoke(HttpMethod.POST, "/_bulk", payload);

        assertThrows(IOException.class, () -> {
            try (var batch = new ElasticBatch(spyElasticSearch)) {
                batch.add(BatchItem.documentEntry("testing", "id-1", """
                    { "some": "data" }
                    """.trim()));
            }
        });
    }

    private ElasticResponse getBadParameterResponse() {
        return new ElasticResponse(400, "Bad Request", null);
    }
}

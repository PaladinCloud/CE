package com.paladincloud.common.search;

import java.io.IOException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

public class ElasticResponse {

    private final int statusCode;
    private final String statusPhrase;
    private final String body;

    public ElasticResponse(Response elasticResponse) throws IOException {
        this.statusCode = elasticResponse.getStatusLine().getStatusCode();
        this.statusPhrase = elasticResponse.getStatusLine().getReasonPhrase();
        var entity = elasticResponse.getEntity();
        if (entity != null && entity.getContentLength() > 0) {
            this.body = EntityUtils.toString(entity);
        } else {
            this.body = null;
        }
    }

    public ElasticResponse(int statusCode, String statusPhrase, String body) {
        this.statusCode = statusCode;
        this.statusPhrase = statusPhrase;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusPhrase() {
        return statusPhrase;
    }

    public String getBody() {
        return body;
    }
}

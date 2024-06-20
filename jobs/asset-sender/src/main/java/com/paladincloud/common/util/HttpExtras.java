package com.paladincloud.common.util;

import com.paladincloud.common.errors.JobException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.utils.CollectionUtils;

public class HttpExtras {

    private static final Logger LOGGER = LogManager.getLogger(HttpExtras.class);

    public static Map<String, String> getBasicHeaders(String base64Credentials) {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", ContentType.APPLICATION_JSON.toString());
        if (!StringUtils.isBlank(base64Credentials)) {
            headers.put("Authorization", STR."Basic \{base64Credentials}");
        }
        return headers;
    }

    public static String get(String uri, Map<String, String> headers) throws IOException {
        HttpGet request = new HttpGet(uri);
        if (CollectionUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        try (var client = getHttpClient()) {
            try (var httpResponse = client.execute(request)) {
                if (httpResponse.getStatusLine().getStatusCode() < 200
                    || httpResponse.getStatusLine().getStatusCode() >= 300) {
                    LOGGER.warn("http get failed with response status code: {} ({}); uri={}",
                        httpResponse.getStatusLine().getStatusCode(),
                        httpResponse.getStatusLine().getReasonPhrase(), uri);
                    throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(),
                        httpResponse.getStatusLine().getReasonPhrase());
                }
                return EntityUtils.toString(httpResponse.getEntity());
            }
        }
    }

    private static CloseableHttpClient getHttpClient() {
        try {
            return HttpClientBuilder.create().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(
                    new SSLContextBuilder().loadTrustMaterial(null, (_, _) -> true).build())
                .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new JobException("Security error getting getHttpClient", e);
        }
    }
}

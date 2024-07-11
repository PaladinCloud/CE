package com.paladincloud.common.util;

import com.paladincloud.common.errors.JobException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import software.amazon.awssdk.utils.CollectionUtils;

public class HttpHelper {

    public enum AuthorizationType {
        BEARER("Bearer"), BASIC("Basic");
        public final String name;
        AuthorizationType(String name) {
            this.name = name;
        }
    }

    private static final Logger LOGGER = LogManager.getLogger(HttpHelper.class);

    public static Map<String, String> getBasicHeaders(AuthorizationType authType, String authCredentials) {
        var headers = new HashMap<String, String>();
        headers.put("Content-Type", ContentType.APPLICATION_JSON.toString());
        if (!StringUtils.isBlank(authCredentials)) {
            headers.put("Authorization", STR."\{authType.name} \{authCredentials}");
        }
        return headers;
    }

    public static String post(String uri, String body, AuthorizationType authType, String token)
        throws IOException {
        try (var client = getHttpClient()) {
            var post = new HttpPost(uri);
            if (StringUtils.isEmpty(body)) {
                post.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
            } else {
                post.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
            }
            if (Strings.isNotEmpty(token)) {
                post.setHeader("Authorization", STR."\{authType.name} \{token}");
            }
            post.setEntity(new StringEntity(body));
            try (var httpResponse = client.execute(post)) {
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new JobException("Unauthorized");
                }
                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new JobException(STR."HTTP call failed: \{httpResponse.getStatusLine()
                        .getStatusCode()} \{httpResponse.getStatusLine().getReasonPhrase()}");
                }
                return EntityUtils.toString(httpResponse.getEntity());
            }
        }
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

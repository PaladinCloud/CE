/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.util;

import com.google.common.base.Strings;
import com.tmobile.cso.pacman.datashipper.exception.UnAuthorisedException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

public class HttpUtil {
    static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
    private static final String API_URI = System.getenv("PACMAN_API_URI");

    private HttpUtil() {
        throw new IllegalStateException("Constants is a utility class");
    }

    /**
     * Gets the data from http
     *
     * @param uri the uri
     * @return the string
     */
    public static String get(String uri, String bearerToken) throws Exception {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader("content-type", "application/json");
        httpGet.addHeader("cache-control", "no-cache");
        if (!Strings.isNullOrEmpty(bearerToken)) {
            httpGet.addHeader("Authorization", "Bearer " + bearerToken);
        }

        CloseableHttpClient httpClient = getHttpClient();
        if (httpClient != null) {
            HttpResponse httpResponse;
            try {

                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new UnAuthorisedException();
                }
                return EntityUtils.toString(httpResponse.getEntity());
            } catch (Exception e) {
                LOGGER.error("Error getting the data ", e);
                throw e;
            }
        }

        return "{}";
    }

    /**
     * Post.
     *
     * @param url         the url
     * @param requestBody the request body
     * @return the string
     * @throws Exception the exception
     */
    public static String post(String url, String requestBody, String token, String tokeType) throws Exception {
        try {
            CloseableHttpClient httpClient = getHttpClient();
            if (httpClient != null) {
                HttpPost httppost = new HttpPost(url);
                if (requestBody == null || requestBody.isEmpty()) {
                    httppost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
                } else {
                    httppost.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
                }

                if (!Strings.isNullOrEmpty(token)) {
                    httppost.addHeader("Authorization", tokeType + " " + token);
                }

                httppost.setEntity(new StringEntity(requestBody));
                HttpResponse httpresponse = httpClient.execute(httppost);
                if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    throw new UnAuthorisedException();
                }

                return EntityUtils.toString(httpresponse.getEntity());
            }
        } catch (Exception e) {
            LOGGER.error("Error getting the data ", e);
            throw e;
        }

        return null;
    }

    /**
     * Gets the http client.
     *
     * @return the http client
     */
    private static CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientBuilder.create().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                            return true;
                        }
                    }).build()).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            LOGGER.error("Error getting getHttpClient ", e);
        }

        return httpClient;
    }

    public static String httpGetMethodWithHeaders(String url, Map<String, Object> headers) throws Exception {
        String json = null;
        HttpGet get = new HttpGet(url);

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                get.setHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        try (CloseableHttpClient httpClient = getHttpClient()) {
            CloseableHttpResponse res = httpClient.execute(get);
            if (res.getStatusLine().getStatusCode() == 200) {
                json = EntityUtils.toString(res.getEntity());
            }
        }

        return json;
    }

    // Method to generate the base URL for the asset service
    public static String getAssetServiceBaseUrl() {
        return API_URI + "/asset/v1";
    }

    // Method to generate the base URL for the compliance service
    public static String getComplianceServiceBaseUrl() {
        return API_URI + "/compliance/v1";
    }

    // Method to generate the base URL for the vulnerability service
    public static String getVulnerabilityServiceBaseUrl() {
        return API_URI + "/vulnerability/v1";
    }
}

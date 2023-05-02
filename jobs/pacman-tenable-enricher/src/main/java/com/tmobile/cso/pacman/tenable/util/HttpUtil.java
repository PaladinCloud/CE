/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.tenable.util;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import com.tmobile.cso.pacman.tenable.Constants;
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

import com.google.common.base.Strings;
import com.tmobile.cso.pacman.tenable.exception.UnAuthorisedException;


/**
 * The Class HttpUtil.
 */
@SuppressWarnings("unchecked")
public class HttpUtil {

    /** The log. */
    static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
    
    /**
     * Instantiates a new http util.
     */
    private HttpUtil(){
    }
    
    /**
     * Gets the.
     *
     * @param uri            the uri
     * @param bearerToken the bearer token
     * @return the string
     * @throws Exception the exception
     */
    public static String get(String uri ,Map<String,String> input ) throws IOException, UnAuthorisedException {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader("content-type", "application/json");
        httpGet.addHeader("cache-control", "no-cache");
        if(!Strings.isNullOrEmpty(input.get(Constants.TENABLE_API_KEYS))){
            httpGet.addHeader(Constants.TENABLE_API_KEYS, input.get(Constants.TENABLE_API_KEYS));
        }
        if(!Strings.isNullOrEmpty(input.get(Constants.USER_AGENT))){
            httpGet.addHeader(Constants.USER_AGENT, input.get(Constants.USER_AGENT));
        }
        CloseableHttpClient httpClient = getHttpClient();
        if(httpClient!=null){
            HttpResponse httpResponse;
                httpResponse = httpClient.execute(httpGet);
                if( httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_UNAUTHORIZED){
                    throw new UnAuthorisedException();
                }
                if(null!= httpResponse && null!= httpResponse.getEntity())
                    return EntityUtils.toString(httpResponse.getEntity());
        }
        return "{}";
    }

    /**
     * Post.
     *
     * @param url            the url
     * @param requestBody            the request body
     * @param token the token
     * @param tokeType the toke type
     * @return the string
     * @throws Exception             the exception
     */
    public static String post(String url, String requestBody,Map<String,String>input ) throws IOException, UnAuthorisedException {

            CloseableHttpClient httpClient = getHttpClient();
            if(httpClient!=null){
                HttpPost httppost = new HttpPost(url);
                httppost.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
                if(!Strings.isNullOrEmpty(input.get(Constants.TENABLE_API_KEYS))){
                    httppost.addHeader(Constants.TENABLE_API_KEYS, input.get(Constants.TENABLE_API_KEYS));
                }
                if(!Strings.isNullOrEmpty(input.get(Constants.USER_AGENT))){
                    httppost.addHeader(Constants.USER_AGENT, input.get(Constants.USER_AGENT));
                }
                httppost.setEntity(new StringEntity(requestBody));
                HttpResponse httpresponse = httpClient.execute(httppost);
                if( httpresponse.getStatusLine().getStatusCode()==HttpStatus.SC_UNAUTHORIZED){
                    throw new UnAuthorisedException();
                }
                if(null!= httpresponse && null!=httpresponse.getEntity())
                    return EntityUtils.toString(httpresponse.getEntity());
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
            LOGGER.error("Error getting getHttpClient " , e);
        }
        return httpClient;
    }
    
    /**
     * Http get method with headers.
     *
     * @param url the url
     * @param headers the headers
     * @return the string
     * @throws Exception the exception
     */
    public static String httpGetMethodWithHeaders(String url,Map<String, Object> headers) throws Exception {
        String json = null;
        
        HttpGet get = new HttpGet(url);
        CloseableHttpClient httpClient = null;
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                get.setHeader(entry.getKey(), entry.getValue().toString());
            }
        }
        try {
            httpClient = getHttpClient();
            if(httpClient!=null){
                CloseableHttpResponse res = httpClient.execute(get);
                if (res.getStatusLine().getStatusCode() == 200) {
                    json = EntityUtils.toString(res.getEntity());
                }
            }

        } finally {
            if (httpClient != null) {
                httpClient.close();
            }
        }
        return json;
    }
}

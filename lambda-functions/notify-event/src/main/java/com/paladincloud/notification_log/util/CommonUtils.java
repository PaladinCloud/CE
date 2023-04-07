package com.paladincloud.notification_log.util;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.common.base.Strings;
import com.paladincloud.notification_log.common.Constants;
import com.paladincloud.notification_log.config.AuthManager;
import com.paladincloud.notification_log.config.ConfigManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;



public class CommonUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);
    /** The Constant APPLICATION_JSON. */
    private static final String APPLICATION_JSON = "application/json";

    /** The Constant CONTENT_TYPE. */
    private static final String CONTENT_TYPE = "Content-Type";

    private static final String HTTPS = "https";
    /** The Constant TLS. */
    private static final String TLS = "TLS";

    /** The prop. */
    static Properties prop;
    static {
        prop = new Properties();
        Hashtable<String, Object> configMap = ConfigManager.getConfigurationsMap();
        if (configMap != null && !configMap.isEmpty()) {
            prop.putAll(configMap);
        }else{
            LOGGER.info("unable to load configuration, exiting now");
            throw new RuntimeException("unable to load configuration");
        }
    }




    /**
     * Checks if is valid resource.
     *
     * @param esUrl the es url
     * @return boolean
     */
    public static boolean isValidResource(String esUrl) {
    	//LambdaLogger logger = context.getLogger();
    	LOGGER.debug("inside valideResource");
        HttpClient httpclient = HttpClientBuilder.create().build();
        HttpHead httpHead = new HttpHead(esUrl);
        HttpResponse response;
        try {
            response = httpclient.execute(httpHead);
            LOGGER.debug(response.getStatusLine().getReasonPhrase());
            return HttpStatus.SC_OK == response.getStatusLine().getStatusCode();
        } catch (ClientProtocolException clientProtocolException) {
        	//LOGGER.debug(clientProtocolException.getMessage());
            LOGGER.error("ClientProtocolException in getHttpHead:" + clientProtocolException);
        } catch (IOException ioException) {
        //	logger.log(ioException.getMessage());
            LOGGER.error("IOException in getHttpHead:" + ioException);
        }
        return false;
    }

    /**
     * Do http put.
     *
     * @param url the url
     * @param requestBody the request body
     * @return String
     * @throws Exception the exception
     */
    public static String doHttpPut(final String url, final String requestBody) throws Exception {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPut httpPut = new HttpPut(url);
            httpPut.setHeader(CONTENT_TYPE, APPLICATION_JSON);

            StringEntity jsonEntity = null;
            if (requestBody != null) {
                jsonEntity = new StringEntity(requestBody);
            }

            httpPut.setEntity(jsonEntity);
            HttpResponse httpresponse = client.execute(httpPut);
            if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(httpresponse.getEntity());
            } else {
                if(AuthManager.getToken()!=null){
                    String accessToken =  AuthManager.getToken();
                    if(!Strings.isNullOrEmpty(accessToken))
                    {
                        httpPut.setHeader(Constants.AUTH_HEADER, "Bearer " + accessToken);
                    }
                }
                httpPut.setEntity(jsonEntity);
                HttpResponse httpresponse1 = client.execute(httpPut);
                if (httpresponse1.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return EntityUtils.toString(httpresponse1.getEntity());
                } else {
                    throw new Exception(
                            "unable to execute put request caused by" + EntityUtils.toString(httpresponse1.getEntity()));
                }
            }
        } catch (ParseException parseException) {
          LOGGER.error("ParseException in getHttpPut :" + parseException.getMessage());
        } catch (IOException ioException) {
            LOGGER.error("IOException in getHttpPut :" + ioException.getMessage());
        }
        return null;
    }


    public static String getPropValue(final String keyname) {

        return prop.getProperty(keyname);
    }

    public static String postUrlEncoded(String url, String requestBody,String token,String tokeType) throws Exception {
        try {
            CloseableHttpClient httpClient = getHttpClient();
            if(httpClient!=null){
                HttpPost httppost = new HttpPost(url);
                httppost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());

                if(!Strings.isNullOrEmpty(token)){
                    httppost.addHeader("Authorization", tokeType+" "+token);
                }
                httppost.setEntity(new StringEntity(requestBody));
                HttpResponse httpresponse = httpClient.execute(httppost);
                if( httpresponse.getStatusLine().getStatusCode()==HttpStatus.SC_UNAUTHORIZED){
                    throw new IOException("non 200 code from rest call--->" + url);
                }
                return EntityUtils.toString(httpresponse.getEntity());
            }
        } catch (Exception e) {
            LOGGER.error("Error getting the data " , e);
            throw e;
        }
        return null;

    }

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
     * Do http post.
     *
     * @param url the url
     * @param requestBody the request body
     * @param headers the headers
     * @return the string
     */
    public static String doHttpPost(final String url, final String requestBody, final Map<String, String> headers) {
        CloseableHttpClient httpclient = null;
        if(Strings.isNullOrEmpty(url)){
            return "";
        }
        try {
            if (url.contains(HTTPS)) {

                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(createNoSSLContext());
                httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            } else {
                httpclient = HttpClients.custom().build();
            }

            HttpPost httppost = new HttpPost(url);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httppost.addHeader(entry.getKey(), entry.getValue());
            }
            httppost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            StringEntity jsonEntity = new StringEntity(requestBody);
            httppost.setEntity(jsonEntity);
            HttpResponse httpresponse = httpclient.execute(httppost);
            if(httpresponse.getStatusLine().getStatusCode()!=HttpStatus.SC_CREATED){
                throw new IOException("non 201 code from rest call--->" + url);
            }
            String responseStr = EntityUtils.toString(httpresponse.getEntity());
            return responseStr;
        } catch (org.apache.http.ParseException parseException) {
            LOGGER.error("ParseException : " + parseException.getMessage());
        } catch (IOException ioException) {
            try{
                if(AuthManager.getToken()!=null){
                    String accessToken =  AuthManager.getToken();
                    if(!Strings.isNullOrEmpty(accessToken))
                    {
                        headers.put(Constants.AUTH_HEADER, "Bearer " + accessToken);
                    }
                }
                if (url.contains(HTTPS)) {

                    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(createNoSSLContext());
                    httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
                } else {
                    httpclient = HttpClients.custom().build();
                }

                HttpPost httppost = new HttpPost(url);
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httppost.addHeader(entry.getKey(), entry.getValue());
                }
                httppost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
                StringEntity jsonEntity = new StringEntity(requestBody);
                httppost.setEntity(jsonEntity);
                HttpResponse httpresponse = httpclient.execute(httppost);
                if(httpresponse.getStatusLine().getStatusCode()!=HttpStatus.SC_OK){
                    throw new IOException("non 200 code from rest call--->" + url);
                }
                String responseStr = EntityUtils.toString(httpresponse.getEntity());
                return responseStr;
            }catch(Exception e){
                LOGGER.error("Exception in isResourceDateExpired: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Creates the no SSL context.
     *
     * @return the SSL context
     */
    public static SSLContext createNoSSLContext() {
        SSLContext ssl_ctx = null;
        try {
            ssl_ctx = SSLContext.getInstance(TLS);
        } catch (NoSuchAlgorithmException e) {
        }
        TrustManager[] trust_mgr = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String t) {
                /**
                 * no implementation required
                 * **/
            }

            public void checkServerTrusted(X509Certificate[] certs, String t) {
                /**
                 * no implementation required
                 * **/
            }
        } };
        try {
            if(null!=ssl_ctx){
                ssl_ctx.init(null, trust_mgr, new SecureRandom());
            }
        } catch (KeyManagementException e) {
        }
        return ssl_ctx;
    }
}

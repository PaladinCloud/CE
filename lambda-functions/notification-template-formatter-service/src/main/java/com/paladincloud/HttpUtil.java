package com.paladincloud;

import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Strings;

import static com.paladincloud.Constants.*;

/**
 * The Class HttpUtil.
 */
public class HttpUtil {

    private static final String API_READ_SCOPE = "API_OPERATION/READ";

    private HttpUtil() {
    }

    /**
     *
     * @param uri the uri
     * @return the string
     */
    public static String get(String uri, String bearerToken) throws Exception {
        String result="";
        HttpGet request = new HttpGet(uri);
        request.addHeader("cache-control", "no-cache");
        if (!Strings.isNullOrEmpty(bearerToken)) {
            request.addHeader("Authorization", "Bearer " + bearerToken);
        }

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse httpResponse = httpClient.execute(request)) {
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                // return it as a String
                result = EntityUtils.toString(entity);
                System.out.println(result);
            }
            if( httpResponse.getStatusLine().getStatusCode()==HttpStatus.SC_UNAUTHORIZED){
                    throw new Exception("User unauthorized !!");
                }

        } catch (Exception exception) {
            System.out.println("Error inside fetch notification settings " + exception.getMessage());
        }
        return result;
}

    /**
     * Post.
     *
     * @param url
     *            the url
     * @param requestBody
     *            the request body
     * @return the string
     * @throws Exception
     *             the exception
     */
    public static String post(String url, String requestBody,String token,String tokeType) throws Exception {
        try {
            CloseableHttpClient httpClient = getHttpClient();
            if(httpClient!=null){
                HttpPost httppost = new HttpPost(url);
                if(requestBody==null || requestBody.isEmpty()){
                    httppost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
                }else {
                    httppost.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
                }
                if(!Strings.isNullOrEmpty(token)){
                    httppost.addHeader("Authorization", tokeType+" "+token);
                }
                httppost.setEntity(new StringEntity(requestBody));
                HttpResponse httpresponse = httpClient.execute(httppost);
                if( httpresponse.getStatusLine().getStatusCode()==HttpStatus.SC_UNAUTHORIZED){
                    throw new Exception("User unauthorized !!");
                }
                return EntityUtils.toString(httpresponse.getEntity());
            }
        } catch (Exception e) {
            System.out.println("Error getting the data " +e.getMessage());
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
            System.out.println("Error getting getHttpClient "+e.getMessage());
        }
        return httpClient;
    }
    public static Map<String, Object> parseJson(String json) {
        try {
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            System.out.println("Error in parseJson "+e.getMessage());
        }
        return new HashMap<>();
    }

    public static String  getToken(String credentials) throws Exception {
        String authapiurl = System.getenv("AUTH_API_URL");
        String response = HttpUtil.post(authapiurl+"/oauth2/token?grant_type=client_credentials&scope="+API_READ_SCOPE,"",credentials,"Basic");
        Map<String,Object> authInfo = parseJson(response);
        Object token = authInfo.get("access_token");
        return token.toString();
    }

    public static Map<String,String> getConfigDetailsForChannels() throws Exception {

        Map<String,String> topicArnDetailsMap = new HashMap<>();
        String configServerUrl = System.getenv("CONFIG_SERVER_URL");
        String credentials = System.getenv("CONFIG_SERVER_CREDENTIALS");
        HttpGet request = new HttpGet(configServerUrl);
        String s1 = new String(Base64.getDecoder().decode(credentials));
        String[] credArray = (String[]) s1.split(":");

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(credArray[0], credArray[1])
        );

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
             CloseableHttpResponse httpResponse = httpClient.execute(request)) {
            System.out.println("statuscode is "+httpResponse.getStatusLine().getStatusCode());
            if(httpResponse.getStatusLine().getStatusCode()!=200){
                throw new Exception("Could not fetch topic arn for notification channels!");
            }
            else{
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    // return it as a String
                    String result = EntityUtils.toString(entity);
                    JsonParser jsonParser = new JsonParser();
                    JsonObject configJson = (JsonObject) jsonParser.parse(result);
                    JsonObject sourceJson = configJson.getAsJsonArray("propertySources").get(0).getAsJsonObject().getAsJsonObject("source");

                    topicArnDetailsMap.put("email", sourceJson.get(mailTopicArn).getAsString());
                    topicArnDetailsMap.put(apiauthinfo, sourceJson.get(apiauthinfo).getAsString());
                    System.out.println(result);
                }
            }
        }
        catch(Exception exception) {
            throw new Exception("Error inside getConfigDetailsForChannels " + exception.getMessage());
        }
        return topicArnDetailsMap;
    }

}


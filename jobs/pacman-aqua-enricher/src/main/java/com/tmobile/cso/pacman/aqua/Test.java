package com.tmobile.cso.pacman.aqua;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.tmobile.cso.pacman.aqua.exception.UnAuthorisedException;
import com.tmobile.cso.pacman.aqua.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

public class Test {

  public static void main(String[] args) {
    String tokenUri = "https://api.cloudsploit.com/v2/signin";
    URL url = null;
    try {
      url = new URL(tokenUri);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      JsonObject json = new JsonObject();
      json.addProperty("email", "mahidhar@paladincloud.io");
      json.addProperty("password", "5761M&Mlu");
      String input = json.toString();
      OutputStream os = conn.getOutputStream();
      os.write(input.getBytes());
      os.flush();
    /*  if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
        throw new RuntimeException("Failed : HTTP error code : "
            + conn.getResponseCode());
      }*/
      BufferedReader br = new BufferedReader(new InputStreamReader(
          (conn.getInputStream())));
      String output;
      System.out.println("Output from Server .... \n");
      while ((output = br.readLine()) != null) {
        System.out.println(output);
      }
      conn.disconnect();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      CloseableHttpClient httpClient = getHttpClient();
      if (httpClient != null) {
        HttpPost httppost = new HttpPost("https://api.cloudsploit.com/v2/signin");
        httppost.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        if (!Strings.isNullOrEmpty(null)) {
          httppost.addHeader("Authorization", "Bearer"+" "+"token");
        }
        JsonObject json = new JsonObject();
        json.addProperty("email", "mahidhar@paladincloud.io");
        json.addProperty("password", "5761M&Mlu");
        String input = json.toString();
        try {
          httppost.setEntity(new StringEntity(input));
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
          unsupportedEncodingException.printStackTrace();
        }
        HttpResponse httpresponse = null;
        try {
          httpresponse = httpClient.execute(httppost);
        } catch (IOException ioException) {
          ioException.printStackTrace();
        }
        if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
          try {
            throw new UnAuthorisedException();
          } catch (UnAuthorisedException unAuthorisedException) {
            unAuthorisedException.printStackTrace();
          }
        }
        try {
          String s = EntityUtils.toString(httpresponse.getEntity());
          System.out.println(Util.getJsonAttribute(s,"data"));


          System.out.println(s);
        } catch (IOException ioException) {
          ioException.printStackTrace();
        }
      }
    } catch (Exception e) {
      //LOGGER.error("Error getting the data " , e);
      throw e;
    }
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
      //LOGGER.error("Error getting getHttpClient " , e);
    }
    return httpClient;
  }
}
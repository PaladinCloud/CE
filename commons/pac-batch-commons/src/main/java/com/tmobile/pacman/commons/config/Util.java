
package com.tmobile.pacman.commons.config;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class Util {

	private static Logger log = LoggerFactory.getLogger(Util.class);


	private Util() {

	}

	public static String base64Decode(String encodedStr) {
		return new String(Base64.getDecoder().decode(encodedStr));
	}

	public static String base64Encode(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	public static Map<String, Object> getHeader(String base64Creds) {
		Map<String, Object> authToken = new HashMap<>();
		authToken.put("Content-Type", ContentType.APPLICATION_JSON.toString());
		authToken.put("Authorization", "Basic " + base64Creds);
		return authToken;
	}

	public static String httpGetMethodWithHeaders(String url, Map<String, Object> headers) throws Exception {
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
			CloseableHttpResponse res = httpClient.execute(get);
			if (res.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(res.getEntity());
			}
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}
		return json;
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
			log.error("Error in HttpUtil post ", e);
		}
		return httpClient;
	}

	public static String httpPostMethodWithHeaders(String url, Map<String, Object> headers) throws Exception {
		String json = null;

		HttpPost post = new HttpPost(url);
		CloseableHttpClient httpClient = null;
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, Object> entry : headers.entrySet()) {
				post.setHeader(entry.getKey(), entry.getValue().toString());
			}
		}
		try {
			httpClient = getHttpClient();
			CloseableHttpResponse res = httpClient.execute(post);
			if (res.getStatusLine().getStatusCode() == 200) {
				json = EntityUtils.toString(res.getEntity());
			}
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
		}
		return json;
	}

	public static String removeFirstSlash(String resourceId) {
		if (resourceId != null && resourceId.startsWith("/")) {
			return resourceId.substring(1);
		}
		return resourceId;

	}

	public static Map<String, String> tagsList(Map<String, Map<String, String>> tagMap, String resourceGroupName,
			Map<String, String> tags) {

		Map<String, String> tagsFinal = new HashMap<String, String>();
		if (tagMap.get(resourceGroupName.toLowerCase()) != null) {
			tagsFinal.putAll(tagMap.get(resourceGroupName.toLowerCase()));
			tagsFinal.putAll(tags);
			return tagsFinal;
		} else {
			return tags;
		}

	}
	
}

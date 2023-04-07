package com.paladincloud.notification_log;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paladincloud.notification_log.common.Constants;
import com.paladincloud.notification_log.config.AuthManager;

public class LogNotificationToOpenSearch implements RequestHandler<SNSEvent, Void> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LogNotificationToOpenSearch.class);
	private static final String ES_URI = "ES_URI";
	private static final String NOTIFICATION_INDEX = "notification";
	private static final String NOTIFICATION_TYPE = "notification";
	private static final String EVENT_ID = "eventId";
	private static final String MAPPING = "_mapping";
	/** The Constant INPUT_TYPE. */
	private static final String INPUT_TYPE = "input_type";

	private static final String APPLICATION_JSON = "application/json";

	/** The Constant CONTENT_TYPE. */
	private static final String CONTENT_TYPE = "Content-Type";

	private static final String HTTPS = "https";
	/** The Constant TLS. */
	private static final String TLS = "TLS";
	
	private static final String LOAD_DATE = "_loaddate";
	private static final String DOC_ID = "_docId";
	private static final String LATEST = "latest";
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:00Z";

	/** The Constant CREATE_MAPPING_REQUEST_BODY_TEMPLATE. */
	private static final String CREATE_MAPPING_REQUEST_BODY_TEMPLATE = "{\"properties\":{\"_entity\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"_entitytype\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"discoverydate\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"docType\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"eventCategory\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"eventCategoryName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"eventDescription\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"eventID\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"eventName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"eventSource\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"eventSourceName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"metaData\":{\"properties\":{\"issueID\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"link\":{\"properties\":{\"method\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"requestPayload\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"url\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}}},\"policyID\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"resourceId\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"status\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}}}}}";

	@Override
	public Void handleRequest(SNSEvent input, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log("Inside Notification Logger handlerRequest Method " + input);
		List<SNSRecord> records = input.getRecords();
		records.forEach(r -> {
			String message = r.getSNS().getMessage();
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				Map<String, Object> eventMap = objectMapper.readValue(message, new TypeReference<Map<String,Object>>(){});
				String loaddate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).format(new java.util.Date());
				UUID uuid = UUID.randomUUID();
				String strUUID = uuid.toString();
				eventMap.put(LOAD_DATE, loaddate);
				eventMap.put(DOC_ID, strUUID);
				eventMap.put(LATEST, true);
				message = objectMapper.writeValueAsString(eventMap);
				logger.log("message : " + message);
				boolean status = postJsonDocumentToIndexAndType(NOTIFICATION_INDEX, NOTIFICATION_TYPE, strUUID,
						message);
				logger.log("status : " + status);
			} catch (JsonProcessingException e) {
				logger.log("json parse exception :"+e);
			}

			

		});

		return null;

		// return status ? "success" : "failed";

	}

	/**
	 *
	 * @param indexName
	 * @param type
	 * @param postBody
	 * @param context
	 * @return
	 */
	private static Boolean postJsonDocumentToIndexAndType(String indexName, String type, String eventID,
			String postBody) {
		// LambdaLogger logger = context.getLogger();
		LOGGER.debug("inside postJosnDocvument");
		String url = System.getenv(ES_URI);
		LOGGER.debug("ES Url:" + url);
		if (url == null || url.isEmpty()) {
			LOGGER.debug("unable to find ES url");
			return false;
		}
		try {
			// logger.debug("validating index");
			if (!isValidIndex(url, indexName)) {
				createIndex(url, indexName);
			}
			// logger.debug("validating type");
			if (!isValidType(url, indexName, type)) {
				createMapping(url, indexName, type);
			}
			// logger.debug("builidng url");
			String esUrl = new StringBuilder(url).append("/").append(indexName).append("/").append(type).append("/")
					.append(eventID).toString();
			// logger.debug("uploading to es");
			doHttpPost(esUrl, postBody, new HashMap<>());
		} catch (Exception e) {
			// logger.debug("unable to publish notification log");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	/**
	 * Checks if is valid index.
	 *
	 * @param url   the url
	 * @param index the index
	 * @return true, if is valid index
	 */
	public static boolean isValidIndex(final String url, final String index) {
		String esUrl = new StringBuilder(url).append("/").append(index).toString();
		return isValidResource(esUrl);
	}

	/**
	 * Checks if is valid type.
	 *
	 * @param url   the url
	 * @param index the index
	 * @param type  the type
	 * @return true, if is valid type
	 */
	public static boolean isValidType(final String url, final String index, final String type) {
		String esUrl = new StringBuilder(url).append("/").append(index).append("/").append(MAPPING).append("/")
				.append(type).toString();
		return isValidResource(esUrl);
	}

	/**
	 * Creates the index.
	 *
	 * @param url       the url
	 * @param indexName the index name
	 * @throws Exception the exception
	 */
	public static void createIndex(String url, String indexName) throws Exception {
		String esUrl = new StringBuilder(url).append("/").append(indexName).toString();
		String payLoad = "{\"settings\": { \"number_of_shards\" : 1,\"number_of_replicas\" : 1 }}";
		doHttpPut(esUrl, payLoad);
	}

	/**
	 * Creates the mapping.
	 *
	 * @param esUrl the es url
	 * @param index the index
	 * @param type  the type
	 * @return the string
	 * @throws Exception the exception
	 */
	public static String createMapping(String esUrl, String index, String type) throws Exception {
		String url = new StringBuilder(esUrl).append("/").append(index).append("/").append(MAPPING).append("/")
				.append(type).toString();
		return doHttpPut(url, CREATE_MAPPING_REQUEST_BODY_TEMPLATE.replace(INPUT_TYPE, type));
	}

	/**
	 * Checks if is valid resource.
	 *
	 * @param esUrl the es url
	 * @return boolean
	 */
	public static boolean isValidResource(String esUrl) {
		// LambdaLogger logger = context.getLogger();
		LOGGER.debug("inside valideResource");
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpHead httpHead = new HttpHead(esUrl);
		HttpResponse response;
		try {
			response = httpclient.execute(httpHead);
			LOGGER.debug(response.getStatusLine().getReasonPhrase());
			return HttpStatus.SC_OK == response.getStatusLine().getStatusCode();
		} catch (ClientProtocolException clientProtocolException) {
			// LOGGER.debug(clientProtocolException.getMessage());
			LOGGER.error("ClientProtocolException in getHttpHead:" + clientProtocolException);
		} catch (IOException ioException) {
			// logger.log(ioException.getMessage());
			LOGGER.error("IOException in getHttpHead:" + ioException);
		}
		return false;
	}

	/**
	 * Do http put.
	 *
	 * @param url         the url
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
				if (AuthManager.getToken() != null) {
					String accessToken = AuthManager.getToken();
					if (!Strings.isNullOrEmpty(accessToken)) {
						httpPut.setHeader(Constants.AUTH_HEADER, "Bearer " + accessToken);
					}
				}
				httpPut.setEntity(jsonEntity);
				HttpResponse httpresponse1 = client.execute(httpPut);
				if (httpresponse1.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					return EntityUtils.toString(httpresponse1.getEntity());
				} else {
					throw new Exception("unable to execute put request caused by"
							+ EntityUtils.toString(httpresponse1.getEntity()));
				}
			}
		} catch (ParseException parseException) {
			LOGGER.error("ParseException in getHttpPut :" + parseException.getMessage());
		} catch (IOException ioException) {
			LOGGER.error("IOException in getHttpPut :" + ioException.getMessage());
		}
		return null;
	}

	/**
	 * Do http post.
	 *
	 * @param url         the url
	 * @param requestBody the request body
	 * @param headers     the headers
	 * @return the string
	 */
	public static String doHttpPost(final String url, final String requestBody, final Map<String, String> headers) {
		CloseableHttpClient httpclient = null;
		if (Strings.isNullOrEmpty(url)) {
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
			if (httpresponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
				throw new IOException("non 201 code from rest call--->" + url);
			}
			String responseStr = EntityUtils.toString(httpresponse.getEntity());
			return responseStr;
		} catch (org.apache.http.ParseException parseException) {
			LOGGER.error("ParseException : " + parseException.getMessage());
		} catch (IOException ioException) {
			try {
				if (AuthManager.getToken() != null) {
					String accessToken = AuthManager.getToken();
					if (!Strings.isNullOrEmpty(accessToken)) {
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
				if (httpresponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					throw new IOException("non 200 code from rest call--->" + url);
				}
				String responseStr = EntityUtils.toString(httpresponse.getEntity());
				return responseStr;
			} catch (Exception e) {
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
				 **/
			}

			public void checkServerTrusted(X509Certificate[] certs, String t) {
				/**
				 * no implementation required
				 **/
			}
		} };
		try {
			if (null != ssl_ctx) {
				ssl_ctx.init(null, trust_mgr, new SecureRandom());
			}
		} catch (KeyManagementException e) {
		}
		return ssl_ctx;
	}

	/*
	 * public static void main(String arg[]) { LogNotificationToOpenSearch
	 * logNotification = new LogNotificationToOpenSearch(); HashMap<String, Object>
	 * notificationObj = new HashMap<>();
	 * 
	 * notificationObj.put("eventID", "6fe019b67d7bab8ce13a4691234561377");
	 * notificationObj.put("eventName", "AWS EC2 AutoFix");
	 * notificationObj.put("eventCategory", "autofix");
	 * notificationObj.put("eventCategoryName", "AutoFix");
	 * notificationObj.put("eventSource", "PaladinCloud");
	 * notificationObj.put("eventSourceName", "PaladinCloud");
	 * notificationObj.put("eventDescription",
	 * "EC2 instances should not be directly accessible from internet");
	 * notificationObj.put("discoverydate", "2023-03-14 07:00:00+0000");
	 * notificationObj.put("docType", "notification");
	 * notificationObj.put("_entitytype", "notification");
	 * notificationObj.put("_entity", "true"); HashMap<String, Object> metaData =
	 * new HashMap<>(); HashMap<String, String> link = new HashMap<>();
	 * 
	 * link.put("url", "https://dev.paladincloud.io"); metaData.put("link", link);
	 * notificationObj.put("metaData", metaData);
	 * 
	 * 
	 * String status = logNotification.handleRequest(notificationObj, null);
	 * LOGGER.info( "status: {}",status); }
	 */
}

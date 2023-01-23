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
package com.tmobile.pacman.api.auth.services;

import com.amazonaws.auth.BasicSessionCredentials;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.auth.common.Constants;
import com.tmobile.pacman.api.auth.domain.UserClientCredentials;
import com.tmobile.pacman.api.auth.domain.UserLoginCredentials;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 	NidhishKrishnan
 * @purpose ApiService Service
 * @since	November 10, 2018
 * @version	1.0
**/
@Service
public class ApiService implements Constants {

	public static final String UTF_8 = "UTF-8";
	public static final String UNEXPECTED_ERROR_OCCURED = "Unexpected Error Occured!!!";
	public static final String SUCCESS = "success";
	public static final String MESSAGE = "message";
	public static final String EXCEPTION_IN_LOGIN_PROXY = "Exception in loginProxy: {}";
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
    private DataSource dataSource;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private CredentialProvider credentialProvider;

	@Autowired
	private CognitoUserService cognitoUserService;

	@Value("${pacman.api.oauth2.client-id}")
	private String oauth2ClientId;

	@Value("${pacman.api.oauth2.client-secret}")
	private String oauth2ClientSecret;

	@Value("${auth.active}")
	private String activeAuth;

	@Bean
	public JdbcTokenStore tokenStore() {
	    return new JdbcTokenStore(dataSource);
	}

	public Map<String, Object> loginProxy(final UserClientCredentials credentials) {
		String requestBodyUrl = StringUtils.EMPTY;
		try {
			requestBodyUrl = "grant_type=password&username=".concat(URLEncoder.encode(credentials.getUsername(), UTF_8)).concat("&password=".concat(URLEncoder.encode(credentials.getPassword(), UTF_8)));
			return generateAccessToken(requestBodyUrl, credentials.getClientId());
		} catch (UnsupportedEncodingException exception) {
			log.error(EXCEPTION_IN_LOGIN_PROXY, exception.getMessage());
			return response(false, UNEXPECTED_ERROR_OCCURED);
		}
	}

	public Map<String, Object> refreshToken(final String refreshToken) {
		String requestBodyUrl = StringUtils.EMPTY;
		String clientId=System.getenv("CLIENT_ID");
		try {
        	requestBodyUrl = "?grant_type=refresh_token&clientId=".concat(clientId).concat("&refresh_token=").concat(URLEncoder.encode(refreshToken, UTF_8));
			return generateAccessToken(requestBodyUrl, clientId);
		} catch (UnsupportedEncodingException exception) {
			log.error(EXCEPTION_IN_LOGIN_PROXY, exception.getMessage());
			return response(false, UNEXPECTED_ERROR_OCCURED);
		}
	}

	private Map<String, Object> generateAccessToken(String requestBodyUrl, String clientId) {
		Map<String, Object> accessTokenDetails = Maps.newHashMap();
		String clientSecret=System.getenv("CLIENT_SECRET");
		try {
			String url = System.getenv("AUTH_API_URL") + "/oauth2/token";
			Map<String, String> headers = Maps.newHashMap();
			headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
			String clientCredentials = null;
			try {
				String authString = clientId.concat(":").concat(clientSecret);
				byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
				clientCredentials = "Basic " + new String(authEncBytes);
			} catch (Exception exception) {
				log.error("Exception in getClientAuthorization: {}", exception.getMessage());
				return response(false, "Client Validation Failed!!!");
			}

			headers.put(HttpHeaders.AUTHORIZATION, clientCredentials);
			String accessToken = doHttpPost(url+requestBodyUrl,"", headers);
			accessTokenDetails = mapper.readValue(accessToken, new TypeReference<HashMap<String, Object>>() {});

			if (accessTokenDetails.containsKey("error_description")) {
				return response(false, accessTokenDetails.get("error_description").toString());
			} else {
				accessTokenDetails.put(SUCCESS, true);
				accessTokenDetails.put(MESSAGE, "Authentication Successfull");
			}
		} catch (Exception exception) {
			log.error(EXCEPTION_IN_LOGIN_PROXY,  exception.getMessage());
			return response(false, UNEXPECTED_ERROR_OCCURED);
		}
		return accessTokenDetails;
	}

	/**
	 *
	 * @param url
	 * @param requestBody
	 * @param headers
	 * @return
	 */
	public String doHttpPost(final String url, final String requestBody, final Map<String, String> headers)
	{
	     try {
			 HttpClient client = HttpClientBuilder.create().build();
		     HttpPost httppost = new HttpPost(url);
			 for (Map.Entry<String, String> entry : headers.entrySet()) {
				 httppost.addHeader(entry.getKey(), entry.getValue());
			 }
		     StringEntity jsonEntity = new StringEntity(requestBody);
		     httppost.setEntity(jsonEntity);
		     HttpResponse httpresponse = client.execute(httppost);
			 return EntityUtils.toString(httpresponse.getEntity());
		} catch (org.apache.http.ParseException parseException) {
			log.error("ParseException : {}",parseException.getMessage());
		} catch (IOException ioException) {
			log.error("IOException : {}",ioException.getMessage());
		}
		return null;
	}

	public String doHttpGet(String url, Map<String, String> headers) {
	  try {
			 HttpClient client = HttpClientBuilder.create().build();
		     HttpGet httpget = new HttpGet(url);
			 for (Map.Entry<String, String> entry : headers.entrySet()) {
				 httpget.addHeader(entry.getKey(), entry.getValue());
			 }
		     HttpResponse httpresponse = client.execute(httpget);
		     if(httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		    	 return EntityUtils.toString(httpresponse.getEntity());
		     }
		} catch (org.apache.http.ParseException parseException) {
			log.error("ParseException : {}",parseException.getMessage());
		} catch (IOException ioException) {
			log.error("IOException : {}",ioException.getMessage());
		}
		return null;
	}

	public Map<String, Object> response(final boolean success, final String message) {
		Map<String, Object> response = Maps.newHashMap();
		response.put(SUCCESS, success);
		response.put(MESSAGE, message);
		return response;
	}

	public Map<String, Object> login(UserLoginCredentials credentials) {

		String region = System.getenv("AWS_USERPOOL_REGION");
		String userPoolId = System.getenv("USERPOOL_ID");
		String clientId = System.getenv("CLIENT_ID");

		BasicSessionCredentials awsBaseCreds = credentialProvider.getBaseAccCredentials();


		Region reg = Region.of(region);
		CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
				.region(reg).credentialsProvider(StaticCredentialsProvider
						.create(AwsSessionCredentials
								.create(awsBaseCreds.getAWSAccessKeyId(), awsBaseCreds.getAWSSecretKey(), awsBaseCreds.getSessionToken()))).build();


		String clientSecret=System.getenv("CLIENT_SECRET");
		Map<String,Object> response=new HashMap<>();

		final Map<String, String> authParams = new HashMap<>();
		authParams.put("USERNAME", credentials.getUsername());
		authParams.put("PASSWORD", credentials.getPassword());
		authParams.put("SECRET_HASH", calculateSecretHash(clientId,clientSecret,credentials.getUsername()));


		AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
				.authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
				.clientId(clientId)
				.userPoolId(userPoolId).authParameters(authParams)
				.build();

		try {
			AdminInitiateAuthResponse result = identityProviderClient.adminInitiateAuth(authRequest);
			AuthenticationResultType authenticationResult = result.authenticationResult();
			if(authenticationResult!=null) {
				log.info("User Authenticated");
				String accessToken = authenticationResult.accessToken();
				response.put("id_token", authenticationResult.idToken());
				response.put("access_token", accessToken);
				response.put("refresh_token", authenticationResult.refreshToken());
				response.put(SUCCESS, true);
				response.put("token_type", authenticationResult.tokenType());
				response.put("expires_in", authenticationResult.expiresIn());
				Map<String, Object> userInfoMap = cognitoUserService.getUserInfo(identityProviderClient, userPoolId, credentials.getUsername());
				userInfoMap.put("userRoles", cognitoUserService.getUserRoles(identityProviderClient, userPoolId, credentials.getUsername()));
				response.put("userInfo", userInfoMap);
			}else{
				log.debug("User is not authentication. Challenge {}", result.challengeName() );
			}
		} catch (CognitoIdentityProviderException ex){
			response.put(SUCCESS,false);
			response.put(MESSAGE,ex.getMessage());
			response.put("statusCode",ex.statusCode());
			response.put("errorCode",ex.awsErrorDetails().errorCode());
			response.put("errorDetail",ex.awsErrorDetails().errorMessage());
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return response;
	}

	public void logout(Principal principal) {
		 JdbcTokenStore jdbcTokenStore = tokenStore();
		 OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) principal;
		 OAuth2AccessToken accessToken = jdbcTokenStore.getAccessToken(oAuth2Authentication);
		 jdbcTokenStore.removeAccessToken(accessToken.getValue());
		 jdbcTokenStore.removeRefreshToken(accessToken.getRefreshToken());
	}

	public String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
		final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

		SecretKeySpec signingKey = new SecretKeySpec(
				userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
				HMAC_SHA256_ALGORITHM);
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
			mac.init(signingKey);
			mac.update(userName.getBytes(StandardCharsets.UTF_8));
			byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
			return java.util.Base64.getEncoder().encodeToString(rawHmac);
		} catch (Exception e) {
			throw new RuntimeException("Error while calculating ");
		}
	}
}

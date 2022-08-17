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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.pacman.api.auth.domain.UserClientCredentials;
import com.tmobile.pacman.api.auth.domain.UserLoginCredentials;
import com.tmobile.pacman.api.auth.model.User;
import com.tmobile.pacman.api.auth.repository.AuthRepository;
import com.tmobile.pacman.api.auth.utils.AuthUtils;

/**
 * @author 	NidhishKrishnan
 * @purpose AzureAdAuthServiceImpl Service Implementation
 * @since	November 10, 2018
 * @version	1.0 
**/
@Service
@ConditionalOnProperty(
        name = "auth.active", 
        havingValue = "azuread")
public class AzureAdAuthServiceImpl implements AuthService {
	
	@Autowired
	private CustomUserService userService;
	
	@Autowired
	private ApiService apiService;
	
	@Value("${azure.issuer}")
	private String issuer;
	
	@Value("${azure.public-key}")
	private String publicKey;
	
	@Value("${azure.id-token.claims.user-id}")
	private String userId;
	
	@Value("${azure.id-token.claims.user-name}")
	private String userName;
	
	@Value("${azure.id-token.claims.first-name}")
	private String firstName;
	
	@Value("${azure.id-token.claims.last-name}")
	private String lastName;
	
	@Value("${azure.id-token.claims.email}")
	private String email;
	
	@Value("${pacman.api.oauth2.client-id}")
	private String oauth2ClientId;
	
	@Value("${auth.active}")
	private String activeAuth;
	
	@Autowired
    private AuthRepository authRepository;
	
	@Value("${azure.public-key.url}")
    private String publicKeyUrl;

	@Override
	public Map<String, Object> doLogin(UserLoginCredentials credentials) {
		return null;
	}

	@Override
	public void logout(Principal principal) {
	}

	@Override
	public Map<String, Object> loginProxy(UserClientCredentials credentials) {
		return null;
	}

	@Override
	public String getUserDefaultAssetGroup(String userId) {
		return null;
	}
	
	public Map<String, Object> authorizeUser(String idToken) {
		if(activeAuth.equalsIgnoreCase("azuread")) {
	        Map<String, Object> userDetails = validateIdTokenAndGetUserDetails(idToken);
	        if(!userDetails.isEmpty() && Boolean.parseBoolean(String.valueOf(userDetails.get("success")))) {
	        	if(oauth2ClientId.equals(userDetails.get("appId"))) {
	        		UserClientCredentials credentials = new UserClientCredentials();
	            	credentials.setClientId(userDetails.get("appId").toString());
	            	credentials.setUsername(userDetails.get("userId").toString());
	            	credentials.setPassword(StringUtils.EMPTY);
	            	return apiService.loginProxy(credentials);
		        }
	        } else {
	        	return apiService.response(false, String.valueOf(userDetails.get("message")));
	        }
		} else {
			return apiService.response(false, "This Api is disabled since azuread is not the active authentication mode");
		}
		return null;
	}
	
	public boolean isAccessTokenValid(final String accessToken) {
		String url = "https://graph.microsoft.com/v1.0/me";
		Map<String, String> headers = Maps.newHashMap();
		headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		String response = apiService.doHttpGet(url, headers);
		Map<String, Object> userDetails = AuthUtils.convertStringToMap(response);
		if(response != null || !userDetails.containsKey("error")) {
			User user = userService.findByUserId(String.valueOf(userDetails.get("userPrincipalName")).toLowerCase());
			if (user == null) {
				Map<String, String> newUser = Maps.newHashMap();
				newUser.put("userId", String.valueOf(userDetails.get("userPrincipalName")).toLowerCase());
				newUser.put("userName", String.valueOf(userDetails.get("displayName")));
				newUser.put("firstName", String.valueOf(userDetails.get("givenName")));
				newUser.put("lastName", String.valueOf(userDetails.get("surname")));
				newUser.put("email", String.valueOf(userDetails.get("mail")).toLowerCase());
				userService.registerNewUser(userDetails);
			}
			return true;
		}
		return false;
	}
    
	public Map<String, Object> validateIdTokenAndGetUserDetails(final String idToken) {   
        Map<String, Object> userDetails = Maps.newHashMap();
        try {
               DecodedJWT jwt = verifyToken(publicKey, idToken);
               userDetails= getUserDetails(jwt);
        } catch (JWTVerificationException exception){
               String newPublicKey = generateNewPublicKey(idToken);
               if (StringUtils.isNotBlank(newPublicKey)) {
                      try {
                            DecodedJWT jwt = verifyToken(newPublicKey, idToken);
                            updateNewPublicKey(newPublicKey);
                            userDetails= getUserDetails(jwt);
                      } catch (Exception excepion) {
                            excepion.printStackTrace();
                            userDetails.put("success", false);
                            userDetails.put("message", "Exception in Id Token verification");
                      }
               } else {
                      userDetails.put("success", false);
                      userDetails.put("message", "Exception in Id Token verification");
               }
        } catch (NoSuchAlgorithmException exception) {
               exception.printStackTrace();
               userDetails.put("success", false);
               userDetails.put("message", "Exception in Id Token verification");
        } catch (InvalidKeySpecException exception) {
               exception.printStackTrace();
               userDetails.put("success", false);
               userDetails.put("message", "Exception in Id Token verification");
        }
        return userDetails;
  }

  private DecodedJWT verifyToken(String publicKey, final String idToken)
               throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        byte[] decoded = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey generatePublic = (RSAPublicKey) kf.generatePublic(spec);
        Algorithm algorithm = Algorithm.RSA256(generatePublic, null);
        JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build();
        DecodedJWT jwt = verifier.verify(idToken);    
        return jwt;
  }

  private Map<String, Object> getUserDetails(DecodedJWT jwt) {
        Map<String, Object> userDetails = Maps.newHashMap();
        userDetails.put("userId", jwt.getClaim(userId).asString().toLowerCase());
        userDetails.put("userName", jwt.getClaim(userName).asString());
        userDetails.put("firstName", jwt.getClaim(firstName).asString());
        userDetails.put("lastName", jwt.getClaim(lastName).asString());
        userDetails.put("email", jwt.getClaim(email).asString().toLowerCase());
        User user = userService.findByUserId(String.valueOf(jwt.getClaim("unique_name").asString().toLowerCase()));
        if (user == null) {
            userService.registerNewUser(userDetails);
        }
        userDetails.put("appId", jwt.getClaim("aud").asString().toLowerCase());
        userDetails.put("success", true);
        return userDetails;
  }
  
  private String generateNewPublicKey(String idToken) {
        try {
               DecodedJWT decodedJWT = JWT.decode(idToken);
               String publicCertificate = StringUtils.EMPTY;
               String response = AuthUtils.httpGet(publicKeyUrl);
               if (!StringUtils.isEmpty(response)) {
                      Gson gson = new Gson();
                      JsonObject keys = gson.fromJson(response, JsonObject.class);
                      publicCertificate = getMatchingCertificate(decodedJWT, keys);
               }
               if (StringUtils.isNotBlank(publicCertificate)) {
                      String certificate = "-----BEGIN CERTIFICATE-----\n" + publicCertificate
                                   + "\n-----END CERTIFICATE-----";
                      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                      InputStream inputStream = new ByteArrayInputStream(certificate.getBytes(StandardCharsets.UTF_8));
                      X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                      return Base64.getEncoder().encodeToString(x509Certificate.getPublicKey().getEncoded());
               }
        } catch (Exception e) {
               e.printStackTrace();
        }
        return StringUtils.EMPTY;
  }

  private String getMatchingCertificate(DecodedJWT decodedJWT, JsonObject discoveryKeyObject) {
        JsonArray keys = discoveryKeyObject.getAsJsonArray("keys");
        for (JsonElement keyEle : keys) {
               JsonObject key = keyEle.getAsJsonObject();
               if (!key.get("kid").isJsonNull() && key.get("kid").getAsString().equals(decodedJWT.getKeyId())) {
                      if(!key.get("x5c").isJsonNull()) {
                            return key.get("x5c").getAsJsonArray().get(0).getAsString();
                      }
               }
        }
        return StringUtils.EMPTY;
  }
  
  public void updateNewPublicKey(String newPublicKey) {
        authRepository.updateAzurePublicKey(newPublicKey);
        System.setProperty("azure.public-key", newPublicKey);
  }

}

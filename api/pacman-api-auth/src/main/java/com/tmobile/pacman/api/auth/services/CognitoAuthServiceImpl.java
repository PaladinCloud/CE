package com.tmobile.pacman.api.auth.services;

import com.amazonaws.auth.BasicSessionCredentials;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.auth.domain.UserClientCredentials;
import com.tmobile.pacman.api.auth.domain.UserLoginCredentials;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GroupType;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;


@Service
@ConditionalOnProperty(
		name = "auth.active",
		havingValue = "cognito")
public class CognitoAuthServiceImpl implements AuthService {

	public static final String SUCCESS = "success";
	public static final String CUSTOM_DEFAULT_ASSET_GROUP = "custom:defaultAssetGroup";
	@Autowired
	private CustomUserService userService;

	@Autowired
	private CognitoUserService cognitoUserService;

	@Autowired
	private ApiService apiService;

	@Autowired
	private CredentialProvider credentialProvider;

	@Value("${auth.active}")
	private String activeAuth;

	private static final String DEFAULT_GROUP = "ROLE_USER";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Map<String, Object> doLogin(UserLoginCredentials credentials) {
		return new HashMap<>();
	}

	@Override
	public void logout(Principal principal) {
		//logout is not implemented, logout is done through the cognito logout url
	}

	@Override
	public Map<String, Object> loginProxy(UserClientCredentials credentials) {
		return new HashMap<>();
	}

	@Override
	public String getUserDefaultAssetGroup(String userId) {
		return null;
	}

	public Map<String, Object> authorizeUser(String idToken) {
		if(activeAuth.equalsIgnoreCase("cognito")) {
			Map<String, Object> userDetails = validateIdTokenAndGetUserDetails(idToken);
			if(!userDetails.isEmpty() && Boolean.parseBoolean(String.valueOf(userDetails.get(SUCCESS)))) {
				return userDetails;
			} else {
				return apiService.response(false, String.valueOf(userDetails.get("message")));
			}
		} else {
			return apiService.response(false, "This Api is disabled since cognito is not the active authentication mode");
		}
	}

	public Map<String, Object> validateIdTokenAndGetUserDetails(final String idToken) {
		Map<String, Object> userDetails = Maps.newHashMap();
		try {
			DecodedJWT jwt = JWT.decode(idToken);
			userDetails= getUserDetails(jwt);
		}catch (JWTDecodeException exception) {
			logger.error(exception.getMessage());
			userDetails.put(SUCCESS, false);
			userDetails.put("message", "Exception in Id Token verification");
		}
		return userDetails;
	}
	private Map<String, Object> getUserDetails(DecodedJWT jwt) {


		String region = System.getenv("AWS_USERPOOL_REGION");
		String userPoolId = System.getenv("USERPOOL_ID");

		BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();

		Region reg = Region.of(region);
		CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
				.region(reg).credentialsProvider(StaticCredentialsProvider
						.create(AwsSessionCredentials
								.create(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey(), credentials.getSessionToken()))).build();

		String userName = jwt.getClaim("cognito:username").asString();

		Map<String, Object> userDetails = new HashMap<>();
		Map<String, Object> userInfo = cognitoUserService.getUserInfo(identityProviderClient, userPoolId, userName);


		jwt.getClaims().entrySet().forEach(entry ->
			logger.info("Entry: {} Value: {}", entry.getKey(), entry.getValue().asString())
		);

		String defaultAgValue = (String) userInfo.get(CUSTOM_DEFAULT_ASSET_GROUP);
		if (StringUtils.isEmpty(defaultAgValue)) {
			logger.info("User default asset group is not set. Setting aws as default asset group for user :{} ", userName);
			userInfo.put("defaultAssetGroup", cognitoUserService.updateDefaultAssetGroup(identityProviderClient,
					userPoolId, userName, CUSTOM_DEFAULT_ASSET_GROUP, "aws"));
		} else {
			userInfo.put("defaultAssetGroup", defaultAgValue);
		}
		GroupType defaultRole = cognitoUserService.getGroup(identityProviderClient, userPoolId, DEFAULT_GROUP);
		if (defaultRole == null) {
			//create role
			GroupType defaultGroup = cognitoUserService.createRole(identityProviderClient, userPoolId, DEFAULT_GROUP);
			logger.info("Default group :{} created for userPoolId:{}", defaultGroup != null ? defaultGroup.groupName() : null, userPoolId);
		}
		cognitoUserService.assignRole(identityProviderClient, userPoolId, userName, DEFAULT_GROUP);
		userInfo.put("userRoles", cognitoUserService.getUserRoles(identityProviderClient, userPoolId, userName));
		userDetails.put("userInfo", userInfo);
		userDetails.put(SUCCESS, true);
		return userDetails;
	}

}

package com.tmobile.pacman.api.auth.services;

import com.amazonaws.auth.BasicSessionCredentials;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.collect.Maps;
import com.tmobile.pacman.api.auth.common.CredentialProvider;
import com.tmobile.pacman.api.auth.domain.UserClientCredentials;
import com.tmobile.pacman.api.auth.domain.UserLoginCredentials;
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

	@Autowired
	private CustomUserService userService;

	@Autowired
	private CognitoUserService cognitoUserService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private ApiService apiService;

	@Autowired
	private CredentialProvider credentialProvider;

	@Value("${auth.active}")
	private String activeAuth;

	private static final String DEFAULT_GROUP = "ROLE_USER";

	public static final String TENANT_ID = "tenantId";

	private final Logger logger = LoggerFactory.getLogger(getClass());

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
		if(activeAuth.equalsIgnoreCase("cognito")) {
			Map<String, Object> userDetails = validateIdTokenAndGetUserDetails(idToken);
			if(!userDetails.isEmpty() && Boolean.parseBoolean(String.valueOf(userDetails.get("success")))) {
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
			//check if token encryption is needed
			//DecodedJWT jwt = verifyToken(newPublicKey, idToken);
			userDetails= getUserDetails(jwt);
		}catch (JWTDecodeException exception) {
			exception.printStackTrace();
			userDetails.put("success", false);
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

		String userName = jwt.getClaim("cognito:username").asString().toLowerCase();
		Claim claimTenantId = jwt.getClaim("custom:tenantId");
		String tenantId = claimTenantId.asString() != null ? claimTenantId.asString().toLowerCase() : null;

		Map<String, Object> userDetails = new HashMap<>();
		Map<String, Object> userInfo = cognitoUserService.getUserInfo(identityProviderClient, userPoolId, userName);


		jwt.getClaims().entrySet().forEach(entry -> {
			logger.info("Entry: {} Value: {}", entry.getKey(), entry.getValue().asString());
		});

		if (StringUtils.isEmpty((String) userInfo.get("custom:defaultAssetGroup"))) {
			logger.info("User default asset group is not set. Setting aws as default asset group for user :{} ", userName);
			userInfo.put("defaultAssetGroup", cognitoUserService.updateDefaultAssetGroup(identityProviderClient,
					userPoolId, userName, "custom:defaultAssetGroup", "aws"));
		} else {
			userInfo.put("defaultAssetGroup", (String) userInfo.get("custom:defaultAssetGroup"));
		}
		if (tenantId == null && StringUtils.isEmpty((String) userInfo.get("custom:tenantId"))) {
			//TenantId value missing from the userAttributes- Adding tenantId
			logger.info("TenantId is not set. updating tenantId value");
			String tenantIdValue = tenantService.getAttributeByUserPool(userPoolId, TENANT_ID);
			userInfo.put("tenantId", cognitoUserService.updateDefaultAssetGroup(identityProviderClient,
					userPoolId, userName, "custom:tenantId", tenantIdValue));
		} else {
			userInfo.put("tenantId", tenantId);
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
		userDetails.put("success", true);
		return userDetails;
	}

}

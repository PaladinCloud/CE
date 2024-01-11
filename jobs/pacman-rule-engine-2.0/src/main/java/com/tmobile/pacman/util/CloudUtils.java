package com.tmobile.pacman.util;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.tmobile.pacman.commons.secrets.AwsSecretManagerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CloudUtils {
    static final Logger logger = LoggerFactory.getLogger(CloudUtils.class);

    public static BasicSessionCredentials getCredentials(String baseAccount, String roleName) {
        AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
        AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount, roleName)).withRoleSessionName("pic-base-ro");
        AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
        return new BasicSessionCredentials(
                assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                assumeResult.getCredentials().getSessionToken());

    }

    private static String getRoleArn(String accout, String role) {
        return "arn:aws:iam::" + accout + ":role/" + role;
    }

    public static Map<String, String> decodeCredetials(String tenant, BasicSessionCredentials credentials, String region, String credentialPrefix, String roleName) {
        Map<String, Map<String, String>> credsMap = new HashMap<>();
        logger.info("Inside decodeCredetials");
        logger.info("Credential prefix:{}", credentialPrefix);
        logger.info("roleName:{}", roleName);
        String secretId = credentialPrefix + "/" + roleName + "/azure/" + tenant;
        AwsSecretManagerUtil awsSecretManagerUtil = new AwsSecretManagerUtil();
        String secretData = awsSecretManagerUtil.fetchSecret(secretId, credentials, region);
        String azureCreds = getJson(secretData).get("secretdata");
        Map<String, String> credInfoMap = new HashMap<>();
        Arrays.asList(azureCreds.split(",")).stream().forEach(
                str -> credInfoMap.put(str.split(":")[0], str.split(":")[1]));

        return credInfoMap;
    }

    public static Map<String, String> getJson(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> dataMap = Collections.emptyMap();
        try {
            // convert JSON string to Map
            dataMap = objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            logger.error("Error in parsing json data", e);
        }
        return dataMap;
    }

    public static GoogleCredentials getGcpCredentials(String baseAccount, String baseRegion, String roleName, String credentialPrefix, String projectId) throws IOException {
        // Specify a credential file by providing a path to GoogleCredentials.Otherwise, credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
        logger.info("Inside getCredential method");
        String secretData = getGcpSecretData(projectId, baseAccount, roleName, credentialPrefix, baseRegion);
        try {
            GoogleCredentials gcpCredentials = GoogleCredentials.fromStream(new ByteArrayInputStream(secretData.getBytes()))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            logger.info("Credentials created: {}", gcpCredentials);
            return gcpCredentials;
        } catch (Exception exc) {
            logger.error("Error:: {}", exc);
        }
        return null;

    }

    private static String getGcpSecretData(String projectId, String baseAccount, String roleName, String credentialPrefix, String region) {
        BasicSessionCredentials credentials = getCredentials(baseAccount, roleName);
        AwsSecretManagerUtil awsSecretManagerUtil = new AwsSecretManagerUtil();
        String secretId = credentialPrefix + "/" + roleName + "/gcp/" + projectId;
        return awsSecretManagerUtil.fetchSecret(secretId, credentials, region);
    }
}

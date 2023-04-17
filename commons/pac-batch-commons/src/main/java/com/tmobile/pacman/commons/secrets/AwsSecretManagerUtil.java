package com.tmobile.pacman.commons.secrets;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AwsSecretManagerUtil {
    private static final Logger logger = LoggerFactory.getLogger(AwsSecretManagerUtil.class);

    public String fetchSecret(String secretId, BasicSessionCredentials credentials, String region) {
        logger.info("Fetching secret from region: {}, secretId:{} ",region,secretId);
        logger.debug("Fetching secret from region: {}, secretId:{} ",region,secretId);
      AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        GetSecretValueRequest getSecretRequest=new GetSecretValueRequest().withSecretId(secretId);
        GetSecretValueResult getResponse = secretClient.getSecretValue(getSecretRequest);
        return getResponse.getSecretString();

    }
}

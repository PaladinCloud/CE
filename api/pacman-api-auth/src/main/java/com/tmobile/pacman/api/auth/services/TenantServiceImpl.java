package com.tmobile.pacman.api.auth.services;

import com.amazonaws.auth.BasicSessionCredentials;
import com.tmobile.pacman.api.auth.common.CredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@Service
public class TenantServiceImpl implements TenantService{

    @Autowired
    private CredentialProvider credentialProvider;
    public static final String TENANT_CONFIG_TABLE="PaladinSaaS-TenantDetails";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public String getAttributeByUserPool(String userPoolId, String attribute){


        String region = System.getenv("AWS_USERPOOL_REGION");

        Region reg=Region.of(region);
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();

        DynamoDbClient client = DynamoDbClient.builder()
                .region(reg)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsSessionCredentials
                                .create(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey(), credentials.getSessionToken()))).build();

        try {
            ScanResponse scanResponse = client.scan(ScanRequest.builder().tableName(TENANT_CONFIG_TABLE)
                    .filterExpression("userPoolId = :userPoolId")
                    .expressionAttributeValues(
                            Collections.singletonMap(":userPoolId", AttributeValue.builder().s(userPoolId).build()))
                            .projectionExpression(attribute)
                    .build());
            Iterator<Map<String, AttributeValue>> iterator = scanResponse.items().iterator();
            if (iterator.hasNext()){
                Map<String, AttributeValue> item = iterator.next();
                AttributeValue tenantIdAttribute = item.get(attribute);
                return tenantIdAttribute.s();
            }
        } catch (DynamoDbException e) {
            logger.error("Error in getting tenantId",e.getMessage());
        }
        logger.info("TenantId not found from userPool:{}",userPoolId);
        return null;
    }

}

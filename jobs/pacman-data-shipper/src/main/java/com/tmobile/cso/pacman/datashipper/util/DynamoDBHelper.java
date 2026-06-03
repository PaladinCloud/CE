package com.tmobile.cso.pacman.datashipper.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class DynamoDBHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBHelper.class);

    private DynamoDBHelper() {
    }

    public static Map<String, String> get(String region, String tableName,
                                          String key, String keyValue, Map<String, String> fieldMap) {

        String account = System.getProperty("base.account");
        String role = System.getProperty("s3.role");

        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(key, new AttributeValue().withS(keyValue));

        GetItemRequest request = new GetItemRequest()
                .withTableName(tableName)
                .withKey(keyMap);

        LOGGER.info("Querying '{}' for item: {}", tableName, request);
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new CredentialProvider().getCredentials(account, role)))
                .withRegion(region)
                .build();
        try {
            Map<String, AttributeValue> item = client.getItem(request).getItem();
            return getFieldsFromRow(item, fieldMap);
        } finally {
            client.shutdown();
        }
    }

    private static Map<String, String> getFieldsFromRow(Map<String, AttributeValue> row,
                                                        Map<String, String> fieldMap) {
        Map<String, String> result = new HashMap<>();
        if (row == null) {
            return result;
        }
        for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
            String fieldName = entry.getKey();
            String propertyName = entry.getValue();
            AttributeValue value = row.get(fieldName);
            if (value != null && value.getS() != null) {
                result.put(propertyName, value.getS());
            } else {
                LOGGER.warn("Field '{}' not found or not a string in table row", fieldName);
            }
        }
        return result;
    }
}
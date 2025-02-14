package com.paladincloud.jobscheduler.service;

import java.util.*;
import java.util.stream.Collectors;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.directory.model.AuthenticationFailedException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.paladincloud.jobscheduler.auth.CredentialProvider;
import com.paladincloud.jobscheduler.model.DataCollectorSQSMessageBody;
import com.paladincloud.jobscheduler.repository.AccountsRepository;
import com.paladincloud.jobscheduler.repository.model.AccountDetails;
import com.paladincloud.jobscheduler.util.Constants;
import org.springframework.util.StringUtils;


@Service
public class DataCollectorSQSService {
	
	 private static final Logger logger = LoggerFactory.getLogger(DataCollectorSQSService.class);
	 
	 
	 @Autowired
	 AccountsRepository accountsRepository;
	
	@Autowired
    CredentialProvider credentialProvider;
	
	 @Value("${base.region}")
	 private String region;

    @Value("${base.account}")
    private String baseAccount;


   
    
    
    public void sendSQSMessage(String pluginType) {

    	DataCollectorSQSMessageBody sqsMessageObject = generateSQSMessage( pluginType);
    	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	try {
			String messageBody = ow.writeValueAsString(sqsMessageObject);
			
			sendMessage(messageBody, sqsMessageObject.getTenant_id());
    	} catch (JsonProcessingException e) {
			logger.error(" error in parsing cqpayload {}", e);
		}
    	
    }
    
    
    
    private DataCollectorSQSMessageBody generateSQSMessage(String pluginType) {
        String tenantID = System.getenv(Constants.TENANT_ID);
        String tenantName = System.getenv(Constants.TENANT_NAME);
    	List<AccountDetails> accountDetailsList =  accountsRepository.findByAccountStatusAndPlatform(Constants.PLUGIN_STATUS, pluginType);
    	List< String> accountList  = new ArrayList<>(accountDetailsList.size());
    	accountDetailsList.forEach(accountObj -> {
    		accountList.add(accountObj.getAccountId());
    	});
        String s3BucketName = null;
        Map<String, AttributeValue> tenantOutputDetails = fetchTenantOutputDetails(tenantID);
        if (tenantOutputDetails != null && tenantOutputDetails.containsKey(Constants.S3_BUCKET_NAME) && tenantOutputDetails.get(Constants.S3_BUCKET_NAME).getM().containsKey(Constants.ID)) {
            s3BucketName = tenantOutputDetails.get(Constants.S3_BUCKET_NAME).getM().get(Constants.ID).getS();
        }
        if(StringUtils.isEmpty(s3BucketName)) {
            throw new NoSuchElementException("unable to send SQS event since required param s3 bucket name is absent");
        }
        DataCollectorSQSMessageBody cQLambdaPayLoad = new DataCollectorSQSMessageBody(pluginType+Constants.JOB_NAME_SUFFIX,
    			accountList,tenantID,tenantName, pluginType, s3BucketName);
    	return cQLambdaPayLoad;
    }
    
    private void sendMessage(String messageBody, String tenantID) {
    	AmazonSQS sqs = generateSQSClient();
        String queueUrl = System.getenv(Constants.DATAMAPPER_SQS_QUEUE_URL); 
        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody)
                .withMessageGroupId(tenantID);

        try {
            // Send the message to the queue
            SendMessageResult result = sqs.sendMessage(request);
            logger.debug("Message sent with message ID: " + result.getMessageId());
        } catch (Exception e) {
        	logger.error("Error sending message: " + e.getMessage());
        }
    }
    
    
    private AmazonSQS generateSQSClient() {
    	
    	BasicSessionCredentials tempCredentials = null;
        try {
        	String role = System.getenv(Constants.SCHUDULER_ROLE);
            tempCredentials = credentialProvider.getCredentials(this.baseAccount, role);
        } catch (Exception e) {
            logger.error("{\"errcode\":\"NO_CRED\" , \"account\":\"" + this.baseAccount + "\", \"Message\":\"Error getting credentials for account " + this.baseAccount + "\" , \"cause\":\"" + e.getMessage() + "\"}");
        }
        if (tempCredentials == null) {
            throw new AuthenticationFailedException("can not get the temp credentials!!");
        }
    	return AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(tempCredentials))
                .withRegion(region) // Replace with your desired region
                .build();
    }

    public List<String> pluginsUsingVersion1AndConfigured(String[] pluginsUsingV1) {
        if (pluginsUsingV1.length == 0) {
            return new ArrayList<>();
        }
        List<String> pluginConfigList = Arrays.stream(pluginsUsingV1).map(plugin -> plugin + ".enabled")
                .collect(Collectors.toList());
        return accountsRepository.getEnabledAccountNameByConfig(pluginConfigList);
    }

    private Map<String, AttributeValue> fetchTenantOutputDetails(String tenantId) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":tenantId", new AttributeValue().withS(tenantId));
        QueryRequest queryRequest = new QueryRequest()
                .withTableName(Constants.TENANT_OUTPUT)
                .withKeyConditionExpression(Constants.TENANT_ID_DYNAMODB + " = :tenantId")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withProjectionExpression(Constants.S3_BUCKET_NAME);
        QueryResult queryResult = buildAmazonDynamoDB().query(queryRequest);
        List<Map<String, AttributeValue>> items = queryResult.getItems();
        if (items != null && items.size() > 0) {
            return items.get(0);
        }
        return null;
    }

    private AmazonDynamoDB buildAmazonDynamoDB() {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withRegion(Regions.fromName(System.getenv(Constants.REGION)))
                .build();
    }
}

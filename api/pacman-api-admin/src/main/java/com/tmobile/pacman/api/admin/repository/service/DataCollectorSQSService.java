package com.tmobile.pacman.api.admin.repository.service;

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
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.DataCollectorSQSMessageBody;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class DataCollectorSQSService {
	
	 private static final Logger logger = LoggerFactory.getLogger(DataCollectorSQSService.class);
	 
 
    @Autowired
    CredentialProvider credentialProvider;
 
    
    public void sendSQSMessage(String pluginType, String tenantID, List<AccountDetails> accountDetailsList) {
    	DataCollectorSQSMessageBody sqsMessageObject = generateSQSMessage( pluginType, tenantID, accountDetailsList);
    	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	try {
			String messageBody = ow.writeValueAsString(sqsMessageObject);
			
			sendMessage(messageBody, tenantID);
    	} catch (JsonProcessingException e) {
			logger.error(" error in parsing cq payload", e);
		}
    	
    }
    
    
    
    private DataCollectorSQSMessageBody generateSQSMessage(String pluginType, String tenantID, List<AccountDetails> accountDetailsList) {
    	
    	List< String> accountList  = new ArrayList<>(accountDetailsList.size());
    	accountDetailsList.forEach(accountObj -> {
    		accountList.add(accountObj.getAccountId());
    	});
    	DataCollectorSQSMessageBody cQLambdaPayLoad = new DataCollectorSQSMessageBody(pluginType+AdminConstants.JOB_NAME_SUFFIX, 
    			accountList,tenantID, pluginType);
    	return cQLambdaPayLoad;
    }
    
    private void sendMessage(String messageBody, String tenantID) {
    	AmazonSQS sqs = generateSQSClient();
        String queueUrl = System.getenv(AdminConstants.DATAMAPPER_SQS_QUEUE_URL); 
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
    	String baseAccount=System.getenv(AdminConstants.BASE_ACCOUNT);
    	String region = System.getenv(AdminConstants.BASE_REGION);
        try {
        	
        	
            tempCredentials = credentialProvider.getBaseAccCredentials();
        } catch (Exception e) {
            logger.error("{\"errcode\":\"NO_CRED\" , \"account\":\"" + baseAccount + "\", \"Message\":\"Error getting credentials for account " +baseAccount + "\" , \"cause\":\"" + e.getMessage() + "\"}");
        }
        if (tempCredentials == null) {
            throw new AuthenticationFailedException("can not get the temp credentials!!");
        }
    	return AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(tempCredentials))
                .withRegion(region) // Replace with your desired region
                .build();
    }

}

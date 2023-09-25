package com.paladincloud.jobscheduler.service;

import java.util.ArrayList;
import java.util.List;

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

    
    
    /* We need to remove this and this property as to come from DB with account Id*/
    public static final String TEMP_TENANTID = "saasdev_ro";

   
    
    
    public void sendSQSMessage(String pluginType) {
    	DataCollectorSQSMessageBody sqsMessageObject = generateSQSMessage( pluginType);
    	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    	try {
			String messageBody = ow.writeValueAsString(sqsMessageObject);
			sendMessage(messageBody);
    	} catch (JsonProcessingException e) {
			logger.error(" error in parsing cqpayload {}", e);
		}
    	
    }
    
    
    
    private DataCollectorSQSMessageBody generateSQSMessage(String pluginType) {
    	List<AccountDetails> accountDetailsList =  accountsRepository.findByAccountStatusAndPlatform(Constants.PLUGIN_STATUS, pluginType);
    	List< String> accountList  = new ArrayList<>(accountDetailsList.size());
    	accountDetailsList.forEach(accountObj -> {
    		accountList.add(accountObj.getAccountId());
    	});
    	DataCollectorSQSMessageBody cQLambdaPayLoad = new DataCollectorSQSMessageBody(pluginType+Constants.JOB_NAME_SUFFIX, 
    			accountList,TEMP_TENANTID, pluginType);
    	return cQLambdaPayLoad;
    }
    
    private void sendMessage(String messageBody) {
    	AmazonSQS sqs = generateSQSClient();
        String queueUrl = System.getenv(Constants.DATAMAPPER_SQS_QUEUE_URL); 
        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody)
                .withMessageGroupId(TEMP_TENANTID);

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

}

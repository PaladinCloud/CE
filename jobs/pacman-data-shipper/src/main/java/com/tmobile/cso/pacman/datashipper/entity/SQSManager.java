/*******************************************************************************
 * Copyright 2024 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.entity;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.datashipper.config.CredentialProvider;
import com.tmobile.cso.pacman.datashipper.dto.JobSchedulerSQSMessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SQSManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQSManager.class);

    private final ObjectMapper objectMapper;

    private SQSManager() {
        objectMapper = new ObjectMapper();
    }

    public static SQSManager getInstance() {
        return InstanceHolder.instance;
    }


    public void sendSQSMessage(String pluginName, String tenantId) {
        JobSchedulerSQSMessageBody sqsMessageBody = generateSQSMessage(pluginName, tenantId);
        try {
            String sqsMessage = objectMapper.writeValueAsString(sqsMessageBody);
            sendMessage(sqsMessage);
        } catch (Exception e) {
            LOGGER.error("Unable to send SQS message", e);
        }
    }

    private JobSchedulerSQSMessageBody generateSQSMessage(String pluginName, String tenantID) {
        return new JobSchedulerSQSMessageBody(pluginName + "-policy-job", tenantID, pluginName);
    }

    private void sendMessage(String messageBody) {
        String queueUrl = System.getenv("SHIPPER_SQS_QUEUE_URL");
        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody(messageBody)
                .withMessageGroupId(UUID.randomUUID().toString());
        try {
            AmazonSQS sqs = generateSQSClient();
            // Send the message to the queue
            if (sqs != null) {
                SendMessageResult result = sqs.sendMessage(request);
                LOGGER.debug("Message sent with message ID: " + result.getMessageId());
                return;
            }
            LOGGER.error("Unable to send message");
        } catch (Exception e) {
            LOGGER.error("Error sending message: " + e.getMessage());
        }
    }

    private AmazonSQS generateSQSClient() {
        BasicSessionCredentials tempCredentials = null;
        String baseAccount = System.getenv("BASE_AWS_ACCOUNT");
        String roleName = System.getenv("PALADINCLOUD_RO");
        String region = System.getenv("REGION");
        try {
            tempCredentials = new CredentialProvider().getCredentials(baseAccount, roleName);
        } catch (Exception e) {
            LOGGER.error("Error getting credentials for account {} , cause : {}", baseAccount, e.getMessage(), e);
        }
        if (tempCredentials == null) {
            LOGGER.error("can't get the temp credentials");
            return null;
        }
        return AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(tempCredentials))
                .withRegion(region)
                .build();
    }

    private static final class InstanceHolder {
        static final SQSManager instance = new SQSManager();
    }
}

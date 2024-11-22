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
package com.tmobile.pacman.commons.aws.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.pacman.commons.aws.CredentialProvider;
import com.tmobile.pacman.commons.dto.AssetStateStartEvent;
import com.tmobile.pacman.commons.dto.JobDoneMessage;
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

    public String sendMessage(AssetStateStartEvent message, String url) {
        return sendMessage(message.toCommandLine(), url);
    }

    public String sendSQSMessage(JobDoneMessage jobDoneMessage, String url) {
        try {
            String sqsMessage = objectMapper.writeValueAsString(jobDoneMessage);
            return sendMessage(sqsMessage, url);
        } catch (Exception e) {
            LOGGER.error("Unable to send SQS message", e);
        }
        return null;
    }
    private String sendMessage(String messageBody, String url) {

        SendMessageRequest request = new SendMessageRequest()
                .withQueueUrl(url)
                .withMessageBody(messageBody)
                .withMessageGroupId(UUID.randomUUID().toString());
        try {
            AmazonSQS sqs = generateSQSClient();
            // Send the message to the queue
            if (sqs != null) {
                SendMessageResult result = sqs.sendMessage(request);
                LOGGER.debug("Message sent with message ID: " + result.getMessageId());
                return result.getMessageId();
            }
            LOGGER.error("Unable to send message");
        } catch (Exception e) {
            LOGGER.error("Error sending message: " + e.getMessage());
        }
        return null;
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
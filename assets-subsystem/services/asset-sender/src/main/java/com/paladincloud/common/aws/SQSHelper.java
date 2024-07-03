package com.paladincloud.common.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.errors.JobException;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Singleton
public class SQSHelper {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    public SQSHelper() {
    }

    public <T> String sendMessage(String queueUrl, T message) {
        String sqsMessage;
        try {
            sqsMessage = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new JobException("Failed sending message: unable to transform message", e);
        }
        return internalSendMessage(queueUrl, sqsMessage);
    }

    private String internalSendMessage(String queueUrl, String message) {
        var request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(message)
            .build();

        try (var sqsClient = SqsClient.builder().build()) {
            var response = sqsClient.sendMessage(request);
            return response.messageId();
        }
        catch (SqsException e) {
            throw new JobException("Failed sending SQS message", e);
        }
    }
}

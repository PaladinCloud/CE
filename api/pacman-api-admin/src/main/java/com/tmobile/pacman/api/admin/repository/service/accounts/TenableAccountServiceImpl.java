/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.pacman.api.admin.repository.service.accounts;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.commons.Constants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TenableAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TenableAccountServiceImpl.class);

    private static final String TENABLE = "tenable";
    private static final String TENABLE_ENABLED = "tenable.enabled";

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Value("${tenable.user-agent}")
    private String tenableUserAgent;

    @Override
    public String serviceType() {
        return TENABLE;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        AccountValidationResponse validateResponse = validateRequestData(accountData);
        if (validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        validateAccountCredentials(accountData, validateResponse);
        return validateResponse;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Adding new Tenable account....");
        AccountValidationResponse validateResponse = validateRequestData(accountData);
        if (validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");
        String roleName = System.getenv(PALADINCLOUD_RO);

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();

        CreateSecretRequest createRequest = new CreateSecretRequest()
                .withName(secretManagerPrefix + "/" + roleName + "/tenable").withSecretString(getTenableSecret(accountData));

        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}", createResponse);
        String accountId = UUID.randomUUID().toString();
        createAccountInDb(accountId, "Tenable-Connector", TENABLE, accountData.getCreatedBy());

        updateConfigProperty(TENABLE_ENABLED, TRUE, JOB_SCHEDULER);
        validateResponse.setValidationStatus(SUCCESS);
        validateResponse.setAccountId(accountId);
        validateResponse.setAccountName("Tenable-Connector");
        validateResponse.setType(TENABLE);
        validateResponse.setMessage("Account added successfully. Account id: " + accountId);
        return validateResponse;
    }

    @Override
    public AccountValidationResponse deleteAccount(String accountId) {
        LOGGER.info("Inside deleteAccount method of TenableAccountServiceImpl. AccountId: {}", accountId);
        AccountValidationResponse response = new AccountValidationResponse();

        //find and delete cred file for account
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");
        String roleName = System.getenv(PALADINCLOUD_RO);

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        String secretId = secretManagerPrefix + "/" + roleName + "/tenable";
        DeleteSecretRequest deleteRequest = new DeleteSecretRequest().withSecretId(secretId).withForceDeleteWithoutRecovery(true);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ", deleteResponse);

        //delete entry from db
        deleteAccountFromDB(accountId);
        updateConfigProperty(TENABLE_ENABLED, FALSE, JOB_SCHEDULER);
        response.setType(TENABLE);
        response.setAccountId(accountId);
        response.setValidationStatus(SUCCESS);
        response.setMessage("Account deleted successfully");

        return response;
    }

    private AccountValidationResponse validateRequestData(CreateAccountRequest accountData) {
        AccountValidationResponse response = new AccountValidationResponse();
        List<String> missingParams = new ArrayList<>();
        String tenableAccessKey = accountData.getTenableAccessKey();
        String tenableSecretKey = accountData.getTenableSecretKey();

        if (StringUtils.isEmpty(tenableAccessKey)) {
            missingParams.add("Access Key");
        }

        if (StringUtils.isEmpty(tenableSecretKey)) {
            missingParams.add("Secret Key");
        }

        if (!missingParams.isEmpty()) {
            String errorMessage = MISSING_MANDATORY_PARAMETER + String.join(", ", missingParams);
            response.setMessage(errorMessage);
            response.setErrorDetails(errorMessage);
            response.setValidationStatus(FAILURE);
        } else {
            response.setValidationStatus(SUCCESS);
        }

        return response;
    }

    private void validateAccountCredentials(CreateAccountRequest accountData, AccountValidationResponse validateResponse) {
        // Requesting scan that doesn't exist to validate the account data.
        HttpGet request = new HttpGet(Constants.TENABLE_API_URL + "/scans/0");
        String apiKey = "accessKey=" + accountData.getTenableAccessKey() + ";secretKey=" + accountData.getTenableSecretKey() + ";";
        request.addHeader("X-ApiKeys", apiKey);
        request.addHeader("content-type", "application/json");
        request.addHeader("cache-control", "no-cache");
        request.addHeader("Accept", "application/json");
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 401) {
                // If the response is 401, then the account data is not valid.
                validateResponse.setValidationStatus(FAILURE);
                validateResponse.setMessage("Couldn't access Tenable API using provided keys.");
                validateResponse.setErrorDetails("Couldn't access Tenable API using provided keys.");
            } else {
                validateResponse.setValidationStatus(SUCCESS);
                validateResponse.setMessage("Tenable validation successful");
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to validate the tenable account ", e);
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Tenable validation Failed");
        } catch (IOException e) {
            LOGGER.error("Failed to validate the tenable account ", e.getMessage());
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Tenable validation Failed: " + e.getMessage());
        }
    }

    private String getTenableSecret(CreateAccountRequest accountRequest) {
        String template = "{\"accessKey\":\"%s\",\"secretKey\":\"%s\",\"apiURL\":\"%s\",\"userAgent\":\"%s\"}";
        return String.format(template, accountRequest.getTenableAccessKey(),
                accountRequest.getTenableSecretKey(),
                Constants.TENABLE_API_URL,
                tenableUserAgent);
    }
}

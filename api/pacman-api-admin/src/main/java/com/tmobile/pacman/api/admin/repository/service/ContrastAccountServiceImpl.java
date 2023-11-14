/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.amazonaws.services.secretsmanager.model.Filter;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ContrastAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContrastAccountServiceImpl.class);
    private static final String CONTRAST = "contrast";
    private static final String VALIDATION_FAILED = "Error in validating account";
    private static final String CONTRAST_TOKEN_TEMPLATE = "{\"api_key\":\"%s\"," +
            "\"authorization\":\"%s\",\"env_name\":\"%s\"}";
    private static final String CONTRAST_URL_TEMPLATE = "https://%s.contrastsecurity.com";
    private static final String ORGANIZATIONS_URL_PATH = "/api/v4/organizations/";
    private static final String CONTRAST_ENABLED = CONTRAST + ".enabled";

    private final CredentialProvider credentialProvider;
    private final ObjectMapper objectMapper;

    public ContrastAccountServiceImpl(CredentialProvider credentialProvider, ObjectMapper objectMapper) {
        this.credentialProvider = credentialProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public String serviceType() {
        return CONTRAST;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        AccountValidationResponse validationResponse = validateRequestData(accountData);
        if (validationResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Validation failed due to missing parameters");
            return validationResponse;
        }
        return getOrganizationName(accountData);
    }

    private AccountValidationResponse getOrganizationName(CreateAccountRequest pluginData) {
        AccountValidationResponse validationResponse = new AccountValidationResponse();
        validationResponse.setValidationStatus(FAILURE);
        validationResponse.setMessage(VALIDATION_FAILED);
        try {
            String apiUrl = new URIBuilder(String.format(CONTRAST_URL_TEMPLATE, pluginData.getContrastEnvironmentName()))
                    .setPath(ORGANIZATIONS_URL_PATH + pluginData.getContrastOrganizationId()).build().toString();
            String authKey = Base64.getEncoder().encodeToString((pluginData.getContrastUserId() + ":"
                    + pluginData.getContrastServiceKey()).getBytes());
            HttpGet request = new HttpGet(apiUrl);
            request.setHeader(HttpHeaders.AUTHORIZATION, authKey);
            request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            request.setHeader("API-Key", pluginData.getContrastApiKey());
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        String errorMessage = handleErrorResponse(response.getStatusLine().getStatusCode());
                        validationResponse.setErrorDetails(errorMessage);
                        return validationResponse;
                    }
                    JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
                    String organizationName = jsonResponse.get("name").textValue();
                    if (StringUtils.isEmpty(organizationName)) {
                        validationResponse.setErrorDetails("Couldn't read Organization name");
                        return validationResponse;
                    }
                    pluginData.setContrastOrganizationName(organizationName);
                    validationResponse.setValidationStatus(SUCCESS);
                    validationResponse.setErrorDetails(null);
                    validationResponse.setMessage("Connection established successfully");
                    return validationResponse;
                }
            }
        } catch (UnknownHostException e) {
            LOGGER.error("Unknown host error: " + e.getMessage(), e);
            validationResponse.setErrorDetails("The Environment name may be incorrect or the server may be down.");
        } catch (Exception e) {
            LOGGER.error("Unexpected error: " + e.getMessage(), e);
            validationResponse.setErrorDetails("Unexpected error occurred");
        }
        return validationResponse;
    }

    private String handleErrorResponse(int statusCode) {
        switch (statusCode) {
            case 400:
            case 404:
                return "Bad request: Check the Organization ID and Keys and make sure they are correct.";
            case 401:
                return "Unauthorized: The User Id and Keys may be incorrect or the User is locked.";
            default:
                return "Authentication failed: Check your credentials and make sure they are correct.";
        }
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Adding new Contrast account....");
        AccountValidationResponse validationResponse;
        validationResponse = validateRequestData(accountData);
        if (validationResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Adding account failed: {}", validationResponse.getMessage());
            return validationResponse;
        }
        validationResponse = getOrganizationName(accountData);
        if (validationResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Adding account failed: {}", validationResponse.getMessage());
            return validationResponse;
        }
        String organizationName = accountData.getContrastOrganizationName();
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");
        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        String secretId = getSecretId(accountData.getContrastOrganizationId(), CONTRAST);
        ListSecretsRequest listSecretsRequest = new ListSecretsRequest()
                .withFilters(new Filter().withKey("name").withValues(secretId))
                .withIncludePlannedDeletion(true);
        ListSecretsResult getListResponse = secretClient.listSecrets(listSecretsRequest);
        if (getListResponse.getSecretList().size() > 0) {
            LOGGER.info("Secret already exists. Deleting in progress.");
            validationResponse = new AccountValidationResponse();
            validationResponse.setValidationStatus(FAILURE);
            validationResponse.setMessage("Prior account is being deleted. Try again in a couple of minutes.");
            return validationResponse;
        }
        CreateSecretRequest createRequest = new CreateSecretRequest()
                .withName(secretId).withSecretString(getContrastSecretString(accountData));
        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}", createResponse);
        String accountId = UUID.randomUUID().toString();
        createAccountInDb(accountData.getContrastOrganizationId(), organizationName, CONTRAST, accountData.getCreatedBy());
        updateConfigProperty(CONTRAST_ENABLED, TRUE, JOB_SCHEDULER);
        sendSQSMessage(CONTRAST, accountData.getContrastOrganizationId());
        validationResponse = new AccountValidationResponse();
        validationResponse.setValidationStatus(SUCCESS);
        validationResponse.setAccountId(accountId);
        validationResponse.setAccountName(organizationName);
        validationResponse.setType(CONTRAST);
        validationResponse.setMessage("Account added successfully. ID: " + accountId);
        return validationResponse;
    }

    private AccountValidationResponse validateRequestData(CreateAccountRequest accountData) {
        AccountValidationResponse response = new AccountValidationResponse();
        List<String> missingParams = new ArrayList<>();
        if (StringUtils.isEmpty(accountData.getContrastOrganizationId())) {
            missingParams.add("organizationId");
        }
        if (StringUtils.isEmpty(accountData.getContrastServiceKey())) {
            missingParams.add("serviceKey");
        }
        if (StringUtils.isEmpty(accountData.getContrastApiKey())) {
            missingParams.add("apiKey");
        }
        if (StringUtils.isEmpty(accountData.getContrastUserId())) {
            missingParams.add("userId");
        }
        if (!missingParams.isEmpty()) {
            String errorMessage = MISSING_MANDATORY_PARAMETER + String.join(", ", missingParams);
            response.setMessage(errorMessage);
            response.setValidationStatus(FAILURE);
        } else {
            response.setValidationStatus(SUCCESS);
        }
        return response;
    }

    @Override
    public AccountValidationResponse deleteAccount(String organizationId) {
        LOGGER.info("Deleting contrast with organizationId: {}", organizationId);
        AccountValidationResponse response = new AccountValidationResponse();
        DeleteSecretResult deleteResponse = deleteSecret(organizationId, CONTRAST);
        LOGGER.info("Delete secret response: {} ", deleteResponse);
        deleteAccountFromDB(organizationId);
        updateConfigProperty(CONTRAST_ENABLED, FALSE, JOB_SCHEDULER);
        response.setType(CONTRAST);
        response.setAccountId(organizationId);
        response.setValidationStatus(SUCCESS);
        response.setMessage("Account deleted successfully");
        return response;
    }

    private String getContrastSecretString(CreateAccountRequest accountRequest) {
        String authKey = Base64.getEncoder().encodeToString((accountRequest.getContrastUserId() + ":"
                + accountRequest.getContrastServiceKey()).getBytes());
        return String.format(CONTRAST_TOKEN_TEMPLATE, accountRequest.getContrastApiKey(), authKey,
                accountRequest.getContrastEnvironmentName());
    }
}

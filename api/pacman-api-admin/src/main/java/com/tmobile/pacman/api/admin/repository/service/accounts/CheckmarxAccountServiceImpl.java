/*******************************************************************************
 *  Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 ******************************************************************************/
package com.tmobile.pacman.api.admin.repository.service.accounts;

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
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.admin.domain.PluginParameters;
import com.tmobile.pacman.api.commons.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckmarxAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckmarxAccountServiceImpl.class);
    private static final String CHECKMARX = "checkmarx";
    private static final String CHECKMARX_TOKEN_TEMPLATE = "{\"refresh_token\":\"%s\"," +
            "\"client_id\":\"%s\",\"tenant_name\":\"%s\"}";
    private static final String CHECKMARX_ACCESS_TOKEN_URL_TEMPLATE =
            "https://deu.iam.checkmarx.net/auth/realms/%s/protocol/openid-connect/token";
    private static final String CHECKMARX_TENANT_OVERVIEW_URL = "https://ast.checkmarx.net/api/dast/scans/tenant";
    private static final String CHECKMARX_ENABLED = CHECKMARX + ".enabled";
    private final ObjectMapper objectMapper;

    public CheckmarxAccountServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String serviceType() {
        return CHECKMARX;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        AccountValidationResponse validationResponse = validateRequestData(accountData);
        if (validationResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Validation failed due to missing parameters");
            return validationResponse;
        }
        return validateAccountCredentials(accountData);
    }

    private AccountValidationResponse validateAccountCredentials(CreateAccountRequest pluginData) {
        AccountValidationResponse validationResponse = new AccountValidationResponse();
        try {
            String apiUrl = new URIBuilder(String.format(CHECKMARX_ACCESS_TOKEN_URL_TEMPLATE, pluginData
                    .getCheckmarxTenantName())).build().toString();
            HttpPost httpPostRequest = new HttpPost(apiUrl);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "refresh_token"));
            params.add(new BasicNameValuePair("client_id", pluginData.getCheckmarxClientId()));
            params.add(new BasicNameValuePair("refresh_token", pluginData.getCheckmarxRefreshToken()));
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                httpPostRequest.setEntity(new UrlEncodedFormEntity(params));
                try (CloseableHttpResponse response = httpClient.execute(httpPostRequest)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        validationResponse.setValidationStatus(FAILURE);
                        validationResponse.setMessage("Checkmarx validation Failed");
                        validationResponse.setErrorDetails("Check your credentials and make sure they are correct.");
                        return validationResponse;
                    }
                    JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
                    String accessToken = jsonResponse.get("access_token").textValue();
                    if (StringUtils.isEmpty(accessToken)) {
                        validationResponse.setValidationStatus(FAILURE);
                        validationResponse.setErrorDetails("Couldn't read Organization details");
                        validationResponse.setMessage("Couldn't read Organization details");
                        return validationResponse;
                    }
                    pluginData.setCheckmarxAccessToken(accessToken);
                    validationResponse.setValidationStatus(SUCCESS);
                    validationResponse.setMessage("Connection established successfully");
                    return validationResponse;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error: " + e.getMessage(), e);
            validationResponse.setValidationStatus(FAILURE);
            validationResponse.setMessage("Checkmarx validation Failed");
            validationResponse.setErrorDetails("Unexpected error occurred");
        }
        return validationResponse;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest pluginData) {
        LOGGER.info("Adding new Checkmarx account....");
        String tenantId = System.getenv(AdminConstants.TENANT_ID);
        AccountValidationResponse validationResponse;
        validationResponse = validateRequestData(pluginData);
        if (validationResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Adding account failed: {}", validationResponse.getMessage());
            return validationResponse;
        }
        validationResponse = validateAccountCredentials(pluginData);
        if (validationResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Adding account failed: {}", validationResponse.getMessage());
            return validationResponse;
        }
        String checkmarxTenantId = getTenantId(pluginData.getCheckmarxAccessToken());
        if (checkmarxTenantId == null) {
            LOGGER.info("Unable to fetch tenant Id");
            validationResponse = new AccountValidationResponse();
            validationResponse.setValidationStatus(FAILURE);
            validationResponse.setMessage("Unable to retrieve tenant details.");
            return validationResponse;
        }
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");
        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        String secretId = getSecretId(checkmarxTenantId, CHECKMARX, tenantId);
        ListSecretsRequest listSecretsRequest = new ListSecretsRequest()
                .withFilters(new Filter().withKey("name").withValues(secretId))
                .withIncludePlannedDeletion(true);
        ListSecretsResult getListResponse = secretClient.listSecrets(listSecretsRequest);
        if (!getListResponse.getSecretList().isEmpty()) {
            LOGGER.info("Secret already exists. Deleting in progress.");
            deleteSecret(checkmarxTenantId, CHECKMARX, tenantId);
            validationResponse = new AccountValidationResponse();
            validationResponse.setValidationStatus(FAILURE);
            validationResponse.setMessage("Prior account is being deleted. Try again in a couple of minutes.");
            return validationResponse;
        }
        CreateSecretRequest createRequest = new CreateSecretRequest()
                .withName(secretId).withSecretString(getCheckmarxSecretString(pluginData));
        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}", createResponse);
        createAccountInDb(checkmarxTenantId, pluginData.getCheckmarxTenantName(), CHECKMARX, pluginData.getCreatedBy());
        updateConfigProperty(CHECKMARX_ENABLED, TRUE, JOB_SCHEDULER);
        sendSQSMessage(CHECKMARX, checkmarxTenantId, tenantId);
        assetGroupService.createOrUpdatePluginAssetGroup(CHECKMARX, "Checkmarx");
        PluginParameters parameters = PluginParameters.builder().pluginName(CHECKMARX)
                .pluginDisplayName(StringUtils.capitalize(CHECKMARX)).id(checkmarxTenantId)
                .createdBy(pluginData.getCreatedBy()).build();
        invokeNotificationAndActivityLogging(parameters, Constants.Actions.CREATE);
        validationResponse = new AccountValidationResponse();
        validationResponse.setValidationStatus(SUCCESS);
        validationResponse.setAccountId(checkmarxTenantId);
        validationResponse.setAccountName(pluginData.getCheckmarxTenantName());
        validationResponse.setType(CHECKMARX);
        validationResponse.setMessage("Account added successfully. ID: " + checkmarxTenantId);
        return validationResponse;
    }

    private AccountValidationResponse validateRequestData(CreateAccountRequest pluginData) {
        AccountValidationResponse response = new AccountValidationResponse();
        List<String> missingParams = new ArrayList<>();
        if (StringUtils.isEmpty(pluginData.getCheckmarxTenantName())) {
            missingParams.add("Tenant Nane");
        }
        if (StringUtils.isEmpty(pluginData.getCheckmarxRefreshToken())) {
            missingParams.add("Refresh Token");
        }
        if (StringUtils.isEmpty(pluginData.getCheckmarxClientId())) {
            missingParams.add("Client Id");
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
    public AccountValidationResponse deleteAccount(PluginParameters parameters) {
        String checkmarxTenantId = parameters.getId();
        LOGGER.info("Deleting checkmarx with checkmarxTenantId: {}", checkmarxTenantId);
        String tenantId = System.getenv(AdminConstants.TENANT_ID);
        DeleteSecretResult deleteResponse = deleteSecret(checkmarxTenantId, CHECKMARX, tenantId);
        LOGGER.info("Delete secret response: {} ", deleteResponse);
        AccountValidationResponse response = deleteAccountFromDB(checkmarxTenantId);
        if (response.getValidationStatus().equalsIgnoreCase(SUCCESS)) {
            disableAssetGroup(CHECKMARX);
            parameters.setPluginDisplayName(StringUtils.capitalize(CHECKMARX));
            invokeNotificationAndActivityLogging(parameters, Constants.Actions.DELETE);
        }
        response.setType(CHECKMARX);
        response.setAccountId(checkmarxTenantId);
        return response;
    }

    private String getCheckmarxSecretString(CreateAccountRequest accountRequest) {
        return String.format(CHECKMARX_TOKEN_TEMPLATE, accountRequest.getCheckmarxRefreshToken(),
                accountRequest.getCheckmarxClientId(), accountRequest.getCheckmarxTenantName());
    }

    private String getTenantId(String accessToken) {
        HttpGet httpGetRequest = new HttpGet(CHECKMARX_TENANT_OVERVIEW_URL);
        httpGetRequest.setHeader(HttpHeaders.AUTHORIZATION, accessToken);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGetRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
                    return jsonResponse.get("tenantID").textValue();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Unable to fetch tenantID " + e.getMessage(), e);
        }
        return null;
    }
}

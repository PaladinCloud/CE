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
package com.tmobile.pacman.api.admin.repository.service.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.PluginParameters;
import com.tmobile.pacman.api.admin.domain.PluginResponse;
import com.tmobile.pacman.api.admin.domain.plugin.ContrastPluginRequest;
import com.tmobile.pacman.api.admin.exceptions.PluginServiceException;
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
import org.springframework.transaction.annotation.Transactional;

import java.net.UnknownHostException;
import java.util.Base64;

@Service
public class ContrastPluginServiceImpl extends AbstractPluginService implements PluginsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContrastPluginServiceImpl.class);
    private static final String CONTRAST_TOKEN_TEMPLATE = "{\"api_key\":\"%s\"," +
            "\"authorization\":\"%s\",\"env_name\":\"%s\"}";
    private static final String CONTRAST_URL_TEMPLATE = "https://%s.contrastsecurity.com";
    private static final String ORGANIZATIONS_URL_PATH = "/api/v4/organizations/";
    private static final String PLUGIN_NAME = "contrast";

    @Override
    @Transactional
    public PluginResponse createPlugin(Object pluginRequest, PluginParameters parameters)
            throws PluginServiceException {
        ContrastPluginRequest request = objectMapper.convertValue(pluginRequest, ContrastPluginRequest.class);
        parameters.setId(request.getOrganizationId());
        try {
            LOGGER.info(String.format(VALIDATING_MSG, parameters.getPluginName()));
            PluginResponse validationResponse = validateContrastPluginRequest(request, parameters.getPluginName());
            if (validationResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
                LOGGER.info(VALIDATION_FAILED);
                return validationResponse;
            }
            LOGGER.info(String.format(ADDING_ACCOUNT, parameters.getPluginName()));
            PluginResponse organizationNameApiResponse = getOrganizationName(request);
            if (!organizationNameApiResponse.getStatus().equalsIgnoreCase(AdminConstants.SUCCESS)) {
                return organizationNameApiResponse;
            }
            String organizationName = organizationNameApiResponse.getMessage();
            LOGGER.info("Organization name for orgId : {} is {}", request.getOrganizationId(), organizationName);
            PluginResponse createResponse = createAccountInDb(parameters, organizationName);
            if (createResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
                LOGGER.info(ACCOUNT_EXISTS);
                return createResponse;
            }
            String authKey = Base64.getEncoder().encodeToString((request.getUserId() + ":"
                    + request.getServiceKey()).getBytes());
            String secretKey = String.format(CONTRAST_TOKEN_TEMPLATE, request.getApiKey(), authKey,
                    request.getEnvironmentName());
            parameters.setSecretKey(secretKey);
            return createSecretAndSendSQSMessage(parameters);
        } catch (Exception e) {
            deletePlugin(parameters);
            throw new PluginServiceException("Unknown Exception occurred while creating plugin", e);
        }
    }

    @Override
    public final PluginResponse deletePlugin(PluginParameters parameters) {
        return super.deletePlugin(parameters);
    }

    @Override
    public PluginResponse validate(Object pluginRequest, String pluginName) {
        ContrastPluginRequest accountData = objectMapper.convertValue(pluginRequest, ContrastPluginRequest.class);
        LOGGER.info(String.format(VALIDATING_MSG, pluginName));
        PluginResponse validationResponse = validateContrastPluginRequest(accountData, pluginName);
        if (validationResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
            LOGGER.info(VALIDATION_FAILED);
            return validationResponse;
        }
        try {
            PluginResponse organizationNameApiResponse = getOrganizationName(accountData);
            if (organizationNameApiResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
                return organizationNameApiResponse;
            }
            String organizationName = organizationNameApiResponse.getMessage();
            LOGGER.info("Organization name for orgId : {} is {}", accountData.getOrganizationId(), organizationName);
            return new PluginResponse(AdminConstants.SUCCESS, VALIDATION_SUCCESSFUL, null);
        } catch (Exception e) {
            LOGGER.error(VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, "Unexpected error occurred");
        }
    }

    @Override
    public String getPluginType() {
        return PLUGIN_NAME;
    }

    private PluginResponse validateContrastPluginRequest(ContrastPluginRequest pluginData, String pluginName) {
        PluginResponse response = new PluginResponse();
        StringBuilder validationErrorDetails = new StringBuilder();
        if (StringUtils.isEmpty(pluginData.getOrganizationId())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, "organizationId"));
        }
        if (StringUtils.isEmpty(pluginData.getServiceKey())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, "serviceKey"));
        }
        if (StringUtils.isEmpty(pluginData.getApiKey())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, "apiKey"));
        }
        if (StringUtils.isEmpty(pluginData.getUserId())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, "userId"));
        }
        String validationError = validationErrorDetails.toString();
        if (!validationError.isEmpty()) {
            response.setStatus(AdminConstants.FAILURE);
            response.setMessage(String.format(INVALID_PLUGIN_REQUEST_MSG, pluginName));
            response.setErrorDetails(validationError);
        } else {
            response.setStatus(AdminConstants.SUCCESS);
        }
        return response;
    }

    private PluginResponse getOrganizationName(ContrastPluginRequest accountData) {
        try {
            String apiUrl = new URIBuilder(String.format(CONTRAST_URL_TEMPLATE, accountData.getEnvironmentName()))
                    .setPath(ORGANIZATIONS_URL_PATH + accountData.getOrganizationId()).build().toString();
            String authKey = Base64.getEncoder().encodeToString((accountData.getUserId() + ":"
                    + accountData.getServiceKey()).getBytes());
            HttpGet request = new HttpGet(apiUrl);
            request.setHeader(HttpHeaders.AUTHORIZATION, authKey);
            request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            request.setHeader("API-Key", accountData.getApiKey());
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode != 200) {
                        String errorMessage = handleErrorResponse(response.getStatusLine().getStatusCode());
                        return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, errorMessage);
                    }
                    JsonNode jsonResponse = objectMapper.readTree(response.getEntity().getContent());
                    String organizationName = jsonResponse.get("name").textValue();
                    if (StringUtils.isEmpty(organizationName)) {
                        return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED,
                                "Couldn't read Organization name");
                    }
                    return new PluginResponse(AdminConstants.SUCCESS, organizationName, null);
                }
            }
        } catch (UnknownHostException e) {
            LOGGER.error("Unknown host error: " + e.getMessage(), e);
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, "Server not found: " +
                    "The Environment name may be incorrect or the server may be down.");
        } catch (Exception e) {
            LOGGER.error("Unexpected error: " + e.getMessage(), e);
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, "Unexpected error occurred");
        }
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
}

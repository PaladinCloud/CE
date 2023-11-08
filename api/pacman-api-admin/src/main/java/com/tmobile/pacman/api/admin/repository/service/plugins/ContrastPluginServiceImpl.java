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
import com.tmobile.pacman.api.admin.exceptions.PluginApiResponseException;
import com.tmobile.pacman.api.admin.exceptions.PluginServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

@Service
public class ContrastPluginServiceImpl extends AbstractPluginService implements PluginsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContrastPluginServiceImpl.class);
    private static final String CONTRAST_TOKEN_TEMPLATE = "{\"api_key\":\"%s\"," +
            "\"authorization\":\"%s\",\"env_name\":\"%s\"}";
    private static final String CONTRAST_URL_TEMPLATE = "https://%s.contrastsecurity.com";
    private static final String ORGANIZATIONS_URL_PATH = "/api/v4/organizations/";

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
            String organizationName = getOrganizationNameFromApiResponse(request);
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
        } catch (PluginApiResponseException pare) {
            LOGGER.error(VALIDATION_FAILED + pare.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, pare.getMessage());
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
            String organizationName = getOrganizationNameFromApiResponse(accountData);
            LOGGER.info("Organization name for orgId : {} is {}", accountData.getOrganizationId(), organizationName);
            if (StringUtils.isEmpty(organizationName)) {
                return new PluginResponse(AdminConstants.FAILURE, "Couldn't read Organization name",
                        "Organization name is null, terminating sequence");
            }
            return new PluginResponse(AdminConstants.SUCCESS, VALIDATION_SUCCESSFUL, null);
        } catch (PluginApiResponseException pare) {
            LOGGER.error(VALIDATION_FAILED + pare.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, pare.getMessage());
        } catch (Exception e) {
            LOGGER.error(VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, e.getMessage());
        }
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

    public String getOrganizationNameFromApiResponse(ContrastPluginRequest accountData) {
        try {
            String apiUrl = new URIBuilder(String.format(CONTRAST_URL_TEMPLATE, accountData.getEnvironmentName()))
                    .setPath(ORGANIZATIONS_URL_PATH + accountData.getOrganizationId()).build().toString();
            String authKey = Base64.getEncoder().encodeToString((accountData.getUserId() + ":"
                    + accountData.getServiceKey()).getBytes());
            HttpGet request = new HttpGet(apiUrl);
            request.setHeader(HttpHeaders.AUTHORIZATION, authKey);
            request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            request.setHeader("API-Key", accountData.getApiKey());
            JsonNode jsonResponse = getApiResponse(request);
            return jsonResponse.get("name").textValue();
        } catch (PluginApiResponseException pare) {
            String message;
            switch (pare.getStatusCode()) {
                case 400:
                    message = "The request could not be understood by the server. " +
                            "Check the Organization ID and Keys and make sure they are correct.";
                    break;
                case 401:
                    message = "The request requires authentication. " +
                            "The User Id and Keys may be incorrect or the User is locked.";
                    break;
                case 1000:
                    message = "The server could not be found. " +
                            "The Environment name may be incorrect or the server may be down.";
                    break;
                default:
                    message = "The server does not have the necessary credentials to authenticate the user. " +
                            "Check your credentials and make sure they are correct.";
                    break;
            }
            throw new PluginApiResponseException(message);
        } catch (Exception e) {
            throw new PluginApiResponseException(e.getMessage(), e);
        }
    }
}

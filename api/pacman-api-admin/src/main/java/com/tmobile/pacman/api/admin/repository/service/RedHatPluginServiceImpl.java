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
package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.PluginParameters;
import com.tmobile.pacman.api.admin.domain.PluginResponse;
import com.tmobile.pacman.api.admin.domain.RedHatPluginRequest;
import com.tmobile.pacman.api.admin.exceptions.PluginServiceException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.UnknownHostException;

@Service
public class RedHatPluginServiceImpl extends AbstractPluginService implements PluginsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedHatPluginServiceImpl.class);
    private static final String PARAM_ACCOUNT_ID = "redhatAccountId";
    private static final String PARAM_TOKEN = "redhatToken";
    private static final String REDHAT_TOKEN_TEMPLATE = "{\"token\":\"%s\"}";
    private static final String REDHAT_URL_TEMPLATE = "https://acs-%s.acs.rhcloud.com";
    private static final String SUMMARY_COUNTS_URL_PATH = "/v1/summary/counts";

    @Override
    @Transactional
    public PluginResponse createPlugin(Object pluginRequest, PluginParameters parameters)
            throws PluginServiceException {
        RedHatPluginRequest request = objectMapper.convertValue(pluginRequest, RedHatPluginRequest.class);
        parameters.setId(request.getRedhatAccountId());
        try {
            LOGGER.info(String.format(VALIDATING_MSG, parameters.getPluginName()));
            if (StringUtils.isEmpty(request.getRedhatAccountName())) {
                request.setRedhatAccountName(request.getRedhatAccountId());
            }
            PluginResponse validationResponse = validateRedhatPluginRequest(request, parameters.getPluginName());
            if (validationResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
                LOGGER.info(VALIDATION_FAILED);
                return validationResponse;
            }
            LOGGER.info(String.format(ADDING_ACCOUNT, parameters.getPluginName()));
            PluginResponse createResponse = createAccountInDb(parameters, request.getRedhatAccountName());
            if (createResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
                LOGGER.info(ACCOUNT_EXISTS);
                return createResponse;
            }
            String secretKey = String.format(REDHAT_TOKEN_TEMPLATE, request.getRedhatToken());
            parameters.setSecretKey(secretKey);
            parameters.setPluginDisplayName("Red Hat");
            return createAndTriggerSQSForPlugin(parameters);
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
        RedHatPluginRequest accountData = objectMapper.convertValue(pluginRequest, RedHatPluginRequest.class);
        LOGGER.info(String.format(VALIDATING_MSG, pluginName));
        PluginResponse validationResponse = validateRedhatPluginRequest(accountData, pluginName);
        if (validationResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
            LOGGER.info(VALIDATION_FAILED);
            return validationResponse;
        }
        String apiUrl = String.format(REDHAT_URL_TEMPLATE, accountData.getRedhatAccountId()) + SUMMARY_COUNTS_URL_PATH;
        String redhatToken = accountData.getRedhatToken();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + redhatToken);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    LOGGER.info(VALIDATION_SUCCESSFUL);
                    return new PluginResponse(AdminConstants.SUCCESS, VALIDATION_SUCCESSFUL, null);
                } else {
                    String errorMessage = String.format(TOKEN_VALIDATION_FAILED + HTTP_STATUS_CODE_TEMPLATE, statusCode);
                    LOGGER.error(errorMessage);
                    return new PluginResponse(AdminConstants.FAILURE, TOKEN_VALIDATION_FAILED, errorMessage);
                }
            }
        } catch (UnknownHostException e) {
            LOGGER.error(ACCOUNT_ID_VALIDATION_FAILED + UNKNOWN_HOST_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, ACCOUNT_ID_VALIDATION_FAILED, e.getMessage());
        } catch (Exception e) {
            LOGGER.error(VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, e.getMessage());
        }
    }

    private PluginResponse validateRedhatPluginRequest(RedHatPluginRequest pluginData, String pluginName) {
        PluginResponse response = new PluginResponse();
        StringBuilder validationErrorDetails = new StringBuilder();
        if (StringUtils.isEmpty(pluginData.getRedhatAccountId())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, PARAM_ACCOUNT_ID));
        }
        if (StringUtils.isEmpty(pluginData.getRedhatToken())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, PARAM_TOKEN));
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
}

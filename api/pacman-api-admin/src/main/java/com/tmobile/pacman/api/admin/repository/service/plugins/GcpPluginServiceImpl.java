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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.plugin.GcpPluginRequest;
import com.tmobile.pacman.api.admin.domain.PluginParameters;
import com.tmobile.pacman.api.admin.domain.PluginResponse;
import com.tmobile.pacman.api.admin.exceptions.PluginServiceException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Service
public class GcpPluginServiceImpl extends AbstractPluginService implements PluginsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcpPluginServiceImpl.class);

    @Override
    @Transactional
    public PluginResponse createPlugin(Object pluginRequest, PluginParameters parameters)
            throws PluginServiceException {
        GcpPluginRequest request = objectMapper.convertValue(pluginRequest, GcpPluginRequest.class);
        parameters.setId(request.getGcpProjectId());
        parameters.setSecretKey(request.getGcpServiceAccountKey());
        try {
            LOGGER.info(String.format(VALIDATING_MSG, parameters.getPluginName()));
            PluginResponse validationResponse = validateRedhatPluginRequest(request, parameters.getPluginName());
            if (validationResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
                LOGGER.info(VALIDATION_FAILED);
                return validationResponse;
            }
            LOGGER.info(String.format(ADDING_ACCOUNT, parameters.getPluginName()));
            Optional<String> projectNameOptional = getProjectName(parameters);
            if (!projectNameOptional.isPresent()) {
                return new PluginResponse(AdminConstants.FAILURE, "Couldn't read project name",
                        "Project name is null, terminating sequence");
            }
            PluginResponse createResponse = createAccountInDb(parameters, projectNameOptional.get());
            if (createResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
                LOGGER.info(ACCOUNT_EXISTS);
                return createResponse;
            }
            return createSecretAndSendSQSMessage(parameters);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unexpected Error while validating private_key : " + e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED,
                    "Validation failed: Invalid Service account key format.");
        } catch (IOException e) {
            LOGGER.error("Unexpected Error while creating GoogleCredentials " + VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED, "Validation failed due to an unexpected error.");
        } catch (GeneralSecurityException e) {
            LOGGER.error("Unexpected Error while fetching project details " +
                    VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED,
                    "Validation failed due to an unexpected error.");
        } catch (Exception e) {
            String message = "Something happened";
            LOGGER.error(message);
            deletePlugin(parameters);
            throw new PluginServiceException(message, e);
        }
    }

    @Override
    public final PluginResponse deletePlugin(PluginParameters parameters) {
        return super.deletePlugin(parameters);
    }

    @Override
    public PluginResponse validate(Object pluginRequest, String pluginName) {
        GcpPluginRequest accountData = objectMapper.convertValue(pluginRequest, GcpPluginRequest.class);
        LOGGER.info(String.format(VALIDATING_MSG, pluginName));
        PluginResponse validationResponse = validateRedhatPluginRequest(accountData, pluginName);
        if (validationResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
            LOGGER.info(VALIDATION_FAILED);
            return validationResponse;
        }
        try {
            PluginParameters parameters = PluginParameters.builder().id(accountData.getGcpProjectId())
                    .secretKey(accountData.getGcpServiceAccountKey()).build();
            Optional<String> projectNameOptional = getProjectName(parameters);
            if (!projectNameOptional.isPresent()) {
                return new PluginResponse(AdminConstants.FAILURE, "Couldn't read project name",
                        "Project name is null, terminating sequence");
            }
            return new PluginResponse(AdminConstants.SUCCESS, VALIDATION_SUCCESSFUL, null);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Unexpected Error while validating private_key : " + e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED,
                    "Validation failed: Invalid Service account key format.");
        } catch (IOException e) {
            LOGGER.error("Unexpected Error while creating GoogleCredentials " +
                    VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED,
                    "Validation failed due to an unexpected error.");
        } catch (GeneralSecurityException e) {
            LOGGER.error("Unexpected Error while fetching project details " +
                    VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED,
                    "Validation failed while fetching project details.");
        } catch (Exception e) {
            LOGGER.error(VALIDATION_FAILED + UNEXPECTED_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, VALIDATION_FAILED,
                    "Validation failed due to an unexpected error.");
        }
    }

    private PluginResponse validateRedhatPluginRequest(GcpPluginRequest pluginData, String pluginName) {
        PluginResponse response = new PluginResponse();
        StringBuilder validationErrorDetails = new StringBuilder();
        if (StringUtils.isEmpty(pluginData.getGcpProjectId())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, "projectId"));
        }
        if (StringUtils.isEmpty(pluginData.getGcpServiceAccountKey())) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, "serviceAccountKey"));
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

    private Optional<String> getProjectName(PluginParameters parameters) throws IOException, GeneralSecurityException {
        GoogleCredentials gcpCredentials = GoogleCredentials.fromStream(new ByteArrayInputStream(
                        parameters.getSecretKey().getBytes()))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        LOGGER.info("Credentials created: {}", gcpCredentials);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        CloudResourceManager cloudResourceManager = new CloudResourceManager.Builder(httpTransport,
                jsonFactory, new HttpCredentialsAdapter(gcpCredentials)).build();
        CloudResourceManager.Projects.Get projectDetails = cloudResourceManager.projects().get(parameters.getId());
        Project project = projectDetails.execute();
        if (project == null) {
            LOGGER.error("Project not found for projectId : {}", parameters.getId());
            return Optional.empty();
        }
        return Optional.of(project.getName());
    }
}

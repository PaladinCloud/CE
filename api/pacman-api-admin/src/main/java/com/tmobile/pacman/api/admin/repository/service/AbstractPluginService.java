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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.amazonaws.services.secretsmanager.model.ResourceExistsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.ActivityLogRequest;
import com.tmobile.pacman.api.admin.domain.ConfigPropertyItem;
import com.tmobile.pacman.api.admin.domain.ConfigPropertyRequest;
import com.tmobile.pacman.api.admin.domain.PluginParameters;
import com.tmobile.pacman.api.admin.domain.PluginResponse;
import com.tmobile.pacman.api.admin.factory.AccountFactory;
import com.tmobile.pacman.api.admin.repository.AccountsRepository;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.admin.repository.model.ConfigProperty;
import com.tmobile.pacman.api.admin.util.AdminUtils;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public abstract class AbstractPluginService {

    protected static final String MISSING_MANDATORY_PARAMETER = "%s cannot be null or empty ";
    protected static final String HTTP_STATUS_CODE_TEMPLATE = " with HTTP status code: %d";
    protected static final String VALIDATION_SUCCESSFUL = "Validation successful";
    protected static final String BEARER_PREFIX = "Bearer ";
    protected static final String VALIDATION_FAILED = "Validation failed";
    protected static final String TOKEN_VALIDATION_FAILED = "Validation failed due to invalid token";
    protected static final String ACCOUNT_ID_VALIDATION_FAILED = "Validation failed due to invalid accountId";
    protected static final String ACCOUNT_EXISTS = "Account already exists";
    protected static final String UNEXPECTED_ERROR_MSG = " due to an unexpected error: {}";
    protected static final String UNKNOWN_HOST_ERROR_MSG = " due to an unknown host: {}";
    protected static final String VALIDATING_MSG = "Validating %s account details";
    protected static final String ADDING_ACCOUNT = "Adding %s account";
    protected static final String INVALID_PLUGIN_REQUEST_MSG = "Invalid %s plugin request";
    /* This code need to revisited during SAAS in share mode.   */
    protected static final String TENANT_ID = System.getenv(AdminConstants.TENANT_ID);
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginService.class);
    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    private static final String ACCOUNTS_TABLE_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    private static final String STATUS_CONFIGURED = "configured";
    private static final String JOB_SCHEDULER = "job-scheduler";
    private static final String ACCOUNT_EXISTS_MSG = "Account already exists";
    private static final String PARAM_MSG = ": {}";
    private static final String INITIAL_COUNT = "0";
    private static final String ACCOUNT_ADDED_MSG = "Account added successfully.";
    private static final String ACCOUNT_CREATION_FAIL_MSG = "Account creation failed.";
    private static final String DATA_ACCESS_EXCEPTION_MSG = "DataAccessException occurred while saving the account.";
    private static final String SAVING_ERROR_MSG = "Error occurred while saving the account.";
    private static final String ACCOUNT_DETAILS_FETCH_DB_ERROR_MSG = "Error occurred in DB while fetching account list: {}";
    private static final String ACCOUNT_DETAILS_FETCH_ERROR_MSG = "Error occurred while fetching account list: {}";
    private static final String FAILED_TO_UPDATE_MSG = "Failed to update configuration property due to an unexpected error";
    private static final String ACCOUNT_UPDATE_ERROR_MSG = "Error while updating configuration property: {}";
    private static final String CONFIG_UPDATED = " config updated successfully.";
    private static final String FAILED_TO_UPDATE_CONFIG = "Failed to update configuration property: updated count is 0";
    private static final String RECORDS_NOT_UPDATED_MSG = "0 records updated for config property";
    private static final String ACCOUNT_DELETION_FAILED_MSG = "Account deletion failed due to an unexpected error";
    private static final String ACCOUNT_DELETION_DB_FAILED_MSG = "Error while deleting account from the database: {}";
    private static final String ACCOUNT_DELETION_ERROR_MSG = "Error in deleting account: {}";
    private static final String ACCOUNT_DELETION_DB_ERROR_MSG = "Account deletion failed due to database related error";
    private static final String FETCH_ONLINE_ACCOUNT_ERROR_MSG = "Error occurred while fetching online accounts from the database";
    private static final String ACCOUNT_DELETION_FAILED_ERROR_MSG = "Account deletion failed: Account doesn't exist";
    private static final String ACCOUNT_DOESNT_EXISTS = "Account doesn't exist";
    private static final String ACCOUNT_DELETED_MSG = "Account deleted successfully";
    private static final String REGION = "REGION";
    private static final String ERROR_MESSAGE_TEMPLATE = "Error in %s account. ";
    private static final String SECRET_EXISTS = "The operation failed because the token/secret already exists";
    private static final String CREATING = "creating";
    private static final String SECRET_CREATION_RESPONSE = "Secret creation response: {}";
    private static final String ACCOUNT_ADDED_SUCCESSFULLY = "Account added successfully: %s";
    private static final String FAILED_TO_UPDATE_CONFIG_MSG = "Failed to update configuration property: {}";
    private static final String PLUGIN_ENABLED = "%s.enabled";
    private static final String DELETING = "deleting";
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private ConfigPropertyService configPropertyService;
    @Autowired
    private CredentialProvider credentialProvider;
    @Autowired
    private DataCollectorSQSService dataCollectorSQSService;
    @Autowired
    private ElasticSearchRepository elasticSearchRepository;
    @Autowired
    private AssetGroupService assetGroupService;
    @Autowired
    private UserPreferencesService userPreferencesService;
    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    protected ObjectMapper objectMapper;

    protected PluginResponse deleteAccountFromDB(String accountId) {
        try {
            Optional<AccountDetails> existingAccount = accountsRepository.findById(accountId);
            if (!existingAccount.isPresent()) {
                return new PluginResponse(AdminConstants.FAILURE, ACCOUNT_DOESNT_EXISTS,
                        ACCOUNT_DELETION_FAILED_ERROR_MSG);
            }
            accountsRepository.deleteById(accountId);
            return new PluginResponse(AdminConstants.SUCCESS, ACCOUNT_DELETED_MSG, null);
        } catch (DataAccessException e) {
            // Handle database-related exceptions
            LOGGER.error(ACCOUNT_DELETION_DB_FAILED_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, ACCOUNT_DELETION_DB_ERROR_MSG,
                    FETCH_ONLINE_ACCOUNT_ERROR_MSG);
        } catch (Exception e) {
            LOGGER.error(ACCOUNT_DELETION_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, ACCOUNT_DELETION_FAILED_MSG,
                    AdminConstants.UNEXPECTED_ERROR_OCCURRED);
        }
    }

    /**
     * Updates a configuration property with the specified key and value for job-scheduler.
     *
     * @param key   The configuration property key, e.g., "redhat.enabled," "gcp.enabled," "aws.enabled," etc.
     * @param value The new value to set for the configuration property, which should be either "true" or "false."
     * @return A PluginResponse indicating the result of the update operation.
     */
    protected PluginResponse updateConfigProperty(String key, String value) {
        try {
            ConfigPropertyRequest configPropertyRequest = new ConfigPropertyRequest();
            ConfigPropertyItem config = new ConfigPropertyItem();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setApplication(JOB_SCHEDULER);
            List<ConfigPropertyItem> configList = new ArrayList<>();
            configList.add(config);
            configPropertyRequest.setConfigProperties(configList);

            List<ConfigProperty> configProperties = configPropertyService
                    .addUpdateProperties(configPropertyRequest, StringUtils.EMPTY, StringUtils.EMPTY,
                            AdminUtils.getFormatedStringDate(DATE_FORMAT, new Date()), false);
            if (configProperties.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_CONFIG);
                return new PluginResponse(AdminConstants.FAILURE, FAILED_TO_UPDATE_CONFIG, RECORDS_NOT_UPDATED_MSG);
            }
            LOGGER.info(configProperties.size() + CONFIG_UPDATED);
            return new PluginResponse(AdminConstants.SUCCESS, configProperties.size() + CONFIG_UPDATED, null);
        } catch (Exception e) {
            LOGGER.error(ACCOUNT_UPDATE_ERROR_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, FAILED_TO_UPDATE_MSG, RECORDS_NOT_UPDATED_MSG);
        }
    }

    protected PluginResponse createAccountInDb(PluginParameters parameters, String accountName) {
        String accountId = parameters.getId();
        Optional<AccountDetails> account = accountsRepository.findById(accountId);
        if (account.isPresent()) {
            LOGGER.error(ACCOUNT_EXISTS_MSG + PARAM_MSG, account.get().getAccountId());
            return new PluginResponse(AdminConstants.FAILURE, ACCOUNT_EXISTS_MSG, ACCOUNT_EXISTS_MSG);
        }
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountId(accountId);
        accountDetails.setViolations(INITIAL_COUNT);
        accountDetails.setAssets(INITIAL_COUNT);
        accountDetails.setAccountName(accountName);
        accountDetails.setPlatform(parameters.getPluginName());
        accountDetails.setAccountStatus(STATUS_CONFIGURED);
        accountDetails.setCreatedBy(parameters.getCreatedBy());
        accountDetails.setCreatedTime(Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC())));
        try {
            accountsRepository.save(accountDetails);
            return new PluginResponse(AdminConstants.SUCCESS, ACCOUNT_ADDED_MSG, null);
        } catch (DataAccessException e) {
            LOGGER.error(DATA_ACCESS_EXCEPTION_MSG + PARAM_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, ACCOUNT_CREATION_FAIL_MSG, DATA_ACCESS_EXCEPTION_MSG);
        } catch (Exception e) {
            LOGGER.error(SAVING_ERROR_MSG + PARAM_MSG, e.getMessage());
            return new PluginResponse(AdminConstants.FAILURE, ACCOUNT_CREATION_FAIL_MSG, SAVING_ERROR_MSG);
        }
    }

    protected List<AccountDetails> findOnlineAccounts(String status, String platform) {
        try {
            return accountsRepository.findByAccountStatusAndPlatform(status, platform);
        } catch (DataAccessException e) {
            LOGGER.error(ACCOUNT_DETAILS_FETCH_DB_ERROR_MSG, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            LOGGER.error(ACCOUNT_DETAILS_FETCH_ERROR_MSG, e.getMessage());
            return Collections.emptyList();
        }
    }

    protected PluginResponse createAndTriggerSQSForPlugin(PluginParameters parameters) {
        PluginResponse response;
        try {
            BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
            Regions region = Regions.fromName(System.getenv(REGION));
            AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
            CreateSecretRequest createRequest = new CreateSecretRequest()
                    .withName(secretManagerPrefix + "/" + TENANT_ID + "/" + parameters.getPluginName() + "/" +
                            parameters.getId()).withSecretString(parameters.getSecretKey());
            CreateSecretResult createSecretResponse = secretClient.createSecret(createRequest);
            LOGGER.info(SECRET_CREATION_RESPONSE, createSecretResponse);
            PluginResponse configUpdateResponse = updateConfigProperty(String.format(PLUGIN_ENABLED,
                    parameters.getPluginName()), Boolean.TRUE.toString());
            if (configUpdateResponse.getStatus().equals(AdminConstants.FAILURE)) {
                LOGGER.error(FAILED_TO_UPDATE_CONFIG_MSG, configUpdateResponse.getErrorDetails());
            }
            List<AccountDetails> onlineAccounts = findOnlineAccounts(STATUS_CONFIGURED, parameters.getPluginName());
            /* Send SQS message to DataCollector SQS to trigger collector, mapper, shipper */
            dataCollectorSQSService.sendSQSMessage(parameters.getPluginName(), TENANT_ID, onlineAccounts);
            response = new PluginResponse(AdminConstants.SUCCESS, String.format(ACCOUNT_ADDED_SUCCESSFULLY,
                    parameters.getId()), null);
            assetGroupService.createOrUpdatePluginAssetGroup(parameters.getPluginName(),
                    parameters.getPluginDisplayName());
            invokeActivityLogging(parameters, "create");
        } catch (ResourceExistsException ree) {
            LOGGER.error(String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + ree.getMessage());
            deleteSecret(parameters.getId(), parameters.getPluginName());
            deleteAccountFromDB(parameters.getId());
            response = new PluginResponse(AdminConstants.FAILURE, String.format(ERROR_MESSAGE_TEMPLATE, CREATING) +
                    SECRET_EXISTS, String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + ree.getMessage());
        } catch (Exception e) {
            LOGGER.error(String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + e.getMessage());
            deleteAccountFromDB(parameters.getId());
            response = new PluginResponse(AdminConstants.FAILURE, String.format(ERROR_MESSAGE_TEMPLATE, CREATING),
                    String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + e.getMessage());
        }
        return response;
    }

    protected void deleteSecret(String accountId, String plugin) {
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        Regions region = Regions.fromName(System.getenv(REGION));
        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
        DeleteSecretRequest deleteRequest = new DeleteSecretRequest().withSecretId(secretManagerPrefix + "/" +
                TENANT_ID + "/" + plugin + "/" + accountId).withForceDeleteWithoutRecovery(true);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ", deleteResponse);
    }

    protected void checkAndDisableOnlineAccounts(String plugin) {
        List<AccountDetails> onlineAccounts = findOnlineAccounts(STATUS_CONFIGURED, plugin);
        if (!Objects.isNull(onlineAccounts) && !onlineAccounts.isEmpty()) {
            LOGGER.info("There are {} online account(s). " +
                    "Therefore, no updates to accounts are required.", onlineAccounts.size());
            return;
        }
        LOGGER.info(String.format("%s accounts have been deleted, so disabling the enable flag for %s.", plugin, plugin));
        PluginResponse configUpdateResponse = updateConfigProperty(String.format(PLUGIN_ENABLED, plugin),
                Boolean.FALSE.toString());
        if (configUpdateResponse.getStatus().equals(AdminConstants.FAILURE)) {
            LOGGER.error(FAILED_TO_UPDATE_CONFIG_MSG, configUpdateResponse.getErrorDetails());
        }
    }

    protected void invokeActivityLogging(PluginParameters parameters, String action) {
        try {
            ObjectNode objectNode = objectMapper.convertValue(parameters, ObjectNode.class);
            //removing secret key for audit log
            objectNode.remove("secretKey");
            ActivityLogRequest request = new ActivityLogRequest();
            request.setAction(action);
            request.setOldState("NA");
            request.setUser(parameters.getCreatedBy());
            request.setObject("Plugin");
            request.setObjectId(parameters.getId());
            request.setNewState(objectMapper.writeValueAsString(objectNode));
            elasticSearchRepository.saveActivityLogToES("activitylog", request.getActivityLogDetails());
        } catch (Exception exception) {
            LOGGER.error("Could not save account created with id - {} to activity log!!", parameters.getId(),
                    exception);
        }
    }

    protected PluginResponse deletePlugin(PluginParameters parameters) {
        LOGGER.info(String.format("Deleting %s account", parameters.getPluginName()));
        PluginResponse response;
        try {
            deleteSecret(parameters.getId(), parameters.getPluginName());
            response = deleteAccountFromDB(parameters.getId());
            if (response.getStatus().equals(AdminConstants.FAILURE)) {
                return response;
            }
            //TODO: Remove plugin services (AbstractPluginService) and use Account service.
            // disableAssetGroup is a common function hence using contrast
            AccountsService accountsService = AccountFactory.getService("contrast");
            accountsService.disableAssetGroup(parameters.getPluginName());
            checkAndDisableOnlineAccounts(parameters.getPluginName());
            invokeActivityLogging(parameters, "delete");
        } catch (Exception e) {
            LOGGER.error(String.format(ERROR_MESSAGE_TEMPLATE, DELETING) + e.getMessage());
            response = new PluginResponse(AdminConstants.FAILURE, String.format(ERROR_MESSAGE_TEMPLATE, DELETING),
                    String.format(ERROR_MESSAGE_TEMPLATE, DELETING) + e.getMessage());
        }
        return response;
    }
}

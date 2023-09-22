package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.ConfigPropertyItem;
import com.tmobile.pacman.api.admin.domain.ConfigPropertyRequest;
import com.tmobile.pacman.api.admin.domain.PluginResponse;
import com.tmobile.pacman.api.admin.domain.RedHatPluginRequest;
import com.tmobile.pacman.api.admin.repository.AccountsRepository;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.admin.repository.model.ConfigProperty;
import com.tmobile.pacman.api.admin.util.AdminUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public abstract class AbstractPluginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginService.class);

    @Autowired
    AccountsRepository accountsRepository;
    @Autowired
    ConfigPropertyService configPropertyService;

    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static final String ACCOUNTS_TABLE_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String STATUS_CONFIGURED = "configured";
    public static final String ROLE_NAME = "PALADINCLOUD_RO";
    public static final String JOB_SCHEDULER = "job-scheduler";
    public static final String ACCOUNT_EXISTS_MSG = "Account already exists";
    public static final String PARAM_MSG = ": {}";
    public static final String INITIAL_COUNT = "0";
    public static final String ACCOUNT_ADDED_MSG = "Account added successfully.";
    public static final String ACCOUNT_CREATION_FAIL_MSG = "Account creation failed.";
    public static final String DATA_ACCESS_EXCEPTION_MSG = "DataAccessException occurred while saving the account.";
    public static final String SAVING_ERROR_MSG = "Error occurred while saving the account.";
    public static final String ACCOUNT_DETAILS_FETCH_DB_ERROR_MSG = "Error occurred in DB while fetching account list: {}";
    public static final String ACCOUNT_DETAILS_FETCH_ERROR_MSG = "Error occurred while fetching account list: {}";
    public static final String FAILED_TO_UPDATE_MSG = "Failed to update configuration property due to an unexpected error";
    public static final String ACCOUNT_UPDATE_ERROR_MSG = "Error while updating configuration property: {}";
    public static final String CONFIG_UPDATED = " config updated successfully.";
    public static final String FAILED_TO_UPDATE_CONFIG = "Failed to update configuration property: updated count is 0";
    public static final String RECORDS_NOT_UPDATED_MSG = "0 records updated for config property";
    public static final String ACCOUNT_DELETION_FAILED_MSG = "Account deletion failed due to an unexpected error";
    public static final String ACCOUNT_DELETION_DB_FAILED_MSG = "Error while deleting account from the database: {}";
    public static final String ACCOUNT_DELETION_ERROR_MSG = "Error in deleting account: {}";
    public static final String ACCOUNT_DELETION_DB_ERROR_MSG = "Account deletion failed due to database related error";
    public static final String FETCH_ONLINE_ACCOUNT_ERROR_MSG = "Error occurred while fetching online accounts from the database";
    public static final String ACCOUNT_DELETION_FAILED_ERROR_MSG = "Account deletion failed: Account doesn't exist";
    public static final String ACCOUNT_DOESNT_EXISTS = "Account doesn't exist";
    public static final String ACCOUNT_DELETED_MSG = "Account deleted successfully";

    public PluginResponse deleteAccountFromDB(String accountId) {
        PluginResponse response = new PluginResponse();

        try {
            Optional<AccountDetails> existingAccount = accountsRepository.findById(accountId);
            if (existingAccount.isPresent()) {
                accountsRepository.deleteById(accountId);
                response.setMessage(ACCOUNT_DELETED_MSG);
                response.setStatus(AdminConstants.SUCCESS);
            } else {
                response.setStatus(AdminConstants.FAILURE);
                response.setErrorDetails(ACCOUNT_DOESNT_EXISTS);
                response.setMessage(ACCOUNT_DELETION_FAILED_ERROR_MSG);
            }
        } catch (DataAccessException e) {
            // Handle database-related exceptions
            LOGGER.error(ACCOUNT_DELETION_DB_FAILED_MSG, e.getMessage());
            response.setStatus(AdminConstants.FAILURE);
            response.setErrorDetails(FETCH_ONLINE_ACCOUNT_ERROR_MSG);
            response.setMessage(ACCOUNT_DELETION_DB_ERROR_MSG);
        } catch (Exception e) {
            LOGGER.error(ACCOUNT_DELETION_ERROR_MSG, e.getMessage());
            response.setStatus(AdminConstants.FAILURE);
            response.setErrorDetails(AdminConstants.UNEXPECTED_ERROR_OCCURRED);
            response.setMessage(ACCOUNT_DELETION_FAILED_MSG);
        }

        return response;
    }

    public PluginResponse updateConfigProperty(String key, String value, String application) {
        PluginResponse response = new PluginResponse();

        try {
            ConfigPropertyRequest configPropertyRequest = new ConfigPropertyRequest();
            ConfigPropertyItem config = new ConfigPropertyItem();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setApplication(application);
            List<ConfigPropertyItem> configList = new ArrayList<>();
            configList.add(config);
            configPropertyRequest.setConfigProperties(configList);

            List<ConfigProperty> configProperties = configPropertyService.addUpdateProperties(configPropertyRequest,
                    StringUtils.EMPTY, StringUtils.EMPTY, AdminUtils.getFormatedStringDate(DATE_FORMAT, new Date()),
                    false);
            if (configProperties.isEmpty()) {
                LOGGER.error(FAILED_TO_UPDATE_CONFIG);
                response.setStatus(AdminConstants.FAILURE);
                response.setErrorDetails(RECORDS_NOT_UPDATED_MSG);
                response.setMessage(FAILED_TO_UPDATE_CONFIG);
            } else {
                response.setStatus(AdminConstants.SUCCESS);
                LOGGER.info(configProperties.size() + CONFIG_UPDATED);
                response.setMessage(configProperties.size() + CONFIG_UPDATED);
            }
        } catch (Exception e) {
            LOGGER.error(ACCOUNT_UPDATE_ERROR_MSG, e.getMessage());
            response.setStatus(AdminConstants.FAILURE);
            response.setErrorDetails(AdminConstants.UNEXPECTED_ERROR_OCCURRED);
            response.setMessage(FAILED_TO_UPDATE_MSG);
        }

        return response;
    }

    public PluginResponse createAccountInDb(RedHatPluginRequest pluginRequest, String platform, String createdBy) {
        PluginResponse response = new PluginResponse();
        String accountId = pluginRequest.getRedhatAccountId();
        String accountName = pluginRequest.getRedhatAccountName();

        Optional<AccountDetails> account = accountsRepository.findById(accountId);
        if (account.isPresent()) {
            LOGGER.error(ACCOUNT_EXISTS_MSG + PARAM_MSG, account.get().getAccountId());
            response.setErrorDetails(ACCOUNT_EXISTS_MSG);
            response.setStatus(AdminConstants.FAILURE);
            response.setMessage(ACCOUNT_EXISTS_MSG);
        } else {
            AccountDetails accountDetails = new AccountDetails();
            accountDetails.setAccountId(accountId);
            accountDetails.setViolations(INITIAL_COUNT);
            accountDetails.setAssets(INITIAL_COUNT);
            accountDetails.setAccountName(accountName);
            accountDetails.setPlatform(platform);
            accountDetails.setAccountStatus(STATUS_CONFIGURED);
            accountDetails.setCreatedBy(createdBy);

            SimpleDateFormat formatter = new SimpleDateFormat(ACCOUNTS_TABLE_DATE_FORMAT);
            Date date = new Date();
            accountDetails.setCreatedTime(formatter.format(date));

            try {
                accountsRepository.save(accountDetails);
                response.setStatus(AdminConstants.SUCCESS);
                response.setMessage(ACCOUNT_ADDED_MSG);
            } catch (DataAccessException e) {
                LOGGER.error(DATA_ACCESS_EXCEPTION_MSG + PARAM_MSG, e.getMessage());
                response.setErrorDetails(DATA_ACCESS_EXCEPTION_MSG);
                response.setStatus(AdminConstants.FAILURE);
                response.setMessage(ACCOUNT_CREATION_FAIL_MSG);
            } catch (Exception e) {
                LOGGER.error(SAVING_ERROR_MSG + PARAM_MSG, e.getMessage());
                response.setErrorDetails(SAVING_ERROR_MSG);
                response.setStatus(AdminConstants.FAILURE);
                response.setMessage(ACCOUNT_CREATION_FAIL_MSG);
            }
        }
        return response;
    }

    public List<AccountDetails> findOnlineAccounts(String status, String platform) {
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

}

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
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.PluginResponse;
import com.tmobile.pacman.api.admin.domain.RedHatPluginRequest;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.UnknownHostException;
import java.util.List;

@Service
public class RedHatPluginServiceImpl extends AbstractPluginService implements PluginsService<RedHatPluginRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedHatPluginServiceImpl.class);

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    CredentialProvider credentialProvider;
    private static final String ERROR_MESSAGE_TEMPLATE = "Error in %s account. ";
    private static final String SECRET_EXISTS = "The operation failed because the token/secret already exists";
    private static final String CREATING = "creating";
    private static final String DELETING = "deleting";
    private static final String HTTP_STATUS_CODE_TEMPLATE = " with HTTP status code: %d";
    private static final String VALIDATION_SUCCESSFUL = "Validation successful";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String TOKEN_VALIDATION_FAILED = "Validation failed due to invalid token";
    private static final String ACCOUNT_ID_VALIDATION_FAILED = "Validation failed due to invalid accountId";
    private static final String ADDING_ACCOUNT = "Adding redhat account";
    private static final String DELETING_ACCOUNT = "Deleting redhat account";
    private static final String FAILED_TO_UPDATE_CONFIG_MSG = "Failed to update configuration property: {}";
    private static final String REDHAT_ENABLED = "redhat.enabled";
    private static final String FORWARD_SLASH = "/";
    private static final String SUMMARY_COUNTS_URL_PATH = "/v1/summary/counts";
    private static final String ACCOUNT_EXISTS = "Account already exists";
    private static final String REGION = "REGION";
    private static final String PARAM_ACCOUNT_ID = "redhatAccountId";
    private static final String PARAM_TOKEN = "redhatToken";
    private static final String DELETE_SECRET_RESPONSE = "Delete secret response: {} ";
    private static final String ACCOUNTS_DELETED_MSG = "Redhat accounts are deleted, disabling redhat enable flag";
    private static final String REDHAT_TOKEN_TEMPLATE = "{\"token\":\"%s\"}";
    private static final String REDHAT_URL_TEMPLATE = "https://acs-%s.acs.rhcloud.com";
    private static final String MISSING_MANDATORY_PARAMETER = "%s cannot be null or empty ";
    private static final String SECRET_CREATION_RESPONSE = "Secret creation response: {}";
    private static final String ACCOUNT_ADDED_SUCCESSFULLY = "Account added successfully: %s";
    private static final String INVALID_PLUGIN_REQUEST_MSG = "Invalid RedHat plugin request";
    private static final String UNEXPECTED_ERROR_MSG = " due to an unexpected error: {}";
    private static final String UNKNOWN_HOST_ERROR_MSG = " due to an unknown host: {}";
    private static final String VALIDATING_MSG = "Validating Red Hat account details";

    @Override
    @Transactional
    public PluginResponse createPlugin(RedHatPluginRequest request, String createdBy) {
        LOGGER.info(VALIDATING_MSG);

        if (StringUtils.isEmpty(request.getRedhatAccountName())) {
            request.setRedhatAccountName(request.getRedhatAccountId());
        }

        PluginResponse validationResponse = validateRedhatPluginRequest(request);

        if (validationResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
            LOGGER.info(VALIDATION_FAILED);
            return validationResponse;
        }

        LOGGER.info(ADDING_ACCOUNT);
        PluginResponse createResponse = createAccountInDb(request, Constants.REDHAT, createdBy);

        if (createResponse.getStatus().equalsIgnoreCase(AdminConstants.FAILURE)) {
            LOGGER.info(ACCOUNT_EXISTS);
            return createResponse;
        }

        try {
            BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
            Regions region = Regions.fromName(System.getenv(REGION));
            String roleName = System.getenv(ROLE_NAME);
            AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region).build();

            CreateSecretRequest createRequest = new CreateSecretRequest()
                    .withName(secretManagerPrefix + FORWARD_SLASH + roleName + FORWARD_SLASH + Constants.REDHAT +
                            FORWARD_SLASH + request.getRedhatAccountId())
                    .withSecretString(String.format(REDHAT_TOKEN_TEMPLATE, request.getRedhatToken()));

            CreateSecretResult createSecretResponse = secretClient.createSecret(createRequest);
            LOGGER.info(SECRET_CREATION_RESPONSE, createSecretResponse);

            PluginResponse configUpdateResponse = updateConfigProperty(REDHAT_ENABLED, Boolean.TRUE.toString(), JOB_SCHEDULER);

            if (configUpdateResponse.getStatus().equals(AdminConstants.FAILURE)) {
                LOGGER.error(FAILED_TO_UPDATE_CONFIG_MSG, configUpdateResponse.getErrorDetails());
            }

            createResponse.setStatus(AdminConstants.SUCCESS);
            createResponse.setMessage(String.format(ACCOUNT_ADDED_SUCCESSFULLY, request.getRedhatAccountId()));
        } catch (ResourceExistsException ree) {
            LOGGER.error(String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + ree.getMessage());
            deleteSecret(request.getRedhatAccountId());
            deleteAccountFromDB(request.getRedhatAccountId());
            createResponse = new PluginResponse();
            createResponse.setStatus(AdminConstants.FAILURE);
            createResponse.setMessage(String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + SECRET_EXISTS);
            createResponse.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + ree.getMessage());
        } catch (Exception e) {
            LOGGER.error(String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + e.getMessage());
            deleteAccountFromDB(request.getRedhatAccountId());
            createResponse = new PluginResponse();
            createResponse.setStatus(AdminConstants.FAILURE);
            createResponse.setMessage(String.format(ERROR_MESSAGE_TEMPLATE, CREATING));
            createResponse.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE, CREATING) + e.getMessage());
        }

        return createResponse;
    }

    @Override
    @Transactional
    public PluginResponse deletePlugin(String accountId) {
        LOGGER.info(DELETING_ACCOUNT);
        PluginResponse response;
        try {
            deleteSecret(accountId);
            response = deleteAccountFromDB(accountId);
            List<AccountDetails> onlineAccounts = findOnlineAccounts(STATUS_CONFIGURED, Constants.REDHAT);

            if (onlineAccounts == null || onlineAccounts.isEmpty()) {
                LOGGER.debug(ACCOUNTS_DELETED_MSG);
                PluginResponse configUpdateResponse = updateConfigProperty(REDHAT_ENABLED, Boolean.FALSE.toString(), JOB_SCHEDULER);

                if (configUpdateResponse.getStatus().equals(AdminConstants.FAILURE)) {
                    LOGGER.error(FAILED_TO_UPDATE_CONFIG_MSG, configUpdateResponse.getErrorDetails());
                }
            }
        } catch (Exception e) {
            response = new PluginResponse();
            response.setStatus(AdminConstants.FAILURE);
            response.setMessage(String.format(ERROR_MESSAGE_TEMPLATE, DELETING));
            response.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE, DELETING) + e.getMessage());
            LOGGER.error(String.format(ERROR_MESSAGE_TEMPLATE, DELETING));
            LOGGER.error(String.format(ERROR_MESSAGE_TEMPLATE, DELETING) + e.getMessage());
        }
        return response;
    }

    private void deleteSecret(String accountId) {
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        Regions region = Regions.fromName(System.getenv(REGION));
        String roleName = System.getenv(ROLE_NAME);
        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        DeleteSecretRequest deleteRequest = new DeleteSecretRequest().withSecretId(secretManagerPrefix +
                FORWARD_SLASH + roleName + FORWARD_SLASH + Constants.REDHAT +
                FORWARD_SLASH + accountId).withForceDeleteWithoutRecovery(true);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info(DELETE_SECRET_RESPONSE, deleteResponse);
    }

    @Override
    public PluginResponse validate(RedHatPluginRequest accountData) {
        LOGGER.info(VALIDATING_MSG);

        PluginResponse validationResponse = validateRedhatPluginRequest(accountData);

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

    private PluginResponse validateRedhatPluginRequest(RedHatPluginRequest pluginData) {
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
            response.setMessage(INVALID_PLUGIN_REQUEST_MSG);
            response.setErrorDetails(validationError);
        } else {
            response.setStatus(AdminConstants.SUCCESS);
        }

        return response;
    }

    @Override
    public final List<AccountDetails> findOnlineAccounts(String status, String platform) {
        return super.findOnlineAccounts(status, platform);
    }

}

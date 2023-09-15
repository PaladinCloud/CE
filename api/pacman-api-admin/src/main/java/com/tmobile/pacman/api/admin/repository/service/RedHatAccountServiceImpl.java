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
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.List;

@Service
public class RedHatAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedHatAccountServiceImpl.class);

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    CredentialProvider credentialProvider;
    private static final String ERROR_MESSAGE_TEMPLATE = "Error in %s account. ";
    private static final String VALIDATING = "validating";
    private static final String CREATING = "creating";
    private static final String DELETING = "deleting";
    private static final String HTTP_STATUS_CODE_TEMPLATE = ", Got HTTP Status Code: %d";
    private static final String VALIDATING_REDHAT_DETAILS = "Validating REDHAT details";
    private static final String VALIDATION_FAILED_DUE_TO_MISSING_PARAMETERS = "Validation failed due to missing parameters";
    private static final String VALIDATION_SUCCESSFUL = "Validation successful";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String TOKEN_VALIDATION_FAILED = "Validation failed due to invalid token";
    private static final String ACCOUNT_ID_VALIDATION_FAILED = "Validation failed due to invalid accountId";
    private static final String ADDING_ACCOUNT = "Adding redhat account";
    private static final String DELETING_ACCOUNT = "Deleting redhat account";
    private static final String ADDING_ACCOUNT_SUCCESS_MESSAGE = "Account added successfully. Account id: %s";
    private static final String REDHAT_ENABLED = "redhat.enabled";
    private static final String SECRET_RESPONSE = "CreateSecret response: {}";
    private static final String FORWARD_SLASH = "/";
    private static final String SUMMARY_COUNTS_URL_PATH = "/v1/summary/counts";
    private static final String ACCOUNT_EXISTS = "Account already exists";
    private static final String REGION = "REGION";
    private static final String PARAM_ACCOUNT_ID = "redhatAccountId";
    private static final String PARAM_ACCOUNT_NAME = "redhatAccountName";
    private static final String PARAM_TOKEN = "redhatToken";
    private static final String DELETE_SECRET_RESPONSE = "Delete secret response: {} ";
    private static final String ACCOUNTS_DELETED_MSG = "Redhat accounts are deleted, disabling redhat enable flag";
    private static final String REDHAT_TOKEN_TEMPLATE = "{\"token\":\"%s\"}";
    private static final String REDHAT_URL_TEMPLATE = "https://acs-%s.acs.rhcloud.com";
    private static final String MISSING_MANDATORY_PARAMETER = "%s cannot be null or empty ";

    @Override
    public String serviceType() {
        return Constants.REDHAT;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        LOGGER.info(VALIDATING_REDHAT_DETAILS);
        AccountValidationResponse accountResponse = validateRequest(accountData);
        if (accountResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info(VALIDATION_FAILED_DUE_TO_MISSING_PARAMETERS);
            return accountResponse;
        }
        String apiUrl = String.format(REDHAT_URL_TEMPLATE, accountData.getRedhatAccountId()) + SUMMARY_COUNTS_URL_PATH;
        String redhatToken = accountData.getRedhatToken();

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(apiUrl);

        // Set the access token in the Authorization header
        request.setHeader(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + redhatToken);

        try {
            // Make the API request
            HttpResponse response = httpClient.execute(request);
            // Get the HTTP status code
            int statusCode = response.getStatusLine().getStatusCode();

            // Create an AccountValidationResponse based on the status code
            if (statusCode == 200) {
                accountResponse.setValidationStatus(SUCCESS);
                accountResponse.setMessage(VALIDATION_SUCCESSFUL);
                LOGGER.info(VALIDATION_SUCCESSFUL);
            } else {
                accountResponse.setValidationStatus(FAILURE);
                accountResponse.setMessage(TOKEN_VALIDATION_FAILED);
                accountResponse.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE +
                        HTTP_STATUS_CODE_TEMPLATE, VALIDATING, statusCode));
                LOGGER.info(VALIDATION_FAILED);
                LOGGER.info(String.format(ERROR_MESSAGE_TEMPLATE +
                        HTTP_STATUS_CODE_TEMPLATE, VALIDATING, statusCode));
            }
        } catch (UnknownHostException e) {
            accountResponse.setValidationStatus(FAILURE);
            accountResponse.setMessage(ACCOUNT_ID_VALIDATION_FAILED);
            accountResponse.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), VALIDATING));
            LOGGER.info(ACCOUNT_ID_VALIDATION_FAILED);
            LOGGER.info(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), VALIDATING));
        } catch (Exception e) {
            accountResponse.setValidationStatus(FAILURE);
            accountResponse.setMessage(VALIDATION_FAILED);
            accountResponse.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), VALIDATING));
            LOGGER.info(VALIDATION_FAILED);
            LOGGER.info(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), VALIDATING));
        }
        return accountResponse;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info(VALIDATING_REDHAT_DETAILS);
        if (accountData.getRedhatAccountName() == null || accountData.getRedhatAccountName().isEmpty()) {
            accountData.setRedhatAccountName(accountData.getRedhatAccountId());
        }
        AccountValidationResponse accountResponse = validateRequest(accountData);
        if (accountResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info(VALIDATION_FAILED_DUE_TO_MISSING_PARAMETERS);
            return accountResponse;
        }
        LOGGER.info(ADDING_ACCOUNT);
        AccountValidationResponse validateResponse;
        try {
            validateResponse = createAccountInDb(accountData.getRedhatAccountId(), accountData.getRedhatAccountName(),
                    Constants.REDHAT, accountData.getCreatedBy());

            if (validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
                LOGGER.info(ACCOUNT_EXISTS);
                return validateResponse;
            }
            BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
            Regions region = Regions.fromName(System.getenv(REGION));
            String roleName = System.getenv(PALADINCLOUD_RO);
            AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region).build();

            CreateSecretRequest createRequest = new CreateSecretRequest()
                    .withName(secretManagerPrefix + FORWARD_SLASH + roleName + FORWARD_SLASH + serviceType() +
                            FORWARD_SLASH + accountData.getRedhatAccountId())
                    .withSecretString(String.format(REDHAT_TOKEN_TEMPLATE, accountData.getRedhatToken()));

            CreateSecretResult createResponse = secretClient.createSecret(createRequest);
            LOGGER.info(SECRET_RESPONSE, createResponse);
            //update redhat enable flag for scheduler job
            updateConfigProperty(REDHAT_ENABLED, TRUE, JOB_SCHEDULER);
            validateResponse.setValidationStatus(SUCCESS);
            validateResponse.setMessage(String.format(ADDING_ACCOUNT_SUCCESS_MESSAGE, accountData.getRedhatAccountId()));
            validateResponse.setAccountName(accountData.getRedhatAccountName());
        } catch (Exception e) {
            deleteAccountFromDB(accountData.getRedhatAccountId());
            validateResponse = new AccountValidationResponse();
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage(String.format(ERROR_MESSAGE_TEMPLATE, CREATING));
            validateResponse.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), CREATING));
            LOGGER.info(String.format(ERROR_MESSAGE_TEMPLATE, CREATING));
            LOGGER.info(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), CREATING));
        }
        return validateResponse;
    }

    @Override
    public AccountValidationResponse deleteAccount(String accountId) {
        LOGGER.info(DELETING_ACCOUNT);
        AccountValidationResponse response;
        try {
            BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
            Regions region = Regions.fromName(System.getenv(REGION));
            String roleName = System.getenv(PALADINCLOUD_RO);
            AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region).build();
            DeleteSecretRequest deleteRequest = new DeleteSecretRequest().withSecretId(secretManagerPrefix +
                    FORWARD_SLASH + roleName + FORWARD_SLASH + serviceType() +
                    FORWARD_SLASH + accountId).withForceDeleteWithoutRecovery(true);
            DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
            LOGGER.info(DELETE_SECRET_RESPONSE, deleteResponse);
            //delete entry from db
            response = deleteAccountFromDB(accountId);
            response.setType(Constants.REDHAT);
            List<AccountDetails> onlineAccounts = findOnlineAccounts(STATUS_CONFIGURED, Constants.REDHAT);
            if (onlineAccounts == null || onlineAccounts.isEmpty()) {
                LOGGER.debug(ACCOUNTS_DELETED_MSG);
                updateConfigProperty(REDHAT_ENABLED, FALSE, JOB_SCHEDULER);
            }
        } catch (Exception e) {
            response = new AccountValidationResponse();
            response.setValidationStatus(FAILURE);
            response.setMessage(String.format(ERROR_MESSAGE_TEMPLATE, DELETING));
            response.setErrorDetails(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), DELETING));
            LOGGER.info(String.format(ERROR_MESSAGE_TEMPLATE, DELETING));
            LOGGER.info(String.format(ERROR_MESSAGE_TEMPLATE + e.getMessage(), DELETING));
        }
        return response;
    }

    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response = new AccountValidationResponse();
        StringBuilder validationErrorDetails = new StringBuilder();
        if (accountData.getRedhatAccountId() == null || accountData.getRedhatAccountId().isEmpty()) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, PARAM_ACCOUNT_ID));
        }

        if (accountData.getRedhatAccountName() != null && accountData.getRedhatAccountName().trim().isEmpty()) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, PARAM_ACCOUNT_NAME));
        }

        if (accountData.getRedhatToken() == null || accountData.getRedhatToken().isEmpty()) {
            validationErrorDetails.append(String.format(MISSING_MANDATORY_PARAMETER, PARAM_TOKEN));
        }
        String validationError = validationErrorDetails.toString();
        if (!validationError.isEmpty()) {
            response.setErrorDetails(validationError);
            response.setValidationStatus(FAILURE);
        } else {
            response.setValidationStatus(SUCCESS);
        }
        return response;
    }
}

package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TenableAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(TenableAccountServiceImpl.class);

    private static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    private static final String FAILURE = "FAILURE";
    private static final String SUCCESS = "SUCCESS";
    private static final String TENABLE = "tenable";

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;


    @Value("${tenable.user-agent}")
    private String tenableUserAgent;


    @Autowired
    CredentialProvider credentialProvider;
    public static final String TENABLE_ENABLED = "tenable.enabled";
    @Override
    public String serviceType() {
        return TENABLE;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        AccountValidationResponse validateResponse= validateRequestData(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }

        return validateAccountCredentials(accountData);
    }

    private AccountValidationResponse validateAccountCredentials(CreateAccountRequest accountData) {
        AccountValidationResponse validationResponse = new AccountValidationResponse();
        // Requesting scan that doesn't exist to validate the account data.
        HttpGet request = new HttpGet(Constants.TENABLE_API_URL + "/scans/0");
        String apiKey = "accessKey=" + accountData.getTenableAccessKey() + ";secretKey=" + accountData.getTenableSecretKey() + ";";
        request.addHeader("X-ApiKeys", apiKey);
        request.addHeader("content-type", "application/json");
        request.addHeader("cache-control", "no-cache");
        request.addHeader("Accept", "application/json");
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            CloseableHttpResponse response = httpClient.execute(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 401) {
                // If the response is 401, then the account data is not valid.
                validationResponse.setValidationStatus(FAILURE);
                validationResponse.setMessage("Tenable API keys validation failed.");
            } else {
                validationResponse.setValidationStatus(SUCCESS);
                validationResponse.setMessage("Tenable account validation passed");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to validate the tenable account ", e.getMessage());
            validationResponse.setValidationStatus(FAILURE);
            validationResponse.setMessage("Tenable account validation failed");
        }

        return validationResponse;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Adding new Tenable account....");
        AccountValidationResponse validateResponse;

        if (getListAccountsByPlatform(TENABLE).size() > 0) {
            LOGGER.info("Tenable account already exists");
            validateResponse = new AccountValidationResponse();
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Tenable account already exists");
            return validateResponse;
        }

        validateResponse = validate(accountData);
        if (validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)) {
            LOGGER.info("Adding account failed: {}", validateResponse.getMessage());
            return validateResponse;
        }
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();

        String secretId = getTenableSecretId();
        ListSecretsRequest listSecretsRequest = new ListSecretsRequest()
                .withFilters(new Filter().withKey("name").withValues(secretId))
                .withIncludePlannedDeletion(true);
        ListSecretsResult getListResponse = secretClient.listSecrets(listSecretsRequest);

        if (getListResponse.getSecretList().size() > 0) {
            LOGGER.info("Secret already exists. Deleting in progress.");
            validateResponse = new AccountValidationResponse();
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Prior account is being deleted. Try again in a couple of minutes.");
            return validateResponse;
        }

        CreateSecretRequest createRequest = new CreateSecretRequest()
                .withName(secretId).withSecretString(getTenableSecretString(accountData));
        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}", createResponse);


        String accountId = UUID.randomUUID().toString();
        createAccountInDb(accountId,"Tenable-Connector", TENABLE,accountData.getCreatedBy());

        updateConfigProperty(TENABLE_ENABLED,TRUE,JOB_SCHEDULER);
        validateResponse = new AccountValidationResponse();
        validateResponse.setValidationStatus(SUCCESS);
        validateResponse.setAccountId(accountId);
        validateResponse.setAccountName("Tenable-Connector");
        validateResponse.setType(TENABLE);
        validateResponse.setMessage("Account added successfully. ID: " + accountId);

        return validateResponse;
    }

    private AccountValidationResponse validateRequestData(CreateAccountRequest accountData) {
        AccountValidationResponse response = new AccountValidationResponse();
        List<String> missingParams = new ArrayList<>();
        String tenableAccessKey = accountData.getTenableAccessKey();
        String tenableSecretKey = accountData.getTenableSecretKey();

        if (StringUtils.isEmpty(tenableAccessKey)) {
            missingParams.add("Access Key");
        }

        if (StringUtils.isEmpty(tenableSecretKey)) {
            missingParams.add("Secret Key");
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
    public AccountValidationResponse deleteAccount(String accountId) {
        LOGGER.info("Inside deleteAccount method of TenableAccountServiceImpl. AccountId: {}",accountId);
        AccountValidationResponse response=new AccountValidationResponse();

        //find and delete cred file for account
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();

        DeleteSecretRequest deleteRequest=new DeleteSecretRequest().withSecretId(getTenableSecretId()).withForceDeleteWithoutRecovery(true);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ",deleteResponse);

        //delete entry from db
        deleteAccountFromDB(accountId);
        updateConfigProperty(TENABLE_ENABLED,FALSE,JOB_SCHEDULER);
        response.setType(TENABLE);
        response.setAccountId(accountId);
        response.setValidationStatus(SUCCESS);
        response.setMessage("Account deleted successfully");

        return response;
    }

    private String getTenableSecretString(CreateAccountRequest accountRequest){
        String template="{\"accessKey\":\"%s\",\"secretKey\":\"%s\",\"apiURL\":\"%s\",\"userAgent\":\"%s\"}";
        return String.format(template,accountRequest.getTenableAccessKey(),
                accountRequest.getTenableSecretKey(),
                Constants.TENABLE_API_URL,
                tenableUserAgent);
    }

    private String getTenableSecretId() {
        String roleName = System.getenv(PALADINCLOUD_RO);
        String secretId = secretManagerPrefix + "/" + roleName + "/tenable";
        return secretId;
    }

}

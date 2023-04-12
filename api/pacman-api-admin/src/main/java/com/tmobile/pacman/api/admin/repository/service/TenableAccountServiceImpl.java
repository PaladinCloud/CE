package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
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

@Service
public class TenableAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(TenableAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    CredentialProvider credentialProvider;
    @Override
    public String serviceType() {
        return Constants.TENABLE;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        AccountValidationResponse validateResponse=validateRequest(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        validateTenableAccount(accountData, validateResponse);
        return validateResponse;
    }

    private void validateTenableAccount(CreateAccountRequest accountData, AccountValidationResponse validateResponse) {


        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(accountData.getTenableAPIUrl());
        String apiKey = "accessKey="+accountData.getTenableAccessKey()+";secretKey="+accountData.getTenableSecretKey()+";";
        request.addHeader("X-ApiKeys", apiKey);
        request.addHeader("content-type", "application/json");
        request.addHeader("cache-control", "no-cache");
        request.addHeader("Accept", "application/json");
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            if(response.getEntity() != null && response.getStatusLine().getStatusCode()==200){
                    validateResponse.setValidationStatus(SUCCESS);
                    validateResponse.setMessage("Tenable validation successful");
            }
            else{
                    validateResponse.setValidationStatus(FAILURE);
                    validateResponse.setErrorDetails("API returned status code : "+response.getStatusLine().getStatusCode());
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to validate the tenable account ",e);
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Tenable validation Failed");
        } catch (IOException e) {
            LOGGER.error("Failed to validate the tenable account ",e.getMessage());
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Tenable validation Failed: "+e.getMessage());
        }
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Adding new Tenable account....");
        AccountValidationResponse validateResponse=validateRequest(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();

        CreateSecretRequest createRequest=new CreateSecretRequest()
                .withName(secretManagerPrefix+"/tenable").withSecretString(getTenableSecret(accountData));

        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}",createResponse);
        String accountId = UUID.randomUUID().toString();
        createAccountInDb(accountId,"Tenable-Connector", Constants.TENABLE);

        validateResponse.setValidationStatus(SUCCESS);
        validateResponse.setMessage("Account added successfully. Account id: "+accountId);
        return validateResponse;
    }

    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response=new AccountValidationResponse();
        StringBuilder validationErrorDetails=new StringBuilder();
        String tenableAPIUrl= accountData.getTenableAPIUrl();
        String tenableAccessKey= accountData.getTenableAccessKey();
        String tenableSecretKey= accountData.getTenableSecretKey();

        if(StringUtils.isEmpty(tenableAccessKey)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Tenable Access Key\n");
        }
        if(StringUtils.isEmpty(tenableSecretKey)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Tenable Secret Key\n");
        }
        if(StringUtils.isEmpty(tenableAPIUrl)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Tenable API URl\n");
        }
        String validationError=validationErrorDetails.toString();
        if(!validationError.isEmpty()){
            validationError=validationError.replace("\n","");
            response.setErrorDetails(validationError);
            response.setValidationStatus(FAILURE);
        }else {
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
        String secretId=secretManagerPrefix+"/tenable";
        DeleteSecretRequest deleteRequest=new DeleteSecretRequest().withSecretId(secretId).withForceDeleteWithoutRecovery(true);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ",deleteResponse);

        //delete entry from db
        deleteAccountFromDB(accountId);

        response.setType(Constants.TENABLE);
        response.setAccountId(accountId);
        response.setValidationStatus(SUCCESS);
        response.setMessage("Account deleted successfully");

        return response;
    }

    private String getTenableSecret(CreateAccountRequest accountRequest){
        String template="{\"accessKey\":\"%s\",\"secretKey\":\"%s\",\"apiURL\":\"%s\"}";
        return String.format(template,accountRequest.getTenableAccessKey(),accountRequest.getTenableSecretKey(),accountRequest.getTenableAPIUrl());
    }
}

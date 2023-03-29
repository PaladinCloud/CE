package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.google.gson.JsonObject;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AquaAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(AquaAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    CredentialProvider credentialProvider;
    @Override
    public String serviceType() {
        return Constants.AQUA;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        AccountValidationResponse validateResponse=validateRequest(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        validateAquaAccount(accountData, validateResponse);
        return validateResponse;
    }

    private void validateAquaAccount(CreateAccountRequest accountData, AccountValidationResponse validateResponse) {


        validateResponse.setAquaUser(accountData.getQualysApiUser());
        validateResponse.setAquaApiUrl(accountData.getAquaApiUrl());

        String tokenUri = accountData.getAquaApiUrl() + "/v2/signin";
        JsonObject inputObject = new JsonObject();
        inputObject.addProperty("email", accountData.getAquaApiUser());
        inputObject.addProperty("password", accountData.getAquaApiPassword());
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(tokenUri);
        httpPost.addHeader("content-type", "application/xml");
        httpPost.addHeader("cache-control", "no-cache");
        httpPost.addHeader("Accept", "application/json");

        StringEntity input = null;
        try {
            input = new StringEntity(inputObject.toString());
            httpPost.setEntity(input);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            if(response.getEntity() != null && response.getStatusLine().getStatusCode()==200){
                    validateResponse.setValidationStatus(SUCCESS);
                    validateResponse.setMessage("Aqua validation successful");
            }
            else{
                    validateResponse.setValidationStatus(FAILURE);
                    validateResponse.setErrorDetails("API returned status code : "+response.getStatusLine().getStatusCode());
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to validate the aqua account ",e);
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Aqua validation successful");
        } catch (IOException e) {
            LOGGER.error("Failed to validate the aqua account "+e.getMessage());
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Aqua validation successful: "+e.getMessage());

        }
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Adding new Aqua account....");
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
                .withName(secretManagerPrefix+"/aqua").withSecretString(getAquaSecret(accountData));

        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}",createResponse);
        String accountId = UUID.randomUUID().toString();
        createAccountInDb(accountId,"Aqua-Connector", Constants.AQUA);

        validateResponse.setValidationStatus(SUCCESS);
        validateResponse.setMessage("Account added successfully. Account id: "+accountId);
        return validateResponse;
    }

    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response=new AccountValidationResponse();
        StringBuilder validationErrorDetails=new StringBuilder();
        String aquaApiUrl= accountData.getAquaApiUrl();
        String aquaClientDomainUrl= accountData.getAquaClientDomainUrl();
        String aquaApiUser=accountData.getAquaApiUser();
        String aquaApiPassword= accountData.getAquaApiPassword();
        if(StringUtils.isEmpty(aquaApiUrl)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Aqua API URL\n");
        }
        if(StringUtils.isEmpty(aquaClientDomainUrl)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Aqua client domain URL\n");
        }
        if(StringUtils.isEmpty(aquaApiUser)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Aqua API User\n");
        }
        if(aquaApiPassword==null){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Aqua API password\n");
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
        LOGGER.info("Inside deleteAccount method of AquaAccountServiceImpl. AccountId: {}",accountId);
        AccountValidationResponse response=new AccountValidationResponse();

        //find and delete cred file for account
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        String secretId=secretManagerPrefix+"/aqua";
        DeleteSecretRequest deleteRequest=new DeleteSecretRequest().withSecretId(secretId);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ",deleteResponse);

        //delete entry from db
        deleteAccountFromDB(accountId);

        response.setType(Constants.AQUA);
        response.setAccountId(accountId);
        response.setValidationStatus(SUCCESS);
        response.setMessage("Account deleted successfully");

        return response;
    }

    private String getAquaSecret(CreateAccountRequest accountRequest){
        String template="{\"aquaApiUrl\":\"%s\"\"aquaClientDomainUrl\":\"%s\"\"apiusername\":\"%s\",\"apipassword\":\"%s\"}";
        return String.format(template,accountRequest.getAquaApiUrl(),accountRequest.getAquaClientDomainUrl(),accountRequest.getAquaApiUser()
                ,accountRequest.getAquaApiPassword());

    }
}

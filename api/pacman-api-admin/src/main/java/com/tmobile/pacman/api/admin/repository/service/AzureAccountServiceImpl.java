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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AzureAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(AzureAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    CredentialProvider credentialProvider;
    @Override
    public String serviceType() {
        return Constants.AZURE;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        return null;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Inside addAccount method of AzureAccountServiceImpl");
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

        String tenantId = accountData.getTenantId();
        String tenantName=accountData.getTenantName();
        CreateSecretRequest createRequest=new CreateSecretRequest()
                .withName(secretManagerPrefix+"/azure/"+ tenantId).withSecretString(getAzureSecretData(accountData.getTenantSecretData()));

        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}",createResponse);
        createAccountInDb(tenantId,tenantName, Constants.AZURE);

        validateResponse.setValidationStatus(SUCCESS);
        validateResponse.setMessage("Account added successfully. Tenant id: "+tenantId);
        return validateResponse;
    }

    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response=new AccountValidationResponse();
        StringBuilder validationErrorDetails=new StringBuilder();
        String tenantId=accountData.getTenantId();
        String tenantName= accountData.getTenantName();
        String tenantSecretData=accountData.getTenantSecretData();

        if(StringUtils.isEmpty(tenantId)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Tenant Id\n");
        }
        if(StringUtils.isEmpty(tenantName)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Tenant Name\n");
        }
        if(tenantSecretData==null){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Tenant Secret Data\n");
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
    public AccountValidationResponse deleteAccount(String tenantId) {
        LOGGER.info("Inside deleteAccount method of AzureAccountServiceImpl. Tenant id: {}",tenantId);
        AccountValidationResponse response=new AccountValidationResponse();

        //find and delete cred file for account
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        String secretId=secretManagerPrefix+"/azure/"+tenantId;
        DeleteSecretRequest deleteRequest=new DeleteSecretRequest().withSecretId(secretId);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ",deleteResponse);

        //delete entry from db
        deleteAccountFromDB(tenantId);

        response.setAccountId(tenantId);
        response.setType(Constants.AZURE);
        response.setMessage("Account deleted successfully");
        response.setValidationStatus(SUCCESS);
        return response;
    }
    private String getAzureSecretData(String secret){
        String jsonTemplate="{\"secretdata\": \"%s\"}";
        return String.format(jsonTemplate,secret);
    }
}

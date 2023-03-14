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
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
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
        return null;
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

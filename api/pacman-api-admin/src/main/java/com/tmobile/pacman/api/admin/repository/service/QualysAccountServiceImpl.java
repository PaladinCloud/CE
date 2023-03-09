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

import java.util.UUID;

@Service
public class QualysAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(QualysAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    CredentialProvider credentialProvider;
    @Override
    public String serviceType() {
        return Constants.QUALYS;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        return null;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Inside addAccount method of QualysAccountServiceImpl");
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
                .withName(secretManagerPrefix+"/qualys").withSecretString(getQualysSecret(accountData));

        CreateSecretResult createResponse = secretClient.createSecret(createRequest);
        LOGGER.info("Create secret response: {}",createResponse);
        String accountId = UUID.randomUUID().toString();
        createAccountInDb(accountId,"Qualys-Connector", Constants.QUALYS);

        validateResponse.setValidationStatus(SUCCESS);
        validateResponse.setMessage("Account added successfully. Account id: "+accountId);
        return validateResponse;
    }

    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response=new AccountValidationResponse();
        StringBuilder validationErrorDetails=new StringBuilder();
        String qualysApiUrl= accountData.getQualysApiUrl();
        String qualysApiUser=accountData.getQualysApiUser();
        String qualysApiPassword= accountData.getQualysApiPassword();
        if(StringUtils.isEmpty(qualysApiUrl)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Qualys API URL\n");
        }
        if(StringUtils.isEmpty(qualysApiUser)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Qualys API User\n");
        }
        if(qualysApiPassword==null){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Qualys API password\n");
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
        LOGGER.info("Inside deleteAccount method of QualysAccountServiceImpl. AccountId: {}",accountId);
        AccountValidationResponse response=new AccountValidationResponse();

        //find and delete cred file for account
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        String region = System.getenv("REGION");

        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
        String secretId=secretManagerPrefix+"/qualys";
        DeleteSecretRequest deleteRequest=new DeleteSecretRequest().withSecretId(secretId);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ",deleteResponse);

        //delete entry from db
        deleteAccountFromDB(accountId);

        response.setType(Constants.QUALYS);
        response.setAccountId(accountId);
        response.setValidationStatus(SUCCESS);
        response.setMessage("Account deleted successfully");

        return response;
    }

    private String getQualysSecret(CreateAccountRequest accountRequest){
        String template="{\"qualysApiUrl\":\"%s\"\"apiusername\":\"%s\",\"apipassword\":\"%s\"}";
        return String.format(template,accountRequest.getQualysApiUrl(),accountRequest.getQualysApiUser()
                ,accountRequest.getQualysApiPassword());

    }
}

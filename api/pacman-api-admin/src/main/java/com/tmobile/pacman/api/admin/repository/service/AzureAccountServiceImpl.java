package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.Azure.Authenticated;
import com.microsoft.azure.management.resources.Subscription;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class AzureAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(AzureAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";

    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";
    public static final String SECRET_ALREADY_EXIST_FOR_ACCOUNT = "Secret already exist for account";


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
        LOGGER.info("Inside validate method of AzureAccountServiceImpl");

        AccountValidationResponse response=new AccountValidationResponse();
        response.setTenantId(accountData.getTenantId());
        response.setType(Constants.AZURE);
        String tenant=accountData.getTenantId();
        String secretData=accountData.getTenantSecretData();
        if(StringUtils.isEmpty(tenant) || StringUtils.isEmpty(secretData)){
            LOGGER.error("Tenant Id or secretData missing");
            response.setValidationStatus(FAILURE);
            response.setMessage("Missing mandatory parameter- Tenant Id or secretData");
            response.setErrorDetails("Missing mandatory parameter- secretData or Tenant Id");
            return response;
        }
        try {
            Map<String,Map<String,String>> credsMap = new HashMap<>();
            Arrays.asList(secretData.split("##")).stream().forEach(cred-> {
                Map<String,String> credInfoMap = new HashMap<>();
                Arrays.asList(cred.split(",")).stream().forEach(str-> credInfoMap.put(str.split(":")[0],str.split(":")[1]));
                credsMap.put(credInfoMap.get("tenant"), credInfoMap);
            });

            String clientId = credsMap.get(tenant).get("clientId");
            String secret = credsMap.get(tenant).get("secretId");

            ApplicationTokenCredentials applicationCreds = new ApplicationTokenCredentials(clientId, tenant, secret, AzureEnvironment.AZURE);
            LOGGER.debug("Application creds:{}", applicationCreds);

            Authenticated azureAuthenticated = Azure.authenticate(applicationCreds);

            LOGGER.debug("Azure authenticated :{}", azureAuthenticated);
            PagedList<Subscription> subscriptions = azureAuthenticated.subscriptions().list();
            LOGGER.debug("Subscriptions fetched :{}", subscriptions);
            response.setValidationStatus(SUCCESS);
            response.setMessage("Azure account validation successful");
        }catch (Exception e){
            LOGGER.error("Tenant Id or secretData missing");
            response.setValidationStatus(FAILURE);
            response.setMessage("Account validation failed.");
            response.setErrorDetails(e.getMessage()!=null?e.getMessage():"Account validation failed.");
        }
        return response;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Inside addAccount method of AzureAccountServiceImpl");
        AccountValidationResponse validateResponse=validateRequest(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        String tenantId = accountData.getTenantId();
        String tenantName=accountData.getTenantName();
        validateResponse=createAccountInDb(tenantId,tenantName, Constants.AZURE);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Account already exists");
            return validateResponse;
        }
        try {
            BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
            String region = System.getenv("REGION");
            String roleName= System.getenv(PALADINCLOUD_RO);
            AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region).build();

            CreateSecretRequest createRequest = new CreateSecretRequest()
                    .withName(secretManagerPrefix+"/"+roleName+ "/azure/" + tenantId).withSecretString(getSecretData(accountData.getTenantSecretData()));

            CreateSecretResult createResponse = secretClient.createSecret(createRequest);
            LOGGER.info("Create secret response: {}", createResponse);
            //update azure enable flag for scheduler job
            String key="azure.enabled";
            String value = "true";
            String application = "job-scheduler";
            updateConfigProperty(key, value, application);
            validateResponse.setValidationStatus(SUCCESS);
            validateResponse.setMessage("Account added successfully. Tenant id: "+tenantId);

        }catch (ResourceExistsException e){
            LOGGER.error(SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage(SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            validateResponse.setErrorDetails(e.getMessage()!=null?e.getMessage(): SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            //Delete the entry from DB
            deleteAccountFromDB(tenantId);
        }
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
        try{
            //find and delete cred file for account
            response.setType(Constants.AZURE);
            response.setTenantId(tenantId);
            response.setValidationStatus(SUCCESS);
            response.setMessage("Account deleted successfully");
            BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
            String region = System.getenv("REGION");
            String roleName= System.getenv(PALADINCLOUD_RO);
            AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region).build();
            String secretId=secretManagerPrefix+"/"+roleName+"/azure/"+tenantId;

            DeleteSecretRequest deleteRequest=new DeleteSecretRequest().withSecretId(secretId).withForceDeleteWithoutRecovery(true);
            DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
            LOGGER.info("Delete secret response: {} ",deleteResponse);

            //delete entry from db
            response=deleteAccountFromDB(tenantId);
            response.setType(Constants.AZURE);
        } catch (SdkClientException e) {
            LOGGER.error("Error in deleting the creds file json: {}", e.getMessage());
            response.setValidationStatus(FAILURE);
            response.setErrorDetails(e.getMessage());
            response.setMessage("Account deletion failed");
        }
        return response;
    }

}

package com.tmobile.pacman.api.admin.repository.service.accounts;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.amazonaws.services.securitytoken.model.AWSSecurityTokenServiceException;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.Azure.Authenticated;
import com.microsoft.azure.management.resources.Subscription;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.admin.repository.AzureAccountRepository;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AzureAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService {

    private static final Logger LOGGER= LoggerFactory.getLogger(AzureAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";

    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";
    public static final String SECRET_ALREADY_EXIST_FOR_ACCOUNT = "Secret already exist for account";
    public static final String AZURE_ENABLED = "azure.enabled";
    @Autowired
    AzureAccountRepository azureAccountRepository;

    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    @Autowired
    CredentialProvider credentialProvider;
    @Autowired
    PacmanRdsRepository pacmanRdsRepository;

    @Override
    public String serviceType() {
        return Constants.AZURE;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        LOGGER.info("Inside validate method of AzureAccountServiceImpl");
        AccountValidationResponse response=new AccountValidationResponse();
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
            response.setTenantId(accountData.getTenantId());
            response.setType(Constants.AZURE);
            PagedList<Subscription> subscriptionsList = authenticateAzureAccount(accountData);
            if(subscriptionsList.isEmpty()){
                LOGGER.error("No subscriptions present for this azure account. Hence account is invalid.");
                response.setValidationStatus(FAILURE);
                response.setMessage("Account validation failed.");
                response.setErrorDetails("There must be at least one subscription linked to the app registration. Please grant the reader access to at least one subscription.");
            }
            else{
                response.setValidationStatus(SUCCESS);
                response.setMessage("Azure account validation successful");
            }
        }catch (Exception e){
            LOGGER.error("Tenant Id or secretData missing");
            response.setValidationStatus(FAILURE);
            response.setMessage("Account validation failed.");
            response.setErrorDetails(e.getMessage()!=null?e.getMessage():"Account validation failed.");
        }
        return response;
    }

    private static PagedList<Subscription> authenticateAzureAccount(CreateAccountRequest accountData) {
        String tenant=accountData.getTenantId();
        String secretData=accountData.getTenantSecretData();
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
        return subscriptions;
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
        validateResponse=createAccountInDb(tenantId,tenantId, Constants.AZURE,accountData.getCreatedBy());

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
            updateConfigProperty(AZURE_ENABLED, TRUE, JOB_SCHEDULER);
            validateResponse.setValidationStatus(SUCCESS);
            validateResponse.setMessage("Account added successfully. Tenant id: "+tenantId);
            PagedList<Subscription> subscriptions = authenticateAzureAccount(accountData);
            String query="INSERT IGNORE INTO cf_AzureTenantSubscription (tenant,subscription,subscriptionName) VALUES (?,?,?)";
            for(Subscription subscription : subscriptions){
                pacmanRdsRepository.update(query,subscription.tenantId(), subscription.subscriptionId(),subscription.displayName());
            }
        }catch (ResourceExistsException e){
            LOGGER.error(SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage(SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            validateResponse.setErrorDetails(e.getMessage()!=null?e.getMessage(): SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            //Delete the entry from DB
            deleteAccountFromDB(tenantId);
        }catch (AWSSecurityTokenServiceException e){
            LOGGER.error(ERROR_IN_ASSUMING_STS_FOR_BASE_ACCOUNT_ROLE);
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage(ERROR_IN_ASSUMING_STS_FOR_BASE_ACCOUNT_ROLE);
            validateResponse.setErrorDetails(e.getMessage()!=null?e.getMessage(): ERROR_IN_ASSUMING_STS_FOR_BASE_ACCOUNT_ROLE);
            //Delete the entry from DB
            deleteAccountFromDB(tenantId);
        }
        return validateResponse;
    }

    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response=new AccountValidationResponse();
        StringBuilder validationErrorDetails=new StringBuilder();
        String tenantId=accountData.getTenantId();
        String tenantSecretData=accountData.getTenantSecretData();

        if(StringUtils.isEmpty(tenantId)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Tenant Id\n");
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
    public AccountValidationResponse deleteAccount(String subscription) {
        LOGGER.info("Inside deleteAccount method of AzureAccountServiceImpl. Subscription id: {}",subscription);
        AccountValidationResponse response=new AccountValidationResponse();
        try{
            //find and delete cred file for account
            response.setType(Constants.AZURE);
            response.setValidationStatus(SUCCESS);
            response.setMessage("Account deleted successfully");
            List<String> tenantId=azureAccountRepository.findTenantBySubscription(subscription);
            azureAccountRepository.deleteById(subscription);
            if(tenantId.isEmpty())
            {
                response.setValidationStatus(FAILURE);
                response.setErrorDetails("Tenant id not found");
                response.setMessage("Account deletion failed");
                return response;
            }
            response.setTenantId(tenantId.get(0));
            List<String> subscriptions=azureAccountRepository.findSubscriptionByTenant(tenantId.get(0));
            if(subscriptions.isEmpty()) {
                BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
                String region = System.getenv("REGION");
                String roleName = System.getenv(PALADINCLOUD_RO);
                AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                        .standard()
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .withRegion(region).build();
                String secretId = secretManagerPrefix + "/" + roleName + "/azure/" + tenantId.get(0);

                DeleteSecretRequest deleteRequest = new DeleteSecretRequest().withSecretId(secretId).withForceDeleteWithoutRecovery(true);
                DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
                LOGGER.info("Delete secret response: {} ", deleteResponse);

                //delete entry from db
                response = deleteAccountFromDB(tenantId.get(0));
            }
            List<AccountDetails> onlineAccounts=findOnlineAccounts(STATUS_CONFIGURED,Constants.AZURE);
            if(onlineAccounts==null || onlineAccounts.isEmpty()){
                LOGGER.debug("Last account for Azure is deleted, disabling aws enable flag");
                updateConfigProperty(AZURE_ENABLED,FALSE,JOB_SCHEDULER);
            }
            response.setType(Constants.AZURE);
        } catch (SdkClientException e) {
            LOGGER.error("Error in deleting the creds file json: {}", e.getMessage());
            response.setValidationStatus(FAILURE);
            response.setErrorDetails(e.getMessage());
            response.setMessage("Account deletion failed");
        }
        return response;
    }

    private String getSecretData(String secret){
        String jsonTemplate="{\"secretdata\": \"%s\"}";
        return String.format(jsonTemplate,secret);
    }

}

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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class QualysAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(QualysAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";
    public static final String QUALYS_CONNECTOR = "Qualys-Connector";

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

        LOGGER.info("Inside validate method of QualysAccountServiceImpl");
        AccountValidationResponse validateResponse=validateRequest(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        String uri=accountData.getQualysApiUrl();
        String user=accountData.getQualysApiUser();
        String pass=accountData.getQualysApiPassword();
        validateResponse.setQualysUser(user);
        validateResponse.setQualysApiUrl(uri);

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String apiUrl=uri+"/api/2.0/fo/knowledge_base/vuln/?action=list&ids=00";
        HttpGet httpGet = new HttpGet(apiUrl);
        httpGet.addHeader("content-type", "application/xml");
        httpGet.addHeader("cache-control", "no-cache");
        httpGet.addHeader("Accept", "application/json");
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, pass);
        httpGet.addHeader(BasicScheme.authenticate(credentials, "UTF-8", false));
        httpGet.addHeader("X-Requested-With", "DEFAULT_USER");
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode()!=200){
                validateResponse.setValidationStatus(FAILURE);
                validateResponse.setMessage("Account validation failed");
                validateResponse.setErrorDetails("API returned status code : "+httpResponse.getStatusLine().getStatusCode());
            }else{
                validateResponse.setValidationStatus(SUCCESS);
                validateResponse.setMessage("Qualys validation successful");
            }
        } catch (IOException e) {
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage("Account validation failed");
            validateResponse.setErrorDetails(e.getMessage());
        }
        return validateResponse;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Inside addAccount method of QualysAccountServiceImpl");
        AccountValidationResponse validateResponse=validateRequest(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        String accountId = UUID.randomUUID().toString();
        String roleName= System.getenv(PALADINCLOUD_RO);
        validateResponse = createAccountInDb(accountId, QUALYS_CONNECTOR, Constants.QUALYS);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Account already exists");
            return validateResponse;
        }
        try {
            BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
            String region = System.getenv("REGION");

            AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion(region).build();

            CreateSecretRequest createRequest = new CreateSecretRequest()
                    .withName(secretManagerPrefix + "/" + roleName + "/qualys").withSecretString(getQualysSecret(accountData));

            CreateSecretResult createResponse = secretClient.createSecret(createRequest);
            LOGGER.info("Create secret response: {}", createResponse);
            validateResponse.setValidationStatus(SUCCESS);
        }catch (ResourceExistsException e){
            LOGGER.error(SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setMessage(SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            validateResponse.setErrorDetails(e.getMessage()!=null?e.getMessage(): SECRET_ALREADY_EXIST_FOR_ACCOUNT);
            //Delete the entry from DB
            deleteAccountFromDB(accountId);
        }
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
        DeleteSecretRequest deleteRequest=new DeleteSecretRequest().withSecretId(secretId).withForceDeleteWithoutRecovery(true);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        LOGGER.info("Delete secret response: {} ",deleteResponse);

        //delete entry from db
        response=deleteAccountFromDB(accountId);

        response.setType(Constants.QUALYS);
        response.setAccountId(accountId);

        return response;
    }

    private String getQualysSecret(CreateAccountRequest accountRequest){
        String template="{\"qualysApiUrl\":\"%s\",\"apiusername\":\"%s\",\"apipassword\":\"%s\"}";
        return String.format(template,accountRequest.getQualysApiUrl(),accountRequest.getQualysApiUser()
                ,accountRequest.getQualysApiPassword());

    }
}

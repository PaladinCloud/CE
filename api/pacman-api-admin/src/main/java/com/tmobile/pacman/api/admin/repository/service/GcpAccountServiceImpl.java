package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.google.auth.oauth2.GoogleCredentials;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;

@Service
public class GcpAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(GcpAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String FAILURE = "FAILURE";
    public static final String SUCCESS = "SUCCESS";
    public static final String GCP_CREDENTIAL = "gcp-credential-";
    public static final String JSON = ".json";
    public static final String ERROR_WHILE_CREATING_CREDENTIAL_FILE = "Error while creating credential file";

    @Value("${credential.file.path}")
    private String credentialFilePath;
    @Value("${s3}")
    private String s3Bucket;
    @Value("${s3.cred.data}")
    private String s3CredData;
    @Value("${s3.region}")
    private String s3Region;

    @Autowired
    CredentialProvider credentialProvider;
    @Override
    public String serviceType() {
        return Constants.GCP;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        LOGGER.info("Inside validate method of GcpAccountServiceImpl");
        AccountValidationResponse validateResponse=new AccountValidationResponse();
        validateResponse.setValidationStatus(SUCCESS);
        if(StringUtils.isEmpty(accountData.getProjectId())) {
            validateResponse.setErrorDetails("Missing mandatory parameter- projectId");
            validateResponse.setValidationStatus(FAILURE);
            return validateResponse;
        }

        String credJson=generateCredentialJson(accountData);
        String fileName= GCP_CREDENTIAL +accountData.getProjectId()+ JSON;
        new File(credentialFilePath).mkdirs();
        String credFilePath = credentialFilePath + File.separator + fileName;
        //create credential json file
        File credFile=new File(credFilePath);
        if(!credFile.exists()){
            try {
                writeToFilePath(credFilePath,credJson,false);
            } catch (IOException e) {
                LOGGER.error("Error in generating credential file:{} ",e.getMessage());
                validateResponse.setValidationStatus(FAILURE);
                validateResponse.setErrorDetails(e.getMessage());
                validateResponse.setMessage(ERROR_WHILE_CREATING_CREDENTIAL_FILE);
                return validateResponse;
            }
        }else{
            LOGGER.info("File already exists");
        }
        //set env variable
        setEnvironment("GOOGLE_APPLICATION_CREDENTIALS", credFilePath);

        //connect
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
            LOGGER.info("Credentials created: {}",credentials);
            validateResponse.setMessage("Connection to project established successfully");
            validateResponse.setValidationStatus(SUCCESS);
        } catch (IOException e) {
            LOGGER.error("Error in connecting to project :{} ",e.getMessage());
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setErrorDetails(e.getMessage());
            validateResponse.setMessage(ERROR_WHILE_CREATING_CREDENTIAL_FILE);
            return validateResponse;
        }finally {
            //reset environment variable
            setEnvironment("GOOGLE_APPLICATION_CREDENTIALS", "");
            File file=new File(credFilePath);
            try {
                Files.deleteIfExists(file.toPath());
            }catch (IOException e) {
                LOGGER.error("Error in deleting the creds file json: {}", e.getMessage());
                validateResponse.setValidationStatus(FAILURE);
                validateResponse.setErrorDetails(e.getMessage());
                validateResponse.setMessage("Account validation failed");
            }
        }
        return validateResponse;
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        LOGGER.info("Inside addAccount method of GcpAccountServiceImpl");
        AccountValidationResponse validateResponse=validateRequest(accountData);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        String credJson=generateCredentialJson(accountData);
        String fileName= GCP_CREDENTIAL +accountData.getProjectId()+ JSON;
        new File(credentialFilePath).mkdirs();
        String credFilePath = credentialFilePath + File.separator + fileName;
        //create credential json file
        File credFile=new File(credFilePath);
        if(!credFile.exists()){
            try {
                writeToFilePath(credFilePath,credJson,false);
            } catch (IOException e) {
                LOGGER.error("Error in generating credential file:{} ",e.getMessage());
                validateResponse.setValidationStatus(FAILURE);
                validateResponse.setErrorDetails(e.getMessage());
                validateResponse.setMessage(ERROR_WHILE_CREATING_CREDENTIAL_FILE);
                return validateResponse;
            }
        }else{
            LOGGER.info("File already exists");
        }
        uploadFileToS3(s3Bucket,s3CredData,s3Region,credFilePath);
        createAccountInDb(accountData.getProjectId(),accountData.getProjectName(), Constants.GCP);
        //update gcp enable flag for scheduler job
        String key="gcp.enabled";
        String value = "true";
        String application = "job-scheduler";
        updateConfigProperty(key, value, application);
        validateResponse.setValidationStatus(SUCCESS);
        validateResponse.setMessage("Account added successfully");
        return validateResponse;
    }

    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response=new AccountValidationResponse();
        StringBuilder validationErrorDetails=new StringBuilder();
        String projectId=accountData.getProjectId();
        String projectName=accountData.getProjectName();
        Long projectNumber=accountData.getProjectNumber();
        String location=accountData.getLocation();
        String workLoadPoolId=accountData.getGetWorkloadIdentityPoolId();
        String workLoadIdentityProviderId=accountData.getWorkloadIdentityProviderId();
        String serviceAccEmail=accountData.getServiceAccountEmail();
        if(StringUtils.isEmpty(projectId)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Project Id\n");
        }
        if(StringUtils.isEmpty(projectName)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Project name\n");
        }
        if(projectNumber==null){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Project number\n");
        }
        if(StringUtils.isEmpty(location)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" location\n");
        }
        if(StringUtils.isEmpty(workLoadPoolId)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Workload pool Id\n");
        }
        if(StringUtils.isEmpty(workLoadIdentityProviderId)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Workload identity provider id\n");
        }
        if(StringUtils.isEmpty(serviceAccEmail)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Service account email\n");
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
    public AccountValidationResponse deleteAccount(String projectId) {
        LOGGER.info("Inside deleteAccount method of GcpAccountServiceImpl. ProjectId: {}",projectId);
        AccountValidationResponse response=new AccountValidationResponse();
        response.setType(Constants.GCP);
        response.setProjectId(projectId);
        response.setValidationStatus(SUCCESS);
        response.setMessage("Account deleted successfully");

        //find and delete cred file for account
        String fileName= GCP_CREDENTIAL +projectId+ JSON;
        String credFilePath = credentialFilePath + File.separator + fileName;
        File file=new File(credFilePath);
        try {
            Files.deleteIfExists(file.toPath());


            //delete entry from db
            deleteAccountFromDB(projectId);

        } catch (IOException e) {
            LOGGER.error("Error in deleting the creds file json: {}", e.getMessage());
            response.setValidationStatus(FAILURE);
            response.setErrorDetails(e.getMessage());
            response.setMessage("Account deletion failed");
        }
        return response;
    }

    private String generateCredentialJson(CreateAccountRequest details){
        String ip="169.254.169.254";
        String jsonTemplate="{\n" +
                "  \"type\": \"external_account\",\n" +
                "  \"audience\": \"//iam.googleapis.com/projects/%s/locations/%s/workloadIdentityPools/%s/providers/%s\",\n" +
                "  \"subject_token_type\": \"urn:ietf:params:aws:token-type:aws4_request\",\n" +
                "  \"service_account_impersonation_url\": \"https://iamcredentials.googleapis.com/v1/projects/-/serviceAccounts/%s:generateAccessToken\",\n" +
                "  \"token_url\": \"https://sts.googleapis.com/v1/token\",\n" +
                "  \"credential_source\": {\n" +
                "    \"environment_id\": \"aws1\",\n" +
                "    \"region_url\": \"http://%s/latest/meta-data/placement/availability-zone\",\n" +
                "    \"url\": \"http://%s/latest/meta-data/iam/security-credentials\",\n" +
                "    \"regional_cred_verification_url\": \"https://sts.{region}.amazonaws.com?Action=GetCallerIdentity&Version=2011-06-15\"\n" +
                "  }\n" +
                "}";

        Long projectNumber=details.getProjectNumber();
        String location=details.getLocation();
        String workloadPoolId=details.getGetWorkloadIdentityPoolId();
        String idProvider=details.getWorkloadIdentityProviderId();
        String serviceAccountEmail=details.getServiceAccountEmail();
        return String.format(jsonTemplate, projectNumber,location,workloadPoolId,idProvider,serviceAccountEmail,ip,ip);

    }
    public void writeToFilePath(String filename, String data, boolean appendto) throws IOException {
        LOGGER.debug("Write to File : {}", filename);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, appendto))) {
            bw.write(data);
            bw.flush();
        }
    }
    public void setEnvironment(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }
    public void uploadFileToS3(String s3Bucket,String dataFolder, String s3Region,String filePath){
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        AmazonS3 s3client = AmazonS3ClientBuilder.standard().withRegion(s3Region).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        uploadAllFiles(s3client,s3Bucket,dataFolder,filePath);

    }

    private void uploadAllFiles(AmazonS3 s3client,String s3Bucket,String dataFolderS3, String filePath){
        LOGGER.info("Uploading files to bucket: {} folder: {}",s3Bucket,dataFolderS3);
        TransferManager xferMgr = TransferManagerBuilder.standard().withS3Client(s3client).build();
        try {
            MultipleFileUpload xfer = xferMgr.uploadDirectory(s3Bucket,
                    dataFolderS3, new File(filePath), false);

            while(!xfer.isDone()){
                delayForCompletion();
                LOGGER.debug("Transfer % Completed :{}" ,xfer.getProgress().getPercentTransferred());
            }
            xfer.waitForCompletion();

            LOGGER.info("Transfer completed");
        } catch(InterruptedException e){
            LOGGER.error("Error in uploadAllFiles",e);
            Thread.currentThread().interrupt();
        }catch (Exception e) {
            LOGGER.error("Exception in loading files to S3:{}" ,e.getMessage()) ;
        }
        xferMgr.shutdownNow();
    }

    private static void delayForCompletion() {
        try{
            Thread.sleep(3000);
        }catch(InterruptedException e){
            LOGGER.error("Error in uploadAllFiles",e);
            Thread.currentThread().interrupt();
        }
    }
}

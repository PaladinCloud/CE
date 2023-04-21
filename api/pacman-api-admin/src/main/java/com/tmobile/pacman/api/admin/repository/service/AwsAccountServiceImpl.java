package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.admin.util.AdminUtils;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

@Service
public class AwsAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(AwsAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String REGION_US_WEST_2 = "us-west-2";
    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";
    public static final String ARN_AWS_IAM = "arn:aws:iam::";
    public static final String COGNITO_ACCOUNT = "COGNITO_ACCOUNT";

    @Autowired
    CredentialProvider credentialProvider;
    @Override
    public String serviceType() {
        return Constants.AWS;
    }

    @Override
    public AccountValidationResponse validate(CreateAccountRequest accountData) {
        LOGGER.info("Inside validate method of AwsAccountServiceImpl");
        AccountValidationResponse validateResponse=validateRequest(accountData);
        validateResponse.setAccountId(accountData.getAccountId());
        validateResponse.setType(Constants.AWS);
        if(validateResponse.getValidationStatus().equalsIgnoreCase(FAILURE)){
            LOGGER.info("Validation failed due to missing parameters");
            return validateResponse;
        }
        String accountId=accountData.getAccountId();
        String roleName="PaladinCloudIntegrationRole";
        String baseAccount=System.getenv(COGNITO_ACCOUNT);
        String paladinRole=System.getenv(PALADINCLOUD_RO);
        BasicSessionCredentials baseSessioncreds =null;
        boolean isPolicyUpdated=false;
        try {
            isPolicyUpdated=updatePolicy(accountId, baseAccount, paladinRole,"add");
            delayForCompletion();
            baseSessioncreds = credentialProvider.getCredentials(accountId, roleName);

        } catch (Exception e) {
            LOGGER.error("Error in connecting to account :{} ",e.getMessage());
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setErrorDetails(e.getMessage());
            validateResponse.setMessage("Error while creating credential file");
            return validateResponse;
        }finally {
            if(isPolicyUpdated) {
                revertPolicyVersion(accountId, baseAccount, paladinRole);
            }
        }
        LOGGER.info("Credentials created: {}",baseSessioncreds);
        if(baseSessioncreds!=null) {
            validateResponse.setMessage("Connection to the account established successfully");
            validateResponse.setValidationStatus("SUCCESS");
        }else{
            validateResponse.setMessage("Connection to the account not established");
            validateResponse.setValidationStatus("FAILURE");
        }
        return validateResponse;
    }

    private void revertPolicyVersion(String accountId, String baseAccount, String paladinRole) {
        LOGGER.info("Reverting the default policy version to previous policy version. AccountId:{}, baseAccountId:{}, baseAccountRole:{}", accountId,baseAccount,paladinRole);
        BasicSessionCredentials credentials = credentialProvider.getCredentials(baseAccount, paladinRole);
        AmazonIdentityManagement iam = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(REGION_US_WEST_2).build();

        String policyArn= ARN_AWS_IAM + baseAccount +":policy/"+ paladinRole;
        Policy p = iam.getPolicy(new GetPolicyRequest().withPolicyArn(policyArn)).getPolicy();
        String defaultversionId = p.getDefaultVersionId();

        ListPolicyVersionsResult policyVersions = iam.listPolicyVersions(new ListPolicyVersionsRequest().withPolicyArn(policyArn));
        PolicyVersion versionToDefault=policyVersions.getVersions().stream().filter(policyVersion -> !policyVersion.isDefaultVersion()).max(Comparator.comparing(PolicyVersion::getCreateDate))
                .orElse(null);
        if(versionToDefault!=null){
            LOGGER.info("Setting the default version to ;{}",versionToDefault.getVersionId());
            iam.setDefaultPolicyVersion(new SetDefaultPolicyVersionRequest().withPolicyArn(policyArn).withVersionId(versionToDefault.getVersionId()));
            iam.deletePolicyVersion(new DeletePolicyVersionRequest().withPolicyArn(policyArn).withVersionId(defaultversionId));
        }
    }

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        AccountValidationResponse response = createAccountInDb(accountData.getAccountId(), accountData.getAccountName(), accountData.getPlatform());
        response.setAccountName(accountData.getAccountName());
        response.setAccountId(accountData.getAccountId());
        if(response.getValidationStatus().equalsIgnoreCase(FAILURE)){
            return response;
        }
        String accountId=System.getenv(COGNITO_ACCOUNT);
        String roleName=System.getenv(PALADINCLOUD_RO);
        try {
            updatePolicy(accountData.getAccountId(), accountId, roleName,"add");
        } catch (SdkException | UnsupportedEncodingException e) {
            LOGGER.error("Error in updating policy for account");
            response.setErrorDetails(e.getMessage());
            response.setValidationStatus(FAILURE);
            response.setMessage("Failed to add account.");
            deleteAccountFromDB(accountData.getAccountId());
        }
        return response;
    }

    @Override
    public AccountValidationResponse deleteAccount(String accountId) {
        AccountValidationResponse response = deleteAccountFromDB(accountId);
        response.setType(Constants.AWS);
        if(response.getValidationStatus().equalsIgnoreCase(FAILURE)){
            return response;
        }
        String baseAccount=System.getenv(COGNITO_ACCOUNT);
        String roleName=System.getenv(PALADINCLOUD_RO);
        try {
            updatePolicy(accountId, baseAccount, roleName,"delete");

        } catch (SdkException | UnsupportedEncodingException e) {
            LOGGER.error("Error in updating policy for account");
            response.setErrorDetails(e.getMessage());
            response.setValidationStatus(FAILURE);
            response.setMessage("Failed to add account.");
        }
        return response;
    }

    private boolean updatePolicy(String accountId, String baseAccount, String roleName, String action) throws UnsupportedEncodingException {
        BasicSessionCredentials baseSessioncreds = credentialProvider.getCredentials(baseAccount, roleName);
        AmazonIdentityManagement iamClient = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(baseSessioncreds)).withRegion(REGION_US_WEST_2).build();

        String policyArn= ARN_AWS_IAM + baseAccount +":policy/"+ roleName;

        String resourceArn= ARN_AWS_IAM + accountId +":role/PaladinCloudIntegrationRole";

        Policy p = iamClient.getPolicy(new GetPolicyRequest().withPolicyArn(policyArn)).getPolicy();
        String versionId = p.getDefaultVersionId();

        GetPolicyVersionResult policyVersion = iamClient.getPolicyVersion(new GetPolicyVersionRequest().withPolicyArn(policyArn).withVersionId(versionId));
        String policyDocument= URLDecoder.decode(policyVersion.getPolicyVersion().getDocument(), StandardCharsets.UTF_8.name());
        String updatedPolicyDoc= action.equalsIgnoreCase("delete")?AdminUtils.deleteResourceFromPolicy(policyDocument,resourceArn):AdminUtils.addResourceInPolicy(policyDocument,resourceArn);
        if(policyDocument.equalsIgnoreCase(updatedPolicyDoc)){
            LOGGER.info("No change in the policy document, skipping change in policy");
            return false;
        }else {
            ListPolicyVersionsResult policyVersions = iamClient.listPolicyVersions(new ListPolicyVersionsRequest().withPolicyArn(policyArn));
            if (policyVersions.getVersions().size() > 4) {
                LOGGER.info("Maximum policy versions found. Deleting oldest version");
                PolicyVersion versionToDelete=policyVersions.getVersions().stream().min(Comparator.comparing(PolicyVersion::getCreateDate))
                        .orElse(null);
                if(versionToDelete!=null){
                    iamClient.deletePolicyVersion(new DeletePolicyVersionRequest().withPolicyArn(policyArn).withVersionId(versionToDelete.getVersionId()));
                    LOGGER.info("Deleted policy version:{}", versionToDelete.getVersionId());
                }else{
                    LOGGER.info("Deleted policy version is null");
                }
            }
            CreatePolicyVersionRequest updatePolicyRequest = new CreatePolicyVersionRequest();
            updatePolicyRequest.setPolicyArn(policyArn);
            updatePolicyRequest.setSetAsDefault(true);
            updatePolicyRequest.setPolicyDocument(updatedPolicyDoc);
            CreatePolicyVersionResult createResponse = iamClient.createPolicyVersion(updatePolicyRequest);
            LOGGER.info("Policy update response:{}", createResponse);
            return true;
        }
    }


    private AccountValidationResponse validateRequest(CreateAccountRequest accountData) {
        AccountValidationResponse response=new AccountValidationResponse();
        StringBuilder validationErrorDetails=new StringBuilder();
        String accountId=accountData.getAccountId();
        String roleName=accountData.getRoleName();
        if(StringUtils.isEmpty(accountId)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Account Id\n");
        }
        if(StringUtils.isEmpty(roleName)){
            validationErrorDetails.append(MISSING_MANDATORY_PARAMETER +" Role name\n");
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
    private static void delayForCompletion() {
        try{
            Thread.sleep(8000);
        }catch(InterruptedException e){
            LOGGER.error("Error in uploadAllFiles",e);
            Thread.currentThread().interrupt();
        }
    }
}

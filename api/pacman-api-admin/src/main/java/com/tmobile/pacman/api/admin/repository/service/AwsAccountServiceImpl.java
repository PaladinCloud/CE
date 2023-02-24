package com.tmobile.pacman.api.admin.repository.service;

import com.amazonaws.auth.BasicSessionCredentials;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AwsAccountServiceImpl extends AbstractAccountServiceImpl implements AccountsService{

    private static final Logger LOGGER= LoggerFactory.getLogger(AwsAccountServiceImpl.class);
    public static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";

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
        String roleName=accountData.getRoleName();
        BasicSessionCredentials baseSessioncreds =null;
        try {
            baseSessioncreds = credentialProvider.getCredentials(accountId, roleName);
        } catch (Exception e) {
            LOGGER.error("Error in connecting to account :{} ",e.getMessage());
            validateResponse.setValidationStatus(FAILURE);
            validateResponse.setErrorDetails(e.getMessage());
            validateResponse.setMessage("Error while creating credential file");
            return validateResponse;
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

    @Override
    public AccountValidationResponse addAccount(CreateAccountRequest accountData) {
        boolean isCreated=createAccountInDb(accountData.getAccountId(),accountData.getAccountName(),accountData.getPlatform());
        AccountValidationResponse accountDetails=new AccountValidationResponse();
        accountDetails.setAccountId(accountData.getAccountId());
        accountDetails.setAccountName(accountData.getAccountName());
        accountDetails.setType(accountData.getPlatform());
        if(isCreated){
            accountDetails.setMessage("configured");
        }else{
            accountDetails.setMessage("failed");
        }
        return accountDetails;
    }

    @Override
    public AccountValidationResponse deleteAccount(String accountId) {
        boolean isDeleted = deleteAccountFromDB(accountId);
        AccountValidationResponse accountDetails=new AccountValidationResponse();
        accountDetails.setAccountId(accountId);
        accountDetails.setType(Constants.AWS);
        if(isDeleted){
            accountDetails.setMessage("Account deleted successfully");
        }else{
            accountDetails.setMessage("Account deletion failed");
        }
        return accountDetails;
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
}

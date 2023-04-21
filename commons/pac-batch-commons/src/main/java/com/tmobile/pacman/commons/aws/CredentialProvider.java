package com.tmobile.pacman.commons.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


public class CredentialProvider {

    String baseaccount = System.getenv("COGNITO_ACCOUNT");
    String baseregion = System.getenv("REGION");
    String rolename = System.getenv("PALADINCLOUD_RO");

	private static final boolean devmode = System.getProperty("PIC_DEV_MODE")!=null;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public  BasicSessionCredentials getCredentials(String accountId,String role){

        BasicSessionCredentials baseAccountCredentials = getBaseAccountCredentials(baseaccount, baseregion, rolename);
        if(baseaccount.equals(accountId)){
            return baseAccountCredentials;
        }
        AWSSecurityTokenServiceClientBuilder awsSecurityTokenServiceClientBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials( new AWSStaticCredentialsProvider(baseAccountCredentials)).withRegion(baseregion);
        AWSSecurityTokenService stsClient = awsSecurityTokenServiceClientBuilder.build();
        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(accountId,role)).withRoleSessionName("pic-ro-"+accountId);
        AssumeRoleResult assumeRoleResult = stsClient.assumeRole(assumeRoleRequest);
        return  new BasicSessionCredentials(
                assumeRoleResult.getCredentials()
                        .getAccessKeyId(), assumeRoleResult.getCredentials().getSecretAccessKey(),
                assumeRoleResult.getCredentials().getSessionToken());
    }

    public BasicSessionCredentials getBaseAccCredentials(){

        LOGGER.info("Fetching base account session credentials. Base Account: {}, Base Region: {}, Role:{}",
                baseaccount, baseregion, rolename);
        return getBaseAccountCredentials(baseaccount, baseregion, rolename);
    }


    public BasicSessionCredentials getBaseAccountCredentials (String baseAccountId, String region,String roleName) {
        if (devmode) {
            String accessKeyId = System.getProperty("ACCESS_KEY");
            String secretKey = System.getProperty("SECRET_KEY");
            BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId, secretKey);
            AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials)).withRegion(region);
            AWSSecurityTokenService awsSecurityTokenService = stsBuilder.build();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccountId, roleName)).withRoleSessionName("pic-base-ro");
            AssumeRoleResult assumeRoleResult = awsSecurityTokenService.assumeRole(assumeRequest);
            return new BasicSessionCredentials(
                    assumeRoleResult.getCredentials().getAccessKeyId(), assumeRoleResult.getCredentials().getSecretAccessKey(),
                    assumeRoleResult.getCredentials().getSessionToken());

        } else {
            AWSSecurityTokenService awsSecurityTokenService = AWSSecurityTokenServiceClientBuilder.defaultClient();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccountId, roleName)).withRoleSessionName("pic-base-ro");
            AssumeRoleResult assumeRoleResult = awsSecurityTokenService.assumeRole(assumeRequest);
            return new BasicSessionCredentials(
                    assumeRoleResult.getCredentials().getAccessKeyId(), assumeRoleResult.getCredentials().getSecretAccessKey(),
                    assumeRoleResult.getCredentials().getSessionToken());
        }
    }

    private String getRoleArn(String accountId, String role){
        return "arn:aws:iam::"+accountId+":role/"+role;
    }
}

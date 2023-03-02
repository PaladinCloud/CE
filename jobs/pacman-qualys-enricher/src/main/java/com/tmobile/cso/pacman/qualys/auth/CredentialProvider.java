package com.tmobile.cso.pacman.qualys.auth;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CredentialProvider {
    private static boolean devMode = System.getProperty("PIC_DEV_MODE")!=null;



    public BasicSessionCredentials getBaseAccountCredentials (String baseAccount, String baseRegion,String roleName) {
        if (devMode) {
            String accessKey = System.getProperty("ACCESS_KEY");
            String secretKey = System.getProperty("SECRET_KEY");
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(baseRegion);
            AWSSecurityTokenService sts = stsBuilder.build();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount, roleName)).withRoleSessionName("pic-base-ro");
            AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
            return new BasicSessionCredentials(
                    assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                    assumeResult.getCredentials().getSessionToken());

        } else {
            AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount, roleName)).withRoleSessionName("pic-base-ro");
            AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
            return new BasicSessionCredentials(
                    assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                    assumeResult.getCredentials().getSessionToken());
        }
    }

    private String getRoleArn(String account, String role){
        return "arn:aws:iam::"+account+":role/"+role;
    }
}

package com.tmobile.pacbot.gcp.inventory.auth;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.springframework.stereotype.Component;


/**
 * The Class CredentialProvider.
 */
@Component
public class AWSCredentialProvider {
	
	/** The dev mode. */
	private static boolean devMode = System.getProperty("PIC_DEV_MODE")==null?false:true;
	
	
	/**
	 * Gets the base account credentials.
	 *
	 * @param roleName the role name
	 * @return the base account credentials
	 */
	public BasicSessionCredentials getCredentials (String baseAccount, String baseRegion,String roleName){
		if(devMode){
			String accessKey = System.getProperty("ACCESS_KEY"); 
			String secretKey = System.getProperty("SECRET_KEY"); 
			BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
			AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials( new AWSStaticCredentialsProvider(awsCreds)).withRegion(baseRegion);
			AWSSecurityTokenService sts = stsBuilder.build();
			AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount,roleName)).withRoleSessionName("pic-base-ro");
			AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
			return new  BasicSessionCredentials(
					assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
					assumeResult.getCredentials().getSessionToken());
			
		}
		else{
			System.out.println("inside");
			AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
			AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount,roleName)).withRoleSessionName("pic-base-ro");
			AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
			return new BasicSessionCredentials(
					assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
					assumeResult.getCredentials().getSessionToken());
		}
	}
	
	/**
	 * Gets the role arn.
	 *
	 * @param accout the accout
	 * @param role the role
	 * @return the role arn
	 */
	private String getRoleArn(String accout, String role){
		return "arn:aws:iam::"+accout+":role/"+role;
	}
}

package com.tmobile.cso.pacman.inventory.localstack;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class LocalStackClient {
    //@Value("${localstack.url:http://localhost:4566}")
    @Value("${localstack.url}")
    String endpoint;
    
    //@Value("${localstack.region:us-east-1}")
    @Value("${localstack.region}")
     String region;
    
    @Value("${localstack.accessKey}")
     String accessKey;
    
    @Value( "${localstack.secretKey}" )
     String secretKey;

    public LocalStackClient(){

    }
    public AmazonEC2 getEc2Client(){
        System.out.println("AccessKey: "+accessKey+" SecretKey: "+secretKey+" Region: "+region);
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AwsClientBuilder.EndpointConfiguration config =
                new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        return AmazonEC2ClientBuilder.standard()
                .withEndpointConfiguration(config)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    public BasicSessionCredentials getBaseCredentials(){

        BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
        AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials( new AWSStaticCredentialsProvider(awsCreds)).withRegion(region);
        AWSSecurityTokenService sts = stsBuilder.build();
        AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn("arn:aws:iam::000000000000:role/LocalStack-Role");//.withRoleSessionName("pic-base-ro");
        AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
        return new BasicSessionCredentials(
                assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                assumeResult.getCredentials().getSessionToken());

    }
}

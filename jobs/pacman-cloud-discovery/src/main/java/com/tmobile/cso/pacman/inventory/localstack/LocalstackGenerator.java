package com.tmobile.cso.pacman.inventory.localstack;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocalstackGenerator {

    @Autowired
    LocalStackClient localStackClient;

    @Value("${localstack.ec2Instances}")
    private String ec2Instances;

    @Value("${localstack.ec2Prefix}")
    private String ec2Prefix;

    Logger logger=LoggerFactory.getLogger(LocalstackGenerator.class);
    private static final String s3Endpoint="http://localhost:4566";
    private static final String REGION= Regions.US_EAST_1.getName();


    public void generateEC2Data(){

        //BasicSessionCredentials baseCreds=localStackClient.getBaseCredentials();
        //AmazonEC2 ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(baseCreds)).withRegion(propertyConfigurer.getRegion()).build();
        AmazonEC2 ec2Client=localStackClient.getEc2Client();
        //Vpc vpc1=createVpc(ec2Client);
        //createSecurityGroups(ec2Client, vpc1.getVpcId());
        createEc2Instances(ec2Client);

    }

    private void createEc2Instances(AmazonEC2 ec2Client) {
        Integer noOfEc2= Integer.parseInt(ec2Instances);
        for(int i=1;i<=noOfEc2;i++){
            String random= String.valueOf(UUID.randomUUID());
            String amiId=ec2Prefix+random;
            RunInstancesRequest run_request = new RunInstancesRequest()
                    .withImageId(amiId)
                    .withInstanceType(InstanceType.T1Micro)
                    .withMaxCount(1)
                    .withMinCount(1);
            RunInstancesResult run_response = ec2Client.runInstances(run_request);

            String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();
           logger.info("Running Instance : {} ",reservation_id);
            logger.info("No of EC2 Instances created: {}",i);
        }

    }

    public static Vpc createVpc(AmazonEC2 ec2Client){
        System.out.println("Creating a VPC");
        CreateVpcRequest newVPC = new CreateVpcRequest("In");
        newVPC.setCidrBlock("10.0.0.0/16");
        CreateVpcResult res = ec2Client.createVpc(newVPC);
        Vpc vp = res.getVpc();
        String vpcId = vp.getVpcId();
        System.out.println("Created VPC " + vpcId);
        return vp;
    }

    public static void createSecurityGroups(AmazonEC2 ec2Client, String vpcId){
        System.out.println("Creating security group for VPC "+vpcId);
        CreateSecurityGroupRequest create_request = new
                CreateSecurityGroupRequest()
                .withGroupName("group_name-1")
                .withDescription("group_desc 1")
                .withVpcId(vpcId);

        CreateSecurityGroupResult create_response =ec2Client.createSecurityGroup(create_request);
        System.out.println("Security group created with response: "+create_response.toString());
    }
}

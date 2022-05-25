package com.tmobile.cso.pacman.inventory.localstack;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LocalstackGenerator {

    @Autowired
    LocalStackClient localStackClient;

    @Value("${localstack.ec2Instances}")
    private String ec2Instances;

    @Value("${localstack.ec2Prefix}")
    private String ec2Prefix;

    @Value("${localstack.securityGroups}")
    private String securityGroups;

    @Value("${localstack.sgPrefix}")
    private String sgPrefix;

    Logger logger=LoggerFactory.getLogger(LocalstackGenerator.class);
    private static final String s3Endpoint="http://localhost:4566";
    private static final String REGION= Regions.US_EAST_1.getName();
    Map<String,String> securityGroupsMap=new HashMap<>();

    public void generateLocalStackData(){

        //BasicSessionCredentials baseCreds=localStackClient.getBaseCredentials();
        //AmazonEC2 ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(baseCreds)).withRegion(propertyConfigurer.getRegion()).build();
        AmazonEC2 ec2Client=localStackClient.getEc2Client();
        //Vpc vpc1=createVpc(ec2Client);
        createSecurityGroups(ec2Client);
        createPublicAccessSG(ec2Client);
        createEc2Instances(ec2Client);

    }

    private void createEc2Instances(AmazonEC2 ec2Client) {
        Integer noOfEc2= Integer.parseInt(ec2Instances);

        for(int i=1;i<=noOfEc2;i++){
            String random= String.valueOf(UUID.randomUUID());
            String amiId=ec2Prefix+random;
            int idx=new Random().nextInt(securityGroupsMap.size());
            String groupId=securityGroupsMap.get(idx);
            RunInstancesRequest run_request = new RunInstancesRequest()
                    .withImageId(amiId)
                    .withInstanceType(InstanceType.T1Micro)
                    .withSecurityGroups(securityGroupsMap.get(idx))
                    .withMaxCount(1)
                    .withMinCount(1);
            RunInstancesResult response = ec2Client.runInstances(run_request);

            String instanceId = response.getReservation().getInstances().get(0).getInstanceId();
           logger.info("Running Instance : {} ",instanceId);
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

    public void createSecurityGroups(AmazonEC2 ec2Client){
        logger.info("Creating security group");
        Integer noOdSg= Integer.parseInt(securityGroups);
        for(int i=0;i<noOdSg;i++){
            String groupName="LocalStack-Group-"+UUID.randomUUID().toString();
            CreateSecurityGroupRequest req = new
                    CreateSecurityGroupRequest()
                    .withGroupName(groupName)
                    .withDescription("Local stack group 1");
            CreateSecurityGroupResult response =ec2Client.createSecurityGroup(req);
            securityGroupsMap.put("localstackGroup",response.getGroupId());
            logger.info("Security group created with Id: {}",response.getGroupId());

        }

        //Default Security group
        logger.info("Adding default security group to security group map");
        DescribeSecurityGroupsRequest searchReq=new DescribeSecurityGroupsRequest().withGroupNames("default");
        DescribeSecurityGroupsResult searchResult =null;
        try {
            searchResult = ec2Client.describeSecurityGroups(searchReq);
            logger.info("Result of search operation: {}",searchResult);
        }catch(AmazonEC2Exception e){
            logger.error("Exception occurred on searching the group:{}",e);
        }
        logger.info("SearchResult: {}",searchResult);
        if(searchResult!=null){
            securityGroupsMap.put("defaultSg",searchResult.getSecurityGroups().get(0).getGroupId());
        }
    }

    public void createPublicAccessSG(AmazonEC2 ec2Client){
        logger.info("Creating security groups");
        String groupName = "localstack-group-"+UUID.randomUUID().toString();
        boolean createGroup=false;
        DescribeSecurityGroupsRequest searchReq=new DescribeSecurityGroupsRequest().withGroupNames(groupName);
        DescribeSecurityGroupsResult searchResult =null;
        try {
            searchResult = ec2Client.describeSecurityGroups(searchReq);
            logger.info("Result of search operation: {}",searchResult);
        }catch(AmazonEC2Exception e){
            logger.error("Exception occurred on searching the group:{}",e);
        }
        logger.info("SearchResult: {}",searchResult);
        String groupId=null;
        if(searchResult!=null){
            groupId=searchResult.getSecurityGroups().get(0).getGroupId();
            logger.info("Security group already created.GroupId : {}",groupId);
        }else{
            createGroup=true;
        }
        if(createGroup){


            logger.info("Group is not present. creating the group now.");
            CreateSecurityGroupRequest request = new CreateSecurityGroupRequest().withGroupName(groupName).withDescription("LocalStack security group");
            CreateSecurityGroupResult response =ec2Client.createSecurityGroup(request);
            logger.info("Security group created with response: {}",response.toString());
            groupId = response.getGroupId();
            logger.info("Security group created: {}", groupId);

            IpRange publicAccessIpRange = new IpRange().withCidrIp("0.0.0.0/0");
            IpPermission port80IpPermission =new IpPermission()
                    .withIpProtocol("tcp")
                    .withFromPort(80)
                    .withToPort(80)
                    .withIpv4Ranges(publicAccessIpRange);

            AuthorizeSecurityGroupIngressRequest authRequest = new AuthorizeSecurityGroupIngressRequest();
            authRequest.withGroupId(groupId).withIpPermissions(port80IpPermission);
            AuthorizeSecurityGroupIngressResult authResponse = ec2Client.authorizeSecurityGroupIngress(authRequest);
            logger.info("Auth Response: {}",authResponse);

            searchResult = ec2Client.describeSecurityGroups(searchReq);
            logger.info("Result of search operation: {}",searchResult);
            SecurityGroup sg=searchResult.getSecurityGroups().get(0);
            List<Tag> tags=new ArrayList<>();
            tags.add(new Tag("tag1", "value1"));
            tags.add(new Tag("tag2", "value2"));
            tags.add(new Tag("tag3", "value3"));
            tags.add(new Tag("tag4", "value4"));
            tags.add(new Tag("tag5", "value5"));
            sg.setTags(tags);
            securityGroupsMap.put("publicAccessGroup", groupId);

        }
        logger.info("Exiting!!");
    }
}

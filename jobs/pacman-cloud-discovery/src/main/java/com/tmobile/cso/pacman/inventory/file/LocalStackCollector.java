package com.tmobile.cso.pacman.inventory.file;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.tmobile.cso.pacman.inventory.file.FileGenerator;
import com.tmobile.cso.pacman.inventory.localstack.LocalStackClient;
import com.tmobile.cso.pacman.inventory.localstack.LocalstackGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
@Component
public class LocalStackCollector {

    @Value( "${localstack.generator.folder}" )
    private String folder;

    @Value( "${localstack.region}" )
    private String region;

    private static final String OPEN_ARRAY="[";
    private static final String CLOSE_ARRAY="]";

    @Autowired
    LocalStackClient localStackClient;
    Logger logger= LoggerFactory.getLogger(LocalStackCollector.class);
    public void runCollector(String folderName) {
        initialise(folderName);
        collectData();
        finalizeFiles();
    }


    public void collectData(){
        logger.info("LocalStack Collector running....");
        Map<String, List<Instance>> instancesMap=fetchInstances();
        logger.info("Ec2 Instances fetched!!");
        for (Map.Entry<String,List<Instance>> entry : instancesMap.entrySet())
            logger.info("Key = {}, Value = " ,entry.getKey(),entry.getValue());
        try {
            generateInstanceFiles(instancesMap);
        } catch (IOException e) {
            logger.error("Exception : {}",e);
        }

    }


    public Map<String, List<Instance>> fetchInstances(){
        Map<String,List<Instance>> instanceMap = new LinkedHashMap<>();
        AmazonEC2 ec2Client= localStackClient.getEc2Client();

        List<Region> regionList=new ArrayList<>();
        Region r=RegionUtils.getRegion("us-east-1");
        regionList.add(r);
        //BasicSessionCredentials temporaryCredentials=localStackClient.getBaseCredentials();
        for(Region region : regionList) {
            try{

                //ec2Client = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(temporaryCredentials)).withRegion(region.getName()).build();
                List<Instance> instanceList = new ArrayList<>();
                DescribeInstancesResult descInstResult ;
                String nextToken = null;
                do{
                    descInstResult =  ec2Client.describeInstances(new DescribeInstancesRequest().withNextToken(nextToken));
                    descInstResult.getReservations().forEach(
                            reservation -> instanceList.addAll(reservation.getInstances().stream().collect(Collectors.toList())));
                    nextToken = descInstResult.getNextToken();
                }while(nextToken!=null);

                if(!instanceList.isEmpty() ) {
                    logger.info("EC2 Instance:  region: {}  >> ", region, instanceList.size());
                    instanceMap.put("A000000`DemoAccount`"+region.getName(), instanceList);
                }

            }catch(Exception e){
                System.out.println("Exception: "+e);
            }
        }
        return instanceMap;
    }


    public void generateInstanceFiles(Map<String,List<Instance>> instanceMap) throws IOException {
        String fieldNames ="";
        String keys ="";

        fieldNames = "instanceId`amiLaunchIndex`architecture`clientToken`ebsOptimized`EnaSupport`Hypervisor`ImageId`InstanceLifecycle`InstanceType`KernelId`KeyName`LaunchTime`Platform`PrivateDnsName`"
                + "PrivateIpAddress`PublicDnsName`PublicIpAddress`RamdiskId`RootDeviceName`RootDeviceType`SourceDestCheck`SpotInstanceRequestId`SriovNetSupport`StateTransitionReason`SubnetId`VirtualizationType`"
                + "VpcId`IamInstanceProfile.Arn`IamInstanceProfile.Id`Monitoring.State`Placement.Affinity`Placement.AvailabilityZone`Placement.GroupName`Placement.HostId`Placement.Tenancy`State.Name`State.Code`StateReason.Message`StateReason.Code";
        keys = "discoverydate`accountid`accountname`region`instanceid`amilaunchindex`architecture`clienttoken`ebsoptimized`enasupport`hypervisor"
                + "`imageid`instancelifecycle`instancetype`kernelid`keyname`launchtime`platform`privatednsname`privateipaddress`"
                + "publicdnsname`publicipaddress`ramdiskid`rootdevicename`rootdevicetype`sourcedestcheck`spotinstancerequestid`"
                + "sriovnetsupport`statetransitionreason`subnetid`virtualizationtype`vpcid`iaminstanceprofilearn`iaminstanceprofileid"
                + "`monitoringstate`affinity`availabilityzone`groupname`hostid`tenancy`statename`statecode`statereasonmessage`statereasoncode";
        FileGenerator.generateJson(instanceMap, fieldNames, "aws-ec2.data",keys);

    }

    public void initialise(String folderName) {
        FileGenerator.folderName = folderName;
        try {
            new File(folderName).mkdirs();
            File file=new File(folderName+File.separator+"aws-ec2.data");
            if(!file.exists()) {
                logger.info("File not created earlier. New File will be created.");
                FileGenerator.writeToFile("aws-ec2.data", OPEN_ARRAY, false);
            }else{
                logger.info("File already created earlier. Data will be appended.");
                FileGenerator.removeTrailingChar(folderName+File.separator+"aws-ec2.data");
            }
        }catch ( IOException e){
            System.out.println("Exception: "+e);
        }
    }
    public void finalizeFiles() {
        try {
            FileGenerator.writeToFile("aws-ec2.data",CLOSE_ARRAY,true);
        }catch ( IOException e){
            System.out.println("Exception: "+e);
        }
    }
}

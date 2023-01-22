package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.functions.v1.CloudFunction;
import com.google.cloud.functions.v1.CloudFunctionsServiceClient;
import com.google.cloud.functions.v1.ListFunctionsRequest;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.util.GCPlocationUtil;
import com.tmobile.pacbot.gcp.inventory.vo.CloudFunctionVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CloudFunctionGen1Collector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    @Autowired
    GCPlocationUtil gcPlocationUtil;
    private static final Logger logger = LoggerFactory.getLogger(CloudFunctionGen1Collector.class);

    public List<CloudFunctionVH> fetchCloudFunctionInventory(ProjectVH project) throws IOException, GeneralSecurityException {
        List<CloudFunctionVH> cloudFunctionVHList = new ArrayList<>();
        logger.info("in fetchCloudFunctionInventory services for Gen 1 ******** ");

        try{
            List<String> regionsList = gcPlocationUtil.getLocations(project.getProjectId());
            List<String> regions = regionsList.stream().distinct().collect(Collectors.toList());
            regions.remove("us");
            regions.remove("global");
            logger.info("fetching regions for CloudFunctionGen1Collector {} ::", regions.size());
            logger.debug("Project name: {} and id :{}", project.getProjectName(), project.getProjectId());
            CloudFunctionsServiceClient cloudFunctionsServiceClient = gcpCredentialsProvider.getFunctionClientGen1();
            for(String region : regions) {
                String parent = "projects/" + project.getProjectId() + "/locations/" + region;
                ListFunctionsRequest listFunctionsRequest = ListFunctionsRequest.newBuilder().setParent(parent).build();
                Iterable<CloudFunction> funcList = cloudFunctionsServiceClient.listFunctions(listFunctionsRequest).iterateAll();
                for (CloudFunction ob : funcList) {
                    CloudFunctionVH cloudFunctionVH = new CloudFunctionVH();
                    cloudFunctionVH.setId(ob.getName());
                    cloudFunctionVH.setFunctionName(ob.getName());
                    cloudFunctionVH.setProjectId(project.getProjectId());
                    cloudFunctionVH.setRegion(region);
                    cloudFunctionVH.setProjectName(project.getProjectName());
                    cloudFunctionVH.setIngressSetting(ob.getIngressSettings().toString());
                    cloudFunctionVH.setVpcConnector(ob.getVpcConnector());
                    cloudFunctionVH.setHttpTrigger(String.valueOf(ob.getHttpsTrigger().getSecurityLevel().getNumber()));
                    cloudFunctionVHList.add(cloudFunctionVH);
                }
            }
            cloudFunctionsServiceClient.close();
        }catch(Exception e){
            logger.error("Error occurred in CloudFunctionGen1Collector {} ::", e.getMessage());
        }
        return cloudFunctionVHList;
    }
}

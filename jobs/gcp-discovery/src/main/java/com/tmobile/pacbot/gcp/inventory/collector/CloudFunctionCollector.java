/*
package com.tmobile.pacbot.gcp.inventory.collector;

import com.google.cloud.functions.v2.Function;
import com.google.cloud.functions.v2.FunctionServiceClient;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.util.GCPlocationUtil;
import com.tmobile.pacbot.gcp.inventory.vo.CloudFunctionVH;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CloudFunctionCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;
    @Autowired
    GCPlocationUtil gcPlocationUtil;
    private static final Logger logger = LoggerFactory.getLogger(CloudFunctionCollector.class);
    public List<CloudFunctionVH> fetchCloudFunctionInventory(ProjectVH project) throws IOException {
        List<CloudFunctionVH>  cloudFunctionVHList = new ArrayList<>();
        logger.info("in fetchCloudFunctionInventory services ********");

        try{
            List<String> regionsList = gcPlocationUtil.getLocations(project.getProjectId());
            List<String> regions = regionsList.stream().distinct().collect(Collectors.toList());
            regions.remove("us");
            regions.remove("global");
            logger.info("fetching regions for cloudfunctioncollector {}::", regions.size());
            logger.debug("Project name: {} and id :{}", project.getProjectName(), project.getProjectId());
            FunctionServiceClient functionServiceClient = gcpCredentialsProvider.getFunctionClient();
            for(String region : regions) {
                String parent = "projects/"+project.getProjectId()+"/locations/"+region;
                logger.info("parent is {} ::", parent);
                FunctionServiceClient.ListFunctionsPagedResponse funcList = functionServiceClient.listFunctions(parent);

                logger.info("funcList is {} ::", funcList);
                if(funcList != null){
                    logger.info("funclist is not null");
                    for (Function ob : funcList.iterateAll()) {
                        logger.info("funclist object is {} ::", ob);
                        CloudFunctionVH cloudFunctionVH = new CloudFunctionVH();
                        cloudFunctionVH.setFunctionName(ob.getName());
                        cloudFunctionVH.setId(ob.getName());
                        cloudFunctionVH.setProjectId(project.getProjectId());
                        cloudFunctionVH.setProjectName(project.getProjectName());
                        cloudFunctionVH.setRegion(region);
                        String ingressSettings = ob.getServiceConfig().getIngressSettings().getValueDescriptor().toString();
                        String vpcConnector = ob.getServiceConfig().getVpcConnector();
                        cloudFunctionVH.setIngressSetting(ingressSettings);
                        cloudFunctionVH.setVpcConnector(vpcConnector);
                        cloudFunctionVHList.add(cloudFunctionVH);
                    }
                }
            }
            functionServiceClient.close();
        }catch(Exception e){
            logger.error("Error occurred in cloudFunctionCollector {} ", e.getMessage());
        }
        return cloudFunctionVHList;
    }
}
*/

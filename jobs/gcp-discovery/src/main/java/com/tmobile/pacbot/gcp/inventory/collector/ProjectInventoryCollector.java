package com.tmobile.pacbot.gcp.inventory.collector;


import com.google.gson.*;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.ProjectVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ProjectInventoryCollector {
    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    @Autowired
    CloudAssetInventoryCollector cloudAssetInventoryCollector;
    private static final Logger logger = LoggerFactory.getLogger(ProjectInventoryCollector.class);
    public List<ProjectVH>  fetchProjectMetadataMetadata(ProjectVH project) throws Exception {

        List<ProjectVH> projectMetadataVHList=new ArrayList<>();
        ProjectVH projectMetadataVH=new ProjectVH();
        logger.info(" metadata project id--> {}",project.getProjectId());
        projectMetadataVH.setProjectName(project.getProjectName());
        projectMetadataVH.setProjectId(project.getProjectId());
        projectMetadataVH.setId(project.getProjectId());
        projectMetadataVH.setProjectNumber(project.getProjectNumber());
        this.fetchComputeEngineMetadata(project.getProjectId().toString(),projectMetadataVH);
        projectMetadataVH.setCloudAsset(cloudAssetInventoryCollector.fetchCloudAssetDetails(project));
        projectMetadataVHList.add(projectMetadataVH);

        logger.info("project data {}",projectMetadataVH);
        return  projectMetadataVHList;
    }


    public void fetchComputeEngineMetadata(String projectId, ProjectVH projectMetadataVH) throws Exception {
         String apiUrlTemplate = "https://www.googleapis.com/compute/v1/projects/%s";

        String url = String.format(apiUrlTemplate,
            URLEncoder.encode(projectId,java.nio.charset.StandardCharsets.UTF_8.toString()));
            String accessToken = gcpCredentialsProvider.getAccessToken();
            String response = CommonUtils.doHttpGet(url, "Bearer",accessToken);
            JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();
        if(responseObj!=null) {
            if (responseObj.get("commonInstanceMetadata") != null) {
                JsonArray metadataList = responseObj.get("commonInstanceMetadata").getAsJsonObject().get("items").getAsJsonArray();

                if (metadataList.size() > 0) {
                    HashMap<String, Object> metadataVHList = new HashMap<>();
                    for (JsonElement metadataItem : metadataList
                    ) {

                        metadataVHList.put(metadataItem.getAsJsonObject().get("key").getAsString(), metadataItem.getAsJsonObject().get("value").getAsString());

                        projectMetadataVH.setComputeInstanceMetadata(metadataVHList);

                    }

                }
            }
        }
        }
}

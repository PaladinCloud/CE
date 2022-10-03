package com.tmobile.pacbot.gcp.inventory.collector;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.gcp.inventory.auth.GCPCredentialsProvider;
import com.tmobile.pacbot.gcp.inventory.vo.CloudAssetVH;
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
public class CloudAssetInventoryCollector {

    @Autowired
    GCPCredentialsProvider gcpCredentialsProvider;

    private static final Logger logger = LoggerFactory.getLogger(CloudAssetInventoryCollector.class);

    public CloudAssetVH fetchCloudAssetDetails(ProjectVH projectVH) throws Exception{

        logger.info("cloud Asset Details");
        String apiUrlTemplate = "https://serviceusage.googleapis.com/v1/projects/%s/services/cloudasset.googleapis.com";
        String url = String.format(apiUrlTemplate,
                URLEncoder.encode(projectVH.getProjectNumber().toString(),java.nio.charset.StandardCharsets.UTF_8.toString()));
        String accessToken = gcpCredentialsProvider.getAccessToken();
        String response = CommonUtils.doHttpGet(url, "Bearer",accessToken);
        JsonObject responseObj = JsonParser.parseString(response).getAsJsonObject();
        logger.info("cloud Asset response {}",responseObj);

        CloudAssetVH cloudAssetVH= new CloudAssetVH();
        if(responseObj!=null){
            cloudAssetVH.setName(responseObj.get("name").getAsString());
            cloudAssetVH.setState(responseObj.get("state").getAsString());

            if(responseObj.get("config")!=null){
                JsonObject config=responseObj.getAsJsonObject("config");
                HashMap<String, Object> configMap=   new Gson().fromJson(config.toString(), HashMap.class);
                cloudAssetVH.setConfig(configMap);

            }

        }

    return cloudAssetVH;
    }
}

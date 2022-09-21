package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.MySQLFlexibleVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySQLFlexibleInventoryCollector {
    @Autowired
    AzureCredentialProvider azureCredentialProvider;
    private static final String serverApiUrlTemplate = "https://management.azure.com/subscriptions/%s/providers/Microsoft.DBforMySQL/flexibleServers?api-version=2021-05-01";
    private static final Logger logger = LoggerFactory.getLogger(MySQLFlexibleInventoryCollector.class);
    private static final String configApiUrlTemplate = "https://management.azure.com/subscriptions/%s/resourceGroups/%s/providers/Microsoft.DBforMySQL/flexibleServers/%s/configurations?api-version=2021-05-01";

    public List<MySQLFlexibleVH> fetchMySQLFlexibleServerDetails(SubscriptionVH subscription)  {
        List<MySQLFlexibleVH> mySQLFlexibleVHList = new ArrayList<>();
        String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
        String url = null;
        try {
            url = String.format(serverApiUrlTemplate,
                    URLEncoder.encode(subscription.getSubscriptionId(),
                            java.nio.charset.StandardCharsets.UTF_8.toString()));
            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            logger.info("response form API: {}",response);
            logger.info("subscriptionName: {}", subscription.getSubscriptionName());
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            logger.info("JSON Response object {}",responseObj);
            JsonArray serverNames=responseObj.getAsJsonArray("value");
            for (int i=0;i<serverNames.size();i++) {
                MySQLFlexibleVH mySQLFlexibleVH=new MySQLFlexibleVH();
                String serverName=serverNames.get(i).getAsJsonObject().get("name").getAsString();
                String id=serverNames.get(i).getAsJsonObject().get("id").getAsString();
                int beginningIndex=id.indexOf("resourceGroups")+15;
                String resourceGroupName=(id).substring(beginningIndex,id.indexOf('/',beginningIndex+2));
                logger.debug("Resource group name: {}",resourceGroupName);
                String configUrl = String.format(configApiUrlTemplate,
                        URLEncoder.encode(subscription.getSubscriptionId(),
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(resourceGroupName,
                                java.nio.charset.StandardCharsets.UTF_8.toString()),
                        URLEncoder.encode(serverName,
                                java.nio.charset.StandardCharsets.UTF_8.toString()));
                String responseConfig = CommonUtils.doHttpGet(configUrl, "Bearer", accessToken);
                logger.info("response form API: {} ",
                        responseConfig);
                JsonObject responseConfigObj = new JsonParser().parse(responseConfig).getAsJsonObject();
                JsonArray value=responseConfigObj.getAsJsonArray("value");
                for (int j=0;j< value.size();j++)
                {
                    logger.debug("json object{} :{}",j,value.get(j));
                    JsonObject properties=value.get(j).getAsJsonObject().getAsJsonObject("properties");

                    String tlsVersion=properties.get("value").getAsString();
                    if(tlsVersion.startsWith("TLS"))
                    {
                        logger.debug("TLS version is :{}",tlsVersion);
                        mySQLFlexibleVH.setTlsVersion(tlsVersion);
                        mySQLFlexibleVH.setResourceGroupName(resourceGroupName);
                        mySQLFlexibleVH.setId(id);
                        mySQLFlexibleVH.setSubscriptionName(subscription.getSubscriptionName());
                        break;
                    }
                }
                mySQLFlexibleVHList.add(mySQLFlexibleVH);
            }



        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mySQLFlexibleVHList;
    }
}

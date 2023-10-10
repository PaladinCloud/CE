package com.tmobile.cso.pacman.datashipper.entity;

import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.util.AssetGroupUtil;
import com.tmobile.cso.pacman.datashipper.util.AuthManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

public class AssetsCountManager implements Constants {

    private static final String ASSET_API_URL = System.getenv("ASSET_API_URL");
    private static final Logger log = LoggerFactory.getLogger(AssetsCountManager.class);
    private List<Map<String,String>> errorList = new ArrayList<>();

    public List<Map<String, String>> populateAssetCount(String platform, List<String> accountIds){
        String token;
        try {
            token = getToken();
        } catch (Exception e1) {
            log.error("populateAssetCount failed as unable to authenticate " , e1);
            Map<String,String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in populateAssetCount. Authorisation failed");
            errorMap.put(ERROR_TYPE,FATAL);
            errorMap.put(EXCEPTION, e1.getMessage());
            errorList.add(errorMap);
            return errorList;
        }

        for (String accountId : accountIds) {
            String assetCount;
            try {
                if(platform.equals("azure")) {
                    String combinedSubscriptionStr = getSubscriptionsForTenant(accountId);
                    String[] subscriptionArray = combinedSubscriptionStr.split(",");
                    Integer totalAssetCount = 0;
                    for(String subscriptionStr : subscriptionArray){
                        String assetCountForSubscription = AssetGroupUtil.fetchAssetCount(ASSET_API_URL, token, platform, subscriptionStr);
                        Integer aCount = Integer.parseInt(assetCountForSubscription);
                        String query="UPDATE cf_AzureTenantSubscription SET assets="+assetCountForSubscription+" WHERE subscription ='"+subscriptionStr+"'";
                        totalAssetCount+=aCount;
                        RDSDBManager.executeUpdate(query);
                    }
                    assetCount = totalAssetCount.toString();
                }
                else {
                    assetCount = AssetGroupUtil.fetchAssetCount(ASSET_API_URL, token, platform, accountId);
                }
            } catch (Exception e1) {
                log.error("fetchAssetCount failed as unable to fetch asset groups ", e1);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in fetchAssetCount");
                errorMap.put(ERROR_TYPE, ERROR);
                errorMap.put(EXCEPTION, e1.getMessage());
                errorList.add(errorMap);
                return errorList;
            }
            int updated=RDSDBManager.executeUpdate("UPDATE cf_Accounts SET assets="+assetCount+" WHERE accountId='"+accountId+"'");
            if(updated==0){
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Not updated the cf_Account with assets");
                errorMap.put(ERROR_TYPE, ERROR);
                errorMap.put(EXCEPTION,"Unable to update the cf_Acoounts with number of assets");
                errorList.add(errorMap);
            }

        }
        return errorList;
    }

    private String getSubscriptionsForTenant(String tenant) {
        List<Map<String, String>> subscriptions =  RDSDBManager.executeQuery("SELECT subscription FROM cf_AzureTenantSubscription WHERE tenant='"+tenant+"'");
        Iterator<Map<String, String>> it = subscriptions.iterator();
        StringBuilder subscriptionList = new StringBuilder();
        while(it.hasNext()) {
            Map<String, String> row = it.next();
            String subscription = row.get("subscription");
            subscriptionList.append(subscription);
            subscriptionList.append(",");
        }
        return subscriptionList.substring(0, subscriptionList.length() - 1);
    }

    private String getToken() {
        return AuthManager.getToken();
    }
}
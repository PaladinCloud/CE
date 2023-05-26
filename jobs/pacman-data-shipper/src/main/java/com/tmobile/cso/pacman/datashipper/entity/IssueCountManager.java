package com.tmobile.cso.pacman.datashipper.entity;
import com.microsoft.azure.management.resources.Subscription;
import com.tmobile.cso.pacman.datashipper.util.AuthManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.util.AssetGroupUtil;

import java.util.*;

public class IssueCountManager implements Constants{
    private static final String COMP_API_URL = System.getenv("CMPL_API_URL");
    private static final Logger log = LoggerFactory.getLogger(IssueCountManager.class);
    private List<Map<String,String>> errorList = new ArrayList<>();
    public List<Map<String, String>> populateViolationsCount(){
        String token;
        try {
            token = getToken();
        } catch (Exception e1) {
            log.error("populateViolationsCount failed as unable to authenticate " , e1);
            Map<String,String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in populateViolationsCount. Authorisation failed");
            errorMap.put(ERROR_TYPE,FATAL);
            errorMap.put(EXCEPTION, e1.getMessage());
            errorList.add(errorMap);
            return errorList;
        }
        List<Map<String, String>> headersfromRDS = RDSDBManager.executeQuery("SELECT accountId,platform FROM cf_Accounts where accountStatus='configured'");
        Iterator<Map<String, String>> it = headersfromRDS.iterator();

        while(it.hasNext()) {
            Map<String, String> account = it.next();
            String accountId = account.get("accountId");
            String platform = account.get("platform");
            String assetCount;
            try {
                if(platform.equals("azure")) {
                    String combinedSubscriptionStr = getSubscriptionsForTenant(accountId);
                    String[] subscriptionArray = combinedSubscriptionStr.split(",");
                    Integer totalViolationCount = 0;
                    for(String subscriptionStr : subscriptionArray){
                        String violationCountForSubscription = AssetGroupUtil.fetchViolationsCount(COMP_API_URL, token, platform, subscriptionStr);
                        Integer vCount = Integer.parseInt(violationCountForSubscription);
                        String query="UPDATE cf_AzureTenantSubscription SET violations="+violationCountForSubscription+" WHERE subscription ='"+subscriptionStr+"'";
                        totalViolationCount+=vCount;
                        RDSDBManager.executeUpdate(query);
                    }
                    assetCount = totalViolationCount.toString();
                }
                else {
                    assetCount = AssetGroupUtil.fetchViolationsCount(COMP_API_URL, token, platform, accountId);
                }
            } catch (Exception e1) {
                log.error("populateViolationsCount failed as unable to fetch issues count", e1);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in populateViolationsCount");
                errorMap.put(ERROR_TYPE, ERROR);
                errorMap.put(EXCEPTION, e1.getMessage());
                errorList.add(errorMap);
                return errorList;
            }
            int updated=RDSDBManager.executeUpdate("UPDATE cf_Accounts SET violations="+assetCount+" WHERE accountId='"+accountId+"'");
            if(updated==0){
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Not updated the cf_Account with violations");
                errorMap.put(ERROR_TYPE, ERROR);
                errorMap.put(EXCEPTION,"Unable to update the cf_Acoounts with number of violations");
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
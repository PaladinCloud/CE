/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.entity;

import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.util.AssetGroupUtil;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IssueCountManager implements Constants {
    private static final Logger log = LoggerFactory.getLogger(IssueCountManager.class);

    public List<Map<String, String>> populateViolationsCount(String platform, List<String> accountIds) {
        List<Map<String, String>> errorList = new ArrayList<>();
        for (String accountId : accountIds) {
            String assetCount;
            try {
                if (platform.equals("azure")) {
                    String combinedSubscriptionStr = getSubscriptionsForTenant(accountId);
                    String[] subscriptionArray = combinedSubscriptionStr.split(",");
                    int totalViolationCount = 0;
                    for (String subscriptionStr : subscriptionArray) {
                        String violationCountForSubscription = AssetGroupUtil.fetchViolationsCount(platform, subscriptionStr);
                        int vCount = Integer.parseInt(violationCountForSubscription);
                        String query = "UPDATE cf_AzureTenantSubscription SET violations=" + violationCountForSubscription + " WHERE subscription ='" + subscriptionStr + "'";
                        totalViolationCount += vCount;
                        RDSDBManager.executeUpdate(query);
                    }
                    assetCount = Integer.toString(totalViolationCount);
                } else {
                    assetCount = AssetGroupUtil.fetchViolationsCount(platform, accountId);
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
            int updated = RDSDBManager.executeUpdate("UPDATE cf_Accounts SET violations=" + assetCount + " WHERE accountId='" + accountId + "'");
            if (updated == 0) {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Not updated the cf_Account with violations");
                errorMap.put(ERROR_TYPE, ERROR);
                errorMap.put(EXCEPTION, "Unable to update the cf_Acoounts with number of violations");
                errorList.add(errorMap);
            }
        }
        return errorList;
    }

    private String getSubscriptionsForTenant(String tenant) {
        List<Map<String, String>> subscriptions = RDSDBManager.executeQuery("SELECT subscription FROM cf_AzureTenantSubscription WHERE tenant='" + tenant + "'");
        Iterator<Map<String, String>> it = subscriptions.iterator();
        StringBuilder subscriptionList = new StringBuilder();
        while (it.hasNext()) {
            Map<String, String> row = it.next();
            String subscription = row.get("subscription");
            subscriptionList.append(subscription);
            subscriptionList.append(",");
        }
        return subscriptionList.substring(0, subscriptionList.length() - 1);
    }
}

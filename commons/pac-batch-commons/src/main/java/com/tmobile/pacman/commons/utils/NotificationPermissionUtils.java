/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.pacman.commons.utils;

import com.google.gson.Gson;
import com.tmobile.pacman.commons.dto.NotificationBaseRequest;
import com.tmobile.pacman.commons.dto.PaladinAccessToken;
import com.tmobile.pacman.commons.dto.PermissionVH;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NotificationPermissionUtils {
    private static final String NOTIFICATION_URL = "notification.lambda.function.url";
    static String SUBJECT = "Insufficient permissions";
    static String opsEventName = "Permission denied for %s";
    static int numberOfPermissionIssuesPerEmail = 40;
    private static PaladinAccessToken accessToken;
    static String notificationsLink = System.getProperty("pacman.host") + "/pl/notifications/notifications-list";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationPermissionUtils.class);

    public static void triggerNotificationForPermissionDenied(List<PermissionVH> permissionVHList, String cloudType) {
        try {
            Gson gson = new Gson();
            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
            if (!permissionVHList.isEmpty()) {
                notificationDetailsList = getPermissionNotificationRequest(cloudType, permissionVHList);
            }
            if (!notificationDetailsList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationDetailsList);
                invokeNotificationUrl(notificationDetailsStr);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("{} Notifications sent for permission of cloud type \"{}\" ", notificationDetailsList.size(), cloudType);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error triggering lambda function url, notification request not sent. Error - {}", e.getMessage());
        }
    }

    private static NotificationBaseRequest getNotificationBaseRequest(JSONObject payload, String cloudType) {
        NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
        notificationBaseRequest.setEventCategory(Constants.NotificationTypes.PERMISSION);
        notificationBaseRequest.setSubject(SUBJECT);
        notificationBaseRequest.setEventCategoryName(Constants.NotificationTypes.PERMISSION.getValue());
        notificationBaseRequest.setEventName(String.format(String.format(opsEventName, cloudType)));
        notificationBaseRequest.setEventDescription(String.format(String.format(opsEventName, cloudType)));
        notificationBaseRequest.setEventSourceName(cloudType);
        notificationBaseRequest.setPayload(payload);
        return notificationBaseRequest;
    }

    private static List<NotificationBaseRequest> getPermissionNotificationRequest(String cloudType, List<PermissionVH> permissionVHList) {
        int numberOfPermissionIssues = 0;
        List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
        JSONObject payload = new JSONObject();
        for (PermissionVH permissionVH : permissionVHList) {
            for (Map.Entry<String, List<String>> assetPermission : permissionVH.getAssetPermissionIssues().entrySet()) {
                numberOfPermissionIssues++;
                payload.put("permissionIssueFor" + assetPermission.getKey().toUpperCase() + "InAccount" + permissionVH.getAccountNumber(), assetPermission.toString());
                if (numberOfPermissionIssues % numberOfPermissionIssuesPerEmail == 0) {
                    payload.put("cloudType", cloudType);
                    payload.put("message", "Unable to collect data due to missing permission");
                    payload.put("sequenceNumber", String.valueOf(numberOfPermissionIssues / numberOfPermissionIssuesPerEmail));
                    payload.put("notificationsLink", notificationsLink);
                    notificationDetailsList.add(getNotificationBaseRequest(payload, cloudType));
                    payload = new JSONObject();
                }
            }
        }
        if (!payload.isEmpty()) {
            payload.put("cloudType", cloudType);
            payload.put("message", "Unable to collect data due to missing permission");
            payload.put("sequenceNumber", String.valueOf((numberOfPermissionIssues / numberOfPermissionIssuesPerEmail) + 1));
            payload.put("notificationsLink", notificationsLink);
            notificationDetailsList.add(getNotificationBaseRequest(payload, cloudType));
        }
        return notificationDetailsList;
    }

    public static void invokeNotificationUrl(String notificationDetailsStr) throws Exception {
        String credentials = System.getProperty(Constants.API_AUTH_INFO);
        String authApiUrl = System.getenv(Constants.AUTH_API_URL);
        String notifyUrl = System.getProperty(NOTIFICATION_URL);
        Map<String, String> headersMap = new HashMap<>();
        if (accessToken == null || accessToken.getExpiresAt() <= System.currentTimeMillis()) {
            accessToken = com.tmobile.pacman.commons.utils.CommonUtils.getAccessToken(authApiUrl, credentials);
        }
        headersMap.put("Authorization", accessToken.getToken());
        com.tmobile.pacman.commons.utils.CommonUtils.doHttpPost(notifyUrl, notificationDetailsStr, headersMap);
    }
}

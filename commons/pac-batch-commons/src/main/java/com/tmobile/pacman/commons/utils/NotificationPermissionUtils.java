package com.tmobile.pacman.commons.utils;

import com.google.gson.Gson;
import com.tmobile.pacman.commons.dto.NotificationBaseRequest;
import com.tmobile.pacman.commons.dto.PermissionNotificationRequest;
import com.tmobile.pacman.commons.dto.PermissionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NotificationPermissionUtils {
    private static final String NOTIFICATION_URL ="notification.lambda.function.url" ;
    static String SUBJECT = "Insufficient permissions";

    static String opsEventName = "Permission denied for %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationPermissionUtils.class);
    public static void triggerNotificationsForPermissionDenied(List<PermissionVH> permissionVHList,String cloudType) {
        try {
            Gson gson = new Gson();
            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
            for (PermissionVH permissionVH : permissionVHList) {
                notificationDetailsList.add(getNotificationBaseRequest(permissionVH,cloudType));
            }
            if (!notificationDetailsList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationDetailsList);
                String notifyUrl = System.getProperty(NOTIFICATION_URL);
                com.tmobile.pacman.commons.utils.CommonUtils.doHttpPost(notifyUrl, notificationDetailsStr);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("{} Notifications sent for permission of cloud type \"{}\" ", notificationDetailsList.size(), cloudType);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error triggering lambda function url, notification request not sent. Error - {}", e.getMessage());
        }
    }
    private static NotificationBaseRequest getNotificationBaseRequest(PermissionVH permissionVH,String cloudType) {
        NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
        notificationBaseRequest.setEventCategory(Constants.NotificationTypes.Permissions);
        notificationBaseRequest.setSubject(SUBJECT);
        notificationBaseRequest.setEventCategoryName(Constants.NotificationTypes.Permissions.getValue());
        notificationBaseRequest.setEventName(String.format(String.format(opsEventName, permissionVH.getErrorVH().getType())));
        notificationBaseRequest.setEventDescription(String.format(String.format(opsEventName, permissionVH.getErrorVH().getType())));

        PermissionNotificationRequest permissionNotificationRequest = getPermissionNotificationRequest(permissionVH,cloudType);
        notificationBaseRequest.setPayload(permissionNotificationRequest);
        return notificationBaseRequest;
    }

    private static PermissionNotificationRequest getPermissionNotificationRequest(PermissionVH permissionVH,String cloudType) {
        PermissionNotificationRequest permissionNotificationRequest = new PermissionNotificationRequest();
        permissionNotificationRequest.setPermission(permissionVH.getErrorVH().getException());
        permissionNotificationRequest.setCloudType(cloudType);
        permissionNotificationRequest.setMessage("Unable to collect data due to missing permission");
        permissionNotificationRequest.setAccountNumber(permissionVH.getAccountNumber());
        permissionNotificationRequest.setAssetType(permissionVH.getErrorVH().getType());
        return permissionNotificationRequest;
    }

}

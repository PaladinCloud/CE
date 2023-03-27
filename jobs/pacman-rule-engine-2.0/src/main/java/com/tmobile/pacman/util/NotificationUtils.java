package com.tmobile.pacman.util;

import com.google.gson.Gson;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.dto.NotificationBaseRequest;
import com.tmobile.pacman.commons.dto.PolicyViolationNotificationRequest;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tmobile.pacman.common.PacmanSdkConstants.*;
import static com.tmobile.pacman.commons.PacmanSdkConstants.POLICY_ID;

public class NotificationUtils {

    private NotificationUtils(){
    }
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationUtils.class);

    public static void triggerNotificationsForViolations(List<Annotation> annotations, Map<String, Map<String, String>> existingIssuesMap, boolean isOpen) {
        try {
            Gson gson = new Gson();
            String hostName = CommonUtils.getPropValue(HOSTNAME);
            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();

            for (Annotation annotation : annotations) {
                String annotationId = CommonUtils.getUniqueAnnotationId(annotation);
                annotation.put(PacmanSdkConstants.ANNOTATION_PK, annotationId);
                Map<String, String> issueAttributes = existingIssuesMap.get(annotationId);
                if (isOpen && null == issueAttributes && PacmanSdkConstants.STATUS_OPEN.equals(annotation.get(PacmanSdkConstants.ISSUE_STATUS_KEY))) {
                    notificationDetailsList.add(getNotificationBaseRequest(annotation, hostName, true, CREATE_VIOLATION_EVENT_NAME));
                } else if (!isOpen) {
                    if (!(!existingIssuesMap.containsKey(annotationId)
                            || PacmanSdkConstants.STATUS_CLOSE.equals(annotation.get(PacmanSdkConstants.ISSUE_STATUS_KEY)))) {
                        notificationDetailsList.add(getNotificationBaseRequest(annotation, hostName, false, CLOSE_VIOLATION_EVENT_NAME));
                    }
                }
            }
            if (!notificationDetailsList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationDetailsList);
                String notifyUrl = CommonUtils.getPropValue(PacmanSdkConstants.NOTIFICATION_URL);
                com.tmobile.pacman.commons.utils.CommonUtils.doHttpPost(notifyUrl, notificationDetailsStr);
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("{} Notifications sent for violations of policy \"{}\" ",notificationDetailsList.size(),annotations.get(0).get(POLICY_NAME));
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error triggering lambda function url, notification request not sent. Error - {}",e.getMessage());
        }
    }

    private static NotificationBaseRequest getNotificationBaseRequest(Annotation annotation, String hostName, boolean isOpen, String violationEventName) {
        NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
        notificationBaseRequest.setEventCategory(Constants.NotificationTypes.VIOLATIONS);
        notificationBaseRequest.setSubject(isOpen?OPEN_VIOLATIONS_SUBJECT:CLOSE_VIOLATIONS_SUBJECT);
        notificationBaseRequest.setEventCategoryName(VIOLATION_CATEGORY_NAME);
        notificationBaseRequest.setEventName(String.format(String.format(violationEventName,annotation.get(POLICY_NAME))));
        notificationBaseRequest.setEventDescription(String.format(String.format(violationEventName,annotation.get(POLICY_NAME))));


        PolicyViolationNotificationRequest request = new PolicyViolationNotificationRequest();
        request.setIssueId(annotation.get(PacmanSdkConstants.ANNOTATION_PK));
        request.setIssueIdLink(hostName + ISSUE_ID_UI_PATH + annotation.get(ANNOTATION_PK) + "?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
        request.setAction(isOpen? Constants.Actions.CREATE:Constants.Actions.CLOSE);
        request.setPolicyName(annotation.get(POLICY_NAME));
        request.setPolicyNameLink(hostName + POLICY_DETAILS_UI_PATH + annotation.get(POLICY_ID) + "/true?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
        request.setResourceId(annotation.get(RESOURCE_ID));
        request.setResourceIdLink(hostName + ASSET_DETAILS_UI_PATH + annotation.get(TARGET_TYPE) + "/" + annotation.get(RESOURCE_ID) + "?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
        request.setDescription(annotation.get(PacmanSdkConstants.DESCRIPTION));
        request.setScanTime(CommonUtils.getCurrentDateStringWithFormat(
                PacmanSdkConstants.PAC_TIME_ZONE, PacmanSdkConstants.DATE_FORMAT));
        notificationBaseRequest.setPayload(request);
        return notificationBaseRequest;
    }
}

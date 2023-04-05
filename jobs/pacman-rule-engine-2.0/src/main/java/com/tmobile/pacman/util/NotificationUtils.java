package com.tmobile.pacman.util;

import com.google.gson.Gson;
import com.tmobile.pacman.common.AutoFixAction;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.dto.NotificationBaseRequest;
import com.tmobile.pacman.dto.AutoFixTransaction;
import com.tmobile.pacman.dto.PolicyViolationNotificationRequest;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.utils.Constants;
import com.tmobile.pacman.dto.AutofixNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        notificationBaseRequest.setEventCategoryName(Constants.NotificationTypes.VIOLATIONS.getValue());
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

    public static boolean triggerAutoFixNotification(Map<String, String> policyParam, AutoFixAction autofixActionEmail, Map<String,String> annotation) {
        try {
            List<NotificationBaseRequest> notificationBaseRequestList = new ArrayList<>();
            Gson gson = new Gson();
            String hostName = CommonUtils.getPropValue(HOSTNAME);
            NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
            notificationBaseRequest.setEventCategory(Constants.NotificationTypes.AUTOFIX);
            notificationBaseRequest.setEventCategoryName(Constants.NotificationTypes.AUTOFIX.getValue());
            AutofixNotificationRequest autofixNotificationRequest = new AutofixNotificationRequest();
            notificationBaseRequest.setPayload(autofixNotificationRequest);
            autofixNotificationRequest.setPolicyName(policyParam.get(POLICY_DISPLAY_NAME));
            autofixNotificationRequest.setPolicyNameLink(hostName + POLICY_DETAILS_UI_PATH + annotation.get(POLICY_ID) + "/true?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
            autofixNotificationRequest.setResourceId(annotation.get(RESOURCE_ID));
            autofixNotificationRequest.setResourceIdLink(hostName + ASSET_DETAILS_UI_PATH + annotation.get(TARGET_TYPE) + "/" + annotation.get(RESOURCE_ID) + "?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
            autofixNotificationRequest.setSeverity(policyParam.get("severity").toUpperCase());
            autofixNotificationRequest.setWaitingTime(policyParam.get(PacmanSdkConstants.AUTOFIX_POLICY_WAITING_TIME));
            autofixNotificationRequest.setIssueId(annotation.get(PacmanSdkConstants.ANNOTATION_PK));

            String accountName = annotation.get("accountname");
            if (autofixActionEmail == AutoFixAction.AUTOFIX_ACTION_EMAIL && "Sandbox".equalsIgnoreCase(accountName)) {
                notificationBaseRequest.setSubject("(Sandbox) : " + AUTOFIX_WARNING_SUBJECT);
                notificationBaseRequest.setEventName(String.format(AUTOFIX_WARNING_EVENT_NAME, annotation.get(RESOURCE_ID)));
                notificationBaseRequest.setEventDescription(String.format(AUTOFIX_WARNING_EVENT_NAME, annotation.get(RESOURCE_ID)));
                autofixNotificationRequest.setDiscoveredOn(getDateTimeInUserFormat(annotation.get(CREATED_DATE)));
                autofixNotificationRequest.setAction(AutoFixAction.AUTOFIX_ACTION_EMAIL);
                autofixNotificationRequest.setIssueIdLink(hostName + ISSUE_ID_UI_PATH + annotation.get(ANNOTATION_PK) + "?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
            } else if (autofixActionEmail == AutoFixAction.AUTOFIX_ACTION_EMAIL) {
                notificationBaseRequest.setSubject(AUTOFIX_WARNING_SUBJECT);
                notificationBaseRequest.setEventName(String.format(AUTOFIX_WARNING_EVENT_NAME, annotation.get(RESOURCE_ID)));
                notificationBaseRequest.setEventDescription(String.format(AUTOFIX_WARNING_EVENT_NAME, annotation.get(RESOURCE_ID)));
                autofixNotificationRequest.setDiscoveredOn(getDateTimeInUserFormat(annotation.get(CREATED_DATE)));
                autofixNotificationRequest.setAction(AutoFixAction.AUTOFIX_ACTION_EMAIL);
                autofixNotificationRequest.setIssueIdLink(hostName + ISSUE_ID_UI_PATH + annotation.get(ANNOTATION_PK) + "?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
            } else if (autofixActionEmail == AutoFixAction.AUTOFIX_ACTION_FIX) {
                notificationBaseRequest.setSubject(AUTOFIX_APPLIED);
                notificationBaseRequest.setEventName(String.format(AUTOFIX_APPLIED_EVENT_NAME, annotation.get(RESOURCE_ID)));
                notificationBaseRequest.setEventDescription(String.format(AUTOFIX_APPLIED_EVENT_NAME, annotation.get(RESOURCE_ID)));
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(NOTIFICATION_TIME_FORMAT);
                LocalDateTime presentTime = LocalDateTime.now(Clock.systemUTC());
                autofixNotificationRequest.setAutofixedOn(dtf.format(presentTime));
                autofixNotificationRequest.setAction(AutoFixAction.AUTOFIX_ACTION_FIX);
                autofixNotificationRequest.setDiscoveredOn(getDateTimeInUserFormat(annotation.get(CREATED_DATE)));
            } else if (autofixActionEmail == AutoFixAction.AUTOFIX_ACTION_EXEMPTED) {
                notificationBaseRequest.setSubject(AUTOFIX_EXEMPTION_SUBJECT);
                notificationBaseRequest.setEventName(String.format(AUTOFIX_EXEMPTION_EVENT_NAME, policyParam.get(POLICY_DISPLAY_NAME)));
                notificationBaseRequest.setEventDescription(String.format(AUTOFIX_EXEMPTION_EVENT_NAME, policyParam.get(POLICY_DISPLAY_NAME)));
                autofixNotificationRequest.setAction(AutoFixAction.AUTOFIX_ACTION_EXEMPTED);
                autofixNotificationRequest.setIssueIdLink(hostName + ISSUE_ID_UI_PATH + annotation.get(ANNOTATION_PK) + "?ag=" + annotation.get(PacmanSdkConstants.DATA_SOURCE_KEY));
                autofixNotificationRequest.setDiscoveredOn(getDateTimeInUserFormat(annotation.get(CREATED_DATE)));
            }
            notificationBaseRequestList.add(notificationBaseRequest);
            if (!notificationBaseRequestList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationBaseRequestList);
                String notifyUrl = CommonUtils.getPropValue(PacmanSdkConstants.NOTIFICATION_URL);
                com.tmobile.pacman.commons.utils.CommonUtils.doHttpPost(notifyUrl, notificationDetailsStr);
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("Autofix Notification sent for violation of policy \"{}\" for resource \"{}\"",policyParam.get(POLICY_DISPLAY_NAME), annotation.get(RESOURCE_ID));
                }
            }
        }
        catch(Exception exception){
            LOGGER.error("exception occurred in triggerAutofixNotification with message - {}",exception.getMessage());
            return false;
        }
        return true;
    }

    private static String getDateTimeInUserFormat(String createdDateStr){
        try{
            String[] cDate = createdDateStr.split("\\.");
            String createdDate = cDate[0];
            LocalDateTime createdLocalDateTime = LocalDateTime.parse(createdDate);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(NOTIFICATION_TIME_FORMAT);
            return dtf.format(createdLocalDateTime);
        }
        catch (Exception exception){
            LOGGER.error("error in getting createdDate - {}",exception.getMessage());
            return "";
        }

    }

    public static void triggerSilentAutofixNotification(List<AutoFixTransaction> silentautoFixTrans, Map<String, String> policyParam) {

        try{
            List<NotificationBaseRequest> notificationBaseRequestList = new ArrayList<>();
            Gson gson = new Gson();
            String hostName = CommonUtils.getPropValue(HOSTNAME);

            for(AutoFixTransaction autoFixTransaction: silentautoFixTrans){
                NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
                notificationBaseRequest.setEventCategory(Constants.NotificationTypes.AUTOFIX);
                notificationBaseRequest.setEventCategoryName(Constants.NotificationTypes.AUTOFIX.getValue());
                notificationBaseRequest.setSubject(SILENT_AUTOFIX_SUBJECT);
                notificationBaseRequest.setEventName(String.format(SILENT_AUTOFIX_EVENT_NAME,autoFixTransaction.getResourceId()));
                notificationBaseRequest.setEventDescription(String.format(SILENT_AUTOFIX_EVENT_NAME,autoFixTransaction.getResourceId()));
                AutofixNotificationRequest autofixNotificationRequest = new AutofixNotificationRequest();
                notificationBaseRequest.setPayload(autofixNotificationRequest);
                autofixNotificationRequest.setPolicyName(policyParam.get(POLICY_DISPLAY_NAME));
                autofixNotificationRequest.setPolicyNameLink(hostName + POLICY_DETAILS_UI_PATH + policyParam.get(POLICY_ID) + "/true?ag=" + policyParam.get("assetGroup"));
                autofixNotificationRequest.setResourceId(autoFixTransaction.getResourceId());
                autofixNotificationRequest.setResourceIdLink(hostName + ASSET_DETAILS_UI_PATH + policyParam.get(TARGET_TYPE) + "/" + autoFixTransaction.getResourceId() + "?ag=" + policyParam.get("assetGroup"));
                autofixNotificationRequest.setSeverity(policyParam.get("severity").toUpperCase());
                autofixNotificationRequest.setWaitingTime(policyParam.get(PacmanSdkConstants.AUTOFIX_POLICY_WAITING_TIME));
                autofixNotificationRequest.setIssueId(autoFixTransaction.getIssueId());
                autofixNotificationRequest.setAction(AutoFixAction.AUTOFIX_ACTION_FIX);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(NOTIFICATION_TIME_FORMAT);
                LocalDateTime presentTime = LocalDateTime.now(Clock.systemUTC());
                String discoveredOn = autoFixTransaction.getAdditionalInfo();
                autofixNotificationRequest.setDiscoveredOn(getDateTimeInUserFormat(discoveredOn));
                autofixNotificationRequest.setAutofixedOn(dtf.format(presentTime));
                notificationBaseRequestList.add(notificationBaseRequest);
            }
            if (!notificationBaseRequestList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationBaseRequestList);
                String notifyUrl = CommonUtils.getPropValue(PacmanSdkConstants.NOTIFICATION_URL);
                com.tmobile.pacman.commons.utils.CommonUtils.doHttpPost(notifyUrl, notificationDetailsStr);
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("{} Silent Autofix Notification sent for violations of policy \"{}\"",silentautoFixTrans.size(),policyParam.get(POLICY_DISPLAY_NAME));
                }
            }
        }
        catch(Exception exception){
            LOGGER.error("exception occurred in triggerAutofixNotification with message - {}",exception.getMessage());
        }
    }
}

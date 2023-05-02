package com.tmobile.pacman.api.admin.repository.service;

import com.google.gson.Gson;
import com.tmobile.pacman.api.admin.domain.AssetGroupExceptionDetailsRequest;
import com.tmobile.pacman.api.admin.dto.StickyExNotificationRequest;
import com.tmobile.pacman.api.admin.repository.model.AssetGroupException;
import com.tmobile.pacman.api.commons.dto.NotificationBaseRequest;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tmobile.pacman.api.admin.common.AdminConstants.*;
import static com.tmobile.pacman.api.commons.Constants.*;

@Component
public class NotificationServiceImpl implements NotificationService {

    @Value("${notification.lambda.function.url}")
    private String notificationUrl;

    /** The ui host. */
    @Value("${pacman.host}")
    private String hostName;

    @Autowired
    PacmanRdsRepository pacmanRdsRepository;

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Async
    public void triggerNotificationForCreateStickyEx(AssetGroupExceptionDetailsRequest assetGroupExceptionDetails, String userId, String subject, List<String> policyIds, Actions action) {
        try {
            Gson gson = new Gson();
            if (!policyIds.isEmpty()) {
                String combinedPolicyIdStr = String.join(",", policyIds.stream().map(str -> "'" + str + "'").collect(Collectors.toList()));
                List<Map<String, Object>> policyIdPolicyNameList = pacmanRdsRepository.getDataFromPacman("SELECT policyId, policyDisplayName FROM cf_PolicyTable WHERE policyId in (" + combinedPolicyIdStr + ")");
                String combinedPolicyNameStr = policyIdPolicyNameList.stream().map(obj -> (String) obj.get("policyDisplayName")).collect(Collectors.joining(","));

                List<NotificationBaseRequest> notificationBaseRequestList = new ArrayList<>();
                NotificationBaseRequest notificationBaseRequest = action==Actions.CREATE?getNotificationBaseRequestObj( String.format(CREATE_EXCEPTION_EVENT_NAME,assetGroupExceptionDetails.getExceptionName().trim()), CREATE_STICKY_EXCEPTION_SUBJECT):getNotificationBaseRequestObj( String.format(UPDATE_EXCEPTION_EVENT_NAME,assetGroupExceptionDetails.getExceptionName().trim()), UPDATE_STICKY_EXCEPTION_SUBJECT);
                StickyExNotificationRequest stickyExNotificationRequest = new StickyExNotificationRequest();
                stickyExNotificationRequest.setExceptionName(assetGroupExceptionDetails.getExceptionName().trim());
                stickyExNotificationRequest.setAssetGroup(assetGroupExceptionDetails.getAssetGroup().trim());
                stickyExNotificationRequest.setExceptionReason(assetGroupExceptionDetails.getExceptionReason().trim());
                stickyExNotificationRequest.setExpiringOn(assetGroupExceptionDetails.getExpiryDate());
                stickyExNotificationRequest.setUserId(assetGroupExceptionDetails.getCreatedBy());
                stickyExNotificationRequest.setPolicyNames(combinedPolicyNameStr);
                stickyExNotificationRequest.setType("sticky");
                stickyExNotificationRequest.setAction(action);
                notificationBaseRequest.setPayload(stickyExNotificationRequest);
                notificationBaseRequestList.add(notificationBaseRequest);
                String notificationDetailsStr = gson.toJson(notificationBaseRequestList);
                PacHttpUtils.doHttpPost(notificationUrl, notificationDetailsStr);
                if(log.isInfoEnabled()){
                    log.info("Notification request sent for create/update of sticky exception - {}",assetGroupExceptionDetails.getExceptionName().trim());
                }
            }
            else{
                log.info("Notification request not sent for create/update of sticky exception as no policy ids are selected.");
            }
        }

        catch (Exception e) {
                log.error("Error triggering lambda function url, notification request not sent for create/update sticky exemption. Error - {}",e.getMessage());
        }
    }


    @Async
    public void triggerNotificationForDelStickyException(AssetGroupException assetGroupException, String userId, String subject, String deletedBy){
        try {
            Gson gson = new Gson();
            List<NotificationBaseRequest> notificationBaseRequestList = new ArrayList<>();
            NotificationBaseRequest notificationBaseRequest = getNotificationBaseRequestObj( String.format(DELETE_EXCEPTION_EVENT_NAME,assetGroupException.getExceptionName().trim()), DELETE_STICKY_EXCEPTION_SUBJECT);
            StickyExNotificationRequest stickyExNotificationRequest = new StickyExNotificationRequest();
            stickyExNotificationRequest.setExceptionName(assetGroupException.getExceptionName().trim());
            stickyExNotificationRequest.setUserId(deletedBy);
            stickyExNotificationRequest.setType("sticky");
            stickyExNotificationRequest.setAction(Actions.DELETE);
            notificationBaseRequest.setPayload(stickyExNotificationRequest);
            notificationBaseRequestList.add(notificationBaseRequest);
            String notificationDetailsStr = gson.toJson(notificationBaseRequestList);
            PacHttpUtils.doHttpPost(notificationUrl, notificationDetailsStr);
            if(log.isInfoEnabled()){
                log.info("Notification request sent for delete of sticky exception - {}",assetGroupException.getExceptionName().trim());
            }
        }
        catch (Exception e) {
            log.error("Error triggering lambda function url, notification request not sent for delete sticky exemption. Error - {}",e.getMessage());
        }
    }

    private static NotificationBaseRequest getNotificationBaseRequestObj( String eventName, String subject){
        NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
        notificationBaseRequest.setEventCategory(NotificationTypes.EXEMPTIONS);
        notificationBaseRequest.setEventCategoryName(NotificationTypes.EXEMPTIONS.getValue());
        notificationBaseRequest.setEventName(eventName);
        notificationBaseRequest.setEventDescription(eventName);
        notificationBaseRequest.setSubject(subject);
        return notificationBaseRequest;
    }
}

package com.tmobile.pacman.api.compliance.service;

import com.google.gson.Gson;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import com.tmobile.pacman.api.compliance.dto.IndividualExNotificationRequest;
import com.tmobile.pacman.api.commons.dto.NotificationBaseRequest;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
import com.tmobile.pacman.api.compliance.domain.IssuesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.tmobile.pacman.api.commons.Constants.*;
import static com.tmobile.pacman.api.compliance.util.Constants.*;

@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired
    PacmanRdsRepository pacmanRdsRepository;
    @Value("${notification.lambda.function.url}")
    private String notificationUrl;

    /** The ui host. */
    @Value("${pacman.host}")
    private String hostName;

    @Autowired
    private CredentialProvider credentialProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Async
    public void triggerCreateExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds, IssuesException issuesException){
        try {
            Gson gson = new Gson();
            List<String> exemptedPoliciesList = issueDetails.stream().map(obj -> (String)obj.get(POLICYID)).map(obj -> "'"+obj+"'").collect(Collectors.toList());
            String combinedPolicyStr = String.join(",",exemptedPoliciesList);
            List<Map<String, Object>> policyIdPolicyNameList = pacmanRdsRepository.getDataFromPacman("SELECT policyId, policyDisplayName FROM cf_PolicyTable WHERE policyId in ("+combinedPolicyStr+")");
            Map<String, String> policyIdPolicyNameMap = policyIdPolicyNameList.stream().collect(Collectors.toMap(obj -> (String)obj.get("policyId"), obj -> (String)obj.get("policyDisplayName")));

            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
            for(Map<String,Object> issueDetail : issueDetails){
                if(!failedIssueIds.contains(issueDetail.get(ES_DOC_ID_KEY))){
                    notificationDetailsList.add(getNotifyBaseReqForExemption( issueDetail, issuesException, CREATE_EXEMPTION_SUBJECT, hostName, policyIdPolicyNameMap, true, CREATE_EXEMPTION_EVENT_NAME));
                }
            }

            if (!notificationDetailsList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationDetailsList);
                PacHttpUtils.doHttpPost(notificationUrl, notificationDetailsStr);
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("{} Notifications sent for create exemption for issueIds - {}",notificationDetailsList.size(),issueDetails.stream().map(obj -> (String)obj.get(ES_DOC_ID_KEY)).filter(obj -> !failedIssueIds.contains(obj)).collect(Collectors.joining(",")));
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error triggering lambda function url, notification request not sent for create exemption. Error message - {}",e.getMessage());
        }
    }
    private NotificationBaseRequest getNotifyBaseReqForExemption(Map<String,Object> issueDetail, IssuesException issuesException, String subject, String hostName, Map<String, String> policyIdPolicyNameMap, boolean isCreate, String eventName){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if(issueDetail.get(POLICYID)!=null && policyIdPolicyNameMap.get(issueDetail.get(POLICYID))!=null) {
            NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
            notificationBaseRequest.setEventCategory(NotificationTypes.EXEMPTIONS);
            notificationBaseRequest.setEventCategoryName(NotificationTypes.EXEMPTIONS.getValue());
            notificationBaseRequest.setEventName(String.format(eventName, issueDetail.get(RESOURCEID)));
            notificationBaseRequest.setEventDescription(String.format(eventName, issueDetail.get(RESOURCEID)));
            notificationBaseRequest.setSubject(subject);
            IndividualExNotificationRequest request = new IndividualExNotificationRequest();
            request.setIssueId((String) issueDetail.get(ES_DOC_ID_KEY));
            request.setResourceId((String)issueDetail.get(RESOURCEID));
            request.setPolicyName(policyIdPolicyNameMap.get(issueDetail.get(POLICYID)));
            request.setIssueIdLink(hostName + ISSUE_ID_UI_PATH + issueDetail.get(ES_DOC_ID_KEY) + "?ag=" + issueDetail.get(DATA_SOURCE_KEY));
            request.setResourceIdLink(hostName + ASSET_DETAILS_UI_PATH + issueDetail.get(TARGET_TYPE) + "/" + issueDetail.get(RESOURCEID) + "?ag=" + issueDetail.get(DATA_SOURCE_KEY));
            request.setPolicyNameLink(hostName + POLICY_DETAILS_UI_PATH + issueDetail.get(POLICYID) + "/true?ag=" + issueDetail.get(DATA_SOURCE_KEY));
            request.setType("individual");
            Map<String,String> tagsKeyAndValueMap = new HashMap<>();
            issueDetail.entrySet().stream().filter(obj -> obj.getKey().startsWith("tags.")).forEach(obj -> tagsKeyAndValueMap.put(obj.getKey().substring(5), (String)obj.getValue()));
            request.getAdditionalInfo().put(TAG_DETAILS,tagsKeyAndValueMap);
            request.getAdditionalInfo().put(CLOUD_TYPE,issueDetail.get(DATA_SOURCE_KEY));
            request.getAdditionalInfo().put(TARGET_TYPE,issueDetail.get(TARGET_TYPE));
            if(isCreate){
                request.setExemptionGrantedDate(sdf.format(issuesException.getExceptionGrantedDate()));
                request.setExemptionReason(issuesException.getExceptionReason());
                request.setCreatedBy(issuesException.getCreatedBy());
                request.setExemptionExpiringOn(sdf.format(issuesException.getExceptionEndDate()));
                request.setAction(Actions.CREATE);
            }
            else{
                request.setAction(Actions.REVOKE);
            }
            notificationBaseRequest.setPayload(request);
            return notificationBaseRequest;
        }
        else{
            return null;
        }
    }

    @Async
    public void triggerRevokeExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds, String subject, String revokedBy){
        try {
            Gson gson = new Gson();
            List<String> exemptedPoliciesList = issueDetails.stream().map(obj -> (String)obj.get(POLICYID)).map(obj -> "'"+obj+"'").collect(Collectors.toList());
            String combinedPolicyStr = String.join(",",exemptedPoliciesList);
            List<Map<String, Object>> policyIdPolicyNameList = pacmanRdsRepository.getDataFromPacman("SELECT policyId, policyDisplayName FROM cf_PolicyTable WHERE policyId in ("+combinedPolicyStr+")");
            Map<String, String> policyIdPolicyNameMap = policyIdPolicyNameList.stream().collect(Collectors.toMap(obj -> (String)obj.get("policyId"), obj -> (String)obj.get("policyDisplayName")));

            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
            for(Map<String,Object> issueDetail : issueDetails){
                if(!failedIssueIds.contains(issueDetail.get(ES_DOC_ID_KEY))){
                    NotificationBaseRequest nbRequest = getNotifyBaseReqForExemption( issueDetail, null, REVOKE_EXEMPTION_SUBJECT, hostName, policyIdPolicyNameMap,false, REVOKE_EXEMPTION_EVENT_NAME);
                    IndividualExNotificationRequest individualExNotificationRequest = (IndividualExNotificationRequest)nbRequest.getPayload();
                    individualExNotificationRequest.setCreatedBy(revokedBy);
                    notificationDetailsList.add(nbRequest);
                }
            }
            if (!notificationDetailsList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationDetailsList);
                PacHttpUtils.doHttpPost(notificationUrl, notificationDetailsStr);
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("{} Notifications sent for revoke exemption for issueIds - {}",notificationDetailsList.size(),issueDetails.stream().map(obj -> (String)obj.get(ES_DOC_ID_KEY)).filter(obj -> !failedIssueIds.contains(obj)).collect(Collectors.joining(",")));
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error triggering lambda function url, notification request not sent for revoke exemption. Error - {}",e.getMessage());
        }
    }
}

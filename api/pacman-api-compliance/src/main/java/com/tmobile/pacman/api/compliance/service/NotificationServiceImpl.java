package com.tmobile.pacman.api.compliance.service;

import com.google.gson.Gson;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import com.tmobile.pacman.api.commons.utils.ThreadLocalUtil;
import com.tmobile.pacman.api.compliance.domain.ExemptionRequest;
import com.tmobile.pacman.api.compliance.dto.IndividualExNotificationRequest;
import com.tmobile.pacman.api.commons.dto.NotificationBaseRequest;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.commons.utils.PacHttpUtils;
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
    private static String notificationUrl;

    /** The ui host. */
    @Value("${pacman.host}")
    private String hostName;

    @Autowired
    private CredentialProvider credentialProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Async
    public void triggerCreateExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds, ExemptionRequest issuesException){
        try {
            Gson gson = new Gson();
            List<String> exemptedPoliciesList = issueDetails.stream().map(obj -> (String)obj.get(POLICYID)).map(obj -> "'"+obj+"'").collect(Collectors.toList());
            String combinedPolicyStr = String.join(",",exemptedPoliciesList);
            List<Map<String, Object>> policyIdPolicyNameList = pacmanRdsRepository.getDataFromPacman("SELECT policyId, policyDisplayName FROM cf_PolicyTable WHERE policyId in ("+combinedPolicyStr+")");
            Map<String, String> policyIdPolicyNameMap = policyIdPolicyNameList.stream().collect(Collectors.toMap(obj -> (String)obj.get("policyId"), obj -> (String)obj.get("policyDisplayName")));

            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
            for(Map<String,Object> issueDetail : issueDetails){
                if (!failedIssueIds.contains(issueDetail.get(ES_DOC_ID_KEY))) {
                    switch (String.valueOf(issueDetail.get(STATUS))) {
                        case EXEMPTION_REQUEST_REVOKED:
                            notificationDetailsList.add(getNotifyBaseReqForExemption(issueDetail, issuesException, hostName,
                                    policyIdPolicyNameMap, Actions.REVOKE_EXEMPTION_REQUEST));
                            break;
                        case EXEMPTION_REQUEST_RAISED:
                            notificationDetailsList.add(getNotifyBaseReqForExemption(issueDetail, issuesException, hostName,
                                    policyIdPolicyNameMap, Actions.CREATE_EXEMPTION_REQUEST));
                            break;
                        case EXEMPTION_REQUEST_CANCELLED:
                            notificationDetailsList.add(getNotifyBaseReqForExemption(issueDetail, issuesException, hostName,
                                    policyIdPolicyNameMap, Actions.CANCEL_EXEMPTION_REQUEST));
                            break;
                        case EXEMPTION_REQUEST_APPROVED:
                            notificationDetailsList.add(getNotifyBaseReqForExemption(issueDetail, issuesException, hostName,
                                    policyIdPolicyNameMap, Actions.APPROVE_EXEMPTION_REQUEST));
                            break;
                        case EXEMPT:
                        default:
                            notificationDetailsList.add(getNotifyBaseReqForExemption(issueDetail, issuesException, hostName,
                                    policyIdPolicyNameMap, Actions.CREATE));
                            break;
                    }
                }
            }
            if (!notificationDetailsList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationDetailsList);
                invokeNotificationUrl(notificationDetailsStr);
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("{} Notifications sent for create exemption for issueIds - {}",notificationDetailsList.size(),issueDetails.stream().map(obj -> (String)obj.get(ES_DOC_ID_KEY)).filter(obj -> !failedIssueIds.contains(obj)).collect(Collectors.joining(",")));
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error triggering lambda function url, notification request not sent for create exemption. Error message - {}",e.getMessage());
        }
    }

    private NotificationBaseRequest getNotifyBaseReqForExemption(Map<String,Object> issueDetail,
                                                                 ExemptionRequest issuesException, String hostName,
                                                                 Map<String, String> policyIdPolicyNameMap,
                                                                 Actions action){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if(issueDetail.get(POLICYID)!=null && policyIdPolicyNameMap.get(issueDetail.get(POLICYID))!=null) {
            NotificationBaseRequest notificationBaseRequest = new NotificationBaseRequest();
            notificationBaseRequest.setEventCategory(NotificationTypes.EXEMPTION);
            notificationBaseRequest.setEventCategoryName(NotificationTypes.EXEMPTION.getValue());
            IndividualExNotificationRequest request = new IndividualExNotificationRequest();
            request.setIssueId((String) issueDetail.get(ES_DOC_ID_KEY));
            request.setDocid((String) issueDetail.get(ES_DOC_ID_KEY));
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
            switch (action) {
                case CREATE:
                    notificationBaseRequest.setEventName(String.format(
                            CREATE_EXEMPTION_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setEventDescription(String.format(
                            CREATE_EXEMPTION_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setSubject(CREATE_EXEMPTION_SUBJECT);
                    request.setExemptionGrantedDate(sdf.format(issuesException.getExceptionGrantedDate()));
                    request.setExemptionReason(issuesException.getExceptionReason());
                    request.setCreatedBy(issuesException.getCreatedBy());
                    request.setExemptionExpiringOn(sdf.format(issuesException.getExceptionEndDate()));
                    request.setAction(action);
                    break;
                case REVOKE:
                    notificationBaseRequest.setEventName(String.format(REVOKE_EXEMPTION_EVENT_NAME,
                            issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setEventDescription(String.format(REVOKE_EXEMPTION_EVENT_NAME,
                            issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setSubject(REVOKE_EXEMPTION_SUBJECT);
                    request.setCreatedBy(issuesException.getCreatedBy());
                    request.setAction(action);
                    break;
                case CREATE_EXEMPTION_REQUEST:
                    notificationBaseRequest.setEventName(String.format(CREATE_EXEMPTION_REQUEST_EVENT_NAME,
                            issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setEventDescription(String.format(CREATE_EXEMPTION_REQUEST_EVENT_NAME,
                            issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setSubject(CREATE_EXEMPTION_REQUEST_SUBJECT);
                    request.setExemptionReason(issuesException.getExceptionReason());
                    request.setCreatedBy(issuesException.getCreatedBy());
                    request.setExemptionExpiringOn(sdf.format(issuesException.getExceptionEndDate()));
                    request.setAction(action);
                    break;
                case REVOKE_EXEMPTION_REQUEST:
                    notificationBaseRequest.setEventName(String.format(
                            REVOKE_EXEMPTION_REQUEST_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setEventDescription(String.format(
                            REVOKE_EXEMPTION_REQUEST_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setSubject(REVOKE_EXEMPTION_REQUEST_SUBJECT);
                    request.setCreatedBy(issuesException.getCreatedBy());
                    request.setAction(action);
                    break;
                case CANCEL_EXEMPTION_REQUEST:
                    notificationBaseRequest.setEventName(String.format(
                            CANCEL_EXEMPTION_REQUEST_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setEventDescription(String.format(
                            CANCEL_EXEMPTION_REQUEST_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setSubject(CANCEL_EXEMPTION_REQUEST_SUBJECT);
                    request.setCreatedBy(issuesException.getCreatedBy());
                    request.setAction(action);
                    break;
                case APPROVE_EXEMPTION_REQUEST:
                    notificationBaseRequest.setEventName(String.format(
                            APPROVE_EXEMPTION_REQUEST_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setEventDescription(String.format(
                            APPROVE_EXEMPTION_REQUEST_EVENT_NAME, issueDetail.get(RESOURCEID)));
                    notificationBaseRequest.setSubject(APPROVE_EXEMPTION_REQUEST_SUBJECT);
                    /*In email, createdBy field is used to show approvedBy*/
                    request.setCreatedBy(issuesException.getApprovedBy());
                    request.setAction(action);
                    break;
                default:
                    break;
            }
            notificationBaseRequest.setPayload(request);
            return notificationBaseRequest;
        }
        else{
            return null;
        }
    }

    @Async
    public void triggerRevokeExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds,
                                                   String revokedBy){
        try {
            Gson gson = new Gson();
            List<String> exemptedPoliciesList = issueDetails.stream().map(obj -> (String)obj.get(POLICYID)).map(obj -> "'"+obj+"'").collect(Collectors.toList());
            String combinedPolicyStr = String.join(",",exemptedPoliciesList);
            List<Map<String, Object>> policyIdPolicyNameList = pacmanRdsRepository.getDataFromPacman("SELECT policyId, policyDisplayName FROM cf_PolicyTable WHERE policyId in ("+combinedPolicyStr+")");
            Map<String, String> policyIdPolicyNameMap = policyIdPolicyNameList.stream().collect(Collectors.toMap(obj -> (String)obj.get("policyId"), obj -> (String)obj.get("policyDisplayName")));

            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
            for(Map<String,Object> issueDetail : issueDetails){
                if(!failedIssueIds.contains(issueDetail.get(ES_DOC_ID_KEY))){
                    ExemptionRequest exemptionRequest = new ExemptionRequest();
                    exemptionRequest.setCreatedBy(revokedBy);
                    NotificationBaseRequest nbRequest = getNotifyBaseReqForExemption(issueDetail, exemptionRequest,
                            hostName, policyIdPolicyNameMap,Actions.REVOKE);
                    IndividualExNotificationRequest individualExNotificationRequest = (IndividualExNotificationRequest)nbRequest.getPayload();
                    individualExNotificationRequest.setCreatedBy(revokedBy);
                    notificationDetailsList.add(nbRequest);
                }
            }
            if (!notificationDetailsList.isEmpty()) {
                String notificationDetailsStr = gson.toJson(notificationDetailsList);
                invokeNotificationUrl(notificationDetailsStr);
                if(LOGGER.isInfoEnabled()){
                    LOGGER.info("{} Notifications sent for revoke exemption for issueIds - {}",notificationDetailsList.size(),issueDetails.stream().map(obj -> (String)obj.get(ES_DOC_ID_KEY)).filter(obj -> !failedIssueIds.contains(obj)).collect(Collectors.joining(",")));
                }
            }
        }
        catch (Exception e) {
            LOGGER.error("Error triggering lambda function url, notification request not sent for revoke exemption. Error - {}",e.getMessage());
        }
    }

    public static void invokeNotificationUrl(String notificationDetailsStr) throws Exception {
        Map<String,String> headersMap = new HashMap<>();
        headersMap.put("Authorization", ThreadLocalUtil.accessToken.get());
        PacHttpUtils.doHttpPost(notificationUrl, notificationDetailsStr,headersMap);
    }
}

package com.tmobile.pacman.api.compliance.service;

import com.amazonaws.auth.BasicSessionCredentials;
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
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

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
            if(isCreate){
                IndividualExNotificationRequest createRequest = new IndividualExNotificationRequest();
                createRequest.setIssueId((String) issueDetail.get(ES_DOC_ID_KEY));
                createRequest.setExemptionGrantedDate(sdf.format(issuesException.getExceptionGrantedDate()));
                createRequest.setExemptionReason(issuesException.getExceptionReason());
                createRequest.setResourceId((String)issueDetail.get(RESOURCEID));
                createRequest.setCreatedBy(getUserName(issuesException.getCreatedBy()));
                createRequest.setExemptionExpiringOn(sdf.format(issuesException.getExceptionEndDate()));
                createRequest.setPolicyName(policyIdPolicyNameMap.get(issueDetail.get(POLICYID)));
                createRequest.setIssueIdLink(hostName + ISSUE_ID_UI_PATH + issueDetail.get(ES_DOC_ID_KEY) + "?ag=" + issueDetail.get(DATA_SOURCE_KEY));
                createRequest.setResourceIdLink(hostName + ASSET_DETAILS_UI_PATH + issueDetail.get(TARGET_TYPE) + "/" + issueDetail.get(RESOURCEID) + "?ag=" + issueDetail.get(DATA_SOURCE_KEY));
                createRequest.setPolicyNameLink(hostName + POLICY_DETAILS_UI_PATH + issueDetail.get(POLICYID) + "/true?ag=" + issueDetail.get(DATA_SOURCE_KEY));
                createRequest.setType("individual");
                createRequest.setAction(Actions.CREATE);
                notificationBaseRequest.setPayload(createRequest);
                return notificationBaseRequest;
            }
            else{
                IndividualExNotificationRequest revokeRequest = new IndividualExNotificationRequest();
                revokeRequest.setIssueId((String) issueDetail.get(ES_DOC_ID_KEY));
                revokeRequest.setResourceId((String)issueDetail.get(RESOURCEID));
                revokeRequest.setPolicyName(policyIdPolicyNameMap.get(issueDetail.get(POLICYID)));
                revokeRequest.setIssueIdLink(hostName + ISSUE_ID_UI_PATH + issueDetail.get(ES_DOC_ID_KEY) + "?ag=" + issueDetail.get(DATA_SOURCE_KEY));
                revokeRequest.setResourceIdLink(hostName + ASSET_DETAILS_UI_PATH + issueDetail.get(TARGET_TYPE) + "/" + issueDetail.get(RESOURCEID) + "?ag=" + issueDetail.get(DATA_SOURCE_KEY));
                revokeRequest.setPolicyNameLink(hostName + POLICY_DETAILS_UI_PATH + issueDetail.get(POLICYID) + "/true?ag=" + issueDetail.get(DATA_SOURCE_KEY));
                revokeRequest.setType("individual");
                revokeRequest.setAction(Actions.REVOKE);
                notificationBaseRequest.setPayload(revokeRequest);
                return notificationBaseRequest;
            }
        }
        else{
            return null;
        }
    }

    @Async
    public void triggerRevokeExemptionNotification(List<Map<String, Object>> issueDetails, List<String> failedIssueIds, String subject){
        try {
            Gson gson = new Gson();
            List<String> exemptedPoliciesList = issueDetails.stream().map(obj -> (String)obj.get(POLICYID)).map(obj -> "'"+obj+"'").collect(Collectors.toList());
            String combinedPolicyStr = String.join(",",exemptedPoliciesList);
            List<Map<String, Object>> policyIdPolicyNameList = pacmanRdsRepository.getDataFromPacman("SELECT policyId, policyDisplayName FROM cf_PolicyTable WHERE policyId in ("+combinedPolicyStr+")");
            Map<String, String> policyIdPolicyNameMap = policyIdPolicyNameList.stream().collect(Collectors.toMap(obj -> (String)obj.get("policyId"), obj -> (String)obj.get("policyDisplayName")));

            List<NotificationBaseRequest> notificationDetailsList = new ArrayList<>();
            for(Map<String,Object> issueDetail : issueDetails){
                if(!failedIssueIds.contains(issueDetail.get(ES_DOC_ID_KEY))){
                    notificationDetailsList.add(getNotifyBaseReqForExemption( issueDetail, null, REVOKE_EXEMPTION_SUBJECT, hostName, policyIdPolicyNameMap,false, REVOKE_EXEMPTION_EVENT_NAME));
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

    private String getUserName(String userId){
        String region = System.getenv("AWS_USERPOOL_REGION");
        String userPoolId = System.getenv("USERPOOL_ID");

        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();

        Region reg = Region.of(region);
        CognitoIdentityProviderClient identityProviderClient = CognitoIdentityProviderClient.builder()
                .region(reg).credentialsProvider(StaticCredentialsProvider
                        .create(AwsSessionCredentials
                                .create(credentials.getAWSAccessKeyId(), credentials.getAWSSecretKey(), credentials.getSessionToken()))).build();

        Map<String, Object> userInfo = getUserInfo(identityProviderClient, userPoolId, userId);
        return (String)userInfo.get(EMAIL);
    }


    public Map<String, Object> getUserInfo(CognitoIdentityProviderClient client, String userPoolId, String username){
        AdminGetUserResponse userDetails = client.adminGetUser(AdminGetUserRequest.builder().userPoolId(userPoolId).username(username).build());
        Map<String, Object> userInfo=new HashMap<>();
        userInfo.put("userId",userDetails.username());

        List<AttributeType> userAttributes = userDetails.userAttributes();
        userAttributes.forEach(attribute->{
            LOGGER.info("Attribute name: {} value: {}",attribute.name(),attribute.value());
            if(attribute.name().equalsIgnoreCase("name")){
                userInfo.put("userName", attribute.value());
            } else if(attribute.name().equalsIgnoreCase("given_name")){
                userInfo.put("firstName", attribute.value());
            } else if(attribute.name().equalsIgnoreCase("family_name")){
                userInfo.put("lastName", attribute.value());
            } else if(attribute.name().equalsIgnoreCase(EMAIL)){
                userInfo.put(EMAIL, attribute.value());
            }else {
                userInfo.put(attribute.name(), attribute.value());
            }
        });
        return userInfo;
    }
}

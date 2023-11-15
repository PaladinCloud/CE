package com.tmobile.cloud.awsrules.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.util.CollectionUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

public class S3PacbotUtils {
    private static final Logger logger = LoggerFactory.getLogger(S3PacbotUtils.class);
    public static final String CLOUD_TRAIL_URL = "/aws/cloudtrail/_search";
    private static final String CLOUD_TRAIL_EVENT_SELECTOR_URL = "/aws/cloudtrail_eventselector/_search";
    public static final String CLOUD_TRAIL_MSG = "CloudTrail log with matching conditions does not exists,accountId: ";
    private static final String DATA_RESOURCE_VALUE = "arn:aws:s3";
    private static final String DATA_RESOURCE_TYPE = "AWS::S3::Object";
    private static final List<String> READ_TYPES = Arrays.asList("All", "ReadOnly");
    private static final List<String> WRITE_TYPES = Arrays.asList("All", "WriteOnly");


    private S3PacbotUtils() {

    }

    /**
     * This method is to check whether s3 bucket has read/write/full control
     *
     * @param grants
     * @param accessTypeToCheck
     * @return List<Permission>, if permissions found else empty
     */
    private static Set<Permission> checkAnyGrantHasOpenToReadOrWriteAccess(List<Grant> grants, String accessTypeToCheck) {

        Set<Permission> permissions = new HashSet();
        for (Grant grant : grants) {
            if ((PacmanRuleConstants.ANY_S3_AUTHENTICATED_USER_URI
                    .equalsIgnoreCase(grant.getGrantee().getIdentifier()) || PacmanRuleConstants.ALL_S3_USER_URI
                    .equalsIgnoreCase(grant.getGrantee().getIdentifier()))

                    &&

                    (accessTypeToCheck.contains(grant.getPermission()
                            .toString()) || grant.getPermission().toString()
                            .equalsIgnoreCase(PacmanRuleConstants.FULL_CONTROL))) {
                permissions.add(grant.getPermission());
            }
        }
        return permissions;
    }

    /**
     * @param awsS3Client
     * @param s3BucketName
     * @param accessType
     * @return
     */
    public static Set<Permission> checkACLPermissions(AmazonS3Client awsS3Client, String s3BucketName, String accessType) {
        AccessControlList bucketAcl;
        Set<Permission> permissionList = new HashSet<>();
        try {
            bucketAcl = awsS3Client.getBucketAcl(s3BucketName);
            List<Grant> grants = bucketAcl.getGrantsAsList();
            if (!CollectionUtils.isNullOrEmpty(grants)) {
                permissionList = checkAnyGrantHasOpenToReadOrWriteAccess(grants, accessType);
            }
        } catch (AmazonS3Exception s3Exception) {
            logger.error("error : ", s3Exception);
            if (s3Exception.getMessage().contains("The specified bucket does not exist")) {
                throw new RuntimeException(s3Exception);
            }
            throw new RuleExecutionFailedExeption(s3Exception.getMessage());
        }
        return permissionList;
    }

    public static Map<String, Boolean> getPublicAccessPolicy(AmazonS3Client awsS3Client, String s3BucketName, String accessType) {

        Map<String, Boolean> map = new HashMap<>();
        JsonArray jsonArray = getPolicyArray(awsS3Client, s3BucketName);

        if (jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject principal = new JsonObject();
                String actionString = null;
                String conditionStr = null;
                String effect = null;
                String principalStr = null;
                String aws = null;

                JsonObject conditionJsonObject = new JsonObject();
                JsonArray conditionJsonArray = new JsonArray();
                JsonArray actionJsonArray = new JsonArray();
                List<String> conditionList = new ArrayList<>();

                JsonObject firstObject = (JsonObject) jsonArray.get(i);
                if (firstObject.has(PacmanRuleConstants.PRINCIPAL) && firstObject.get(PacmanRuleConstants.PRINCIPAL).isJsonObject()) {
                    principal = firstObject.get(PacmanRuleConstants.PRINCIPAL).getAsJsonObject();
                } else {
                    if (firstObject.has(PacmanRuleConstants.PRINCIPAL)) {
                        principalStr = firstObject.get(PacmanRuleConstants.PRINCIPAL).getAsString();
                    }
                }
                try {
                    if (principal.has("AWS") || "*".equals(principalStr)) {

                        JsonArray awsArray = null;
                        if (principal.has("AWS") && principal.get("AWS").isJsonArray()) {
                            awsArray = principal.get("AWS").getAsJsonArray();
                            if (awsArray.size() > 0) {
                                logger.debug(
                                        "Not checking the s3 read/write public access for principal array values : {}",
                                        awsArray);
                            }
                        }

                        if (principal.has("AWS") && !principal.get("AWS").isJsonArray()) {
                            aws = principal.get("AWS").getAsString();
                        }
                        if ("*".equals(principalStr)) {
                            aws = firstObject.get(PacmanRuleConstants.PRINCIPAL).getAsString();
                        }

                        if ("*".equals(aws) && !firstObject.has(PacmanRuleConstants.CONDITION)) {
                            if (firstObject.get(PacmanRuleConstants.ACTION).isJsonObject()) {
                                JsonObject actionJson = firstObject.get(PacmanRuleConstants.ACTION).getAsJsonObject();
                                actionString = actionJson.getAsString();
                            } else if (firstObject.get(PacmanRuleConstants.ACTION).isJsonArray()) {
                                actionJsonArray = firstObject.get(PacmanRuleConstants.ACTION).getAsJsonArray();
                            } else {
                                actionString = firstObject.get(PacmanRuleConstants.ACTION).getAsString();
                            }

                            effect = firstObject.get(PacmanRuleConstants.EFFECT).getAsString();
                            if (firstObject.has(PacmanRuleConstants.CONDITION)
                                    && (firstObject.get(PacmanRuleConstants.CONDITION).getAsJsonObject()
                                    .has(PacmanRuleConstants.IP_ADDRESS_CAP))
                                    && (firstObject.get(PacmanRuleConstants.CONDITION).getAsJsonObject()
                                    .get(PacmanRuleConstants.IP_ADDRESS_CAP).getAsJsonObject()
                                    .has(PacmanRuleConstants.SOURCE_IP))) {
                                if (firstObject.get(PacmanRuleConstants.CONDITION).getAsJsonObject()
                                        .get(PacmanRuleConstants.IP_ADDRESS_CAP).getAsJsonObject()
                                        .get(PacmanRuleConstants.SOURCE_IP).isJsonObject()) {
                                    conditionJsonObject = firstObject.get(PacmanRuleConstants.CONDITION)
                                            .getAsJsonObject().get(PacmanRuleConstants.IP_ADDRESS_CAP)
                                            .getAsJsonObject().get(PacmanRuleConstants.SOURCE_IP).getAsJsonObject();
                                } else if (firstObject.get(PacmanRuleConstants.CONDITION).getAsJsonObject()
                                        .get(PacmanRuleConstants.IP_ADDRESS_CAP).getAsJsonObject()
                                        .get(PacmanRuleConstants.SOURCE_IP).isJsonArray()) {
                                    conditionJsonArray = firstObject.get(PacmanRuleConstants.CONDITION)
                                            .getAsJsonObject().get(PacmanRuleConstants.IP_ADDRESS_CAP)
                                            .getAsJsonObject().get(PacmanRuleConstants.SOURCE_IP).getAsJsonArray();
                                } else {
                                    conditionStr = firstObject.get(PacmanRuleConstants.CONDITION).getAsJsonObject()
                                            .get(PacmanRuleConstants.IP_ADDRESS_CAP).getAsJsonObject()
                                            .get(PacmanRuleConstants.SOURCE_IP).getAsString();
                                }
                            }

                            JsonElement cJson = conditionJsonArray;
                            Type listType = new TypeToken<List<String>>() {
                            }.getType();

                            conditionList = new Gson().fromJson(cJson, listType);
                            if (!org.apache.commons.lang.StringUtils.isEmpty(actionString)) {
                                map = getReadWriteAccess(actionString, accessType, effect, conditionJsonObject,
                                        conditionList, conditionStr, map);
                            }
                            if (actionJsonArray.size() > 0) {
                                for (int j = 0; j < actionJsonArray.size(); j++) {
                                    actionString = actionJsonArray.get(j).getAsString();
                                    map = getReadWriteAccess(actionString, accessType, effect, conditionJsonObject,
                                            conditionList, conditionStr, map);
                                }
                            }
                        }
                    }
                } catch (Exception e1) {
                    logger.error("error", e1);
                    throw new RuleExecutionFailedExeption(e1.getMessage());
                }
            }
        }
        return map;
    }

    private static Map<String, Boolean> getReadWriteAccess(String actionString,
                                                           String accessType, String effect, JsonObject conditionJsonObject,
                                                           List<String> conditionList, String conditionStr,
                                                           Map<String, Boolean> accessMap) {
        if ((actionString.startsWith(PacmanRuleConstants.S3_PUT) || actionString
                .startsWith("s3:*"))
                && accessType.contains(PacmanRuleConstants.WRITE_ACCESS)
                && (PacmanRuleConstants.ALLOW.equalsIgnoreCase(effect))) {
            getReadOrWriteAccessDetails(PacmanRuleConstants.WRITE, accessMap,
                    PacmanRuleConstants.CIDR_FILTERVALUE, conditionStr,
                    conditionJsonObject, conditionList);

        } else if ((actionString.startsWith(PacmanRuleConstants.S3_GET) || actionString
                .startsWith("s3:*"))
                && accessType.contains(PacmanRuleConstants.READ_ACCESS)
                && (PacmanRuleConstants.ALLOW.equalsIgnoreCase(effect))) {
            getReadOrWriteAccessDetails("Read", accessMap,
                    PacmanRuleConstants.CIDR_FILTERVALUE, conditionStr,
                    conditionJsonObject, conditionList);

        }
        return accessMap;
    }

    private static Map<String, Boolean> getReadOrWriteAccessDetails(
            String type, Map<String, Boolean> accessMap, String publicIp,
            String conditionStr, JsonObject conditionJsonObject,
            List<String> conditionList) {
        if ((conditionJsonObject.size() == 0) && (conditionList.isEmpty()) && null == conditionStr) {
            accessMap.put(type, true);
        }
        if (!conditionJsonObject.isJsonNull()) {
            if (conditionJsonObject.toString().equals(publicIp)) {
                accessMap.put(type, true);
            }
        }
        if (null != conditionStr && conditionStr.contains(publicIp)) {
            accessMap.put(type, true);
        }
        if (conditionList.contains(publicIp)) {
            accessMap.put(type, true);
        }
        return accessMap;
    }

    public static Map<String, Boolean> checkS3HasOpenAccess(String checkId,
                                                            String accountId, String esUrl, String resourceId) throws Exception {
        Map<String, Boolean> publicAccess = new HashMap<>();
        String resourceinfo = PacmanUtils.getQueryDataForCheckid(checkId, esUrl, resourceId, null, accountId);
        if (org.apache.commons.lang.StringUtils.isNotEmpty(resourceinfo)) {
            resourceinfo = resourceinfo.substring(1, resourceinfo.length() - 1);

            Map<String, Object> resourceinfoMap = new HashMap<>();
            String[] pairs = resourceinfo.split(",");
            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                String[] keyValue = pair.split(":");
                String key = keyValue[0].replace("\"", "");
                String value = keyValue[1].replace("\"", "");
                if ("Bucket Name".equals(key) && "null".equals(value)) {
                    logger.info("bucket name is null");
                } else {
                    resourceinfoMap.put(key, value);
                }
            }

            processResourceInfoMap(resourceinfoMap, resourceId, publicAccess);
        }
        return publicAccess;
    }

    private static Map<String, Boolean> processResourceInfoMap(Map<String, Object> resourceinfoMap, String resourceId, Map<String, Boolean> publicAccess) {
        String policyAllowsAccess = null;
        String aclAllowsAccess = null;
        if (resourceinfoMap.get("Bucket Name").equals(resourceId)) {
            policyAllowsAccess = resourceinfoMap.get("Policy Allows Access").toString();
            aclAllowsAccess = resourceinfoMap.get("ACL Allows List").toString();
            if (!com.amazonaws.util.StringUtils.isNullOrEmpty(policyAllowsAccess) && "Yes".equalsIgnoreCase(policyAllowsAccess)) {
                publicAccess.put("bucketPolicy_found", true);
            } else {
                publicAccess.put("bucketPolicy_found", false);
            }

            if (!com.amazonaws.util.StringUtils.isNullOrEmpty(aclAllowsAccess) && "Yes".equalsIgnoreCase(aclAllowsAccess)) {
                publicAccess.put("acl_found", true);
            } else {
                publicAccess.put("acl_found", false);
            }
        }
        return publicAccess;
    }

    private static JsonArray getPolicyArray(AmazonS3Client awsS3Client, String s3BucketName) {
        JsonParser jsonParser = new JsonParser();
        JsonArray policyJsonArray = new JsonArray();
        BucketPolicy bucketPolicy = awsS3Client.getBucketPolicy(s3BucketName);


        if (!com.amazonaws.util.StringUtils.isNullOrEmpty(bucketPolicy.getPolicyText())) {
            JsonObject resultJson = (JsonObject) jsonParser.parse(bucketPolicy.getPolicyText());
            policyJsonArray = resultJson.get("Statement").getAsJsonArray();
        }
        return policyJsonArray;
    }

    public static String checkValidationForS3ObjectLevelLogging(Map<String, String> resourceAttributes,
                                                                boolean isTypeRead) {

        String type = isTypeRead ? "read" : "write";
        String bucketName = resourceAttributes.get(PacmanRuleConstants.NAME);
        String accountId = resourceAttributes.get(PacmanRuleConstants.ACCOUNTID);
        String pacmanHost = PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI);

        if (!PacmanUtils.doesAllHaveValue(pacmanHost, accountId, bucketName)) {
            logger.info(PacmanRuleConstants.MISSING_CONFIGURATION);
            throw new InvalidInputException(PacmanRuleConstants.MISSING_CONFIGURATION);
        }

        try {
            String esEndPoint = pacmanHost + CLOUD_TRAIL_EVENT_SELECTOR_URL;
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.ACCOUNTID, accountId);
            mustFilter.put(PacmanRuleConstants.DATA_RESOURCE_TYPE, DATA_RESOURCE_TYPE);
            HashMultimap<String, Object> shouldFilter = HashMultimap.create();
            Map<String, Object> mustTermsFilter = new HashMap<>();

            Set<String> resourceValueSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                    shouldFilter, mustTermsFilter, PacmanRuleConstants.DATA_RESOURCE_VALUE, null);

            if (Objects.isNull(resourceValueSet) || resourceValueSet.isEmpty()) {
                return S3PacbotUtils.CLOUD_TRAIL_MSG + accountId + " for s3 bucket: " + bucketName +
                        " for S3ObjectLevel" + type + "Logging";
            }
            List<String> resourceValues = S3PacbotUtils.getValidResourceValue(resourceValueSet, bucketName);
            if (resourceValues.isEmpty()) {
                return S3PacbotUtils.CLOUD_TRAIL_MSG + accountId + " for s3 bucket: " + bucketName
                        + " and resourceValue is not matching for S3ObjectLevel" + type + "Logging";
            }
            if (isTypeRead) {
                return S3PacbotUtils.getCloudTrailUsingResourceValue(resourceValues, esEndPoint, accountId, mustFilter,
                        bucketName, shouldFilter, pacmanHost, mustTermsFilter, READ_TYPES);
            } else {
                return S3PacbotUtils.getCloudTrailUsingResourceValue(resourceValues, esEndPoint, accountId, mustFilter,
                        bucketName, shouldFilter, pacmanHost, mustTermsFilter, WRITE_TYPES);
            }
        } catch (Exception ex) {
            logger.error("Object-level logging for " + type +
                    " events is not enabled for S3 bucket" + ex.getMessage(), ex);
            return "Object-level logging for " + type +
                    " events is enabled for S3 bucket";
        }
    }

    public static String getCloudTrailUsingResourceValue(List<String> resourceValues, String esEndPoint,
                                                         String accountId, Map<String, Object> mustFilter,
                                                         String bucketName, HashMultimap<String, Object> shouldFilter,
                                                         String pacmanHost, Map<String, Object> mustTermsFilter,
                                                         List<String> readWriteTypes) throws Exception {
        for (String resourceValue : resourceValues) {
            mustFilter.put(PacmanRuleConstants.DATA_RESOURCE_VALUE, resourceValue);
            Set<String> readWriteTypeSet = PacmanUtils
                    .getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                            shouldFilter, mustTermsFilter, "readwritetype", null);
            if (Objects.isNull(readWriteTypeSet) || readWriteTypeSet.isEmpty()) {
                return CLOUD_TRAIL_MSG + accountId + " for s3 bucket: " + bucketName
                        + " and readwritetype is not matching";
            }
            if (!isValidReadWriteType(readWriteTypeSet, readWriteTypes)) {
                return "CloudTrail log with matching conditions does not exists," +
                        "readwritetype: " + String.join(",", readWriteTypeSet) + ",accountId: " + accountId
                        + " for s3 bucket: " + bucketName;
            }
            Set<String> trailArnSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                    shouldFilter, mustTermsFilter, "trailarn", null);
            if (isTrailByTrailArnExists(trailArnSet, pacmanHost, shouldFilter, mustTermsFilter)) {
                return null;
            }
        }
        return "CloudTrail log with matching conditions does not exists,isMultiRegionTrail: true"
                + ",accountId: " + accountId + " for s3 bucket: " + bucketName;
    }

    private static boolean isTrailByTrailArnExists(Set<String> trailArnSet, String pacmanHost,
                                                   HashMultimap<String, Object> shouldFilter,
                                                   Map<String, Object> mustTermsFilter) throws Exception {
        if (Objects.isNull(trailArnSet) || trailArnSet.isEmpty()) {
            return false;
        }
        for (String trailFromSearch : trailArnSet) {
            String esEndPoint = pacmanHost + CLOUD_TRAIL_URL;
            Map<String, Object> mustFilter = new HashMap<>();
            mustFilter.put(PacmanRuleConstants.MULTI_REGION_TRAIL, "true");
            mustFilter.put(PacmanRuleConstants.TRAIL_ARN, trailFromSearch);
            Set<String> resultSet = PacmanUtils.getValueFromElasticSearchAsSet(esEndPoint, mustFilter,
                    shouldFilter, mustTermsFilter, "trailarn", null);
            if (!(Objects.isNull(resultSet) || resultSet.isEmpty())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidReadWriteType(Set<String> readWriteTypeSet, List<String> readWriteTypes) {
        if (!readWriteTypeSet.isEmpty()) {
            for (String readWriteType : readWriteTypeSet) {
                if (readWriteTypes.contains(readWriteType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getValidResourceValue(Set<String> resourceValueSet, String bucketName) {
        List<String> valueList = new ArrayList<>();
        for (String resourceValue : resourceValueSet) {
            for (String value : resourceValue.split(",")) {
                if (value.equalsIgnoreCase(DATA_RESOURCE_VALUE) ||
                        (value.equalsIgnoreCase(DATA_RESOURCE_VALUE + ":::" + bucketName + "/"))) {
                    valueList.add(resourceValue);
                }
            }
        }
        return valueList;
    }

}

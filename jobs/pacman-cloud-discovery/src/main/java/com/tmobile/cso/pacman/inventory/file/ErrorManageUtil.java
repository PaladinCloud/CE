/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.cso.pacman.inventory.file;

import com.google.api.client.util.Strings;
import com.tmobile.cso.pacman.inventory.util.InventoryConstants;
import com.tmobile.pacman.commons.dto.ErrorVH;
import com.tmobile.pacman.commons.dto.CollectorIssuesVH;
import com.tmobile.pacman.commons.utils.NotificationPermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.tmobile.cso.pacman.inventory.util.Constants.ERROR_PREFIX;
import static com.tmobile.pacman.commons.PacmanSdkConstants.ENDING_QUOTES;

public class ErrorManageUtil {

    private static final Logger log = LoggerFactory.getLogger(ErrorManageUtil.class);
    private static final Map<String, List<ErrorVH>> errorMap = new ConcurrentHashMap<>();

    private ErrorManageUtil() {
    }

    public static Map<String, List<ErrorVH>> getErrorMap() {
        return errorMap;
    }

    public static void initialise() {
        try {
            FileGenerator.writeToFile("aws-loaderror.data", InventoryConstants.OPEN_ARRAY, false);
        } catch (IOException e) {
            log.error(ERROR_PREFIX + "Exception occurred while initializing aws-loaderror.data" + ENDING_QUOTES, e);
        }
    }

    public static void finalise() {
        try {
            FileGenerator.writeToFile("aws-loaderror.data", InventoryConstants.CLOSE_ARRAY, true);
        } catch (IOException e) {
            log.error(ERROR_PREFIX + "Writing to aws-loaderror.data failed" + ENDING_QUOTES, e);
        }
    }

    /**
     * @param account   the account
     * @param region    the region
     * @param type      the type
     * @param exception the exception
     */
    public static synchronized void uploadError(String account, String region, String type, String exception) {
        try {
            List<ErrorVH> errorList = errorMap.get(account);
            if (errorList == null) {
                errorList = new CopyOnWriteArrayList<>();
                errorMap.put(account, errorList);
            }
            ErrorVH error = new ErrorVH();
            error.setException(exception);
            error.setRegion(region);
            error.setType(type);
            errorList.add(error);
        } catch (Exception e) {
            log.error("Error in uploadError", e);
        }
    }

    public static void writeErrorFile() {
        try {
            FileManager.generateErrorFile(errorMap);
        } catch (Exception e) {
            log.error(ERROR_PREFIX + "Writing to aws-loaderror.data failed" + ENDING_QUOTES, e);
        }
    }

    public static Map<String, Object> formErrorCode() {
        Map<String, Object> errorCode = new HashMap<>();
        errorCode.put("jobName", System.getProperty("jobName"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        errorCode.put("executionEndDate", sdf.format(new Date()));

        List<Map<String, Object>> errors = new ArrayList<>();
        for (Entry<String, List<ErrorVH>> errorDetail : errorMap.entrySet()) {
            Map<String, Object> error = new HashMap<>();
            List<Map<String, String>> details = new ArrayList<>();

            error.put("error", "Error while fetching Inventory for account " + errorDetail.getKey());
            for (ErrorVH errorVH : errorDetail.getValue()) {
                Map<String, String> detail = new HashMap<>();
                detail.put("type", errorVH.getType());
                detail.put("region", errorVH.getRegion());
                detail.put("exception", errorVH.getException());
                detail.put("account", errorDetail.getKey());
                details.add(detail);
            }
            error.put("details", details);
            errors.add(error);
        }

        errorCode.put("errors", errors);
        if (errors.isEmpty()) {
            errorCode.put("status", "Success");
        } else {
            errorCode.put("status", "Partial Success");
        }

        log.info("Return Info {}", errorCode);
        return errorCode;
    }

    public static void omitOpsAlert() {
        List<CollectorIssuesVH> permissionIssue = new ArrayList<>();
        for (Map.Entry<String, List<ErrorVH>> entry : errorMap.entrySet()) {
            List<ErrorVH> errorVHList = entry.getValue();
            Map<String, List<String>> assetPermissionMapping = new HashMap<>();
            for (ErrorVH errorVH : entry.getValue()) {
                List<String> permissionIssues = assetPermissionMapping.get(errorVH.getType());
                // omit errors
                omitErrors(errorVHList, errorVH);
                omitPermissionErrors(errorVHList, assetPermissionMapping, errorVH, permissionIssues);
            }

            if (!assetPermissionMapping.isEmpty()) {
                CollectorIssuesVH collectorIssuesVH = new CollectorIssuesVH();
                collectorIssuesVH.setAccountNumber(entry.getKey());
                collectorIssuesVH.setAssetIssues(assetPermissionMapping);
                permissionIssue.add(collectorIssuesVH);
            }

            if (errorVHList.isEmpty()) {
                errorMap.remove(entry.getKey());
            }
        }

        // print the remaining errors that need to be looked into
        errorMap.forEach((account, errors) -> {
            log.error("Printing errors for Account: {}", account);
            errors.forEach(errorVH -> {
                log.error("Error: {} for type: {} for region: {}", errorVH.getException(), errorVH.getType(), errorVH.getRegion());
            });
        });

        if (!permissionIssue.isEmpty()) {
            NotificationPermissionUtils.triggerNotificationForPermissionDenied(permissionIssue, "AWS");
        }
    }

    private static void omitPermissionErrors(List<ErrorVH> errorVHList, Map<String, List<String>> assetPermissionMapping, ErrorVH errorVH, List<String> permissionIssues) {
        List<String> permissionExceptionList = Arrays.asList("AccessDenied", "SubscriptionRequiredException", "AWSSupportException", "Amazon Web Services Premium Support Subscription is required to use this service", "not subscribed to AWS Security Hub", "is not authorized to perform: sts:AssumeRole", "InsufficientPrivilegesException", "ValidationError", "AuthorizationError", "UnauthorizedOperation");
        List<String> misconfiguredExceptionList = Arrays.asList("ValidationError");
        if (exceptionPresentInList(errorVH.getException(), permissionExceptionList) || exceptionPresentInList(errorVH.getException(), misconfiguredExceptionList)) {
            shortenMessageForKMS(errorVH);
            if (permissionIssues != null && !errorVH.getType().equals("phd") && !errorVH.getType().equals("security hub")) {
                permissionIssues.add(errorVH.getException());
            } else {
                permissionIssues = new ArrayList<>();
                permissionIssues.add(errorVH.getException());
            }
            if (exceptionPresentInList(errorVH.getException(), misconfiguredExceptionList)) {
                permissionIssues = new ArrayList<>();
                permissionIssues.add(errorVH.getException());
                assetPermissionMapping.put("misconfiguredFor" + errorVH.getType(), permissionIssues);
            } else {
                assetPermissionMapping.put(errorVH.getType(), permissionIssues);
            }
            errorVHList.remove(errorVH);
        }
    }

    private static boolean exceptionPresentInList(String errorException, List<String> exceptionList) {
        for (String exception : exceptionList) {
            if (Strings.isNullOrEmpty(errorException)) {
                return false;
            }

            if (errorException.contains(exception)) return true;
        }

        return false;
    }

    private static void omitErrors(List<ErrorVH> errorVHList, ErrorVH errorVH) {
        if (errorVH.getException().contains("AWS was not able to validate the provided access credentials")
                || errorVH.getException().contains("The security token included in the request is invalid")
                || errorVH.getException().contains("Unable to execute HTTP request: elasticbeanstalk")
                || errorVH.getException().contains("Rate exceeded")
                || errorVH.getException().contains("ListenerNotFound")
                || errorVH.getException().contains("InvalidSnapshot.NotFound")
                || errorVH.getException().contains("TrailNotFoundException")) {
            errorVHList.remove(errorVH);
        }
    }

    private static void shortenMessageForKMS(ErrorVH errorVH) {
        String exception = errorVH.getException();
        if (errorVH.getType().equals("kms") && (exception.contains("ListResourceTags") || exception.contains("GetKeyRotationStatus") || exception.contains("DescribeKey"))) {
            errorVH.setException(errorVH.getException().substring(exception.indexOf("arn"), exception.indexOf("because")));
        }
    }
}

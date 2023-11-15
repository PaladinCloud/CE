package com.tmobile.cso.pacman.inventory.file;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.tmobile.pacman.commons.dto.ErrorVH;
import com.tmobile.pacman.commons.dto.PermissionVH;
import com.tmobile.pacman.commons.utils.NotificationPermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tmobile.cso.pacman.inventory.InventoryConstants;

public class ErrorManageUtil {

    private static Logger log = LoggerFactory.getLogger(ErrorManageUtil.class);

    private static Map<String, List<ErrorVH>> errorMap = new ConcurrentHashMap<>();

    private ErrorManageUtil() {

    }

    public static Map<String, List<ErrorVH>> getErrorMap() {
        return errorMap;
    }

    public static void initialise() {
        try {
            FileGenerator.writeToFile("aws-loaderror.data", InventoryConstants.OPEN_ARRAY, false);
        } catch (IOException e) {
            log.error("Error in Initialise", e);
        }
    }

    public static void finalise() {
        try {
            FileGenerator.writeToFile("aws-loaderror.data", InventoryConstants.CLOSE_ARRAY, true);
        } catch (IOException e) {
            log.error("Error in finalise", e);
        }
    }

    /**
     * Upload error.
     *
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
            log.error("Error in writeErrorFile", e);
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
        List<PermissionVH> permissionIssue = new ArrayList<>();
        for (Map.Entry<String, List<ErrorVH>> entry : errorMap.entrySet()) {
            List<ErrorVH> errorVHList = entry.getValue();
            Map<String, List<String>> assetPermissionMapping = new HashMap<>();
            for (ErrorVH errorVH : entry.getValue()) {
                List<String> permissionIssues = assetPermissionMapping.get(errorVH.getType());
                if ((errorVH.getType().equals("phd") && (errorVH.getException().contains("AccessDeniedException") || errorVH.getException().contains("SubscriptionRequiredException"))) || (errorVH.getType().equals("all") && (errorVH.getException().contains("AWSSecurityTokenService") && errorVH.getException().contains("is not authorized to perform: sts:AssumeRole")))) {
                    log.info("Omit exception :{}", errorVH.getException());
                    permissionIssues = new ArrayList<>();
                    permissionIssues.add(errorVH.getException());
                    assetPermissionMapping.put(errorVH.getType(), permissionIssues);
                    errorVHList.remove(errorVH);
                }
                if (((errorVH.getType().equals("kms") || errorVH.getType().equals("s3")) && errorVH.getException().contains("AccessDeniedException")) || (errorVH.getType().equals("checks") && errorVH.getException().contains("AWSSupportException"))) {
                    log.info("Omit exception :{}", errorVH.getException());
                    shortenMessageforKMS(errorVH);
                    if (permissionIssues != null) {
                        permissionIssues.add(errorVH.getException());
                    } else {
                        permissionIssues = new ArrayList<>();
                        permissionIssues.add(errorVH.getException());
                    }
                    assetPermissionMapping.put(errorVH.getType(), permissionIssues);
                    errorVHList.remove(errorVH);
                }
            }
            if (!assetPermissionMapping.isEmpty()) {
                PermissionVH permissionVH = new PermissionVH();
                permissionVH.setAccountNumber(entry.getKey());
                permissionVH.setAssetPermissionIssues(assetPermissionMapping);
                permissionIssue.add(permissionVH);
            }
            if (errorVHList.isEmpty()) {
                errorMap.remove(entry.getKey());
            }
        }
        NotificationPermissionUtils.triggerNotificationForPermissionDenied(permissionIssue, "AWS");
    }

    private static void shortenMessageforKMS(ErrorVH errorVH) {
        String exception = errorVH.getException();
        if (errorVH.getType().equals("kms") && (exception.contains("ListResourceTags") || exception.contains("GetKeyRotationStatus") || exception.contains("DescribeKey"))) {
            errorVH.setException(errorVH.getException().substring(exception.indexOf("arn"), exception.indexOf("because")));
        }
    }
}
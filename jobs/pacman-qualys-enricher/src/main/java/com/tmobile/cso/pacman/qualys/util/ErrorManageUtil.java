package com.tmobile.cso.pacman.qualys.util;

import com.tmobile.cso.pacman.qualys.Constants;
import com.tmobile.pacman.commons.dto.PermissionVH;
import com.tmobile.pacman.commons.utils.NotificationPermissionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;


public class ErrorManageUtil implements Constants{
    
    private ErrorManageUtil() {
        
    }

    private static Logger log = LoggerFactory.getLogger(ErrorManageUtil.class);

    public static Map<String,Object> formErrorCode(List<Map<String,String>> errorList) {
        Map<String,Object> errorCode = new HashMap<>();
        //to do write error file
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        errorCode.put("endTime", sdf.format(new Date()));
        
        String status = "";
        omitOpsAlert(errorList);
        List<Map<String,Object>> errors = new ArrayList<>();
        if(!errorList.isEmpty()) {
            for(Map<String, String> errorDetail :errorList) {
                Map<String,Object> error = new HashMap<>();
                error.put(ERROR, errorDetail.get(ERROR));
                List<Map<String,String>> details = new ArrayList<>();
                Map<String,String> detail = new HashMap<>();
                detail.put(EXCEPTION,errorDetail.get(EXCEPTION));
                log.error("error detail - {} ,  exception - {}", errorDetail.get(ERROR), errorDetail.get(EXCEPTION));
                details.add(detail);
                error.put("details",details);
                errors.add(error);
                
                if(!FAILED.equalsIgnoreCase(status)) {
                    status = (FATAL.equalsIgnoreCase(errorDetail.get(ERROR_TYPE))) ? FAILED:"Partial Success";
                }
            }
        }
        else {
            status = "success";
        }
        
        errorCode.put("errors", errors);
        errorCode.put("status", status);
        return errorCode;
    }

    private static void omitOpsAlert(List<Map<String, String>> errorList) {
        List<Map<String, String>> copyErrorList=new ArrayList<>(errorList);
        List<PermissionVH> permissionIssue = new ArrayList<>();
        Map<String, List<String>> assetPermissionMapping = new HashMap<>();
        for(Map<String, String> error:copyErrorList)
        {
            if(error.get("exception").contains("UnAuthorisedException")) {
                List<String> permissionIssues=new ArrayList<>();
                permissionIssues.add(error.get("exception"));
                assetPermissionMapping.put("ec2,virtual machines,onpremserver", permissionIssues);
                errorList.remove(error);
            }
        }
        if (!assetPermissionMapping.isEmpty()) {
            PermissionVH permissionVH = new PermissionVH();
            permissionVH.setAccountNumber("Qualys");
            permissionVH.setAssetPermissionIssues(assetPermissionMapping);
            permissionIssue.add(permissionVH);
        }
        NotificationPermissionUtils.triggerNotificationForPermissionDenied(permissionIssue,"Qualys");
    }
}

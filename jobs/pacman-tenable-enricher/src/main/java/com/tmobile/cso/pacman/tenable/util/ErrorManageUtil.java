package com.tmobile.cso.pacman.tenable.util;

import com.tmobile.cso.pacman.tenable.Constants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ErrorManageUtil{
    
    private ErrorManageUtil() {
        
    }

    public static Map<String,Object> formErrorCode(List<Map<String,String>> errorList) {
        Map<String,Object> errorCode = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        errorCode.put("endTime", sdf.format(new Date()));
        
        String status = "";
        
        List<Map<String,Object>> errors = new ArrayList<>();
        if(!errorList.isEmpty()) {
            for(Map<String, String> errorDetail :errorList) {
                Map<String,Object> error = new HashMap<>();
                error.put(Constants.ERROR, errorDetail.get(Constants.ERROR));
                
                List<Map<String,String>> details = new ArrayList<>();
                Map<String,String> detail = new HashMap<>();
                detail.put(Constants.EXCEPTION,errorDetail.get(Constants.EXCEPTION));
                details.add(detail);
                error.put("details",details);
                errors.add(error);
                
                if(!Constants.FAILED.equalsIgnoreCase(status)) {
                    status = (Constants.FATAL.equalsIgnoreCase(errorDetail.get(Constants.ERROR_TYPE))) ? Constants.FAILED:"Partial Success";
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
}

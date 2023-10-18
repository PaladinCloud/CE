package com.tmobile.cso.pacman.datashipper.util;

import java.text.SimpleDateFormat;
import java.util.*;

public class ErrorManageUtil implements Constants {

    private ErrorManageUtil() {
        throw new IllegalStateException("ErrorManageUtil is a utility class");
    }

    public static Map<String, Object> formErrorCode(String job, List<Map<String, String>> errorList) {
        Map<String, Object> errorCode = new HashMap<>();
        errorCode.put("jobName", job);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        errorCode.put("executionEndDate", sdf.format(new Date()));

        String status = "";

        List<Map<String, Object>> errors = new ArrayList<>();
        if (!errorList.isEmpty()) {
            for (Map<String, String> errorDetail : errorList) {
                Map<String, Object> error = new HashMap<>();
                error.put(ERROR, errorDetail.get(ERROR));

                List<Map<String, String>> details = new ArrayList<>();
                Map<String, String> detail = new HashMap<>();
                detail.put(EXCEPTION, errorDetail.get(EXCEPTION));
                details.add(detail);
                error.put("details", details);
                errors.add(error);

                if (!FAILED.equalsIgnoreCase(status)) {
                    status = (FATAL.equalsIgnoreCase(errorDetail.get(ERROR_TYPE))) ? FAILED : "partial failed";
                }
            }
        } else {
            status = "success";
        }

        errorCode.put("errors", errors);
        errorCode.put("status", status);
        return errorCode;
    }
}

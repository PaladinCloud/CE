/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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

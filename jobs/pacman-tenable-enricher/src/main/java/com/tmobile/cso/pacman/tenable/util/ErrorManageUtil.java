/*******************************************************************************
 * Copyright 2022 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.cso.pacman.tenable.util;

import com.tmobile.cso.pacman.tenable.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorManageUtil {

    private ErrorManageUtil() {
    }

    public static Map<String, Object> formErrorCode(List<Map<String, String>> errorList) {
        Map<String, Object> errorCode = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        errorCode.put("endTime", sdf.format(new Date()));

        String status = "";

        List<Map<String, Object>> errors = new ArrayList<>();
        if (!errorList.isEmpty()) {
            for (Map<String, String> errorDetail : errorList) {
                Map<String, Object> error = new HashMap<>();
                error.put(Constants.ERROR, errorDetail.get(Constants.ERROR));

                List<Map<String, String>> details = new ArrayList<>();
                Map<String, String> detail = new HashMap<>();
                detail.put(Constants.EXCEPTION, errorDetail.get(Constants.EXCEPTION));
                details.add(detail);
                error.put("details", details);
                errors.add(error);

                if (!Constants.FAILED.equalsIgnoreCase(status)) {
                    status = (Constants.FATAL.equalsIgnoreCase(errorDetail.get(Constants.ERROR_TYPE))) ? Constants.FAILED : "Partial Success";
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

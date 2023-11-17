/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.cso.pacman.tenable;

import com.tmobile.cso.pacman.tenable.jobs.TenableVMVulnerabilityDataImporter;
import com.tmobile.cso.pacman.tenable.util.ErrorManageUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@PacmanJob(methodToexecute = "execute", jobName = "Tenable Data importer", desc = "Job to enrich Tenable data in ES", priority = 5)
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        Arrays.stream(args).forEach(obj -> {
            String[] paramArray = obj.split("[:]");
            params.put(paramArray[0], paramArray[1]);
        });
        execute(params);
    }

    /**
     * Execute.
     *
     * @param params the params
     */
    public static void execute(Map<String, String> params) {
        Map<String, Object> errorInfo;

        try {
            MainUtil.setup(params);
        } catch (Exception e) {
            log.error("Job failed to Execute", e);

            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(Constants.ERROR, "Exception in setting up Job ");
            errorMap.put(Constants.ERROR_TYPE, Constants.WARN);
            errorMap.put(Constants.EXCEPTION, e.getMessage());

            List<Map<String, String>> errorList = new ArrayList<>();
            errorList.add(errorMap);
            ErrorManageUtil.formErrorCode(errorList);

            throw new RuntimeException(e);
        }

        String jobHint = params.get("job_hint");
        int days = Integer.parseInt(params.get("days"));
        if (jobHint.equals("tenable_vm_vulnerability")) {
            errorInfo = TenableVMVulnerabilityDataImporter.getInstance().execute(days);
            if (!errorInfo.isEmpty() && errorInfo.get("error") != null) {
                log.warn("Job executed with some errors -> {}", errorInfo);
            } else {
                log.info("Job executed successfully");
            }
        } else {
            log.warn("Job hint is not supplied");
        }
    }

}

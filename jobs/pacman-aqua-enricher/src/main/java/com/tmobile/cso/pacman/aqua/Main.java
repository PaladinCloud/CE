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
package com.tmobile.cso.pacman.aqua;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmobile.cso.pacman.aqua.jobs.AquaFunctionVulnerabilityDataImporter;
import com.tmobile.cso.pacman.aqua.jobs.AquaImageVulnerabilityDataImporter;
import com.tmobile.cso.pacman.aqua.jobs.AquaVMVulnerabilityDataImporter;
import com.tmobile.cso.pacman.aqua.util.ErrorManageUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PacmanJob(methodToexecute = "execute", jobName = "Aqua Data importer", desc = "Job to enrich Aqua data in ES", priority = 5)
public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

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
     * @return Returns map if in case of failures
     */
    public static Map<String, Object> execute(Map<String, String> params) {

        Map<String, Object> errorInfo = new HashMap<>();
        List<Map<String, String>> errorList = new ArrayList<>();
        try {
            MainUtil.setup(params);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(Constants.ERROR, "Exception in setting up Job ");
            errorMap.put(Constants.ERROR_TYPE, Constants.WARN);
            errorMap.put(Constants.EXCEPTION, e.getMessage());
            errorList.add(errorMap);

            return ErrorManageUtil.formErrorCode(errorList);
        }

        String jobHint = params.get("job_hint");
        switch (jobHint) {
            case "aqua_image_vulnerability":
                errorInfo = new AquaImageVulnerabilityDataImporter().execute();
                break;
            case "aqua_vm_vulnerability":
                errorInfo = new AquaVMVulnerabilityDataImporter().execute();
                break;
            case "aqua_functions_vulnerability":
                errorInfo = new AquaFunctionVulnerabilityDataImporter().execute();
                break;
            default:
                log.info("Job hint is not supplied.");
        }

        return errorInfo;
    }

}

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
package com.tmobile.cso.pacman.datashipper;

import com.tmobile.cso.pacman.datashipper.dto.DatasourceData;
import com.tmobile.cso.pacman.datashipper.entity.*;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.ErrorManageUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@PacmanJob(methodToexecute = "shipData", jobName = "data-shipper", desc = "Job to load data from s3 to OP", priority = 5)
public class Main implements Constants {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        Arrays.stream(args).forEach(obj -> {
            String[] paramArray = obj.split(":");
            params.put(paramArray[0], paramArray[1]);
        });

        shipData(params);
        System.exit(0);
    }

    /**
     * Ship data.
     *
     * @param params the params
     * @return
     */
    public static Map<String, Object> shipData(Map<String, String> params) {
        String jobName = System.getProperty("jobName");
        List<Map<String, String>> errorList = new ArrayList<>();
        try {
            MainUtil.setup(params);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in setting up Job ");
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
            return ErrorManageUtil.formErrorCode(jobName, errorList);
        }
        String ds = params.get("datasource");
        ESManager.configureIndexAndTypes(ds, errorList);
        errorList.addAll(new EntityManager().uploadEntityData(ds));
        ExternalPolicies.getInstance().uploadPolicyDefinition(ds);
        try {
            DatasourceData datasourceData = DatasourceDataFetcher.getInstance().fetchDatasourceData(ds);
            if (datasourceData != null) {
                List<String> accountIds = datasourceData.getAccountIds();
                List<String> assetGroups = datasourceData.getAssetGroups();
                if (assetGroups != null && !assetGroups.isEmpty()) {
                    AssetGroupStatsCollector assetGroupStatsCollector = new AssetGroupStatsCollector();
                    errorList.addAll(assetGroupStatsCollector.collectAssetGroupStats(datasourceData));
                }
                if (accountIds != null && !accountIds.isEmpty()) {
                    IssueCountManager issueCountManager = new IssueCountManager();
                    errorList.addAll(issueCountManager.populateViolationsCount(ds, accountIds));
                    AssetsCountManager assetsCountManager = new AssetsCountManager();
                    errorList.addAll(assetsCountManager.populateAssetCount(ds, accountIds));
                }
            } else {
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Unexpected error while fetching accountIds and assetGroups, " +
                        "DatasourceData is null");
                errorMap.put(ERROR_TYPE, ERROR);
                errorList.add(errorMap);
                LOGGER.error("Datasource data is null");
            }
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in updating stats");
            errorMap.put(ERROR_TYPE, ERROR);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
            LOGGER.error("Error while updating stats", e);
        }
        Map<String, Object> status = ErrorManageUtil.formErrorCode(jobName, errorList);
        LOGGER.info("Job Return Status {} ", status);
        return status;
    }
}

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
package com.tmobile.cso.pacman.inventory;

import com.tmobile.cso.pacman.inventory.file.AssetFileGenerator;
import com.tmobile.cso.pacman.inventory.file.ErrorManageUtil;
import com.tmobile.cso.pacman.inventory.file.S3Uploader;
import com.tmobile.pacman.commons.database.RDSDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.tmobile.cso.pacman.inventory.util.Constants.ERROR_PREFIX;
import static com.tmobile.pacman.commons.PacmanSdkConstants.ENDING_QUOTES;

/**
 * The Class InventoryFetchOrchestrator.
 */
@Component
public class InventoryFetchOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(InventoryFetchOrchestrator.class);

    @Autowired
    AssetFileGenerator fileGenerator;

    @Autowired
    S3Uploader s3Uploader;

    @Autowired
    RDSDBManager rDSDBManager;

    @Value("${accountinfo:}")
    private String accountInfo;

    @Value("${region.ignore}")
    private String skipRegions;

    @Value("${s3}")
    private String s3Bucket;

    @Value("${s3.data}")
    private String s3Data;

    @Value("${s3.processed}")
    private String s3Processed;

    @Value("${s3.region}")
    private String s3Region;

    @Value("${file.path}")
    private String filePath;

    private List<Map<String, String>> accounts;

    /**
     * Instantiates a new inventory fetch orchestrator.
     */
    private void fetchAccountInfo() {
        String accountQuery = "SELECT accountId,accountName,accountStatus FROM cf_Accounts where platform = 'aws'";
        accounts = rDSDBManager.executeQuery(accountQuery);
    }

    /**
     * Orchestrate.
     *
     * @return
     */
    public Map<String, Object> orchestrate() {
        try {
            fetchAccountInfo();
            log.info("Inventory Fetch requested for Accounts {}", accounts);
            log.info("Start : Asset Discovery and File Creation");
            fileGenerator.generateFiles(accounts, skipRegions, filePath);
            log.info("End : Asset Discovery and File Creation");

            log.info("Start : Backup Current Files");
            s3Uploader.backUpFiles(s3Bucket, s3Region, s3Data, s3Processed + "/" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()));
            log.info("End : Backup Current Files");

            log.info("Start : Upload Files to S3");
            s3Uploader.uploadFiles(s3Bucket, s3Data, s3Region, filePath);
            log.info("End : Upload Files to S3");

        } catch (Exception e) {
            log.error(ERROR_PREFIX + "Asset Discovery Failed" + ENDING_QUOTES, e);
            System.exit(1);
        }

        return ErrorManageUtil.formErrorCode();
    }
}

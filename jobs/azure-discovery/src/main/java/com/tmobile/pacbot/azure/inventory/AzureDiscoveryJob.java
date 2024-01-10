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
package com.tmobile.pacbot.azure.inventory;

import com.tmobile.pacbot.azure.inventory.config.ConfigUtil;
import com.tmobile.pacbot.azure.inventory.util.InventoryConstants;
import com.tmobile.pacman.commons.jobs.PacmanJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.tmobile.pacbot.azure.inventory.util.Constants.ERROR_PREFIX;

/**
 * The Class InventoryCollectionJob.
 */
@PacmanJob(methodToexecute = "execute", jobName = "AWS Data Collector", desc = "Job to fetch aws info and load to Redshift", priority = 5)
public class AzureDiscoveryJob {

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(AzureDiscoveryJob.class);

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        Arrays.asList(args).forEach(obj -> {
            String[] keyValue = obj.split(":");
            params.put(keyValue[0], keyValue[1]);
        });
		execute(params);
    }

    /**
     * Execute.
     *
     * @param params the params
     * @return
     */
    public static Map<String, Object> execute(Map<String, String> params) {
        try {
            ConfigUtil.setConfigProperties(params.get(InventoryConstants.CONFIG_CREDS));
            if (!params.isEmpty()) {
                params.forEach(System::setProperty);
            }
        } catch (Exception e) {
            //below logger is used to create data alert
            log.error(ERROR_PREFIX + "while fetching config properties", e);
            System.exit(1);
        }

        return AzureDiscoveryApplication.collect();
    }
}

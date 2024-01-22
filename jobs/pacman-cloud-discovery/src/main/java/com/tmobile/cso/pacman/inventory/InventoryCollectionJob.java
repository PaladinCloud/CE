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

import com.tmobile.cso.pacman.inventory.config.ConfigUtil;
import com.tmobile.pacman.commons.jobs.PacmanJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.tmobile.cso.pacman.inventory.util.Constants.ERROR_PREFIX;
import static com.tmobile.pacman.commons.PacmanSdkConstants.ENDING_QUOTES;

@PacmanJob(methodToexecute = "execute", jobName = "AWS Data Collector", desc = "Job to fetch AWS info and load to s3", priority = 5)
public class InventoryCollectionJob {

    private static final Logger log = LoggerFactory.getLogger(InventoryCollectionJob.class);

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
        if (!(params == null || params.isEmpty())) {
            params.forEach(System::setProperty);
        }
        try {
            ConfigUtil.setConfigProperties();
        } catch (Exception e) {
            //below logger is used to create data alert
            log.error(ERROR_PREFIX + "Fetching config properties failed" + ENDING_QUOTES);
            System.exit(1);
        }
        return InventoryFetchApplication.main(new String[]{});
    }
}

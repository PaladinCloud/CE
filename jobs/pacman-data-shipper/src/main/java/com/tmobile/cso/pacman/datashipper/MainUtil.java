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

import com.tmobile.cso.pacman.datashipper.util.ConfigUtil;
import com.tmobile.cso.pacman.datashipper.util.Constants;

import java.util.Map;

public class MainUtil {

    /**
     * Setup.
     *
     * @param params the params
     */
    public static void setup(Map<String, String> params) throws Exception {

        ConfigUtil.setConfigProperties(params.get(Constants.CONFIG_CREDS));

        if (!params.isEmpty()) {
            params.forEach(System::setProperty);
        }

        if (params.get(Constants.CONFIG_QUERY) == null) {
            System.setProperty(Constants.CONFIG_QUERY, "select targetName,targetConfig,displayName from cf_Target where domain ='Infra & Platforms'");
        }
    }
}

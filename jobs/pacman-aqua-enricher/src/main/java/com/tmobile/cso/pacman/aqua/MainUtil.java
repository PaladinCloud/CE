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
package com.tmobile.cso.pacman.aqua;

import java.util.Map;

import com.tmobile.cso.pacman.aqua.util.ConfigUtil;


/**
 * The Class MainUtil.
 */
public class MainUtil {

    /**
     * Setup.
     *
     * @param params            the params
     * @throws Exception the exception
     */
    public static void setup(Map<String, String> params) throws Exception {
      ConfigUtil.setConfigProperties(params.get(Constants.CONFIG_CREDS));
      if (!params.isEmpty()) {
        params.forEach(System::setProperty);
      }
    }

}

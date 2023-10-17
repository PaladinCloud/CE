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
package com.tmobile.cso.pacman.tenable;

public class Constants {

    // TODO: Set to true to get detailed debug logs
    public static final boolean IS_DEBUG_MODE = true;

    public static final String FAILED = "failed";
    public static final String ERROR = "error";
    public static final String EXCEPTION = "exception";
    public static final String ERROR_TYPE = "type";
    public static final String WARN = "warn";
    public static final String FATAL = "fatal";
    public static final String SOURCE = "source";
    public static final String NAME = "name";

    public static final String CONFIG_CREDS = "config_creds";
    public static final String X_API_KEYS_HEADER_NAME = "X-ApiKeys";
    public static final String USER_AGENT_HEADER_NAME = "User-Agent";

    private Constants() {
        throw new IllegalStateException("Constants is a utility class");
    }

}

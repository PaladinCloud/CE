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
package com.tmobile.cso.pacman.datashipper.util;

public interface Constants {
    String TARGET_TYPE_INFO = "targetTypes";

    String CONFIG_CREDS = "config_creds";

    String TARGET_TYPE_OUTSCOPE = "typesNotInScope";

    String API_AUTH_INFO = "apiauthinfo";

    String CONFIG_QUERY = "configquery";

    String FAILED = "failed";
    String ERROR = "error";
    String EXCEPTION = "exception";
    String ERROR_TYPE = "type";
    String WARN = "warn";
    String FATAL = "fatal";

    String SOURCE = "source";
    String NAME = "name";

    String DOC_TYPE = "docType";
    String ACCOUNT_ID_SQL_QUERY = "SELECT accountName FROM pacmandata.cf_Accounts WHERE accountId =";
}

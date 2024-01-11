/***************************************************************************************************
 * Copyright 2024 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************************************/
package com.tmobile.cso.pacman.inventory.util;

public final class InventoryConstants {

    public static final String ERROR_PREFIX_CODE = "{\"errcode\": \"NO_RES_REG\" ,\"account\": \"";
    public static final String ERROR_PREFIX_EC2 = "\",\"Message\": \"Exception in fetching info for resource in specific region\" "
            + ",\"type\": \"EC2\" , \"region\":\"";
    public static final String ACCOUNT = "Account : ";
    public static final String ERROR_CAUSE = "\", \"cause\":\"";
    public static final String REGION_US_WEST_2 = "us-west-2";
    public static final String SOURCE = "source";
    public static final String NAME = "name";
    public static final String APPLICATION = "application";
    public static final String BATCH = "batch";
    public static final String INVENTORY = "inventory";
    public static final String OPEN_ARRAY = "[";
    public static final String CLOSE_ARRAY = "]";
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNT_NAME = "accountName";
    public static final String REGION_GLOBAL = "global";
    public static final String JOB_NAME = " aws-data-collector-job ";

    private InventoryConstants() {
    }
}

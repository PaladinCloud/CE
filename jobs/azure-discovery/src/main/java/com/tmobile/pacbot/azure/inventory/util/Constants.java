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

package com.tmobile.pacbot.azure.inventory.util;

import static com.tmobile.pacbot.azure.inventory.util.InventoryConstants.JOB_NAME;
import static com.tmobile.pacman.commons.PacmanSdkConstants.DATA_ALERT_ERROR_STRING;
import static com.tmobile.pacman.commons.PacmanSdkConstants.ERROR_MESSAGE;

public class Constants {

    public static final String ERROR_PREFIX = DATA_ALERT_ERROR_STRING + JOB_NAME + ERROR_MESSAGE;

    private Constants() {

    }
}

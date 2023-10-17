/***************************************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************************************/

package com.tmobile.cso.pacman.datashipper.util;

public class AppConstants {
    public static final String FIRST_DISCOVERED = "firstdiscoveredon";
    public static final String DISCOVERY_DATE = "discoverydate";
    public static final String PAC_OVERRIDE = "pac_override_";
    public static final String S3_ACCOUNT = System.getProperty("base.account");
    public static final String S3_REGION = System.getProperty("base.region");
    public static final String S3_ROLE = System.getProperty("s3.role");
    public static final String BUCKET_NAME = System.getProperty("s3");
    public static final String DATA_PATH = System.getProperty("s3.data");
    public static final String ATTRIBUTES_TO_PRESERVE = System.getProperty("shipper.attributes.to.preserve");

    private AppConstants() {
        throw new IllegalStateException("AppConstants is a utility class");
    }
}

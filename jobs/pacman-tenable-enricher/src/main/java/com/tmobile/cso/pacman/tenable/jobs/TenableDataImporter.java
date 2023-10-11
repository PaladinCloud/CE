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
package com.tmobile.cso.pacman.tenable.jobs;

import com.amazonaws.auth.BasicSessionCredentials;
import com.tmobile.cso.pacman.tenable.auth.CredentialProvider;
import com.tmobile.cso.pacman.tenable.util.Util;
import com.tmobile.pacman.commons.secrets.AwsSecretManagerUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class TenableDataImporter {

    protected String accessKey;
    protected String secretKey;
    protected String userAgent;
    protected String tenableApiUrl;
    protected String apiKeys;

    Map<String, String> apiMap;
    AwsSecretManagerUtil secretManagerUtil = new AwsSecretManagerUtil();
    CredentialProvider credentialProvider = new CredentialProvider();

    protected TenableDataImporter() {
        apiMap = new HashMap<>();

        apiMap.put("assetsCount",
                "/assets");
        apiMap.put("assetsExportTrigger",
                "/assets/export");
        apiMap.put("assetsExportStatus",
                "/assets/export/{export_id}/status");
        apiMap.put("assetsExport",
                "/assets/export/{export_id}/chunks/{chunk_id}");
        apiMap.put("vulnerabilityExportTrigger",
                "/vulns/export");
        apiMap.put("vulnerabilityExportStatus",
                "/vulns/export/{export_id}/status");
        apiMap.put("vulnerabilityExport",
                "/vulns/export/{export_id}/chunks/{chunk_id}");

        getTenableInfo();
    }

    public abstract Map<String, Object> execute(int days);

    private void getTenableInfo() {
        String secretManagerPrefix = System.getProperty("secret.manager.path"); // paladincloud/secret
        String baseAccount = System.getProperty("base.account");
        String baseRegion = System.getProperty("base.region");
        String roleName = System.getProperty("s3.role");

        BasicSessionCredentials credential = credentialProvider.getBaseAccountCredentials(baseAccount, baseRegion, roleName);
        String secretData = secretManagerUtil.fetchSecret(secretManagerPrefix + "/tenable", credential, baseRegion);

        Map<String, String> dataMap = Util.getJsonData(secretData);
        accessKey = dataMap.get("accessKey");
        secretKey = dataMap.get("secretKey");
        tenableApiUrl = dataMap.get("apiURL");
        userAgent = dataMap.get("userAgent");
        apiKeys = "accessKey=" + accessKey + ";secretKey=" + secretKey + ";";
    }
}

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
package com.tmobile.cso.pacman.aqua.jobs;

import com.amazonaws.auth.BasicSessionCredentials;
import com.google.gson.JsonObject;
import com.tmobile.cso.pacman.aqua.auth.CredentialProvider;
import com.tmobile.cso.pacman.aqua.exception.AquaDataImportException;
import com.tmobile.cso.pacman.aqua.util.HttpUtil;
import com.tmobile.cso.pacman.aqua.util.Util;
import com.tmobile.pacman.commons.secrets.AwsSecretManagerUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class AquaDataImporter {

    protected String apiUsername;
    protected String apiPassword;
    protected String aquaApiUrl;
    protected String aquaClientDomainUrl;

    Map<String, String> apiMap;
    AwsSecretManagerUtil secretManagerUtil = new AwsSecretManagerUtil();
    CredentialProvider credentialProvider = new CredentialProvider();

    public abstract Map<String, Object> execute();

    protected AquaDataImporter() {
        apiMap = new HashMap<>();
        apiMap.put("signIn",
                "/v2/signin");
        apiMap.put("image_vulnerabilities",
                "/api/v2/risks/vulnerabilities");
        apiMap.put("vm_vulnerabilities", "/api/v2/risks/functions/vulnerabilities");
        apiMap.put("hostassetcount", "/qps/rest/2.0/count/am/hostasset");
        getAquaInfo();
    }

    private void getAquaInfo() {
        String secretManagerPrefix = System.getProperty("secret.manager.path");
        String baseAccount = System.getProperty("base.account");
        String baseRegion = System.getProperty("base.region");
        String roleName = System.getProperty("s3.role");

        BasicSessionCredentials credential = credentialProvider.getBaseAccountCredentials(baseAccount, baseRegion, roleName);
        String secretName = secretManagerPrefix + "/" + roleName + "/aqua";
        String secretData = secretManagerUtil.fetchSecret(secretName, credential, baseRegion);
        Map<String, String> dataMap = Util.getJsonData(secretData);

        apiUsername = dataMap.get("apiusername");
        apiPassword = dataMap.get("apipassword");
        aquaApiUrl = dataMap.get("aquaApiUrl");
        aquaClientDomainUrl = dataMap.get("aquaClientDomainUrl");
    }

    public String getBearerToken() throws AquaDataImportException {
        String token = null;
        String tokenUri = aquaApiUrl + apiMap.get("signIn");
        JsonObject inputObject = new JsonObject();
        inputObject.addProperty("email", apiUsername);
        inputObject.addProperty("password", apiPassword);

        String input = inputObject.toString();
        try {
            String response = HttpUtil.post(tokenUri, input, null, null);
            Map<String, Object> data = (Map) Util.getJsonAttribute(response, "data");
            token = (String) data.get("token");
        } catch (Exception e) {
            throw new AquaDataImportException(e.getMessage());
        }
        return token;
    }

    protected int getDefaultPageSize() {
        return Integer.parseInt(System.getProperty("default_page_size"));
    }

}

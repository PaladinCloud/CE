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
package com.tmobile.cso.pacman.datashipper.config;

import com.amazonaws.auth.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;

public class CredentialProvider {
    private static final boolean devMode = System.getProperty("PIC_DEV_MODE") != null;
    private final String baseAccount = System.getProperty("base.account");
    private final String baseRegion = System.getProperty("base.region");

    /**
     * Gets the credentials.
     *
     * @param account  the account
     * @param roleName the role name
     * @return the credentials
     */
    public AWSCredentialsProvider getCredentials(String account, String roleName) {
        AWSCredentialsProvider baseProvider = getBaseAccountCredentials(roleName);
        if (baseAccount.equals(account)) {
            return baseProvider;
        }
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(baseProvider)
                .withRegion(baseRegion)
                .build();
        return new STSAssumeRoleSessionCredentialsProvider.Builder(
                getRoleArn(account, roleName), "pic-ro-" + account)
                .withStsClient(stsClient)
                .build();
    }


    /**
     * Gets the base account credentials.
     *
     * @param roleName the role name
     * @return the base account credentials
     */
    private AWSCredentialsProvider getBaseAccountCredentials(String roleName) {
        if (devMode) {
            String accessKey = System.getProperty("ACCESS_KEY");
            String secretKey = System.getProperty("SECRET_KEY");
            AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(accessKey, secretKey)))
                    .withRegion(baseRegion)
                    .build();
            return new STSAssumeRoleSessionCredentialsProvider.Builder(
                    getRoleArn(baseAccount, roleName), "pic-base-ro")
                    .withStsClient(sts)
                    .build();
        } else {
            AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
            return new STSAssumeRoleSessionCredentialsProvider.Builder(
                    getRoleArn(baseAccount, roleName), "pic-base-ro")
                    .withStsClient(sts)
                    .build();
        }
    }

    /**
     * Gets the role arn.
     *
     * @param accout the accout
     * @param role   the role
     * @return the role arn
     */
    private String getRoleArn(String accout, String role) {
        return "arn:aws:iam::" + accout + ":role/" + role;
    }
}

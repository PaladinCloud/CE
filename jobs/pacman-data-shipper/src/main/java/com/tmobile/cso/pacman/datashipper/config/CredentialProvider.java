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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
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
    public BasicSessionCredentials getCredentials(String account, String roleName) {
        BasicSessionCredentials baseAccntCreds = getBaseAccountCredentials(roleName);
        if (baseAccount.equals(account)) {
            return baseAccntCreds;
        }
        AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(baseAccntCreds)).withRegion(baseRegion);
        AWSSecurityTokenService stsClient = stsBuilder.build();
        AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(account, roleName)).withRoleSessionName("pic-ro-" + account);
        AssumeRoleResult assumeResult = stsClient.assumeRole(assumeRequest);
        return new BasicSessionCredentials(
                assumeResult.getCredentials()
                        .getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                assumeResult.getCredentials().getSessionToken());
    }

    /**
     * Gets the base account credentials.
     *
     * @param roleName the role name
     * @return the base account credentials
     */
    private BasicSessionCredentials getBaseAccountCredentials(String roleName) {
        if (devMode) {
            String accessKey = System.getProperty("ACCESS_KEY");
            String secretKey = System.getProperty("SECRET_KEY");
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(baseRegion);
            AWSSecurityTokenService sts = stsBuilder.build();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount, roleName)).withRoleSessionName("pic-base-ro").withDurationSeconds(3600);
            AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
            return new BasicSessionCredentials(
                    assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                    assumeResult.getCredentials().getSessionToken());

        } else {
            AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount, roleName)).withRoleSessionName("pic-base-ro").withDurationSeconds(3600);
            AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);
            return new BasicSessionCredentials(
                    assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                    assumeResult.getCredentials().getSessionToken());
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

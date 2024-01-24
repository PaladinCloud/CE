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
package com.tmobile.cso.pacman.inventory.auth;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Class CredentialProvider.
 */
@Component
public class CredentialProvider {

    public static final String PALADIN_CLOUD_INTEGRATION_ROLE = "PaladinCloudIntegrationRole";
    /**
     * The dev mode.
     */
    private static final boolean DEV_MODE = System.getProperty("PIC_DEV_MODE") != null;
    private static final Logger log = LoggerFactory.getLogger(CredentialProvider.class);

    @Value("${base.account}")
    private String baseAccount;
    @Value("${base.region}")
    private String baseRegion;
    @Value("${enable.external.id}")
    private boolean isExternalIdIsUsed;
    @Value("${external.id}")
    private String externalId;

    /**
     * Gets the credentials.
     *
     * @param account  the account
     * @param roleName the role name
     * @return the credentials
     */
    public BasicSessionCredentials getCredentials(String account, String roleName) {
        log.info("Account: {}", account);
        log.info("roleName: {}", roleName);
        log.info("isExternalIdIsUsed: {}", isExternalIdIsUsed);
        log.info("externalId: {}", externalId);

        BasicSessionCredentials baseAccntCreds = getBaseAccountCredentials(baseAccount, baseRegion, roleName);

        if (baseAccount.equals(account)) {
            return baseAccntCreds;
        }

        AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(baseAccntCreds)).withRegion(baseRegion);
        AWSSecurityTokenService stsClient = stsBuilder.build();

        AssumeRoleRequest assumeRequest = null;
        assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(account, PALADIN_CLOUD_INTEGRATION_ROLE)).withRoleSessionName("pic-ro-" + account);

        if (isExternalIdIsUsed) {
            assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(account, PALADIN_CLOUD_INTEGRATION_ROLE)).withRoleSessionName("pic-ro-" + account).withExternalId(externalId);
        }

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
    private BasicSessionCredentials getBaseAccountCredentials(String baseAccount, String baseRegion, String roleName) {
        if (DEV_MODE) {
            String accessKey = System.getProperty("ACCESS_KEY");
            String secretKey = System.getProperty("SECRET_KEY");
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
            AWSSecurityTokenServiceClientBuilder stsBuilder = AWSSecurityTokenServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(baseRegion);
            AWSSecurityTokenService sts = stsBuilder.build();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount, roleName)).withRoleSessionName("pic-base-ro");
            AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);

            return new BasicSessionCredentials(
                    assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                    assumeResult.getCredentials().getSessionToken());
        } else {
            AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
            AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(getRoleArn(baseAccount, roleName)).withRoleSessionName("pic-base-ro");
            AssumeRoleResult assumeResult = sts.assumeRole(assumeRequest);

            return new BasicSessionCredentials(
                    assumeResult.getCredentials().getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                    assumeResult.getCredentials().getSessionToken());
        }
    }

    /**
     * Gets the role arn.
     *
     * @param account the account
     * @param role    the role
     * @return the role arn
     */
    private String getRoleArn(String account, String role) {
        return "arn:aws:iam::" + account + ":role/" + role;
    }
}

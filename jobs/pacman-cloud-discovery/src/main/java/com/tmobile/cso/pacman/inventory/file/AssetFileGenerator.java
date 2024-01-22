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
package com.tmobile.cso.pacman.inventory.file;

import com.amazonaws.auth.BasicSessionCredentials;
import com.tmobile.cso.pacman.inventory.auth.CredentialProvider;
import com.tmobile.cso.pacman.inventory.util.*;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.database.RDSDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.tmobile.cso.pacman.inventory.util.Constants.ERROR_PREFIX;
import static com.tmobile.pacman.commons.PacmanSdkConstants.ENDING_QUOTES;


/**
 * The Class AssetFileGenerator.
 */
@Component
public class AssetFileGenerator {

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(AssetFileGenerator.class);

    /**
     * The cred provider.
     */
    @Autowired
    CredentialProvider credProvider;
    @Autowired
    RDSDBManager rdsdbManager;
    /**
     * The target types.
     */
    @Value("${target-types:}")
    private String targetTypes;
    /**
     * The target types.
     */
    @Value("${discovery.role}")
    private String roleName;
    /**
     * The target types.
     */
    @Value("${ec2.statenames:running,stopped,stopping}")
    private String ec2StatenameFilters;

    /**
     * Generate files.
     *
     * @param accounts    the accounts
     * @param skipRegions the skip regions
     * @param filePath    the file path
     */
    public void generateFiles(List<Map<String, String>> accounts, String skipRegions, String filePath) {
        try {
            FileManager.initialise(filePath);
            ErrorManageUtil.initialise();
        } catch (IOException e1) {
            log.error(ERROR_PREFIX + "exception occurred while initialising file" + ENDING_QUOTES, e1);
            System.exit(1);
        }

		/*
		  This will collect all the customer managed policy details.
		 */
        for (Map<String, String> account : accounts) {
            String accountId = account.get(InventoryConstants.ACCOUNT_ID);
            String accountName = account.get(InventoryConstants.ACCOUNT_NAME);

            log.info("Started Discovery for account {}", accountId);
            BasicSessionCredentials tempCredentials = null;
            try {
                tempCredentials = credProvider.getCredentials(accountId, roleName);
                log.info("updating account status of aws account- {} to online.", accountId);
                rdsdbManager.executeUpdate("UPDATE cf_Accounts SET accountStatus='configured' WHERE accountId=?", Collections.singletonList(accountId));
            } catch (Exception e) {
                log.error("{\"errcode\":\"NO_CRED\" , \"account\":\"" + accountId + "\", \"Message\":\"Error getting credentials for account " + accountId + "\" , \"cause\":\"" + e.getMessage() + "\"}");
                ErrorManageUtil.uploadError(accountId, "all", "all", e.getMessage());

                rdsdbManager.executeUpdate("UPDATE cf_Accounts set accountStatus='offline' WHERE accountId=?", Collections.singletonList(accountId));
                log.error("Error updating account status of aws account - {} to offline", accountId);
                continue;
            }

            final BasicSessionCredentials temporaryCredentials = tempCredentials;
            String expPrefix = "{\"errcode\": \"NO_RES\" ,\"account\": \"" + accountId + "\",\"Message\": \"Exception in fetching info for resource\" ,\"type\": \"";
            String infoPrefix = "Fetching Type for Account : " + accountId + " Type : ";

            ExecutorService executor = Executors.newCachedThreadPool();
            executor.execute(() ->
            {
                if (!(isTypeInScope("ec2"))) {
                    return;
                }
                try {
                    log.info("{}EC2", infoPrefix);
                    FileManager.generateInstanceFiles(InventoryUtil.fetchInstances(temporaryCredentials, skipRegions, accountId, accountName, ec2StatenameFilters));
                } catch (Exception e) {
                    log.error(expPrefix + "EC2\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "ec2", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("ecr"))) {
                    return;
                }
                try {
                    log.info("{}ECR", infoPrefix);
                    FileManager.generateRepositoryFiles(InventoryUtil.fetchRepositories(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ECR\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "ecr", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("asg"))) {
                    return;
                }
                try {
                    log.info("{}ASG", infoPrefix);
                    FileManager.generateAsgFiles(InventoryUtil.fetchAsg(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ASG\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "asg", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("stack"))) {
                    return;
                }
                try {
                    log.info("{}Cloud Formation Stack", infoPrefix);
                    FileManager.generateCloudFormationStackFiles(InventoryUtil.fetchCloudFormationStack(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Stack\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "stack", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("dynamodb"))) {
                    return;
                }
                try {
                    log.info("{}DynamoDB", infoPrefix);
                    FileManager.generateDynamoDbFiles(InventoryUtil.fetchDynamoDBTables(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "DynamoDB\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "dynamodb", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("documentdb"))) {
                    return;
                }
                try {
                    log.info("{}Documentdb", infoPrefix);
                    FileManager.generateDocumentDbFiles(InventoryUtil.fetchDocumentDBTables(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Documentdb\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "documentdb", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("dms"))) {
                    return;
                }
                try {
                    log.info("{}DMS", infoPrefix);
                    FileManager.generateDMSFiles(InventoryUtil.fetchDBMigrationService(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "dms\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "dms", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("efs"))) {
                    return;
                }
                try {
                    log.info("{}EFS", infoPrefix);
                    FileManager.generateEfsFiles(InventoryUtil.fetchEFSInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "EFS\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "efs", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("emr"))) {
                    return;
                }
                try {
                    log.info("{}EMR", infoPrefix);
                    FileManager.generateEmrFiles(InventoryUtil.fetchEMRInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "EMR\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "emr", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("lambda"))) {
                    return;
                }
                try {
                    log.info("{}Lambda", infoPrefix);
                    FileManager.generateLamdaFiles(InventoryUtil.fetchLambdaInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Lambda\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "lambda", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("eks"))) {
                    return;
                }
                try {
                    log.info("{}eks", infoPrefix);
                    FileManager.generateEKSFiles(InventoryUtil.fetcheksInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "eks\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "eks", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("classicelb"))) {
                    return;
                }
                try {
                    log.info("{}Classic ELB", infoPrefix);
                    FileManager.generateClassicElbFiles(InventoryUtil.fetchClassicElbInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Classic ELB\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "classicelb", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("appelb"))) {
                    return;
                }
                try {
                    log.info("{}Application ELB", infoPrefix);
                    FileManager.generateApplicationElbFiles(InventoryUtil.fetchElbInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Application ELB\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "appelb", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("targetgroup"))) {
                    return;
                }
                try {
                    log.info("{}Target Group", infoPrefix);
                    FileManager.generateTargetGroupFiles(InventoryUtil.fetchTargetGroups(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Target Group\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "targergroup", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("nat"))) {
                    return;
                }

                try {
                    log.info("{}Nat Gateway", infoPrefix);
                    FileManager.generateNatGatewayFiles(InventoryUtil.fetchNATGatewayInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Nat Gateway\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "nat", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("rdsdb"))) {
                    return;
                }

                try {
                    log.info("{}RDS Instance", infoPrefix);
                    FileManager.generateRDSInstanceFiles(InventoryUtil.fetchRDSInstanceInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "RDS Instance\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "rdsdb", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("rdscluster"))) {
                    return;
                }

                try {
                    log.info("{}RDS Cluster", infoPrefix);
                    FileManager.generateRDSClusterFiles(InventoryUtil.fetchRDSClusterInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "RDS Cluster\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "rdscluster", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("s3"))) {
                    return;
                }

                try {
                    log.info("{}S3", infoPrefix);
                    FileManager.generateS3Files(InventoryUtil.fetchS3Info(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "S3\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "s3", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("eni"))) {
                    return;
                }

                try {
                    log.info("{}Network Interface", infoPrefix);
                    FileManager.generateNwInterfaceFiles(InventoryUtil.fetchNetworkInterfaces(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Network Interface\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "eni", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("sg"))) {
                    return;
                }

                try {
                    log.info("{}Security Group", infoPrefix);
                    FileManager.generateSecGroupFile(InventoryUtil.fetchSecurityGroups(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Security Group\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "sg", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("subnet"))) {
                    return;
                }

                try {
                    log.info("{}Subnet", infoPrefix);
                    FileManager.generateSubnetFiles(InventoryUtil.fetchSubnets(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Subnet\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "subnet", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("checks"))) {
                    return;
                }

                try {
                    log.info("{}Trusted Advisor Check", infoPrefix);
                    FileManager.generateTrustedAdvisorFiles(InventoryUtil.fetchTrusterdAdvisorsChecks(temporaryCredentials, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Trusted Advisor Check\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "checks", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("redshift"))) {
                    return;
                }

                try {
                    log.info("{}Redshift", infoPrefix);
                    FileManager.generateRedshiftFiles(InventoryUtil.fetchRedshiftInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Redshift\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "redshift", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("volume"))) {
                    return;
                }

                try {
                    log.info("{}Volume", infoPrefix);
                    FileManager.generatefetchVolumeFiles(InventoryUtil.fetchVolumetInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Volume\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "volume", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("snapshot"))) {
                    return;
                }

                try {
                    log.info("{}Snapshot", infoPrefix);
                    FileManager.generateSnapshotFiles(InventoryUtil.fetchSnapshots(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Snapshot\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "snapshot", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("vpc"))) {
                    return;
                }

                try {
                    log.info("{}VPC", infoPrefix);
                    FileManager.generateVpcFiles(InventoryUtil.fetchVpcInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "VPC\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "vpc", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("api"))) {
                    return;
                }

                try {
                    log.info("{}ApiGateway", infoPrefix);
                    FileManager.generateApiGatewayFiles(InventoryUtil.fetchApiGateways(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "API\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "api", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("iamuser"))) {
                    return;
                }

                try {
                    log.info("{}IAM User", infoPrefix);
                    FileManager.generateIamUserFiles(InventoryUtil.fetchIAMUsers(temporaryCredentials, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "iAM muser\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "iamuser", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("rdssnapshot"))) {
                    return;
                }

                try {
                    log.info("{}RDS Snapshot", infoPrefix);
                    FileManager.generateRDSSnapshotFiles(InventoryUtil.fetchRDSDBSnapshots(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "RDS Snapshot\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "rdssnapshot", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("iamrole"))) {
                    return;
                }

                try {
                    log.info("{}IAM Roles", infoPrefix);
                    FileManager.generateIamRoleFiles(InventoryUtil.fetchIAMRoles(temporaryCredentials, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "IAM Roles\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "iamrole", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("kms"))) {
                    return;
                }

                try {
                    log.info("{}KMS", infoPrefix);
                    FileManager.generateKMSFiles(InventoryUtil.fetchKMSKeys(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "KMS\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "kms", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("cloudfront"))) {
                    return;
                }

                try {
                    log.info("{}CloudFront", infoPrefix);
                    FileManager.generateCloudFrontFiles(InventoryUtil.fetchCloudFrontInfo(temporaryCredentials, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "CloudFront\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "cloudfront", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("beanstalk"))) {
                    return;
                }

                try {
                    log.info("{}beanstalk", infoPrefix);
                    FileManager.generateEBSFiles(InventoryUtil.fetchEBSInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "beanstalk\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "beanstalk", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("phd"))) {
                    return;
                }

                try {
                    log.info("{}PHD", infoPrefix);
                    FileManager.generatePHDFiles(InventoryUtil.fetchPHDInfo(temporaryCredentials, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "PHD\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "phd", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("routetable"))) {
                    return;
                }

                try {
                    log.info("{}EC2 Route table", infoPrefix);
                    FileManager.generateEC2RouteTableFiles(EC2InventoryUtil.fetchRouteTables(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "EC2 Route table\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "routetable", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("networkacl"))) {
                    return;
                }

                try {
                    log.info("{}EC2 Network Acl", infoPrefix);
                    FileManager.generateNetworkAclFiles(EC2InventoryUtil.fetchNetworkACL(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "EC2 Network Acl\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "networkacl", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("elasticip"))) {
                    return;
                }

                try {
                    log.info("{}EC2 Elastic IP", infoPrefix);
                    FileManager.generateElasticIPFiles(EC2InventoryUtil.fetchElasticIPAddresses(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "EC2 Elastic IP\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "elasticip", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("launchconfig"))) {
                    return;
                }

                try {
                    log.info("{}ASG Launch Configurations", infoPrefix);
                    FileManager.generateLaunchConfigurationsFiles(ASGInventoryUtil.fetchLaunchConfigurations(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ASG Launch Configurations\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "launchconfig", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("internetgw"))) {
                    return;
                }

                try {
                    log.info("{}EC2 Internet Gateway", infoPrefix);
                    FileManager.generateInternetGatewayFiles(EC2InventoryUtil.fetchInternetGateway(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "EC2 Internet Gateway\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "internetgw", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("vpngw"))) {
                    return;
                }

                try {
                    log.info("{}EC2 Vpn Gateway", infoPrefix);
                    FileManager.generateVPNGatewayFiles(EC2InventoryUtil.fetchVPNGateway(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "EC2 Vpn Gateway\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "vpngw", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("asgpolicy"))) {
                    return;
                }

                try {
                    log.info("{}ASG Scaling Policy", infoPrefix);
                    FileManager.generateScalingPolicies(ASGInventoryUtil.fetchScalingPolicies(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ASG Scaling Policy\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "asgpolicy", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("snstopic"))) {
                    return;
                }

                try {
                    log.info("{}SNS Topics", infoPrefix);
                    FileManager.generateSNSTopics(SNSInventoryUtil.fetchSNSTopics(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "SNS Topics\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "snstopic", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("egressgateway"))) {
                    return;
                }

                try {
                    log.info("{}Egress Gateway", infoPrefix);
                    FileManager.generateEgressGateway(EC2InventoryUtil.fetchEgressGateway(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Egress Gateway\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "egressgateway", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("dhcpoption"))) {
                    return;
                }

                try {
                    log.info("{}Dhcp Options", infoPrefix);
                    FileManager.generateDhcpOptions(EC2InventoryUtil.fetchDHCPOptions(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Dhcp Options\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "dhcpoption", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("peeringconnection"))) {
                    return;
                }

                try {
                    log.info("{}Peering Connections", infoPrefix);
                    FileManager.generatePeeringConnections(EC2InventoryUtil.fetchPeeringConnections(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Peering Connections\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "peeringconnection", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("customergateway"))) {
                    return;
                }

                try {
                    log.info("{}Customer Gateway", infoPrefix);
                    FileManager.generateCustomerGateway(EC2InventoryUtil.fetchCustomerGateway(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Customer Gateway\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "customergateway", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("vpnconnection"))) {
                    return;
                }

                try {
                    log.info("{}VPN Connection", infoPrefix);
                    FileManager.generateVpnConnection(EC2InventoryUtil.fetchVPNConnections(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "VPN Connection\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "vpnconnection", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("directconnect"))) {
                    return;
                }

                try {
                    log.info("{}Direct Connection", infoPrefix);
                    FileManager.generateDirectConnection(DirectConnectionInventoryUtil.fetchDirectConnections(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Direct Connection\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "directconnect", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("virtualinterface"))) {
                    return;
                }

                try {
                    log.info("{}Direct Connection Virtual Interfaces", infoPrefix);
                    FileManager.generateDirectConnectionVirtualInterfaces(DirectConnectionInventoryUtil.fetchDirectConnectionsVirtualInterfaces(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Direct Connection Virtual Interfaces\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "virtualinterface", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("elasticsearch"))) {
                    return;
                }

                try {
                    log.info("{}ES Domain", infoPrefix);
                    FileManager.generateESDomain(ESInventoryUtil.fetchESInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ES Domain\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "elasticsearch", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("reservedinstance"))) {
                    return;
                }

                try {
                    log.info("{}reservedinstance", infoPrefix);
                    FileManager.generateReservedInstances(EC2InventoryUtil.fetchReservedInstances(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "reservedinstance\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "reservedinstance", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("ssm"))) {
                    return;
                }

                try {
                    log.info("{}ssm", infoPrefix);
                    FileManager.generateSsmFiles(EC2InventoryUtil.fetchSSMInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "SSM\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "ssm", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("elasticache"))) {
                    return;
                }

                try {
                    log.info("{}elasticache", infoPrefix);
                    FileManager.generateElastiCacheFiles(ElastiCacheUtil.fetchElastiCacheInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "elasticache\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "elasticache", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("datastream"))) {
                    return;
                }

                try {
                    log.info("{}datastream", infoPrefix);
                    FileManager.generateKinesisDataStreamFiles(KinesisInventoryUtil.fetchDataStreamInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "datastream\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "datastream", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("sqs"))) {
                    return;
                }

                try {
                    log.info("{}sqs", infoPrefix);
                    FileManager.generateSQSFiles(InventoryUtil.fetchSQSInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "sqs\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "sqs", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("deliverystream"))) {
                    return;
                }

                try {
                    log.info("{}deliverystream", infoPrefix);
                    FileManager.generateKinesisDeliveryStreamFiles(KinesisInventoryUtil.fetchDeliveryStreamInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "deliverystream\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "deliverystream", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("videostream"))) {
                    return;
                }

                try {
                    log.info("{}videostream", infoPrefix);
                    FileManager.generateKinesisVideoStreamFiles(KinesisInventoryUtil.fetchVideoStreamInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "videostream\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "videostream", e.getMessage());
                }
            });

            //****** Changes For Federated Rules Start ******
            executor.execute(() ->
            {
                if (!(isTypeInScope("acmcertificate"))) {
                    return;
                }

                try {
                    log.info("{}acmcertificate", infoPrefix);
                    FileManager.generateACMCertificateFiles(InventoryUtil.fetchACMCertficateInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "acmcertificate\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "acmcertificate", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("iamcertificate"))) {
                    return;
                }

                try {
                    log.info("{}iamcertificate", infoPrefix);
                    FileManager.generateIAMCertificateFiles(InventoryUtil.fetchIAMCertificateInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "iamcertificate\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "iamcertificate", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("account"))) {
                    return;
                }

                try {
                    log.info("{}Account", infoPrefix);
                    FileManager.generateAccountFiles(InventoryUtil.fetchAccountsInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "AccountInfo\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "AccountInfo", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("iamgroup"))) {
                    return;
                }

                try {
                    log.info("{}IAM Groups", infoPrefix);
                    FileManager.generateIamGroupFiles(InventoryUtil.fetchIAMGroups(temporaryCredentials, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "IAM Groups\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "iamgroup", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("cloudtrail"))) {
                    return;
                }

                try {
                    log.info("{}CloudTrail", infoPrefix);
                    FileManager.generateCloudTrailFiles(InventoryUtil.fetchCloudTrails(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Cloud Trailt\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "cloudtrail", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("cloudwatchlogs"))) {
                    return;
                }

                try {
                    log.info("{}cloudwatchlogs", infoPrefix);
                    FileManager.generateCloudWatchLogsFiles(InventoryUtil.fetchCloudWatchLogs(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Cloud Watch Logs\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "cloudwatchlogs", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("cloudwatchalarm"))) {
                    return;
                }

                try {
                    log.info("{}cloudwatchalarm", infoPrefix);
                    FileManager.generateCloudWatchAlarm(InventoryUtil.fetchCloudWatchAlarm(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "Cloud Watch alarm\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "cloudwatchalarm", e.getMessage());
                }
            });
            //****** Changes For Federated Rules End ******

            executor.execute(() ->
            {
                if (!(isTypeInScope("daxcluster"))) {
                    return;
                }
                try {
                    log.info("{}daxcluster", infoPrefix);
                    FileManager.generateDAXClusterFiles(InventoryUtil.fetchDAXClusterInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "daxcluster\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "daxcluster", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("awsathena"))) {
                    return;
                }
                try {
                    log.info("{}awsathena", infoPrefix);
                    FileManager.generateAWSAthenaFiles(InventoryUtil.fetchAWSAthenaInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "awsathena\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "awsathena", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("awscomprehend"))) {
                    return;
                }
                try {
                    log.info("{}awscomprehend", infoPrefix);
                    FileManager.generateAWSComprehendFiles(InventoryUtil.fetchAWSComprehendInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "awscomprehend\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "awscomprehend", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("appflow"))) {
                    return;
                }
                try {
                    log.info("{}appflow", infoPrefix);
                    FileManager.generateAWSAppFlowFiles(InventoryUtil.fetchAppFlowInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "appflow\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "appflow", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("ecstaskdefinition"))) {
                    return;
                }
                try {
                    log.info("{}ecstaskdefinition", infoPrefix);
                    FileManager.generateAWSECSFiles(InventoryUtil.fetchECSTaskDefInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ecstaskdefinition\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "ecstaskdefinition", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("ecscluster"))) {
                    return;
                }
                try {
                    log.info("{}ecscluster", infoPrefix);
                    FileManager.generateAWSECSClusterFiles(InventoryUtil.fetchECSClusterInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ecscluster\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "ecscluster", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("accessanalyzer"))) {
                    return;
                }
                try {
                    log.info("{}accessanalyzer", infoPrefix);
                    FileManager.generateAccessAnalyzerFiles(InventoryUtil.fetchAccessAnalyzerInfo(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "accessanalyzer\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "accessanalyzer", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("ami"))) {
                    return;
                }
                try {
                    log.info("{}ami", infoPrefix);
                    FileManager.generateAMIFiles(InventoryUtil.fetchAMI(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "ami\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "ami", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("backupvault"))) {
                    return;
                }
                try {
                    log.info("{}backupvault", infoPrefix);
                    FileManager.generateBackupvalut(InventoryUtil.fetchBackupVaults(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "backupvault\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "backupvault", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("iampolicies"))) {
                    return;
                }
                try {
                    log.info("{}iampolicies", infoPrefix);
                    FileManager.generateIamPolicyFiles(InventoryUtil.fetchIAMCustomerManagedPolicies(temporaryCredentials, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "iampolicies\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "iampolicies", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("securityhub"))) {
                    return;
                }
                try {
                    log.info("{}securityhub", infoPrefix);
                    FileManager.generateSecurityHubFiles(InventoryUtil.fetchSecurityHub(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "securityhub\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "securityhub", e.getMessage());
                }
            });

            executor.execute(() ->
            {
                if (!(isTypeInScope("launchtemplate"))) {
                    return;
                }
                try {
                    log.info("{}launchtemplate", infoPrefix);
                    FileManager.generateLaunchTemplateFiles(InventoryUtil.fetchLaunchTemplates(temporaryCredentials, skipRegions, accountId, accountName));
                } catch (Exception e) {
                    log.error(expPrefix + "launchtemplate\", \"cause\":\"" + e.getMessage() + "\"}");
                    ErrorManageUtil.uploadError(accountId, "", "launchtemplate", e.getMessage());
                }
            });

            executor.shutdown();
            while (!executor.isTerminated()) {
            }

            log.info("Completed Discovery for accountId - {}", accountId);
        }

        ErrorManageUtil.writeErrorFile();
        ErrorManageUtil.omitOpsAlert();
        if (!ErrorManageUtil.getErrorMap().isEmpty()) {
            //Below logger message is used by datadog to create data alert.
            log.error(ERROR_PREFIX + "At least one collector failed" + ENDING_QUOTES);
        }

        try {
            FileManager.finalise();
            ErrorManageUtil.finalise();
        } catch (IOException e) {
            log.error(ERROR_PREFIX + "Exception occurred while writing data to file" + ENDING_QUOTES, e);
            System.exit(1);
        }
    }

    /**
     * Checks if is type in scope.
     *
     * @param type the type
     * @return true, if is type in scope
     */
    private boolean isTypeInScope(String type) {
        if ("".equals(targetTypes)) {
            return true;
        } else {
            List<String> targetTypesList = Arrays.asList(targetTypes.split(","));
            return targetTypesList.contains(type);
        }
    }
}

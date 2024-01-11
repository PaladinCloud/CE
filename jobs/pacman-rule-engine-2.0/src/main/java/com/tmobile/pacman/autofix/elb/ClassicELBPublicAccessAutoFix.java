/**
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p>
 * Copyright (C) 2018 T Mobile Inc - All Rights Reserve
 * Purpose: Classic ELB's publicly accessible AWS resources
 * Author :Santhoshi,Kanchana
 * Modified Date: Oct 22, 2018
 **/
/**
 Copyright (C) 2018 T Mobile Inc - All Rights Reserve
 Purpose: Classic ELB's publicly accessible AWS resources
 Author :Santhoshi,Kanchana
 Modified Date: Oct 22, 2018
 **/
package com.tmobile.pacman.autofix.elb;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.tmobile.pacman.autofix.publicaccess.PublicAccessAutoFix;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.common.exception.AutoFixException;
import com.tmobile.pacman.commons.autofix.BaseFix;
import com.tmobile.pacman.commons.autofix.FixResult;
import com.tmobile.pacman.commons.autofix.PacmanFix;
import com.tmobile.pacman.dto.AutoFixTransaction;
import com.tmobile.pacman.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@PacmanFix(key = "publicly-accessible-classicelb", desc = "Classic ELB Applies Security Group without public access")
public class ClassicELBPublicAccessAutoFix extends BaseFix {
    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassicELBPublicAccessAutoFix.class);

    private static final String EXISTING_GROUPS = "existingClassicElbGroups";
    private static String ATTACHED_SG = null;
    private static String DETACHED_SG = null;

    @Override
    public FixResult executeFix(Map<String, String> issue, Map<String, Object> clientMap, Map<String, String> ruleParams) {
        String resourceId = issue.get(PacmanSdkConstants.RESOURCE_ID);
        String defaultCidrIp = ruleParams.get("defaultCidrIp");
        String scheme = issue.get("scheme");
        Map<String, Object> ec2ClinetMap = null;
        List<SecurityGroup> securityGroupsDetails = null;
        AmazonEC2 ec2Client = null;
        Collection<IpPermission> ipPermissionsToBeAdded = null;
        if (("internet-facing").equals(scheme)) {
            try {
                ec2ClinetMap = PublicAccessAutoFix.getAWSClient("ec2", issue, CommonUtils.getPropValue(PacmanSdkConstants.AUTO_FIX_ROLE_NAME));

                List<String> securityGroupsTobeApplied = new ArrayList<>();

                AmazonElasticLoadBalancing amazonApplicationElasticLoadBalancing = (AmazonElasticLoadBalancing) clientMap.get("client");
                List<String> securityGroupNames = PublicAccessAutoFix.getSgListForClassicElbResource(clientMap, resourceId);
                Set<String> securityGroupsSet = new HashSet<>(securityGroupNames);
                Set<String> alreadyCheckedSgSet = new HashSet<>();
                if (ec2ClinetMap != null) {
                    ec2Client = (AmazonEC2) ec2ClinetMap.get("client");
                    securityGroupsDetails = PublicAccessAutoFix.getExistingSecurityGroupDetails(securityGroupsSet, ec2Client);
                }

                String vpcid;
                String securityGroupId = null;
                Set<String> publiclyAccessible = new HashSet<>();
                boolean isSgApplied = false;


                for (SecurityGroup securityGroup : securityGroupsDetails) {
                    ipPermissionsToBeAdded = new ArrayList<>();
                    publiclyAccessible = new HashSet<>();
                    vpcid = securityGroup.getVpcId();
                    securityGroupId = securityGroup.getGroupId();
                    PublicAccessAutoFix.nestedSecurityGroupDetails(securityGroupId, ipPermissionsToBeAdded, ec2Client, publiclyAccessible, alreadyCheckedSgSet, 0);

                    if (!publiclyAccessible.isEmpty()) {
                        // copy the security group and remove in bound rules
                        String createdSgId = PublicAccessAutoFix.createSecurityGroup(securityGroupId, vpcid, ec2Client, ipPermissionsToBeAdded, resourceId, defaultCidrIp, securityGroup.getIpPermissions());
                        if (!StringUtils.isEmpty(createdSgId)) {
                            securityGroupsTobeApplied.add(createdSgId);
                        }
                    } else {
                        securityGroupsTobeApplied.add(securityGroupId);
                    }
                }
                if (!securityGroupsTobeApplied.isEmpty()) {
                    isSgApplied = PublicAccessAutoFix.applySecurityGroupsToClassicELB(amazonApplicationElasticLoadBalancing, securityGroupsTobeApplied, resourceId);
                    ATTACHED_SG = securityGroupsTobeApplied.toString();
                }

                if (isSgApplied) {
                    LOGGER.info("{} sg's successfully applied for the resource {}", securityGroupsTobeApplied, resourceId);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
        return new FixResult(PacmanSdkConstants.STATUS_SUCCESS_CODE, "the classic elb " + resourceId + " is now fixed");
    }


    /* * (non-Javadoc)
     *
     * @see
     * com.tmobile.pacman.commons.autofix.BaseFix#backupExistingConfigForResource
     * (java.lang.String, java.lang.String, java.util.Map, java.util.Map,
     * java.util.Map)*/

    @Override
    public boolean backupExistingConfigForResource(final String resourceId, final String resourceType, Map<String, Object> clientMap, Map<String, String> ruleParams, Map<String, String> issue) throws AutoFixException {
        StringBuilder oldConfig = new StringBuilder();
        List<String> originalSgMembers;
        try {
            originalSgMembers = PublicAccessAutoFix.getSgListForClassicElbResource(clientMap, resourceId);
            for (String sgm : originalSgMembers) {

                if (oldConfig.length() > 0) {
                    oldConfig.append(",").append(sgm);
                } else {
                    oldConfig.append(sgm);
                }

            }
        } catch (Exception e) {
            LOGGER.error("back up failed", e.getMessage());
            throw new AutoFixException("backup failed");
        }
        DETACHED_SG = oldConfig.toString();
        backupOldConfig(resourceId, EXISTING_GROUPS, oldConfig.toString());
        LOGGER.debug("backup complete for {}", resourceId);
        return true;
    }


    /* * (non-Javadoc)
     *
     * @see
     * com.tmobile.pacman.commons.autofix.BaseFix#isFixCandidate(java.lang.String
     * , java.lang.String, java.util.Map, java.util.Map, java.util.Map)*/

    @Override
    public boolean isFixCandidate(String resourceId, String resourceType, Map<String, Object> clientMap, Map<String, String> ruleParams, Map<String, String> issue) throws AutoFixException {
        String scheme = issue.get("scheme");
        Map<String, Object> ec2ClinetMap = null;
        List<SecurityGroup> securityGroupsDetails = null;
        AmazonEC2 ec2Client = null;
        Collection<IpPermission> ipPermissionsToBeAdded = null;
        Boolean hasPublicAccess = false;
        if (("internet-facing").equals(scheme)) {
            try {
                ec2ClinetMap = PublicAccessAutoFix.getAWSClient("ec2", issue, CommonUtils.getPropValue(PacmanSdkConstants.AUTO_FIX_ROLE_NAME));
                List<String> securityGroupNames = PublicAccessAutoFix.getSgListForClassicElbResource(clientMap, resourceId);
                Set<String> securityGroupsSet = new HashSet<>(securityGroupNames);
                Set<String> alreadyCheckedSgSet = new HashSet<>();
                if (ec2ClinetMap != null) {
                    ec2Client = (AmazonEC2) ec2ClinetMap.get("client");
                    securityGroupsDetails = PublicAccessAutoFix.getExistingSecurityGroupDetails(securityGroupsSet, ec2Client);
                }

                String securityGroupId = null;
                Set<String> publiclyAccessible = new HashSet<>();


                for (SecurityGroup securityGroup : securityGroupsDetails) {
                    ipPermissionsToBeAdded = new ArrayList<>();
                    publiclyAccessible = new HashSet<>();
                    securityGroupId = securityGroup.getGroupId();
                    PublicAccessAutoFix.nestedSecurityGroupDetails(securityGroupId, ipPermissionsToBeAdded, ec2Client, publiclyAccessible, alreadyCheckedSgSet, 0);

                    if (!publiclyAccessible.isEmpty()) {
                        hasPublicAccess = true;
                    }
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
        }
        return hasPublicAccess;

    }


    /** (non-Javadoc)
     *
     * @see
     * com.tmobile.pacman.commons.autofix.BaseFix#addDetailsToTransactionLog()*/

    @Override
    public AutoFixTransaction addDetailsToTransactionLog(Map<String, String> annotation) {
        LinkedHashMap<String, String> transactionParams = new LinkedHashMap();
        if (!StringUtils.isEmpty(annotation.get("_resourceid"))) {
            transactionParams.put("resourceId", annotation.get("_resourceid"));
        } else {
            transactionParams.put("resourceId", "No Data");
        }
        if (!StringUtils.isEmpty(annotation.get("accountid"))) {
            transactionParams.put("accountId", annotation.get("accountid"));
        } else {
            transactionParams.put("accountId", "No Data");
        }
        if (!StringUtils.isEmpty(annotation.get("region"))) {
            transactionParams.put("region", annotation.get("region"));
        } else {
            transactionParams.put("region", "No Data");
        }
        if (!StringUtils.isEmpty(ATTACHED_SG)) {
            transactionParams.put("attachedSg", ATTACHED_SG);
        } else {
            transactionParams.put("attachedSg", "No Data");
        }
        if (!StringUtils.isEmpty(DETACHED_SG)) {
            transactionParams.put("detachedSg", DETACHED_SG);
        } else {
            transactionParams.put("detachedSg", "No Data");
        }
        ATTACHED_SG = null;
        DETACHED_SG = null;
        return new AutoFixTransaction(null, transactionParams);
    }

}

/*******************************************************************************
  * Copyright 2019 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
/**
  Copyright (C) 2019 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :Amisha
  Modified Date: May 10, 2022

 **/
package com.tmobile.cloud.awsrules.cloudtrail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class})
public class CloudTrailGlobalServicesRuleTest{
	
	@InjectMocks
	CloudTrailGlobalServicesRule cloudTrailGlobalServicesRule;
	
	@Test
	public void executeTest() throws Exception{
		mockStatic(PacmanUtils.class);
		when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(
	                true);
		assertThat(cloudTrailGlobalServicesRule.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
		when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(
                false);
		assertThatThrownBy(
                () -> cloudTrailGlobalServicesRule.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
	}
	
	 
    @Test
    public void getHelpTextTest(){
        assertThat(cloudTrailGlobalServicesRule.getHelpText(), is(notNullValue()));
    }
	
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("iamPriviliges","iamPriviliges");
        commonMap.put(",lambda:*,*",",lambda:*,*");
        commonMap.put(",ec2:*,*",",ec2:*,*");
        commonMap.put("lambda","lambda");
        commonMap.put(",ec2:*,*,s3:*,s3:put*",",ec2:*,*,s3:*,s3:put*");

        commonMap.put("cidripv6", "cidripv6");
        commonMap.put("username", "svc_123");
        commonMap.put("associationid", "associationid");
        commonMap.put("domainname", "domainname");
        commonMap.put("accesspolicies", "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":\"*\"},\"Action\":\"es:*\",\"Resource\":\"123/*\"}]}");
        commonMap.put("scheme", "internet-facing");
        commonMap.put("subnets", "subnets");
        commonMap.put("esElbWithSGUrl", "esElbWithSGUrl");
        commonMap.put("esEc2SgURL", "esEc2SgURL");
        commonMap.put("endpoint", "endpoint");
        commonMap.put("esRoutetableAssociationsURL", "esRoutetableAssociationsURL");
        commonMap.put("esRoutetableRoutesURL", "esRoutetableRoutesURL");
        commonMap.put("esRoutetableURL", "esRoutetableURL");
        commonMap.put("esSgRulesUrl", "esSgRulesUrl");
        commonMap.put("esSubnetURL", "esSubnetURL");
        commonMap.put("identifiableKey", "identifiableKey");
        commonMap.put("subnetEsURL", "subnetEsURL");
        commonMap.put("esSubnetURL", "esSubnetURL");
        commonMap.put("awsSearch", "awsSearch");
        commonMap.put("kernelInfoApi", "kernelInfoApi");
        commonMap.put("esNonAdminAccntsWithIAMFullAccessUrl", "esNonAdminAccntsWithIAMFullAccessUrl");
        commonMap.put("esLdapUrl", "esLdapUrl");
        commonMap.put("esQualysUrl", "esQualysUrl");
        commonMap.put("esSatAndSpacewalkUrl", "esSatAndSpacewalkUrl");
        commonMap.put("esServiceURL", "esServiceURL");
        commonMap.put("esAdGroupURL", "esAdGroupURL");
        commonMap.put("esEbsWithInstanceUrl", "esEbsWithInstanceUrl");
        commonMap.put("esAppTagURL", "esAppTagURL");
        commonMap.put("esEc2SgURL", "esEc2SgURL");
        commonMap.put("esEc2WithVulnInfoForS5Url", "esEc2WithVulnInfoForS5Url");
        commonMap.put("esEc2PubAccessPortUrl", "esEc2PubAccessPortUrl");
        commonMap.put("esSsmWithInstanceUrl", "esSsmWithInstanceUrl");
        commonMap.put("esElasticIpUrl", "esElasticIpUrl");
        commonMap.put("esAppElbWithInstanceUrl", "esAppElbWithInstanceUrl");
        commonMap.put("esClassicElbWithInstanceUrl", "esClassicElbWithInstanceUrl");
        commonMap.put("esGuardDutyUrl", "esGuardDutyUrl");
        commonMap.put("esNonAdminAccntsWithIAMFullAccessUrl", "esNonAdminAccntsWithIAMFullAccessUrl");
        commonMap.put("esSgRulesUrl", "esSgRulesUrl");
        commonMap.put("esServiceWithSgUrl", "esServiceWithSgUrl");
        commonMap.put("ES_URI", "ES_URI");
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "low");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("accountid", "12345");
        commonMap.put("checkId", "1234567");
        commonMap.put("serviceEsURL", "url");
        commonMap.put("serviceAccountEsURL", "serviceAccountEsURL");
        commonMap.put("description", "R FND");
        commonMap.put("elasticIpEsUrl", "elasticIpEsUrl");
        commonMap.put("region", "us-east-1");
        commonMap.put("authType", "authType");
        commonMap.put("splitterChar", ",");
        commonMap.put("roleIdentifyingString", "roleIdentifyingString");
        commonMap.put("ldapApi", "ldapApi");
        commonMap.put("satAndSpacewalkApi", "satAndSpacewalkApi");
        commonMap.put("qualysApi", "qualysApi");
        commonMap.put("kernelVersionByInstanceIdUrl",
                "kernelVersionByInstanceIdUrl");
        commonMap.put("defaultKernelCriteriaUrl", "defaultKernelCriteriaUrl");
        commonMap.put("accountNames", "accountNames");
        commonMap.put("sourceType", "sourceType");
        commonMap.put("statename", "running");
        commonMap.put("ebsWithInstanceUrl", "ebsWithInstanceUrl");
        commonMap.put("volumeid", "volumeid");
        commonMap.put("loadbalancername", "loadbalancername");
        commonMap.put("targetExpireDuration", "150");
        commonMap.put("validto", "12/10/2018 23:33");
        commonMap.put("appElbWithInstanceUrl", "appElbWithInstanceUrl");
        commonMap.put("loadbalancerarn", "loadbalancerarn");
        commonMap.put("classicElbWithInstanceUrl", "classicElbWithInstanceUrl");
        commonMap.put("guardDutyEsUrl", "guardDutyEsUrl");
        commonMap.put("dbinstanceidentifier", "dbinstanceidentifier");
        commonMap.put("dbsnapshotarn", "dbsnapshotarn");
        commonMap.put("publiclyaccessible", "true");
        commonMap.put("apiGWURL", "apiGWURL");
        commonMap.put("portToCheck", "22");
        commonMap.put("sgRulesUrl", "sgRulesUrl");
        commonMap.put("cidrIp", "cidrIp");
        commonMap.put("serviceWithSgUrl", "serviceWithSgUrl");
        commonMap.put("esUrl", "esUrl");
        commonMap.put("groupid", "groupid");
        commonMap.put("adGroupEsURL", "adGroupEsURL");
        commonMap.put("target", "30");
        commonMap.put("inScope", "true");
        commonMap.put("role", "role");
        commonMap.put("passwordlastused", "2018-07-16 12:16:38+00");
        commonMap.put("pwdInactiveDuration", "1");
        commonMap.put("status_RED", "status_RED");
        commonMap.put("tags.Application", "identifiableKey");
        commonMap.put("_entitytype", "elasticache");
        commonMap.put("appTagEsURL", "appTagEsURL");
        commonMap.put("heimdallESURL", "heimdallESURL");
        commonMap.put("deprecatedInstanceType", "deprecatedInstanceType");
        commonMap.put("instancetype", "xyz");
        commonMap.put("running", "running");
        commonMap.put("instanceid", "instanceid");
        commonMap.put("ec2PubAccessPortUrl", "ec2PubAccessPortUrl");
        commonMap.put("ec2WithVulnInfoForS5Url", "ec2WithVulnInfoForS5Url");
        commonMap.put("ec2PortRuleId", "ec2PortRuleId");
        commonMap.put("severityVulnValue", "severityVulnValue");
        commonMap.put("publicipaddress", "publicipaddress");
        commonMap.put("Stopped", "Stopped");
        commonMap.put("statetransitionreason",
                "User initiated (2017-10-20 11:36:20 GMT)");
        commonMap.put("targetstoppedDuration", "30");
        commonMap.put("privateipaddress", "privateipaddress");
        commonMap.put("port", "22");
        commonMap.put("ssmWithInstanceUrl", "ssmWithInstanceUrl");
        commonMap.put("mandatoryTags", "mandatoryTags");
        commonMap.put("targetType", "targetType");
        commonMap.put("internetGateWay", "internetGateWay");
        commonMap.put("ec2SgEsURL", "ec2SgEsURL");
        commonMap.put("routetableAssociationsEsURL",
                "routetableAssociationsEsURL");
        commonMap.put("routetableRoutesEsURL", "routetableRoutesEsURL");
        commonMap.put("routetableEsURL", "routetableEsURL");
        commonMap.put("target", "30");
        commonMap.put("sgRulesUrl", "sgRulesUrl");
        commonMap.put("cidrIp", "cidrIp");
        commonMap.put("subnetid", "subnetid");
        commonMap.put("vpcid", "vpcid");
        commonMap.put("accountname", "accountname");
        commonMap.put("client", "client");
        commonMap.put("platform", "platform");
        commonMap.put("ruleName", "ruleName");
        commonMap.put("functionname", "functionname");
        commonMap.put("timePeriodInHours", "30");
        commonMap.put("threshold", "30");
        commonMap.put("rolename", "rolename");
        commonMap.put("adminRolesToCompare", "adminRolesToCompare");
        commonMap.put("kernelversionForComparision.x86_64",
                "kernelversionForComparision.x86_64");
        commonMap.put("reponse", "success");
        commonMap.put("lucene_version", "success");
        commonMap.put("final_u_last_patched", "2018-08-01 00:00:00.000000");
        commonMap.put("final_kernel_release", "123");
        commonMap.put("firstdiscoveredon", "2018-08-03 10:00:00+00");
        commonMap.put("discoveredDaysRange", "7");
        commonMap.put("vpc", "vpc");
        commonMap.put("securitygroups", "securitygroups");
        commonMap.put("ruleId", "PacMan_Amazon_CloudTrail_Global_Services");
        commonMap.put("policyId", "PacMan_Amazon_CloudTrail_Global_Services_Version-1");
        commonMap.put("policyVersion", "Version-1");
        commonMap.put("policyVersion", "Version-1");
        commonMap.put("includeglobalserviceevents", "false");
        return commonMap;
    }
}

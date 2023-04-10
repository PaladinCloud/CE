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
package com.tmobile.pacman.api.compliance.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.exception.ServiceException;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import com.tmobile.pacman.api.compliance.domain.IssueExceptionResponse;
import com.tmobile.pacman.api.compliance.domain.IssuesException;
import com.tmobile.pacman.api.compliance.repository.ComplianceRepository;
import com.tmobile.pacman.api.compliance.repository.FilterRepository;
import com.tmobile.pacman.api.compliance.util.CommonTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ComplianceServiceImplTest {

    @InjectMocks
    private ComplianceServiceImpl complianceService;

    @Mock
    private ComplianceRepository complianceRepository;

    @Mock
    private FilterRepository filterRepository;
    
    @Mock
    PacmanRdsRepository rdsepository;
    
    @Mock
    private ElasticSearchRepository elasticSearchRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getResourceDetailsTest() throws Exception {

        Map<String, Object> policyMap = new HashMap<>();
        policyMap.put("policyId", "TaggingPolicy_version-1_Ec2TaggingPolicy_ec2");

        List<Map<String, Object>> policyList = new ArrayList<>();
        policyList.add(policyMap);
        when(
                complianceRepository.getResourceDetailsFromES(anyString(),
                        anyString())).thenReturn(policyList);
        assertThat(complianceService.getResourceDetails("dummyString",
                "testString"), is(notNullValue()));

        when(
                complianceRepository.getResourceDetailsFromES(anyString(),
                        anyString())).thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.getResourceDetails("dummyString",
                        "testString")).isInstanceOf(ServiceException.class);
    }

    @Test
    public void getRecommendationsTest() throws Exception {

        Map<String, Object> policyMap = new HashMap<>();
        policyMap.put("policyId", "TaggingPolicy_version-1_Ec2TaggingPolicy_ec2");

        List<Map<String, Object>> policyList = new ArrayList<>();
        policyList.add(policyMap);
        when(complianceRepository.getRecommendations(anyString(), anyString()))
                .thenReturn(policyList);
        assertThat(complianceService.getRecommendations("dummyString",
                "dummyString"), is(notNullValue()));

        when(complianceRepository.getRecommendations(anyString(), anyString()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.getRecommendations("dummyString",
                        "dummyString")).isInstanceOf(ServiceException.class);
    }

    @Test
    public void getIssuesCountTest() throws Exception {

        long issueCount = 100;
        when(
                complianceRepository.getIssuesCount(anyString(), anyString(),
                        anyString(),null)).thenReturn(issueCount);
        assertThat(complianceService.getIssuesCount("dummyString",
                "dummyString", "dummyString",null), is(100l));

        when(
                complianceRepository.getIssuesCount(anyString(), anyString(),
                        anyString(),null)).thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.getIssuesCount("dummyString",
                        "dummyString", "dummyString",null)).isInstanceOf(
                ServiceException.class);
    }

    @Test
    public void getDistributionTest() throws Exception {
        Map<String, Long> sevDistubutionMap = new HashMap<>();
        sevDistubutionMap.put("high", 100l);
        sevDistubutionMap.put("security", 200l);

        Map<String, Object> policyCategoryDistubutionMap = new HashMap<>();
        policyCategoryDistubutionMap.put("high", 100l);
        policyCategoryDistubutionMap.put("security", 200l);
        when(complianceRepository.getTargetTypeForAG(anyString(), anyString()))
                .thenReturn(CommonTestUtil.getTargetTypes());
        long issueCount = 100;
        when(
                complianceRepository.getIssuesCount(anyString(), anyString(),
                        anyString(),null)).thenReturn(issueCount);
        when(complianceRepository.getPolicyIds(anyString())).thenReturn(
                CommonTestUtil.getRules());
        when(
                complianceRepository.getPoliciesDistribution(anyString(),
                        anyString(), anyObject(), anyString())).thenReturn(
                sevDistubutionMap);
        when(
                complianceRepository.getPoliciesDistribution(anyString(),
                        anyString(), anyObject(), anyString())).thenReturn(
                sevDistubutionMap);
        when(
                complianceRepository.getPolicyCategoryPercentage(anyObject(),
                        anyObject())).thenReturn(policyCategoryDistubutionMap);

        assertThat(
                complianceService.getDistribution("dummyString", "dummyString",null),
                is(notNullValue()));
    }

    @Test
    public void getTaggingTest() throws Exception {

        Map<String, Long> taggingMap = new HashMap<>();
        taggingMap.put("assets", 100l);
        taggingMap.put("tagged", 100l);
        when(complianceRepository.getTagging(anyString(), anyString()))
                .thenReturn(taggingMap);
        assertThat(complianceService.getTagging("dummyString", "dummyString"),
                is(notNullValue()));

        when(complianceRepository.getTagging(anyString(), anyString()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService
                        .getTagging("dummyString", "dummyString"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    public void getCertificatesTest() throws Exception {

        Map<String, Long> certMap = new HashMap<>();
        certMap.put("assetCount", 100l);
        when(complianceRepository.getCertificates(anyString())).thenReturn(
                certMap);
        assertThat(complianceService.getCertificates("dummyString"),
                is(notNullValue()));

        when(complianceRepository.getCertificates(anyString())).thenThrow(
                new DataException());

        assertThatThrownBy(
                () -> complianceService.getCertificates("dummyString"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    public void revokeIssueExceptionTest() throws Exception {

        Boolean isIssueIdRevoked = true;
        when(complianceRepository.revokeAndUpdateIssueDetails(anyString()))
                .thenReturn(isIssueIdRevoked);
        assertThat(complianceService.revokeIssueException("dummyString"),
                is(true));

        when(complianceRepository.revokeAndUpdateIssueDetails(anyString()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.revokeIssueException("dummyString"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    public void updateKernelVersionTest() throws Exception {

        Map<String, Object> kernelMap = new HashMap<>();
        kernelMap.put("kernelversion", 123);
        when(
                complianceRepository
                        .updateKernelVersion(CommonTestUtil.getKernelVersion()))
                .thenReturn(kernelMap);
        assertThat(
                complianceService
                        .updateKernelVersion(CommonTestUtil.getKernelVersion()),
                is(notNullValue()));
    }

    @Test
    public void getIssuesTest() throws Exception {

        // request.setAg("aws-all");
        when(complianceRepository.getIssuesFromES(anyObject())).thenReturn(
                CommonTestUtil.getResponseWithOrder());
        assertThat(complianceService.getIssues(CommonTestUtil.getRequest()),
                is(notNullValue()));

        when(complianceRepository.getIssuesFromES(anyObject())).thenThrow(
                new DataException());

        assertThatThrownBy(
                () -> complianceService.getIssues(CommonTestUtil.getRequest()))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    public void getPatchingTest() throws Exception {

        when(filterRepository.getListOfTargetTypes(anyString(), anyString()))
                .thenReturn(CommonTestUtil.getAssetCountByApps());

        when(
                complianceRepository.getPatchabeAssetsCount(anyString(),
                        anyString(),anyString(),anyString(),anyString())).thenReturn(1000l);

        when(
                complianceRepository.getUnpatchedAssetsCount(anyString(),
                        anyString(),anyString())).thenReturn(1000l);

        assertThat(complianceService.getPatching("test", "",""),
                is(notNullValue()));
        assertThat(complianceService.getPatching("test", "test",""),
                is(notNullValue()));
    }

    @Test
    public void addIssueExceptionTest() throws Exception {

        when(complianceRepository.exemptAndUpdateIssueDetails(anyObject()))
                .thenReturn(true);
        assertThat(complianceService.addIssueException(CommonTestUtil
                .getIssueResponse()), is(notNullValue()));

        when(complianceRepository.exemptAndUpdateIssueDetails(anyObject()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.addIssueException(CommonTestUtil
                        .getIssueResponse())).isInstanceOf(
                ServiceException.class);
    }

    @Test
    public void getPolicycomplianceTest() throws Exception {
        when(complianceRepository.getTargetTypeForAG(anyString(), anyString()))
                .thenReturn(CommonTestUtil.getTargetTypes());
        when(complianceRepository.getInstanceCountForQualys(anyString(),anyString(),anyString(),anyString(),anyString()))
        .thenReturn(5000l);

        when(
                complianceRepository
                        .getPolicyIdWithDisplayNameWithPolicyCategoryQuery(
                                anyString(), anyString())).thenReturn(
                CommonTestUtil.getMapList());
        when(complianceRepository.getPoliciesLastScanDate()).thenReturn(
                CommonTestUtil.getMapList());
        when(complianceRepository.getTotalAssetCount(anyString(), anyString(),anyString(),anyString()))
                .thenReturn(CommonTestUtil.getMapLong());
        when(complianceRepository.getPolicyIdDetails(anyString())).thenReturn(
                CommonTestUtil.getMapList());
        when(complianceRepository.getPolicyIDsForTargetType(anyString()))
                .thenReturn(CommonTestUtil.getMapList());

        when(complianceRepository.getTaggingByAG(anyString(),anyString(),anyString())).thenReturn(CommonTestUtil.
                getMapObject());
        when(
                complianceRepository.getNonCompliancePolicyByEsWithAssetGroup(
                        anyString(), anyString(), anyObject(), anyInt(),
                        anyInt(), anyString())).thenReturn(
                CommonTestUtil.getMapLong());

        assertThat(complianceService.getPolicyCompliance(CommonTestUtil
                .getRequest()), is(notNullValue()));
    }

    @Test
    public void getPolicycomplianceWithoutPolicyIdTest() throws Exception {
        assertThat(complianceService.getPolicyCompliance(CommonTestUtil
                .getRequest()), is(nullValue()));
    }

    @Test
    public void getIssueAuditLogTest() throws Exception {

        when(
                complianceRepository.getIssueAuditLogCount(anyString(),
                        anyString())).thenReturn(5000l);
        when(
                complianceRepository.getIssueAuditLog(anyString(),anyString(), anyString(),
                        anyInt(), anyInt(), anyString())).thenReturn(
                CommonTestUtil.getLinkedHashMapObjectList());
        assertThat(complianceService.getIssueAuditLog("aws","dummyString",
                "dummyString", 2, 2, "dummyString"), is(notNullValue()));

        when(
                complianceRepository.getIssueAuditLog(anyString(),anyString(), anyString(),
                        anyInt(), anyInt(), anyString())).thenThrow(
                new DataException());

        assertThatThrownBy(
                () -> complianceService.getIssueAuditLog("aws","dummyString",
                        "dummyString", 2, 2, "dummyString")).isInstanceOf(
                ServiceException.class);
    }

    @Test
    public void getPolicyDetailsbyApplicationTest() throws Exception {

        when(
                complianceRepository.getPolicyDetailsByApplicationFromES(
                        anyString(), anyString(), anyString())).thenReturn(
                CommonTestUtil.getJsonArray());

        when(complianceRepository.getTargetTypeByPolicyId(anyString()))
                .thenReturn(CommonTestUtil.getMapList());

        when(
                complianceRepository.getPatchableAssetsByApplication(
                        anyString(), anyString(), anyString())).thenReturn(
                CommonTestUtil.getMapLong());
        when(
                complianceRepository.getAllApplicationsAssetCountForTargetType(
                        anyString(), anyString())).thenReturn(
                CommonTestUtil.getMapLong());

        when(
                complianceRepository.getInstanceCountForQualysByAppsOrEnv(
                        anyString(), anyString(),anyString(), anyString(),anyString())).thenReturn(
                CommonTestUtil.getMapLong());

        assertThat(
                complianceService.getPolicyDetailsByApplication(
                        "dummyString",
                        "PacMan_cloud-kernel-compliance_version-1_Ec2-Kernel-Compliance-Policy_ec2",
                        "dummyString"), is(notNullValue()));

        assertThat(
                complianceService.getPolicyDetailsByApplication(
                        "dummyString",
                        "PacMan_onpremisekernelversion_version-1_onpremKernelVersionPolicy_onpremserver",
                        "dummyString"), is(notNullValue()));

        assertThat(
                complianceService.getPolicyDetailsByApplication(
                        "dummyString",
                        "PacMan_Ec2InstanceScannedByQualys_version-1_Ec2-instance-scanned-by-qualys-API_ec2",
                        "dummyString"), is(notNullValue()));
        assertThat(complianceService.getPolicyDetailsByApplication("dummyString",
                "", "dummyString"), is(notNullValue()));

    }

    @Test
    public void getPolicyDetailsbyEnvironmentTest() throws Exception {

        when(
                complianceRepository.getPolicyDetailsByEnvironmentFromES(
                        anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtil.getJsonArray());

        when(complianceRepository.getTargetTypeByPolicyId(anyString()))
                .thenReturn(CommonTestUtil.getMapList());

        when(
        		complianceRepository.getTotalAssetCountByEnvironment(
                        anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtil.getMapLong());

        when(
        		complianceRepository.getInstanceCountForQualys(
                        anyString(), anyString(),anyString(), anyString(),anyString())).thenReturn(5000l);

        assertThat(
                complianceService.getPolicyDetailsByEnvironment(
                        "dummyString",
                        "PacMan_cloud-kernel-compliance_version-1_Ec2-Kernel-Compliance-Policy_ec2",
                        "", "dummyString"), is(notNullValue()));

        assertThat(
                complianceService.getPolicyDetailsByEnvironment(
                        "dummyString",
                        "PacMan_onpremisekernelversion_version-1_onpremKernelVersionPolicy_onpremserver",
                        "", "dummyString"), is(notNullValue()));
        assertThat(complianceService.getPolicyDetailsByEnvironment("dummyString",
                "", "", "dummyString"), is(notNullValue()));

        assertThat(
                complianceService.getPolicyDetailsByEnvironment(
                        "dummyString",
                        "PacMan_Ec2InstanceScannedByQualys_version-1_Ec2-instance-scanned-by-qualys-API_ec2",
                        "dummyString",""), is(notNullValue()));
        assertThat(complianceService.getPolicyDetailsByEnvironment("dummyString",
                "", "","dummyString"), is(notNullValue()));
    }

    @Test
    public void getPolicyDescriptionTest() throws Exception {

        when(complianceRepository.getPolicyDescriptionFromDb(anyString()))
                .thenReturn(CommonTestUtil.getMapList());

        assertThat(complianceService.getPolicyDescription("dummyString"),
                is(notNullValue()));

        when(complianceRepository.getPolicyDescriptionFromDb(anyString()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.getPolicyDescription("dummyString"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    public void getResourceTypeTest() throws Exception {

        when(complianceRepository.getTargetTypeForAG(anyString(), anyString()))
                .thenReturn(CommonTestUtil.getTargetTypes());
        ReflectionTestUtils.setField(complianceService, "projEligibletypes",
                "ec2,onpremserver");
        assertThat(
                complianceService.getResourceType("dummyString", "dummyString"),
                is(notNullValue()));
    }

    @Test
    public void getKernelComplianceByInstanceIdFromDbTest() throws Exception {

        when(
                complianceRepository
                        .getKernelComplianceByInstanceIdFromDb(anyString()))
                .thenReturn(CommonTestUtil.getMapList());
        assertThat(
                complianceService
                        .getKernelComplianceByInstanceIdFromDb("dummyString"),
                is(notNullValue()));

        when(
                complianceRepository
                        .getKernelComplianceByInstanceIdFromDb(anyString()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService
                        .getKernelComplianceByInstanceIdFromDb("dummyString"))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    public void getPolicyViolationDetailsByIssueIdTest() throws Exception {

        when(
                complianceRepository.getPolicyViolationDetailsByIssueId(
                        anyString(), anyString())).thenReturn(CommonTestUtil.getMapObject());
        when(complianceRepository.getPolicyDescriptionFromDb(anyString()))
                .thenReturn(CommonTestUtil.getMapList());
        assertThat(complianceService.getPolicyViolationDetailsByIssueId(
                "dummyString", "dummyString"), is(notNullValue()));

        when(
                complianceRepository.getPolicyViolationDetailsByIssueId(
                        anyString(), anyString())).thenThrow(
                new DataException());

        assertThatThrownBy(
                () -> complianceService.getPolicyViolationDetailsByIssueId(
                        "dummyString", "dummyString")).isInstanceOf(
                ServiceException.class);
    }

    @Test
    public void getOverallComplianceByDomainTest() throws Exception {

        when(complianceRepository.getTargetTypeForAG(anyString(), anyString()))
                .thenReturn(CommonTestUtil.getTargetTypes());
        when(complianceRepository.getPolicyIds(anyString())).thenReturn(
                CommonTestUtil.getRules());
        when(complianceRepository.getPolicyCategoryWeightagefromDB(anyString()))
                .thenReturn(CommonTestUtil.getMapObject());
        getPolicycomplianceTest();

        assertThat(
                complianceService.getOverallComplianceByDomain("test", "test"),
                is(notNullValue()));
    }

    @Test
    public void getOverallComplianceByDomainPolicyCategoryWeightageEmptyCheckTest()
            throws Exception {

        when(complianceRepository.getTargetTypeForAG(anyString(), anyString()))
                .thenReturn(CommonTestUtil.getTargetTypes());
        when(complianceRepository.getPolicyIds(anyString())).thenReturn(
                CommonTestUtil.getRules());

        when(complianceRepository.getPolicyCategoryWeightagefromDB(anyString()))
                .thenReturn(new HashMap<String, Object>());

        getPolicycomplianceTest();

        assertThat(
                complianceService.getOverallComplianceByDomain("test", "test"),
                is(notNullValue()));
    }

    @Test
    public void closeIssuesByPolicyTest() throws Exception {
        when(complianceRepository.closeIssuesByPolicy(anyObject())).thenReturn(
                true);
        assertThat(complianceService.closeIssuesByPolicy(CommonTestUtil
                .getRuleDetails()), is(notNullValue()));
    }

    @Test
    public void formatExceptionTest() throws Exception {
        assertThat(complianceService.formatException(new ServiceException(
                "No Data Found")), is(notNullValue()));
    }

    @Test
    public void closeIssuesByPolicyElsePartTest() throws Exception {
        when(complianceRepository.closeIssuesByPolicy(anyObject())).thenReturn(
                false);
        assertThat(complianceService.closeIssuesByPolicy(CommonTestUtil
                .getRuleDetails()), is(notNullValue()));
    }
    
    @Test
    public void addMultipleIssueExceptionTest() throws Exception {

        
        IssueExceptionResponse issueExceptionResponse = new IssueExceptionResponse();
        issueExceptionResponse.setStatus("Success");
        when(complianceRepository.exemptAndUpdateMultipleIssueDetails("",anyObject()))
                .thenReturn(issueExceptionResponse);
        
        IssuesException issuesException = new IssuesException();
        List<String> issueIds = new ArrayList<>();
        issueIds.add("1234");
        issuesException.setIssueIds(issueIds);
        assertThat(complianceService.addMultipleIssueException("",issuesException).getStatus().equals("Success"),
                is(true));

        when(complianceRepository.exemptAndUpdateMultipleIssueDetails("aws",anyObject()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.addMultipleIssueException("",issuesException))
                .isInstanceOf(ServiceException.class);
    }
    
    @Test
    public void revokeIssueMultipleExceptionTest() throws Exception {

        IssueExceptionResponse issueExceptionResponse = new IssueExceptionResponse();
        issueExceptionResponse.setStatus("Success");
        when(complianceRepository.revokeAndUpdateMultipleIssueDetails("aws",anyObject(), anyString()))
                .thenReturn(issueExceptionResponse);
        
        assertThat(complianceService.revokeMultipleIssueException("aws",new ArrayList<>(), "admin").getStatus().equals("Success"),
                is(true));

        when(complianceRepository.revokeAndUpdateMultipleIssueDetails("aws",anyObject(), anyString()))
                .thenThrow(new DataException());

        assertThatThrownBy(
                () -> complianceService.revokeMultipleIssueException("aws",new ArrayList<>(), "admin"))
                .isInstanceOf(ServiceException.class);
    }
    
/*    @Test
    public void getExemptedIssuesTest() throws Exception {

        when(complianceRepository.getTargetTypeForAG(anyString(),anyString())).thenReturn(CommonTestUtil.getTargetTypes());
        ReflectionTestUtils.setField(complianceService, "projEligibletypes",
                "ec2,onpremserver");
        ReflectionTestUtils.setField(complianceService, "mandatoryTags", "mandatoryTags");
        when(complianceRepository.getPolicyIdWithDisplayNameQuery(anyString())).thenReturn(CommonTestUtil.getListMapObject());
        when(rdsepository.getDataFromPacman(anyString())).thenReturn(CommonTestUtil.getListMapObject());
        when(complianceRepository.getPolicyIds(anyString())).thenReturn(new ArrayList<Object>());
        when(complianceRepository.getExemptedResourceDetails(anyString(),anyString())).thenReturn(CommonTestUtil.getMapOfMapObject());
        when(complianceRepository.getExemptedIssuesForTaggingPolicy(anyObject(),anyString(),anyString(),anyString())).thenReturn(CommonTestUtil.getListMapObject());
        when(complianceRepository.getExemptedUntaggedCount(anyString(),anyString(),anyString(),anyObject())).thenReturn(1000);
       
        when(elasticSearchRepository.getSortedDataFromESBySize(anyString(),anyString(),anyObject(),anyObject(),anyObject(),anyObject(),anyInt(),anyInt(),anyString(),anyObject(),anyObject())).thenReturn(CommonTestUtil.getListMapObject());
        when(elasticSearchRepository.getTotalDocumentCountForIndexAndType(anyString(),anyString(),anyObject(),anyObject(),anyObject(),anyString(),anyObject())).thenReturn(1000l);
        assertThat(complianceService.getExemptedIssues(CommonTestUtil.getRequest()),
                is(notNullValue()));
        
        assertThat(complianceService.getExemptedIssues(CommonTestUtil.getRequestWithIssueId()),
                is(notNullValue()));
        
        assertThat(complianceService.getExemptedIssues(CommonTestUtil.getRequestWithIssueId()),
                is(notNullValue()));
        
    }*/

}

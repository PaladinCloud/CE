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
package com.tmobile.cso.pacman.datashipper.entity;


import com.tmobile.cso.pacman.datashipper.dto.DatasourceData;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.AssetGroupUtil;
import com.tmobile.cso.pacman.datashipper.util.AuthManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AssetGroupUtil.class, ESManager.class, AuthManager.class})
public class AssetGroupStatsCollectorTest {

    AssetGroupStatsCollector assetGroupStatsCollector = new AssetGroupStatsCollector();

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(AuthManager.class);
        when(AuthManager.getToken()).thenReturn("");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadAssetGroupTagCompliance() throws Exception {
        PowerMockito.mockStatic(AssetGroupUtil.class);
        Map<String, Object> comSummaryMap = new HashMap<>();
        comSummaryMap.put("total", 1345L);
        comSummaryMap.put("compliant", 1000L);
        comSummaryMap.put("noncompliant", 345L);
        when(AssetGroupUtil.fetchTaggingSummary(anyString())).thenReturn(comSummaryMap);

        PowerMockito.mockStatic(ESManager.class);
        doNothing().when(ESManager.class);
        ESManager.uploadData(anyString(), anyString(), anyList(), anyString(), anyBoolean());

        assetGroupStatsCollector.uploadAssetGroupTagCompliance(Collections.singletonList("pacman"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadAssetGroupRuleCompliance() throws Exception {
        PowerMockito.mockStatic(AssetGroupUtil.class);
        List<Map<String, Object>> ruleInfoList = new ArrayList<>();
        Map<String, Object> ruleInfo = new HashMap<>();
        ruleInfo.put("domain", "infra");
        ruleInfo.put("policyId", "testruleid1");
        ruleInfo.put("compliance_percent", 55);
        ruleInfo.put("total", 1345L);
        ruleInfo.put("compliant", 1000L);
        ruleInfo.put("noncompliant", 345L);
        ruleInfo.put("contribution_percent", 66);
        ruleInfoList.add(ruleInfo);
        when(AssetGroupUtil.fetchPolicyComplianceInfo(anyString(), anyList())).thenReturn(ruleInfoList);

        PowerMockito.mockStatic(ESManager.class);
        doNothing().when(ESManager.class);
        ESManager.uploadData(anyString(), anyString(), anyList(), anyString(), anyBoolean());

        List<String> assetGroups = new ArrayList<>();
        List<String> accountIds = new ArrayList<>();
        assetGroups.add("test");
        accountIds.add("test");
        DatasourceData datasourceData = new DatasourceData();
        datasourceData.setAssetGroups(assetGroups);
        datasourceData.setAccountIds(accountIds);
        assetGroupStatsCollector.uploadAssetGroupRuleCompliance(datasourceData);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUploadAssetGroupCompliance() throws Exception {
        PowerMockito.mockStatic(AssetGroupUtil.class);
        List<Map<String, Object>> ruleInfoList = new ArrayList<>();
        Map<String, Object> complnInfo = new HashMap<>();
        complnInfo.put("domain", "infra");
        complnInfo.put("tagging", 60);
        complnInfo.put("security", 89);
        complnInfo.put("costOptimization", 66);
        complnInfo.put("governance", 83);
        complnInfo.put("overall", 74);
        ruleInfoList.add(complnInfo);
        when(AssetGroupUtil.fetchPolicyComplianceInfo(anyString(), anyList())).thenReturn(ruleInfoList);

        PowerMockito.mockStatic(ESManager.class);
        doNothing().when(ESManager.class);
        ESManager.uploadData(anyString(), anyString(), anyList(), anyString(), anyBoolean());

        List<String> assetGroups = new ArrayList<>();
        List<String> accountIds = new ArrayList<>();
        assetGroups.add("test");
        accountIds.add("test");
        DatasourceData datasourceData = new DatasourceData();
        datasourceData.setAssetGroups(assetGroups);
        datasourceData.setAccountIds(accountIds);
        assetGroupStatsCollector.uploadAssetGroupCompliance(datasourceData);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testUploadAssetGroupCountStats() throws Exception {
        PowerMockito.mockStatic(AssetGroupUtil.class);
        List<Map<String, Object>> typeCounts = new ArrayList<>();
        Map<String, Object> typeCount = new HashMap<>();
        typeCount.put("type", "ec2");
        typeCount.put("count", 125L);
        typeCounts.add(typeCount);
        when(AssetGroupUtil.fetchTypeCounts(anyString())).thenReturn(typeCounts);


        Map<String, Map<String, Map<String, Object>>> currentInfo = new HashMap<>();
        Map<String, Object> minMax = new HashMap<>();
        minMax.put("min", 100);
        minMax.put("max", 120);
        Map<String, Map<String, Object>> typeMap = new HashMap<>();
        typeMap.put("ec2", minMax);
        currentInfo.put("pacman", typeMap);
        PowerMockito.mockStatic(ESManager.class);
        when(ESManager.fetchCurrentCountStatsForAssetGroups(anyString())).thenReturn(currentInfo);
        doNothing().when(ESManager.class);
        ESManager.uploadData(anyString(), anyString(), anyList(), anyString(), anyBoolean());

        Map<String, List<String>> assetGroups = new HashMap<>();
        List<String> domains = new ArrayList<>();
        domains.add("infra");
        assetGroups.put("pacman", domains);
        assetGroupStatsCollector.uploadAssetGroupCountStats(Collections.singletonList("pacman"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUploadAssetGroupIssues() throws Exception {
        PowerMockito.mockStatic(AssetGroupUtil.class);
        List<Map<String, Object>> returnList = new ArrayList<>();
        Map<String, Object> issuesInfo = new HashMap<>();
        issuesInfo.put("domain", "infra");
        issuesInfo.put("total", 123L);
        returnList.add(issuesInfo);
        when(AssetGroupUtil.fetchIssuesInfo(anyString(), anyList())).thenReturn(returnList);

        PowerMockito.mockStatic(ESManager.class);
        doNothing().when(ESManager.class);
        ESManager.uploadData(anyString(), anyString(), anyList(), anyString(), anyBoolean());

        List<String> assetGroups = new ArrayList<>();
        List<String> accountIds = new ArrayList<>();
        assetGroups.add("test");
        accountIds.add("test");
        DatasourceData datasourceData = new DatasourceData();
        datasourceData.setAssetGroups(assetGroups);
        datasourceData.setAccountIds(accountIds);
        assetGroupStatsCollector.uploadAssetGroupIssues(datasourceData);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testCollectAssetGroupStats() throws Exception {

        List<String> assetGroups = new ArrayList<>();
        List<String> accountIds = new ArrayList<>();
        assetGroups.add("test");
        accountIds.add("test");
        DatasourceData datasourceData = new DatasourceData();
        datasourceData.setAssetGroups(assetGroups);
        datasourceData.setAccountIds(accountIds);

        PowerMockito.mockStatic(AssetGroupUtil.class);


        PowerMockito.mockStatic(ESManager.class);
        doNothing().when(ESManager.class);
        ESManager.createIndex(anyString(), anyList());
        ESManager.createType(anyString(), anyString(), anyList());

        assetGroupStatsCollector = PowerMockito.spy(assetGroupStatsCollector);
        doNothing().when(assetGroupStatsCollector).uploadAssetGroupRuleCompliance(any());
        doNothing().when(assetGroupStatsCollector).uploadAssetGroupCountStats(anyList());
        doNothing().when(assetGroupStatsCollector).uploadAssetGroupCompliance(any());
        doNothing().when(assetGroupStatsCollector).uploadAssetGroupTagCompliance(anyList());
        doNothing().when(assetGroupStatsCollector).uploadAssetGroupIssues(any());
        assetGroupStatsCollector.collectAssetGroupStats(datasourceData);

    }
}


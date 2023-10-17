package com.tmobile.cso.pacman.datashipper.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpUtil.class})
public class AssetGroupUtilTest {
    private final String issuesInfoMockResponse = "{\n" +
            "    \"data\": {\n" +
            "        \"distribution\": {\n" +
            "            \"total_issues\": 3426,\n" +
            "            \"distribution_policyCategory\": {\n" +
            "                \"tagging\": 309,\n" +
            "                \"security\": 2768,\n" +
            "                \"operations\": 329,\n" +
            "                \"cost\": 20\n" +
            "            },\n" +
            "            \"policyCategory_percentage\": {\n" +
            "                \"tagging\": 9.0,\n" +
            "                \"security\": 80.0,\n" +
            "                \"operations\": 9.0,\n" +
            "                \"cost\": 2.0\n" +
            "            },\n" +
            "            \"distribution_by_severity\": {\n" +
            "                \"high\": 1399,\n" +
            "                \"critical\": 564,\n" +
            "                \"low\": 1106,\n" +
            "                \"medium\": 357\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"message\": \"success\"\n" +
            "}";

    private final String mockRuleComplianceInfo = "{\n" +
            "    \"data\": {\n" +
            "        \"response\": [\n" +
            "            {\n" +
            "                \"severity\": \"high\",\n" +
            "                \"name\": \"Assign Mandatory Tags to BigQueryDataset\",\n" +
            "                \"compliance_percent\": 0.0,\n" +
            "                \"lastScan\": \"2023-10-13T15:50:15.689Z\",\n" +
            "                \"policyCategory\": \"tagging\",\n" +
            "                \"riskScore\": 0,\n" +
            "                \"resourcetType\": \"bigquerydataset\",\n" +
            "                \"provider\": \"GCP\",\n" +
            "                \"policyId\": \"TaggingRule_version-1_BigQueryDatasetTagging_bigquerydataset\",\n" +
            "                \"assetsScanned\": 1,\n" +
            "                \"passed\": 0,\n" +
            "                \"failed\": 1,\n" +
            "                \"contribution_percent\": 0.0,\n" +
            "                \"autoFixEnabled\": false,\n" +
            "                \"autoFixAvailable\": false,\n" +
            "                \"exempted\": 0,\n" +
            "                \"isAssetsExempted\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"severity\": \"high\",\n" +
            "                \"name\": \"Assign Mandatory Tags to CloudSQL\",\n" +
            "                \"compliance_percent\": 0.0,\n" +
            "                \"lastScan\": \"2023-10-13T15:52:17.198Z\",\n" +
            "                \"policyCategory\": \"tagging\",\n" +
            "                \"riskScore\": 0,\n" +
            "                \"resourcetType\": \"cloudsql\",\n" +
            "                \"provider\": \"GCP\",\n" +
            "                \"policyId\": \"TaggingRule_version-1_CloudSqlTagging_cloudsql\",\n" +
            "                \"assetsScanned\": 4,\n" +
            "                \"passed\": 0,\n" +
            "                \"failed\": 4,\n" +
            "                \"contribution_percent\": 0.0,\n" +
            "                \"autoFixEnabled\": false,\n" +
            "                \"autoFixAvailable\": false,\n" +
            "                \"exempted\": 0,\n" +
            "                \"isAssetsExempted\": false\n" +
            "            },\n" +
            "            {\n" +
            "                \"severity\": \"high\",\n" +
            "                \"name\": \"Assign Mandatory Tags to CloudStorage\",\n" +
            "                \"compliance_percent\": 100.0,\n" +
            "                \"lastScan\": \"2023-10-13T15:35:20.204Z\",\n" +
            "                \"policyCategory\": \"tagging\",\n" +
            "                \"riskScore\": 0,\n" +
            "                \"resourcetType\": \"cloudstorage\",\n" +
            "                \"provider\": \"GCP\",\n" +
            "                \"policyId\": \"TaggingRule_version-1_CloudStorageTagging_cloudstorage\",\n" +
            "                \"assetsScanned\": 0,\n" +
            "                \"passed\": 0,\n" +
            "                \"failed\": 0,\n" +
            "                \"contribution_percent\": 0.0,\n" +
            "                \"autoFixEnabled\": false,\n" +
            "                \"autoFixAvailable\": false,\n" +
            "                \"exempted\": 0,\n" +
            "                \"isAssetsExempted\": false\n" +
            "            }       ],\n" +
            "        \"total\": 3\n" +
            "    },\n" +
            "    \"message\": \"success\"\n" +
            "}";

    @Before
    public void setUp() {
        PowerMockito.mockStatic(HttpUtil.class);
        PowerMockito.mockStatic(System.class);

        PowerMockito.when(System.getenv("PACMAN_API_URI")).thenReturn("");
    }

    @Test
    public void testFetchTypeCounts() throws Exception {
        String typeCountJson = "{\"data\":{\"ag\":\"aws-all\",\"assetcount\":[{\"count\":1949,\"type\":\"subnet\"},{\"count\":5885,\"type\":\"stack\"},{\"count\":714,\"type\":\"asgpolicy\"},{\"count\":3926,\"type\":\"rdssnapshot\"},{\"count\":84,\"type\":\"rdscluster\"},{\"count\":1320,\"type\":\"cert\"},{\"count\":481,\"type\":\"internetgateway\"},{\"count\":419,\"type\":\"rdsdb\"}]}}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(typeCountJson);
        List<Map<String, Object>> typeCounts = AssetGroupUtil.fetchTypeCounts(anyString());

        assertThat(typeCounts.size(), is(8));
    }

    @Test(expected = Exception.class)
    public void testFetchTypeCountsException() throws Exception {
        String typeCountJson = "{\"data1\":{\"ag\":\"aws-all\",\"assetcount\":[{\"count\":1949,\"type\":\"subnet\"},{\"count\":5885,\"type\":\"stack\"},{\"count\":714,\"type\":\"asgpolicy\"},{\"count\":3926,\"type\":\"rdssnapshot\"},{\"count\":84,\"type\":\"rdscluster\"},{\"count\":1320,\"type\":\"cert\"},{\"count\":481,\"type\":\"internetgateway\"},{\"count\":419,\"type\":\"rdsdb\"}]}}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(typeCountJson);
        AssetGroupUtil.fetchTypeCounts(anyString());
    }

    @Test
    public void testFetchComplianceInfo() throws Exception {
        String complResponse = "{\"data\":{\"distribution\":{\"tagging\":59,\"security\":89,\"costOptimization\":67,\"governance\":82,\"overall\":74}},\"message\":\"success\"}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(complResponse);
        List<Map<String, Object>> complianceInfo = AssetGroupUtil.fetchComplianceInfo(anyString(), Collections.singletonList("infra"));

        assertThat(complianceInfo.size(), is(1));
        assertThat(complianceInfo.get(0).get("domain").toString(), is("infra"));
        assertThat(complianceInfo.get(0).get("overall").toString(), is("74"));
    }

    @Test(expected = Exception.class)
    public void testFetchComplianceInfoException() throws Exception {
        String complResponse = "{\"data1\":{\"distribution\":{\"tagging\":59,\"security\":89,\"costOptimization\":67,\"governance\":82,\"overall\":74}},\"message\":\"success\"}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(complResponse);
        AssetGroupUtil.fetchComplianceInfo(anyString(), Collections.singletonList("infra"));
    }

    @Test
    public void testFetchRuleComplianceInfo() throws Exception {
        when(HttpUtil.post(anyString(), anyString(), anyString(), anyString())).thenReturn(mockRuleComplianceInfo);
        List<Map<String, Object>> complianceInfo = AssetGroupUtil.fetchPolicyComplianceInfo(anyString(), Collections.singletonList("infra"));

        assertThat(complianceInfo.size(), is(3));
        assertThat(complianceInfo.get(0).get("domain").toString(), is("infra"));
        assertThat(complianceInfo.get(0).get("policyId").toString(), is("TaggingRule_version-1_BigQueryDatasetTagging_bigquerydataset"));
    }

    @Test(expected = Exception.class)
    public void testFetchRuleComplianceInfoException() throws Exception {
        when(HttpUtil.post(anyString(), anyString(), anyString(), anyString())).thenReturn(mockRuleComplianceInfo.replace("data", "data1"));
        AssetGroupUtil.fetchPolicyComplianceInfo(anyString(), Collections.singletonList("infra"));
    }

    @Test
    public void testFetchVulnerabilitySummary() throws Exception {
        String vulnSummaryJson = "{\"data\":{\"output\":{\"hosts\":7192,\"vulnerabilities\":132285,\"totalVulnerableAssets\":5815}},\"message\":\"success\"}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(vulnSummaryJson);
        Map<String, Object> vulnSummary = AssetGroupUtil.fetchVulnSummary(anyString());

        assertThat(vulnSummary.get("total"), is(7192L));
    }

    @Test(expected = Exception.class)
    public void testFetchVulnerabilitySummaryException() throws Exception {
        String vulnSummaryJson = "{\"data1\":{\"output\":{\"hosts\":7192,\"vulnerabilities\":132285,\"totalVulnerableAssets\":5815}},\"message\":\"success\"}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(vulnSummaryJson);
        AssetGroupUtil.fetchVulnSummary(anyString());
    }

    @Test
    public void testFetchTaggingSummary() throws Exception {
        String tagSummaryJson = "{\"data\":{\"output\":{\"assets\":124704,\"untagged\":49384,\"tagged\":75320,\"compliance\":60}},\"message\":\"success\"}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(tagSummaryJson);
        Map<String, Object> tagSummary = AssetGroupUtil.fetchTaggingSummary(anyString());

        assertThat(tagSummary.get("total"), is(124704L));
    }

    @Test(expected = Exception.class)
    public void testFetchTaggingSummaryException() throws Exception {
        String tagSummaryJson = "{\"data1\":{\"output\":{\"assets\":124704,\"untagged\":49384,\"tagged\":75320,\"compliance\":60}},\"message\":\"success\"}";
        when(HttpUtil.get(anyString(), anyString())).thenReturn(tagSummaryJson);
        AssetGroupUtil.fetchTaggingSummary(anyString());
    }

    @Test
    public void testFetchIssuesInfo() throws Exception {
        when(HttpUtil.get(anyString(), anyString())).thenReturn(issuesInfoMockResponse);
        List<Map<String, Object>> issueDistribution = AssetGroupUtil.fetchIssuesInfo(anyString(), Collections.singletonList("infra"));

        assertThat(issueDistribution.get(0).get("domain"), is("infra"));
        assertThat(issueDistribution.get(0).get("total"), is(3426L));
    }

    @Test(expected = Exception.class)
    public void testFetchIssuesInfoException() throws Exception {
        when(HttpUtil.get(anyString(), anyString())).thenReturn(issuesInfoMockResponse.replace("data", "data1"));
        AssetGroupUtil.fetchIssuesInfo(anyString(), Collections.singletonList("infra"));
    }
}

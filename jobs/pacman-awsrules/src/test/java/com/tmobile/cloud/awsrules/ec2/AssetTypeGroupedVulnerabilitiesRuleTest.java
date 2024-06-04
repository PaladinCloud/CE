package com.tmobile.cloud.awsrules.ec2;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.model.CveDetails;
import com.tmobile.cloud.model.VulnerabilityInfo;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, Annotation.class})
public class AssetTypeGroupedVulnerabilitiesRuleTest {
    @InjectMocks
    AssetTypeGroupedVulnerabilitiesRule assetTypeGroupedVulnerabilitiesRule;

    @Test
    public void correctlyProcessCritical() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        String vulnerabilities = "{\"instanceId\":\"iid-1\",\"critical\":[{\"id\":\"CVE-2024-29986\",\"severity\":\"severe\",\"title\":\"Microsoft Edge Chromium: CVE-2024-29986\",\"url\":\"https://example.com/microsoft-edge-cve-2024-29986/\"},{\"id\":\"\",\"severity\":\"severe\",\"title\":\"Weak LAN Manager hashing permitted\",\"url\":\"https://example.com/weak-lan-manager-hashing-permitted\",\"cveUrl\":\"https://support.microsoft.com/en-us/help/2793313/security-guidance-for-ntlmv1-and-lm-network-authentication\"}]}";
        when(PacmanUtils.matchAssetAgainstSourceVulnIndex(anyString(), anyString(), anyString(), anyObject())).thenReturn(convertVulnerabilities(vulnerabilities));
        PolicyResult result = assetTypeGroupedVulnerabilitiesRule.execute(getRuleParams("critical"), getResourceAttributes());
        assertThat(result, is(notNullValue()));
        String stringDetails = result.getAnnotation().get("vulnerabilityDetails");
        assertThat(stringDetails, is(notNullValue()));
        VulnerabilityInfo[] results = new Gson().fromJson(stringDetails, VulnerabilityInfo[].class);
        assertThat(results, is(notNullValue()));
        assertEquals(Arrays.toString(getExpectedVulnerabilityInfo()), Arrays.toString(results));
    }

    @Test
    public void correctlyProcessNoVulnerabilities() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString())).thenReturn(true);
        String noVulnerabilities = "{\"instanceId\":\"iid-1\",\"medium\":[]}";
        when(PacmanUtils.matchAssetAgainstSourceVulnIndex(anyString(), anyString(), anyString(), anyObject())).thenReturn(convertVulnerabilities(noVulnerabilities));
        PolicyResult result = assetTypeGroupedVulnerabilitiesRule.execute(getRuleParams("medium"), getResourceAttributes());
        assertThat(result, is(notNullValue()));
        assertThat(result.getStatus(), is("success"));
        assertNull(result.getAnnotation());
    }

    private List<JsonObject> convertVulnerabilities(String vulnerabilitiesJson) {
        Gson gson = new Gson();
        List<JsonObject> array = new ArrayList<>();
        array.add(gson.fromJson(vulnerabilitiesJson, JsonObject.class));
        return array;
    }

    private VulnerabilityInfo[] getExpectedVulnerabilityInfo() {
        VulnerabilityInfo[] v = new VulnerabilityInfo[2];
        CveDetails[] cveList1 = {new CveDetails("CVE-2024-29986", "https://nvd.nist.gov/vuln/detail/CVE-2024-29986")};
        v[0] = new VulnerabilityInfo();
        v[0].setTitle("Microsoft Edge Chromium: CVE-2024-29986");
        v[0].setVulnerabilityUrl("https://example.com/microsoft-edge-cve-2024-29986/");
        v[0].setCveList(Arrays.asList(cveList1));

        CveDetails[] cveList2 = {new CveDetails("", "https://support.microsoft.com/en-us/help/2793313/security-guidance-for-ntlmv1-and-lm-network-authentication")};
        v[1] = new VulnerabilityInfo();
        v[1].setTitle("Weak LAN Manager hashing permitted");
        v[1].setVulnerabilityUrl("https://example.com/weak-lan-manager-hashing-permitted/");
        v[1].setCveList(Arrays.asList(cveList2));
        return v;
    }

    private Map<String, String> getRuleParams(String severity) {
        Map<String, String> map = new HashMap<>();
        map.put("severity", severity);
        map.put("severityMatchCriteria", severity);
        map.put("policyCategory", "one");
        return map;
    }

    private Map<String, String> getResourceAttributes() {
        Map<String, String> map = new HashMap<>();
        map.put("_resourceid", "iid-1");
        return map;
    }
}

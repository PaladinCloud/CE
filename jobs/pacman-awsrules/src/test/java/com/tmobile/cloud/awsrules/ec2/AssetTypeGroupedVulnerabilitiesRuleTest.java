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
//        String vulnerabilities = "{ \"instanceId\": \"iid-1\", \"critical\": [ { \"id\": \"CVE-2024-29986\", \"severity\": \"severe\", \"title\": \"Microsoft Edge Chromium: CVE-2024-29986\", \"url\": \"https://example.com/microsoft-edge-cve-2024-29986/\" }, { \"id\": \"CVE-2024-29987\", \"severity\": \"severe\", \"title\": \"Microsoft Edge Chromium: CVE-2024-29987\", \"url\": \"https://example.com/microsoft-edge-cve-2024-29987/\" } ] }";
        String vulnerabilities = "{\"instanceId\":\"iid-1\",\"critical\":[{\"severity\":\"critical\",\"vulnerabilities\":[{\"id\":\"CVE-2020-36330\",\"url\":\"https://nvd.nist.gov/vuln/detail/CVE-2020-36330\"},{\"id\":\"CVE-2018-25014\",\"url\":\"https://nvd.nist.gov/vuln/detail/CVE-2018-25014\"},{\"id\":\"CVE-2018-25013\",\"url\":\"https://nvd.nist.gov/vuln/detail/CVE-2018-25013\"},{\"id\":\"CVE-2020-36331\",\"url\":\"https://nvd.nist.gov/vuln/detail/CVE-2020-36331\"}],\"title\":\"libwebp 0.3.0-10.amzn2\",\"url\":\"https://example.com\",\"referenceUrl\":\"https://nvd.nist.gov/vuln/detail/CVE-2020-36330\"},{\"severity\":\"critical\",\"vulnerabilities\":[{\"id\":\"CVE-2015-8394\",\"url\":\"https://nvd.nist.gov/vuln/detail/CVE-2015-8394\"},{\"id\":\"CVE-2015-8390\",\"url\":\"https://nvd.nist.gov/vuln/detail/CVE-2015-8390\"}],\"title\":\"pcre 8.32-17.amzn2.0.2\",\"url\":\"https://example.com\",\"referenceUrl\":\"https://nvd.nist.gov/vuln/detail/CVE-2015-8394\"}]}";
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
        CveDetails[] cveList1 = {
            new CveDetails("CVE-2020-36330", "https://nvd.nist.gov/vuln/detail/CVE-2020-36330"),
            new CveDetails("CVE-2018-25014", "https://nvd.nist.gov/vuln/detail/CVE-2018-25014"),
            new CveDetails("CVE-2018-25013", "https://nvd.nist.gov/vuln/detail/CVE-2018-25013"),
            new CveDetails("CVE-2020-36331", "https://nvd.nist.gov/vuln/detail/CVE-2020-36331")
        };
        v[0] = new VulnerabilityInfo();
        v[0].setTitle("libwebp 0.3.0-10.amzn2");
        v[0].setVulnerabilityUrl("https://example.com");
        v[0].setCveList(Arrays.asList(cveList1));

        CveDetails[] cveList2 = {
            new CveDetails("CVE-2015-8394", "https://nvd.nist.gov/vuln/detail/CVE-2015-8394"),
            new CveDetails("CVE-2015-8390", "https://nvd.nist.gov/vuln/detail/CVE-2015-8390")
        };
        v[1] = new VulnerabilityInfo();
        v[1].setTitle("pcre 8.32-17.amzn2.0.2");
        v[1].setVulnerabilityUrl("https://example.com/");
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

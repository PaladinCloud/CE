package com.tmobile.cloud.awsrules.rds;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.BasePolicy;
import com.tmobile.pacman.commons.policy.PolicyResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, BasePolicy.class})
public class CheckForRDSTransportEncryptionTest {
    private static final String RDS_DB_INSTANCE_PARAM_URL = "/aws/rdsdb_parameters/_search";
    Map<String, String> ruleParam;
    Map<String, String> resourceAttribute;
    @InjectMocks
    private CheckForRDSTransportEncryption checkForRDSTransportEncryption;

    @Before
    public void setup() {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.getPacmanHost(PacmanRuleConstants.ES_URI)).thenReturn(PacmanRuleConstants.ES_URI);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        ruleParam = getInputParamMap();
        resourceAttribute = getValidResourceData();
    }

    @Test
    public void executeTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + RDS_DB_INSTANCE_PARAM_URL), any(),
                any(), any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("test")));
        PolicyResult ruleResult = checkForRDSTransportEncryption.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
    }

    @Test
    public void executeFailByParamValueTest() throws Exception {
        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + RDS_DB_INSTANCE_PARAM_URL), any(),
                any(), any(), any(), any())).thenReturn(new HashSet<>(Collections.singletonList("0")));
        PolicyResult ruleResult = checkForRDSTransportEncryption.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailTest() throws Exception {

        resourceAttribute.put(PacmanRuleConstants.ENCRYPTED, "false");
        PolicyResult ruleResult = checkForRDSTransportEncryption.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void executeFailThrowsExceptionTest() throws Exception {

        when(PacmanUtils.getValueFromElasticSearchAsSet(eq(PacmanRuleConstants.ES_URI + RDS_DB_INSTANCE_PARAM_URL), any(),
                any(), any(), any(), any())).thenThrow(new RuntimeException());
        PolicyResult ruleResult = checkForRDSTransportEncryption.execute(ruleParam, resourceAttribute);
        assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
    }

    @Test
    public void getHelpTest() {
        assertNotNull(checkForRDSTransportEncryption.getHelpText());
    }

    private Map<String, String> getInputParamMap() {
        Map<String, String> ruleParam = new HashMap<>();
        ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
        ruleParam.put(PacmanSdkConstants.POLICY_ID,
                "test_version-1_test");
        ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
        ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);
        ruleParam.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        ruleParam.put(PacmanRuleConstants.REGIONS, "test");
        return ruleParam;
    }

    private Map<String, String> getValidResourceData() {
        Map<String, String> resObj = new HashMap<>();
        resObj.put("_resourceid", "test");
        resObj.put(PacmanRuleConstants.ACCOUNTID, "123456789");
        resObj.put(PacmanRuleConstants.ENGINE, "postgres");
        resObj.put(PacmanRuleConstants.DB_INSTANCE_IDENTIFIER, "test");
        resObj.put(PacmanRuleConstants.DB_PARAMETER_GROUP_NAME, "test");
        return resObj;
    }
}

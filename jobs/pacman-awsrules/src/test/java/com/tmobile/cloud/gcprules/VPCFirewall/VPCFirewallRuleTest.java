package com.tmobile.cloud.gcprules.VPCFirewall;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.VPCNetwork.VPCFirewallUncommonports;
import com.tmobile.cloud.gcprules.VPCNetwork.VPCNetworkRule;
import com.tmobile.cloud.gcprules.utils.GCPUtils;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static com.tmobile.cloud.gcprules.VPCFirewall.Utils.TestUtils.getHitsJsonArrayForVPCFIreWall;
import static com.tmobile.cloud.gcprules.VPCFirewall.Utils.TestUtils.getHitsJsonArrayForVPCFIreWallFailure;
import static com.tmobile.cloud.gcprules.VPCFirewall.Utils.TestUtils.getHitsJsonArrayForVPCFIreWallWithMultiplePorts;
import static com.tmobile.cloud.gcprules.VPCFirewall.Utils.TestUtils.getHitsJsonArrayForVPCFIreWallWithRangeOfPorts;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PacmanUtils.class, GCPUtils.class})
public class VPCFirewallRuleTest {

    @InjectMocks
    VPCNetworkRule vpcNetworkRule;
    @InjectMocks
    VPCFirewallUncommonports uncommonPortsPolicy;

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("port", "123,12,1234");

        return commonMap;
    }

    @Before
    public void setUp() {
        mockStatic(PacmanUtils.class);
        mockStatic(GCPUtils.class);
    }

    @Test
    public void executeSuccessTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonArrayForVPCFIreWall());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        Map<String, String> policyParams = getMapString("r_123 ");

        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        assertThat(uncommonPortsPolicy.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        policyParams.put("port", "");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        assertThat(uncommonPortsPolicy.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    @Test
    public void executeFailureTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonArrayForVPCFIreWall());
        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        Map<String, String> policyParams = getMapString("r_123 ");
        policyParams.put("port", "123,80,1234");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
        assertThat(uncommonPortsPolicy.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
        policyParams.put("port", "80");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
        assertThat(uncommonPortsPolicy.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithMultiplePorts() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonArrayForVPCFIreWallWithMultiplePorts());
        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        Map<String, String> policyParams = getMapString("r_123 ");
        policyParams.put("port", "123,80,1234");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
        policyParams.put("port", "90");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeSuccessTestWithMultiplePorts() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonArrayForVPCFIreWallWithMultiplePorts());
        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        Map<String, String> policyParams = getMapString("r_123 ");
        policyParams.put("port", "123,3306,1234");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        policyParams.put("port", "100");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureTestWithRangeOfPortsPorts() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonArrayForVPCFIreWallWithRangeOfPorts());
        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        Map<String, String> policyParams = getMapString("r_123 ");
        policyParams.put("port", "85-95");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
        policyParams.put("port", "90");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
        policyParams.put("port", "70-81");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
        policyParams.put("port", "100-105");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeSuccessTestWithRangeOfPortsPorts() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonArrayForVPCFIreWallWithRangeOfPorts());
        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        Map<String, String> policyParams = getMapString("r_123 ");
        policyParams.put("port", "65-75");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        policyParams.put("port", "110");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        policyParams.put("port", "70-79");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        policyParams.put("port", "101-105");
        assertThat(vpcNetworkRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonArrayForVPCFIreWallFailure());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        assertThatThrownBy(() -> vpcNetworkRule.execute(getMapString("r_123 "), getMapString("r_123 ")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    public void getHelpTextTest() {
        assertThat(vpcNetworkRule.getHelpText(), is(notNullValue()));
    }
}

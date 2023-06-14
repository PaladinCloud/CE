package com.tmobile.cloud.gcprules.VPCFirewall;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.gcprules.VPCNetwork.VPCNetworkOutboundRule;
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

import static com.tmobile.cloud.gcprules.VPCFirewall.Utils.TestUtils.getHitsJsonArrayForVPCFIreWallEgress;
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
public class VPCFireWallEgressTest {

    @InjectMocks
    VPCNetworkOutboundRule vPCNetworkOutboundRule;

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("port", "0-65535");

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
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject())).thenReturn(getHitsJsonArrayForVPCFIreWallEgress());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        Map<String, String> policyParams = getMapString("r_123 ");
        policyParams.put("port", "81-65535");
        assertThat(vPCNetworkOutboundRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));
        policyParams.put("port", "0-79");
        assertThat(vPCNetworkOutboundRule.execute(policyParams, getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_SUCCESS));

    }

    @Test
    public void executeFailureTest() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonArrayForVPCFIreWallEgress());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                true);
        assertThat(vPCNetworkOutboundRule.execute(getMapString("r_123 "), getMapString("r_123 ")).getStatus(),
                is(PacmanSdkConstants.STATUS_FAILURE));
    }

    @Test
    public void executeFailureTestWithInvalidInputException() throws Exception {

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");
        when(GCPUtils.getHitsArrayFromEs(anyObject(), anyObject()))
                .thenReturn(getHitsJsonArrayForVPCFIreWallEgress());

        when(PacmanUtils.createAnnotation(anyString(), anyObject(), anyString(), anyString(), anyString()))
                .thenReturn(CommonTestUtils.getAnnotation("123"));
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(
                false);
        assertThatThrownBy(() -> vPCNetworkOutboundRule.execute(getMapString("r_123 "), getMapString("r_123 ")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    public void getHelpTextTest() {
        assertThat(vPCNetworkOutboundRule.getHelpText(), is(notNullValue()));
    }
}

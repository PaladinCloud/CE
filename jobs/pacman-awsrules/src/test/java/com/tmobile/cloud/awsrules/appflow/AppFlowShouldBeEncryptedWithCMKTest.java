package com.tmobile.cloud.awsrules.appflow;

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
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class AppFlowShouldBeEncryptedWithCMKTest {

    @InjectMocks
    AppFlowShouldBeEncryptedWithCMK appFlowShouldBeEncryptedWithCMK;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(true);
        
        when(PacmanUtils.checkIfResourceEncryptedWithKmsCmks(anyString(), anyString(), anyString())).thenReturn(true);
        assertThat(appFlowShouldBeEncryptedWithCMK.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.checkIfResourceEncryptedWithKmsCmks(anyString(), anyString(), anyString())).thenReturn(false);
        assertThat(appFlowShouldBeEncryptedWithCMK.execute(getMapString("r_123 "),getMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.checkIfResourceEncryptedWithKmsCmks(anyString(), anyString(), anyString())).thenThrow(new Exception());
        assertThatThrownBy(() -> appFlowShouldBeEncryptedWithCMK.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(RuleExecutionFailedExeption.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> appFlowShouldBeEncryptedWithCMK.execute(getMapString("r_123 "),getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(appFlowShouldBeEncryptedWithCMK.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "Aws_appflow_encryption_using_KMS_CMKs_version-1_aws_KMS_CMKs_Encryption_appflow");
        commonMap.put("policyId", "Aws_appflow_encryption_using_KMS_CMKs_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("storageencrypted", "true");
        commonMap.put("kmsarn", "arn:aws:kms:us-east-2a:123456789012:key/8c5b2c63-b9bc-45a3-a87a-5513eEXAMPLE");
        return commonMap;
    }
  
    
}

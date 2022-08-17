package com.tmobile.cloud.awsrules.backup;

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
@PrepareForTest({ PacmanUtils.class})
public class VaultWithoutDeleteProtectionTest {

    @InjectMocks
    VaultWithoutDeleteProtection vaultWithoutDeleteProtection;
   
    @Test
    public void executeTest() throws Exception {
    	mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(true);
        
        assertThat(vaultWithoutDeleteProtection.execute(getValidMapString("r_123 "),getValidMapString("r_123 ")), is(notNullValue()));
        
        assertThat(vaultWithoutDeleteProtection.execute(getInvalidMapString("r_123 "),getInvalidMapString("r_123 ")), is(notNullValue()));
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> vaultWithoutDeleteProtection.execute(getValidMapString("r_123 "),getValidMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(vaultWithoutDeleteProtection.getHelpText(), is(notNullValue()));
    }
    
    public static Map<String, String> getValidMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "AwsVaultMissingAccessPolicy_version-1_AwsVaultMissingAccessPolicy_backupvault");
        commonMap.put("policyId", "AwsVaultMissingAccessPolicy_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("accessPolicy", "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Deny\",\"Principal\":{\"AWS\":\"*\"},\"Action\":[\"backup:DeleteBackupVault\",\"backup:DeleteBackupVaultAccessPolicy\",\"backup:DeleteRecoveryPoint\",\"backup:StartCopyJob\",\"backup:StartRestoreJob\",\"backup:UpdateRecoveryPointLifecycle\"],\"Resource\":\"*\"}]}");
        return commonMap;
    }
    public static Map<String, String> getInvalidMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("accountid", "12345");
        commonMap.put("ruleId", "AwsVaultMissingAccessPolicy_version-1_AwsVaultMissingAccessPolicy_backupvault");
        commonMap.put("policyId", "AwsVaultMissingAccessPolicy_version-1");
        commonMap.put("policyVersion", "version-1");
        commonMap.put("accessPolicy", "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Deny\",\"Principal\":{\"AWS\":\"*\"},\"Action\":\"backup:DeleteBackupVault\",\"Resource\":\"*\"}]}");
        return commonMap;
    }
  
    
}

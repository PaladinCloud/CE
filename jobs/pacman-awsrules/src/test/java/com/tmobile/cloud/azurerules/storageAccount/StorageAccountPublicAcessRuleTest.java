package com.tmobile.cloud.azurerules.storageAccount;

import com.amazonaws.services.identitymanagement.model.InvalidInputException;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.StorageAccount.StorageAccountCMKEncryptionRule;
import com.tmobile.cloud.azurerules.StorageAccount.StorageAccountPublicAcessRule;
import com.tmobile.pacman.commons.rule.BaseRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BaseRule.class })
public class StorageAccountPublicAcessRuleTest {

    @InjectMocks
    StorageAccountPublicAcessRule storageAccountPublicAcessRule;

    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(true);
        assertThat(storageAccountPublicAcessRule.execute(getMapNotExistString("r_123 "), getMapNotExistString("r_123 ")),
                is(notNullValue()));

        when(PacmanUtils.getPacmanHost(anyString())).thenReturn("host");

        when(PacmanUtils.checkISResourceIdExistsFromElasticSearch("_resourceid", "azure_storageaccount/_search", "", ""))
                .thenReturn(true);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString())).thenReturn(false);
        assertThatThrownBy(
                () -> storageAccountPublicAcessRule.execute(getMapString("r_123 "), getMapString("r_123 ")))
                .isInstanceOf(InvalidInputException.class);

    }

    public static Map<String, String> getMapString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("ruleId",
                "Azure_Check_Public_Access_For_Storage_Account");
        commonMap.put("policyId", "Azure_Check_Public_Access_For_Storage_Account");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    public static Map<String, String> getMapNotExistString(String passRuleResourceId) {
        Map<String, String> commonMap = new HashMap<>();
        commonMap.put("executionId", "1234");
        commonMap.put("_resourceid", passRuleResourceId);
        commonMap.put("severity", "high");
        commonMap.put("ruleCategory", "security");
        commonMap.put("type", "Task");
        commonMap.put("ruleId",
                "Azure_Check_Public_Access_For_Storage_Account");
        commonMap.put("policyId", "Azure_Check_Public_Access_For_Storage_Account");
        commonMap.put("policyVersion", "version-1");
        return commonMap;
    }

    @Test
    public void getHelpTextTest() {
        assertThat(storageAccountPublicAcessRule.getHelpText(), is(notNullValue()));
    }
}


package com.tmobile.cloud.awsrules.ebs;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class})
public class EbsShouldBeEncryptedWithKMSKeysTest {

    @InjectMocks
    EbsShouldBeEncryptedWithKMSKeys ebsShouldBeEncryptedWithKMSKeys;
 
    @Test
	public void executeTest() throws Exception {
		mockStatic(PacmanUtils.class);
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.RULE_ID, "ruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY, PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY, PacmanSdkConstants.SEV_HIGH);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.formatUrl(anyObject(), anyString())).thenReturn("host");
		when(PacmanUtils.checkISResourceIdExistsFromElasticSearch(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.checkIfEbsVolumeEncryptedWithCustomKMSKeys(anyString(), anyString(), anyString())).thenReturn(true);
		assertThat(ebsShouldBeEncryptedWithKMSKeys.execute(ruleParam, getEncryptedEBSSnaphost("ebs123 ")), is(notNullValue()));

		when(PacmanUtils.checkISResourceIdExistsFromElasticSearch(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.checkIfEbsVolumeEncryptedWithCustomKMSKeys(anyString(), anyString(), anyString())).thenReturn(false);
		assertThat(ebsShouldBeEncryptedWithKMSKeys.execute(ruleParam, getEncryptedEBSSnaphost("ebs123")), is(notNullValue()));

		when(PacmanUtils.checkISResourceIdExistsFromElasticSearch(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
		when(PacmanUtils.checkIfEbsVolumeEncryptedWithCustomKMSKeys(anyString(), anyString(), anyString())).thenThrow(new Exception());
		assertThatThrownBy(() -> ebsShouldBeEncryptedWithKMSKeys.execute(ruleParam, getEncryptedEBSSnaphost("ebs123"))).isInstanceOf(RuleExecutionFailedExeption.class);

		when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
		assertThatThrownBy(() -> ebsShouldBeEncryptedWithKMSKeys.execute(ruleParam, getEncryptedEBSSnaphost("ebs123"))).isInstanceOf(InvalidInputException.class);

	}

    @Test
    public void getHelpTextTest(){
        assertThat(ebsShouldBeEncryptedWithKMSKeys.getHelpText(), is(notNullValue()));
    }
    private Map<String, String> getEncryptedEBSSnaphost(String ebsID) {
		Map<String, String> ebsResourceObj = new HashMap<>();
		ebsResourceObj.put("_resourceid", ebsID);
		ebsResourceObj.put("encrypted", "true");
		ebsResourceObj.put("esKmsUrl", "esKmsUrl");
		ebsResourceObj.put("volumeid", "volume-1");
		return ebsResourceObj;
	}
    
   
}

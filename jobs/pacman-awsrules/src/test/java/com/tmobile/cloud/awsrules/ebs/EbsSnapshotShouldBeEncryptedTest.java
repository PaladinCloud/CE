
package com.tmobile.cloud.awsrules.ebs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashMap;
import java.util.Map;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.pacman.commons.policy.Annotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, Annotation.class})
public class EbsSnapshotShouldBeEncryptedTest {

    @InjectMocks
    EbsSnapshotShouldBeEncrypted ebsSnapshotShouldBeEncrypted;
 
    @Test
    public void executeTest() throws Exception {
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(), anyObject())).thenReturn(CommonTestUtils.getMockAnnotation());
        mockStatic(PacmanUtils.class);
        Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "ruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY,PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY,PacmanSdkConstants.SEV_HIGH);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(
                true);
        when(PacmanUtils.formatUrl(anyObject(),anyString())).thenReturn("host");
        when(PacmanUtils.checkISResourceIdExistsFromElasticSearch(anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        assertThat(ebsSnapshotShouldBeEncrypted.execute(ruleParam,getEBSSnaphostWithoutEncryption("ebs123 ")), is(notNullValue()));
        
        when(PacmanUtils.checkISResourceIdExistsFromElasticSearch(anyString(),anyString(),anyString(),anyString())).thenReturn(true);
        assertThat(ebsSnapshotShouldBeEncrypted.execute(ruleParam, getEncryptedEBSSnaphost("ebs123")), is(notNullValue()));
             
          }
    
    private Map<String, String> getEncryptedEBSSnaphost(String ebsID) {
		Map<String, String> ebsResourceObj = new HashMap<>();
		ebsResourceObj.put("_resourceid", ebsID);
		ebsResourceObj.put("encrypted", "true");
		return ebsResourceObj;
	}
    
    private Map<String, String> getEBSSnaphostWithoutEncryption(String ebsID) {
		Map<String, String> ebsResourceObj = new HashMap<>();
		ebsResourceObj.put("_resourceid", ebsID);
		ebsResourceObj.put("encrypted", "false");
		return ebsResourceObj;
	}
    
    @Test
    public void getHelpTextTest(){
        assertThat(ebsSnapshotShouldBeEncrypted.getHelpText(), is(notNullValue()));
    }
}

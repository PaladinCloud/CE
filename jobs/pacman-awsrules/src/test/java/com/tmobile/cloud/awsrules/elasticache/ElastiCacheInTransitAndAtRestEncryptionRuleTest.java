package com.tmobile.cloud.awsrules.elasticache;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.pacman.commons.policy.Annotation;
import com.tmobile.pacman.commons.utils.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.PolicyResult;

@PowerMockIgnore("jdk.internal.reflect.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class,Annotation.class })
public class ElastiCacheInTransitAndAtRestEncryptionRuleTest {

	@InjectMocks
	ElastiCacheInTransitAndAtRestEncryptionRule elastiCacheInTransitAndAtRestEncryptionRule;
	
	
	@Test
	public void elasticCacheWithEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "cloudsqlbackupruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY,PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY,PacmanSdkConstants.SEV_HIGH);
		ruleParam.put("engineType","redis");
		ruleParam.put("engineVersion","3.2.6");
		Map<String, String> resourceAttribute =  getResourceForEncyptedCache("ESCACHE1234");
		PolicyResult ruleResult = elastiCacheInTransitAndAtRestEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_SUCCESS, ruleResult.getStatus());
	}
	
	@Test
	public void elasticCacheWithOutEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "cloudsqlbackupruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY,PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY,PacmanSdkConstants.SEV_HIGH);
		ruleParam.put("engineType","redis");
		ruleParam.put("engineVersion","3.2.6");
//		System.setProperty(Constants.RDS_USER,"paladin");
//		System.setProperty(Constants.RDS_PWD,"***REMOVED***");
//		System.setProperty(Constants.RDS_DB_URL,"jdbc:mysql://paladincloud-data.ca1f8qf1livk.us-east-1.rds.amazonaws.com:3306/pacmandata?autoReconnect=true&useSSL=false");
		Map<String, String> resourceAttribute =  getResourceForWithOutEncryption("ESCACHE1234");
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		PolicyResult ruleResult = elastiCacheInTransitAndAtRestEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void elasticCacheWithOutTransitEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "cloudsqlbackupruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY,PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY,PacmanSdkConstants.SEV_HIGH);
		ruleParam.put("engineType","redis");
		ruleParam.put("engineVersion","3.2.6");
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		Map<String, String> resourceAttribute =  getResourceForWithOutTransitEncryption("ESCACHE1234");
		PolicyResult ruleResult = elastiCacheInTransitAndAtRestEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void elasticCacheWithOutAtRestEncryption() {
		Map<String, String> ruleParam = new HashMap<>();
		ruleParam.put(PacmanSdkConstants.EXECUTION_ID, "exectionid");
		ruleParam.put(PacmanSdkConstants.POLICY_ID, "cloudsqlbackupruleid");
		ruleParam.put(PacmanRuleConstants.CATEGORY,PacmanSdkConstants.SECURITY);
		ruleParam.put(PacmanRuleConstants.SEVERITY,PacmanSdkConstants.SEV_HIGH);
		ruleParam.put("engineType","redis");
		ruleParam.put("engineVersion","3.2.6");
		mockStatic(Annotation.class);
		when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());
		Map<String, String> resourceAttribute =  getResourceForWithOutAtRestEncryption("ESCACHE1234");
		PolicyResult ruleResult = elastiCacheInTransitAndAtRestEncryptionRule.execute(ruleParam, resourceAttribute);
		assertEquals(PacmanSdkConstants.STATUS_FAILURE, ruleResult.getStatus());
	}
	
	@Test
	public void getHelpTextTest() {
		assertThat(elastiCacheInTransitAndAtRestEncryptionRule.getHelpText(), is(notNullValue()));
	}

	
	private Map<String, String> getResourceForEncyptedCache(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "elasticache-cluster");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("engine","redis");
		clusterObj.put("engineversion","3.2.6");
		clusterObj.put("atrestencryptionenabled","true");
		clusterObj.put("transitencryptionenabled","true");
		return clusterObj;
	}
	
	
	private Map<String, String> getResourceForWithOutEncryption(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "elasticache-cluster");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("engine","redis");
		clusterObj.put("engineversion","3.2.6");
		clusterObj.put("atrestencryptionenabled","false");
		clusterObj.put("transitencryptionenabled","false");
		return clusterObj;
	}
	
	private Map<String, String> getResourceForWithOutTransitEncryption(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "elasticache-cluster");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("engine","redis");
		clusterObj.put("engineversion","3.2.6");
		clusterObj.put("atrestencryptionenabled","true");
		clusterObj.put("transitencryptionenabled","false");
		return clusterObj;
	}
	
	private Map<String, String> getResourceForWithOutAtRestEncryption(String clusterID) {
		Map<String, String> clusterObj = new HashMap<>();
		clusterObj.put("_resourceid", clusterID);
		clusterObj.put("name", "elasticache-cluster");
		clusterObj.put("creationTimestamp", "2022-01-10T13:00:38.628-08:00");
		clusterObj.put("engine","redis");
		clusterObj.put("engineversion","3.2.6");
		clusterObj.put("atrestencryptionenabled","false");
		clusterObj.put("transitencryptionenabled","true");
		return clusterObj;
	}

	private Annotation getMockAnnotation() {
		Annotation annotation=new Annotation();
		annotation.put(PacmanSdkConstants.POLICY_NAME,"Mock policy name");
		annotation.put(PacmanSdkConstants.POLICY_ID, "Mock policy id");
		annotation.put(PacmanSdkConstants.POLICY_VERSION, "Mock policy version");
		annotation.put(PacmanSdkConstants.RESOURCE_ID, "Mock resource id");
		annotation.put(PacmanSdkConstants.TYPE, "Mock type");
		return annotation;
	}

}

package com.tmobile.cloud.awsrules.s3;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.rule.BaseRule;
@PowerMockIgnore({"javax.net.ssl.*","javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class,BaseRule.class})
public class CheckMFADeleteEnabledRuleTest {

    @InjectMocks
    CheckMFADeleteEnabledRule checkMFADeleteEnabledRule;
    
    
    @Mock
    AmazonS3Client awsS3Client;

    @Before
    public void setUp() throws Exception{
        awsS3Client = PowerMockito.mock(AmazonS3Client.class); 
    }
    @Test
    public void test()throws Exception{
        Collection<String> li = new ArrayList<>();
        li.add("123");
        BucketVersioningConfiguration configuration = new BucketVersioningConfiguration();
        configuration.setMfaDeleteEnabled(false);
        
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                true);
        
        Map<String,Object>map=new HashMap<String, Object>();
        map.put("client", awsS3Client);
        CheckMFADeleteEnabledRule spy = Mockito.spy(new CheckMFADeleteEnabledRule());
        
        Mockito.doReturn(map).when((BaseRule)spy).getClientFor(anyObject(), anyString(), anyObject());
        
        when(awsS3Client.getBucketVersioningConfiguration(anyString())).thenReturn(configuration);
        spy.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 "));
        
        BucketVersioningConfiguration configurationEnabled = new BucketVersioningConfiguration();
        configurationEnabled.setMfaDeleteEnabled(true);
        when(awsS3Client.getBucketVersioningConfiguration(anyString())).thenReturn(configurationEnabled);
        spy.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 "));
        
        when(awsS3Client.getBucketVersioningConfiguration(anyString())).thenThrow(new RuleExecutionFailedExeption());
        assertThatThrownBy( 
                () -> checkMFADeleteEnabledRule.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString())).thenReturn(
                false);
        assertThatThrownBy(
                () -> checkMFADeleteEnabledRule.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
    }
  
    
    @Test
    public void getHelpTextTest(){
        assertThat(checkMFADeleteEnabledRule.getHelpText(), is(notNullValue()));
    }

}

/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cloud.awsrules.ec2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.tmobile.pacman.commons.PacmanSdkConstants;
import com.tmobile.pacman.commons.policy.Annotation;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.pacman.commons.exception.InvalidInputException;
@PowerMockIgnore("javax.net.ssl.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class,HttpClientBuilder.class, Annotation.class})
public class ElasticSearchInternalAccessRuleTest {

    @InjectMocks
    ElasticSearchInternalAccessRule elasticSearchInternalAccessRule;
    
    HttpClientBuilder httpClientBuilder;
    
    
    CloseableHttpClient closeableHttpClient;
  
    
    CloseableHttpResponse httpResponse; 
   @Before
   public void setUp() throws Exception{
       mockStatic(HttpClientBuilder.class);
       mockStatic(HttpClient.class);
       mockStatic(CloseableHttpClient.class);
       mockStatic(HttpResponse.class);
       mockStatic(CloseableHttpResponse.class);
       
       closeableHttpClient = PowerMockito.mock(CloseableHttpClient.class);
       HttpClientBuilder httpClientBuilder = PowerMockito.mock(HttpClientBuilder.class);
       PowerMockito.when(HttpClientBuilder.create()).thenReturn(httpClientBuilder);
       PowerMockito.when(HttpClientBuilder.create().setConnectionTimeToLive(anyLong(), anyObject())).thenReturn(httpClientBuilder);
       PowerMockito.when(HttpClientBuilder.create().setConnectionTimeToLive(anyLong(), anyObject()).build()).thenReturn(closeableHttpClient);
       HttpGet httpGet = PowerMockito.mock(HttpGet.class); 
       PowerMockito.whenNew(HttpGet.class).withAnyArguments().thenReturn(httpGet);
       httpResponse = PowerMockito.mock(CloseableHttpResponse.class);
       HttpEntity entity = PowerMockito.mock(HttpEntity.class);
       InputStream input = new ByteArrayInputStream("lucene_version".getBytes() );
       PowerMockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));
       PowerMockito.when(entity.getContent()).thenReturn(input);
       PowerMockito.when(httpResponse.getEntity()).thenReturn(entity);
   }
    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(
                true);
        mockStatic(Annotation.class);
        when(Annotation.buildAnnotation(anyObject(),anyObject())).thenReturn(getMockAnnotation());

        PowerMockito.when(closeableHttpClient.execute((HttpGet) any())).thenReturn(httpResponse);
        assertThat(elasticSearchInternalAccessRule.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));
        
        assertThatThrownBy(
                () -> elasticSearchInternalAccessRule.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getEmptyMapString())).isInstanceOf(InvalidInputException.class);
        
        when(PacmanUtils.doesAllHaveValue(anyString(),anyString(),anyString())).thenReturn(
                false);
        assertThatThrownBy(
                () -> elasticSearchInternalAccessRule.execute(CommonTestUtils.getMapString("r_123 "),CommonTestUtils.getMapString("r_123 "))).isInstanceOf(InvalidInputException.class);
        
        
        
        
    }
    
    @Test
    public void getHelpTextTest(){
        assertThat(elasticSearchInternalAccessRule.getHelpText(), is(notNullValue()));
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

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
package com.tmobile.pacman.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.util.CommonHttpUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonHttpUtils.class})
public class ConfigManagerTest {

    @InjectMocks
    ConfigManager configManager;

    @Test
    public void getConfigurationsMapTest() throws Exception {

        String str = "{\"name\":\"123\",\"profiles\":[\"123\"],\"label\":\"123\",\"version\":null,\"state\":null,\"propertySources\":[{\"name\":\"rule-stg\",\"source\":{\"test\":\"test\"}},{\"name\":\"application-stg\",\"source\":{\"test\":\"test\"}}]}";
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject) jsonParser.parse(str);
        mockStatic(CommonHttpUtils.class);
        when(CommonHttpUtils.getEnvironmentVariable(anyString())).thenReturn("123");
        when(CommonHttpUtils.getHeader(anyString())).thenReturn(new HashMap<String, Object>());
        when(CommonHttpUtils.getConfigurationsFromConfigApi(anyString(), anyObject())).thenReturn(jo);
        assertThat(configManager.getConfigurationsMap(), is(notNullValue()));

        when(CommonHttpUtils.getConfigurationsFromConfigApi(anyString(), anyObject())).thenReturn(jo);
        assertThat(configManager.getConfigurationsMap(), is(notNullValue()));

        when(CommonHttpUtils.getEnvironmentVariable(anyString())).thenReturn(null);
        assertThatThrownBy(
                () -> configManager.getConfigurationsMap()).isInstanceOf(InvalidInputException.class);

    }

}

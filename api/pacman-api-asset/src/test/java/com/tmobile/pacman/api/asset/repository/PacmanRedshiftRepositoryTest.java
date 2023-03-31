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
package com.tmobile.pacman.api.asset.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DriverManager.class, PacmanRedshiftRepository.class})
public class PacmanRedshiftRepositoryTest {

    @Mock
    Connection connection;

    @Mock
    Statement statement;

    @Mock
    ResultSet rs;

    PacmanRedshiftRepository repository = new PacmanRedshiftRepository();

    @Test
    public void testCount() throws Exception {
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt(anyString())).thenReturn(5);
        doNothing().when(rs).close();
        doNothing().when(connection).close();
        doNothing().when(statement).close();

        mockStatic(DriverManager.class);
        when(DriverManager.getConnection(anyString(), any(Properties.class))).thenReturn(connection);
        ReflectionTestUtils.setField(repository, "userName", "");
        ReflectionTestUtils.setField(repository, "password", "");
        ReflectionTestUtils.setField(repository, "dbURL", "");

        int a = repository.count("query");

        assertEquals(5, a);
    }
}

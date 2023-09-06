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
/**
  Copyright (C) 2017 T Mobile Inc - All Rights Reserve
  Purpose:
  Author :kkumar
  Modified Date: Oct 27, 2017

 **/
/*
 *Copyright 2016-2017 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 *Licensed under the Amazon Software License (the "License"). You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tmobile.pacman.api.commons.repo;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author kkumar
 *
 */
@Repository
public class PacmanRdsRepository
{

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final Logger logger = LoggerFactory.getLogger(PacmanRdsRepository.class);

	private static final String ERROR_QUERYING_DATABASE = "Error in querying database - ";

	/**
	 *
	 * @return
	 */
	public List<Map<String, Object>> getDataFromPacman(String query)
	{
		return jdbcTemplate.queryForList(query);
	}

	public List<String> getStringList(String query)
	{
		return jdbcTemplate.queryForList(query, String.class);
	}

	public int update(String query, Object... queryParams)
	{
		return jdbcTemplate.update(query, queryParams);
	}

	public int count(String query)
	{
		return jdbcTemplate.queryForObject(query, Integer.class);
	}

	public String queryForString(String query)
	{
		return jdbcTemplate.queryForObject(query, String.class);
	}
	public int[] executeQuery(String query)
	{

		return jdbcTemplate.batchUpdate(query);
	}
	public boolean isExists(String query, String param) {
		try {
			return jdbcTemplate.queryForObject(query, new Object[]{param}, Integer.class) > 0;
		} catch (Exception e) {
			logger.error(ERROR_QUERYING_DATABASE, e);
			return false;
		}
	}
}

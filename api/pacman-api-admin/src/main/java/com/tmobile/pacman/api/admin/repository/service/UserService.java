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
package com.tmobile.pacman.api.admin.repository.service;

import java.util.List;
import java.util.Map;

import com.tmobile.pacman.api.admin.domain.CognitoUserDetails;
import com.tmobile.pacman.api.admin.domain.CreateCognitoUserDetails;
import com.tmobile.pacman.api.admin.domain.UserDetails;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.model.CognitoUser;
import com.tmobile.pacman.api.admin.repository.model.User;

/**
 * User Service Functionalities
 */
public interface UserService {

	/**
	 * Service to get user details by email id
	 *
	 * @author Nidhish
	 * @param emailId - valid email Id
	 * @return User details
	 * @throws PacManException
	 */
	UserDetails getUserByEmailId(final String emailId) throws PacManException;

	/**
	 * Service to get all sign-in users
	 *
	 * @author Nidhish
	 * @return Users List
	 * @throws PacManException
	 */
	List<User> getAllLoginUsers() throws PacManException;

	List<CognitoUser> getAllUsers(Integer cursor, Integer limit, String filter) throws  PacManException;

	Map<String, Object> updateRoleMembership(String username, CognitoUserDetails details) throws  PacManException;

	Object editStatusToUser(String username) throws  PacManException;


	Map<String, Object> createUser(CreateCognitoUserDetails details) throws  PacManException;

	Map<String, Object> deleteUser(String username) throws  PacManException;

}

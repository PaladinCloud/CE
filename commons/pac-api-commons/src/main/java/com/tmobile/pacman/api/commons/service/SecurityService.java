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
package com.tmobile.pacman.api.commons.service;

import com.tmobile.pacman.api.commons.config.RoleMappingLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Security Service Functionalities
 */
@Service
public class SecurityService {

	@Autowired
	private RoleMappingLoader roleMappingLoader;

	/**
	 * Service to check user permission
	 *
	 * @param authentication - valid user authentication details
	 * @return Boolean value
	 * @author Nidhish
	 */
	public boolean hasPermission(Authentication authentication, String... permissions) {
		final Set<String> userRoles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
		Map<String, List<String>> rolePermissionMappings = roleMappingLoader.getRoleList();
		List<String> allowedPermissions = new ArrayList<>();
		userRoles.stream().forEach(role -> {
			List<String> permissionList = rolePermissionMappings.get(role);
			if (permissionList != null) {
				allowedPermissions.addAll(permissionList);
			}
		});
		//check if any one of the required permission(input- permissisons) is in allowedPermission
		return Arrays.asList(permissions).stream().map(String::toLowerCase)
				.anyMatch( allowedPermissions.stream().map(String::toLowerCase)
								.collect(Collectors.toSet())::contains);
	}
}

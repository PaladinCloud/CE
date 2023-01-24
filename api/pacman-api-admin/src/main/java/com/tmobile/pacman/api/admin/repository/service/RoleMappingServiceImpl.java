package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.domain.Permission;
import com.tmobile.pacman.api.admin.domain.RoleMapping;
import com.tmobile.pacman.api.admin.domain.RoleMappingResponse;
import com.tmobile.pacman.api.commons.exception.DataException;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoleMappingServiceImpl implements RoleMappingService {

	private static final Logger log = LoggerFactory.getLogger(RoleMappingServiceImpl.class);
	@Autowired
	private PacmanRdsRepository rdsRepository;

	public static final String ROLE_NAME = "role_name";
	public static final String PERMISSION_NAME = "permission_name";
	public static final String IS_ADMIN = "is_admin";

	private static final String ROLE_MAPPING_QUERY="select r.role_name,p.permission_name, p.is_admin from role r,  "+
			"role_permissions rp,permission p\n" +
			"where r.role_id= rp.role_id\n" +
			"and rp.permission_id=p.permission_id\n" +
			"and r.role_name in ( %s )";

	@Override
	public RoleMappingResponse getRoleCapabilityMapping(String roles) throws Exception {
		if(roles.contains(",")){
			roles=Arrays.stream(roles.split(",")).map(role -> "'" + role.trim() + "'")
					.collect(Collectors.joining(","));
		}else{
			roles="'"+roles+"'";
		}
		log.info("Fetching role permission mapping from DB for roles: {}",roles);
		log.info("Role permission mapping query: {}",ROLE_MAPPING_QUERY);
		List<Map<String, Object>> queryResponse = rdsRepository.getDataFromPacman(String.format(ROLE_MAPPING_QUERY, roles));
		if (queryResponse.isEmpty()) {
			throw new DataException("Data not found for the roles: "+roles);
		}
		Map<String, List<Permission>> rolePermissionMappings=new HashMap<>();
		queryResponse.stream().forEach(map->{
			String roleName=(String) map.get(ROLE_NAME);
			String permissionName=(String) map.get(PERMISSION_NAME);
			boolean isAdminCapability=(boolean) map.get(IS_ADMIN);
			Permission permission=new Permission();
			permission.setPermissionName(permissionName);
			permission.setAdminCapability(isAdminCapability);
			if(rolePermissionMappings.containsKey(roleName)){
				rolePermissionMappings.get(roleName).add(permission);
			}else{
				rolePermissionMappings.put(roleName, Stream.of(permission).collect(Collectors.toList()));
			}
		});
		RoleMappingResponse response=new RoleMappingResponse();
		response.setStatus("SUCCESS");
		List<RoleMapping> roleMappings=new ArrayList<>();
		rolePermissionMappings.entrySet().forEach(key->{
			RoleMapping mapping=new RoleMapping();
			mapping.setRoleName(key.getKey());
			mapping.setPermissions(key.getValue());
			roleMappings.add(mapping);
		});
		response.setRoleMappings(roleMappings);
		return response;
	}
}

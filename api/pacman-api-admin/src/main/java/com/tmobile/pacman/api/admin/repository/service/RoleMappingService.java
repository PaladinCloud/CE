package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.domain.RoleMappingResponse;

public interface RoleMappingService {
	public RoleMappingResponse getRoleCapabilityMapping(String roles) throws Exception;
}

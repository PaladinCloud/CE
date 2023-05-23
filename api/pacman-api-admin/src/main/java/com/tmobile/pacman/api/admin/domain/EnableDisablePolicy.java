package com.tmobile.pacman.api.admin.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EnableDisablePolicy {

	private String policyId;
	private String action;
	private String expireDate;
	private String description;
	
}

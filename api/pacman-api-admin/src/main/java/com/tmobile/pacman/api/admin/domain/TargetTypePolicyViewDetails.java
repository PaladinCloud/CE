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
package com.tmobile.pacman.api.admin.domain;

/**
 * TargetTypeRuleViewDetails Domain Class
 */
public class TargetTypePolicyViewDetails extends TargetTypePolicyBase {

	private Object policies;
	
	private boolean isAdded;
	

	public Object getPolicies() {
		return policies;
	}

	public void setPolicies(Object policies) {
		this.policies = policies;
	}

	public boolean isAdded() {
		return isAdded;
	}

	public void setAdded(boolean isAdded) {
		this.isAdded = isAdded;
	}
}

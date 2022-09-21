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
package com.tmobile.pacman.api.admin.config;

import com.tmobile.pacman.api.admin.domain.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class PacmanConfiguration {

	private RuleProperty rule;

	private JobProperty job;
	
	private AWSProperty aws;

	private AzureProperty azure;

	private GcpProperty gcp;

	private AdminProperty admin;

	private SecurityProperty security;

	private ElasticSearchProperty elasticSearch;

	private TargetTypesProperty targetTypes;

	public RuleProperty getRule() {
		return rule;
	}

	public JobProperty getJob() {
		return job;
	}

	public void setRule(RuleProperty rule) {
		this.rule = rule;
	}

	public void setJob(JobProperty job) {
		this.job = job;
	}

	public AdminProperty getAdmin() {
		return admin;
	}

	public void setAdmin(AdminProperty admin) {
		this.admin = admin;
	}

	public SecurityProperty getSecurity() {
		return security;
	}

	public void setSecurity(SecurityProperty security) {
		this.security = security;
	}

	public ElasticSearchProperty getElasticSearch() {
		return elasticSearch;
	}

	public void setElasticSearch(ElasticSearchProperty elasticSearch) {
		this.elasticSearch = elasticSearch;
	}

	public TargetTypesProperty getTargetTypes() {
		return targetTypes;
	}

	public void setTargetTypes(TargetTypesProperty targetTypes) {
		this.targetTypes = targetTypes;
	}

	public AWSProperty getAws() {
		return aws;
	}

	public void setAws(AWSProperty aws) {
		this.aws = aws;
	}

	public AzureProperty getAzure() {
		return azure;
	}

	public void setAzure(AzureProperty azure) {
		this.azure = azure;
	}

	public GcpProperty getGcp() {
		return gcp;
	}

	public void setGcp(GcpProperty gcp) {
		this.gcp = gcp;
	}
}

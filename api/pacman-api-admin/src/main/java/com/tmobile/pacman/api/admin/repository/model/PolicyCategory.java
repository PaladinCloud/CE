package com.tmobile.pacman.api.admin.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The Class PolicyCategory.
 */
@Entity
@Table(name = "cf_PolicyCategoryWeightage", uniqueConstraints = @UniqueConstraint(columnNames = "policyCategory"))
public class PolicyCategory {

	@Id
	@Column(name = "policyCategory", unique = true, nullable = false)
	private String policyCategory;
	private String domain;
	private String weightage;
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getPolicyCategory() {
		return policyCategory;
	}
	public void setPolicyCategory(String policyCategory) {
		this.policyCategory = policyCategory;
	}
	public String getWeightage() {
		return weightage;
	}
	public void setWeightage(String weightage) {
		this.weightage = weightage;
	}
}

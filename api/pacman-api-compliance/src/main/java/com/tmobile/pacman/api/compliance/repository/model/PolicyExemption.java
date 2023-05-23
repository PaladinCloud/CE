package com.tmobile.pacman.api.compliance.repository.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cf_PolicyExemption", uniqueConstraints = @UniqueConstraint(columnNames = "id"))
public class PolicyExemption {

	
	 @Id
	 @Column(name = "id", unique = true, nullable = false)
	 private String id;
	 
	 private String policyID;
	 
	 private String exemptionDesc;
	 
	 private Date expireDate;
	 
	 private String filter;
	 
	 private String createdBy;
	 
	 private Date ceatedOn;
	 
	 private String modifiedBy;
	 
	 private Date modifiedOn;
	 
	 private String status;
	 
	 
	 
}


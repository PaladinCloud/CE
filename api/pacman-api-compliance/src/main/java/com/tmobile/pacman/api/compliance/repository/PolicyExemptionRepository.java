package com.tmobile.pacman.api.compliance.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.compliance.repository.model.PolicyExemption;



@Repository
public interface PolicyExemptionRepository extends JpaRepository<PolicyExemption, String>{
	
	@Query("SELECT pe FROM PolicyExemption pe WHERE pe.policyID = :policyID AND pe.expireDate >= :expireDate AND pe.status = 'open'")
	public List<PolicyExemption> findByPolicyIDAndExpireDate(@Param("policyID") String policyID, @Param("expireDate")  Date expireDate);

}

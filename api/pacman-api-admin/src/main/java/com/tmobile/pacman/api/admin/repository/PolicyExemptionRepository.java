package com.tmobile.pacman.api.admin.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.admin.repository.model.PolicyExemption;

@Repository
public interface PolicyExemptionRepository extends JpaRepository<PolicyExemption, String>{
	
	@Query("SELECT pe FROM PolicyExemption pe WHERE pe.policyID = :policyID AND pe.expireDate >= :expireDate AND pe.status = 'open'")
	public List<PolicyExemption> findByPolicyIDAndExpireDate(@Param("policyID") String policyID, @Param("expireDate")  Date expireDate);
	
	@Query("SELECT pe FROM PolicyExemption pe WHERE pe.policyID = :policyID AND pe.status = 'open' AND pe.expireDate < :expireDate" )
	public List<PolicyExemption> findByPolicyIDAndStatusOpen(@Param("policyID") String policyID, @Param("expireDate")  Date expireDate);
	
	@Query("SELECT pe FROM PolicyExemption pe WHERE pe.policyID = :policyID order by pe.ceatedOn desc" )
	public List<PolicyExemption> findByPolicyID(@Param("policyID") String policyID);

}

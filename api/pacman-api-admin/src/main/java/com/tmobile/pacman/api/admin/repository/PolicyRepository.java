package com.tmobile.pacman.api.admin.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.admin.domain.PolicyProjection;
import com.tmobile.pacman.api.admin.repository.model.Policy;


/**
 * Policy Repository Interface
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {

	public Policy findByPolicyId(final String policyId); 
	
	public List<Policy> findByTargetTypeIgnoreCase(final String targetType); 
	
	@Query("SELECT p.policyId AS id, p.policyType AS type, p.status AS status, p.policyName AS text FROM Policy p WHERE LOWER(p.targetType) LIKE %:targetType% GROUP BY p.policyId")
	public List<PolicyProjection> findByTargetType(@Param("targetType") final String targetType); 
	
	@Query("SELECT p.policyId AS id, p.policyType AS type, p.status AS status, p.policyName AS text FROM Policy p WHERE LOWER(p.targetType) LIKE %:targetType% AND p.policyId NOT IN (:policyIdList) GROUP BY p.policyId")
	public List<PolicyProjection> findByTargetTypeAndPolicyIdNotIn(@Param("targetType") String targetType, @Param("policyIdList") List<String> policyIdList); 
	
	@Query("SELECT p.policyId AS id, p.policyType AS type, p.status AS status, p.policyName AS text FROM Policy p WHERE LOWER(p.targetType) LIKE %:targetType% AND p.policyId IN (:policyIdList) GROUP BY p.policyId")
	public List<PolicyProjection> findByTargetTypeAndPolicyIdIn(@Param("targetType") String targetType, @Param("policyIdList")  List<String> policyIdList);

	@Query(value = "SELECT p FROM Policy p WHERE "
			+ "LOWER(p.policyId) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyUUID) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyName) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyDisplayName) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyDesc) LIKE %:searchTerm% OR "
			+ "LOWER(p.resolution) LIKE %:searchTerm% OR "
			+ "LOWER(p.resolutionUrl) LIKE %:searchTerm% OR "
			+ "LOWER(p.targetType) LIKE %:searchTerm% OR "
			+ "LOWER(p.assetGroup) LIKE %:searchTerm% OR "
			+ "LOWER(p.alexaKeyword) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyParams) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyFrequency) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyExecutable) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyRestUrl) LIKE %:searchTerm% OR "
			+ "LOWER(p.policyArn) LIKE %:searchTerm% OR "
			+ "LOWER(p.status) LIKE %:searchTerm% OR "
			+ "LOWER(p.userId) LIKE %:searchTerm% OR "
			+ "LOWER(p.createdDate) LIKE %:searchTerm% OR "
			+ "LOWER(p.severity) LIKE %:searchTerm% OR "
			+ "LOWER(p.category) LIKE %:searchTerm% OR "
			+ "LOWER(p.modifiedDate) LIKE %:searchTerm% GROUP BY p.policyId", 
			
			countQuery = "SELECT COUNT(*) FROM Policy p WHERE "
					+ "LOWER(p.policyId) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyUUID) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyName) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyDisplayName) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyDesc) LIKE %:searchTerm% OR "
					+ "LOWER(p.resolution) LIKE %:searchTerm% OR "
					+ "LOWER(p.resolutionUrl) LIKE %:searchTerm% OR "
					+ "LOWER(p.targetType) LIKE %:searchTerm% OR "
					+ "LOWER(p.assetGroup) LIKE %:searchTerm% OR "
					+ "LOWER(p.alexaKeyword) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyParams) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyFrequency) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyExecutable) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyRestUrl) LIKE %:searchTerm% OR "
					+ "LOWER(p.policyArn) LIKE %:searchTerm% OR "
					+ "LOWER(p.status) LIKE %:searchTerm% OR "
					+ "LOWER(p.userId) LIKE %:searchTerm% OR "
					+ "LOWER(p.createdDate) LIKE %:searchTerm% OR "
					+ "LOWER(p.severity) LIKE %:searchTerm% OR "
					+ "LOWER(p.category) LIKE %:searchTerm% OR "
					+ "LOWER(p.modifiedDate) LIKE %:searchTerm% GROUP BY p.policyId")
	public Page<Policy> findAll(@Param("searchTerm") String searchTerm, Pageable pageable);
	
	@Query("SELECT alexaKeyword FROM Policy WHERE alexaKeyword != '' AND alexaKeyword != null GROUP BY alexaKeyword")
	public Collection<String> getAllAlexaKeywords();

	@Query("SELECT policyId FROM Policy WHERE policyId != '' AND policyId != null GROUP BY policyId")
	public Collection<String> getAllPolicyIds();
}

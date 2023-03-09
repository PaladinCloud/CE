package com.tmobile.pacman.api.admin.repository.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.tmobile.pacman.api.admin.domain.CreateUpdatePolicyDetails;
import com.tmobile.pacman.api.admin.domain.PolicyProjection;
import com.tmobile.pacman.api.admin.exceptions.PacManException;
import com.tmobile.pacman.api.admin.repository.model.Policy;
import com.tmobile.pacman.api.admin.repository.model.PolicyCategory;

/**
 * Policy Service Functionalities
 */
public interface PolicyService {

	/**
     * Service to get Policy by Policy Id
     *
     * @author 
     * @param policyId - valid policy Id
     * @return The Policy details
     */
	public Policy getByPolicyId(String policyId);

	/**
     * Service to get all alexaKeywords
     *
     * @author 
     * @return List of alexaKeywords
     */
	public Collection<String> getAllAlexaKeywords();

	/**
     * Service to get all Policies
     *
     * @author 
     * @param searchTerm - searchTerm to be searched.
     * @param page - zero-based page index.
     * @param size - the size of the page to be returned.
     * @return All Policies details
     */
	public Page<Policy> getPolicies(final String searchTerm, final int page, final int size);

	/**
     * Service to create new Policy
     *
     * @author 
     * @param fileToUpload - valid executable Policy jar file
     * @param policyDetails - details for creating new policy
     * @param userId - userId who performs the action
     * @return Success or Failure response
     * @throws PacManException
     */
	public String createPolicy(final MultipartFile fileToUpload, final CreateUpdatePolicyDetails policyDetails, final String userId) throws PacManException;

	/**
     * Service to update existing Policy
     *
     * @author 
     * @param fileToUpload - valid executable Policy jar file
     * @param updatePolicyDetails - details for creating new Policy
     * @param userId - userId who performs the action
     * @return Success or Failure response
     * @throws PacManException
     */
	public String updatePolicy(final MultipartFile fileToUpload, final CreateUpdatePolicyDetails updatePolicyDetails, final String userId) throws PacManException;

	/**
     * Service to invoke a Policy
     *
     * @author 
     * @param policyId - valid policy Id
     * @param policyOptionalParams - valid policy optional parameters which need to be passed while invoking policy
     * @return Success or Failure response
     */
	public String invokePolicy(final String policyId, final List<Map<String, Object>> policyOptionalParams);

	/**
     * Service to enable disable policy
     *
     * @author 
     * @param policyId - valid policy Id
     * @param action - valid action (disable/ enable)
     * @param userId - userId who performs the action
     * @return Success or Failure response
     * @throws PacManException
     */
	public String enableDisablePolicy(final String policyId, final String action, final String userId) throws PacManException;

	/**
     * Service to get all policy by targetType
     *
     * @author 
     * @param targetType - valid targetType
     * @return List of all Policies
     */
	public List<Policy> getAllPoliciesByTargetType(final String targetType);

	/**
     * Service to get all policies by targetType and not in policyId list
     *
     * @author 
     * @param targetType - valid targetType
     * @param policyIdList - valid policy Id list
     * @return List of policies details
     */
	public List<PolicyProjection> getAllPoliciesByTargetTypeAndNotInPolicyIdList(final String targetType, final List<String> policyIdList);

	/**
     * Service to get all policies by targetType and policyId list
     *
     * @author 
     * @param targetType - valid targetType
     * @param policyIdList - valid policy Id list
     * @return List of policies details
     */
	public List<PolicyProjection> getAllPoliciesByTargetTypeAndPolicyIdList(final String targetType, final List<String> policyIdList);

	/**
     * Service to get all policies by targetType name
     *
     * @author 
     * @param targetType - valid targetType
     * @return List of policies details
     */
	public List<PolicyProjection> getAllPoliciesByTargetTypeName(String targetType);

	/**
     * Service to invoke all Policies
     *
     * @author 
     * @param policyIds - valid policy id list
     * @return Success and failure policy id details
     */
	public Map<String, Object> invokeAllPolicies(List<String> policyIds);

	/**
     * Service to get all Policy Id's
     *
     * @author 
     * @return List of Policy Id's
     */
	public Collection<String> getAllPolicyIds();
	
	/**
	 * Gets the all policy categories.
	 *
	 * @return the all policy categories
	 */
	public List<PolicyCategory> getAllPolicyCategories() throws PacManException;
	
	/**
     * Service to enable disable AutoFix
     *
     * @author 
     * @param policyId - valid policy Id
     * @param action - valid action (disable/ enable)
     * @param userId - userId who performs the action
     * @return Success or Failure response
     * @throws PacManException
     */
	public String enableDisableAutofix(final String policyId, final String action, final String userId) throws PacManException;

	
}

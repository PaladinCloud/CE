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
package com.tmobile.cloud.awsrules.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import com.amazonaws.services.identitymanagement.model.*;
import com.tmobile.pacman.commons.PacmanSdkConstants;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.policy.Action;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.util.CollectionUtils;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;

public class IAMUtils {
    
    private static final Logger logger = LoggerFactory
            .getLogger(IAMUtils.class);

    private IAMUtils() {

    }

    /**
     * This method will fetch the access key information of a particular user.
     * 
     * @param userName
     * @param iamClient
     * @return list of access key meta data
     */
    public static List<AccessKeyMetadata> getAccessKeyInformationForUser(
            final String userName, AmazonIdentityManagementClient iamClient) {
        ListAccessKeysRequest accessKeysRequest = new ListAccessKeysRequest();
        accessKeysRequest.setUserName(userName);
        logger.debug("userName {} ",userName);
        List<AccessKeyMetadata> accessKeyMetadatas = new ArrayList<>();
        ListAccessKeysResult keysResult = null;
        do {
			for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
				try {
					keysResult = iamClient.listAccessKeys(accessKeysRequest);
				} catch (AmazonIdentityManagementException e) {
					if (e.getMessage().startsWith("Rate exceeded")) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException ex) {
							throw new RuntimeException(ex);
						}
						if (i == 2) {
							logger.error(e.getMessage());
							throw e;
						}
						logger.info("Retrying inside getAccessKeyInformationForUser for username -{}", userName);
						continue;
					}
					else{
						throw e;
					}
				}
				break;
			}
            accessKeyMetadatas.addAll(keysResult.getAccessKeyMetadata());
            accessKeysRequest.setMarker(keysResult.getMarker());
        } while (keysResult.isTruncated());

        return accessKeyMetadatas;
    }
    
    /**
	 * This method will fetch the policy .
	 * 
	 * @param policyArn
	 * @param iamClient
	 * @return Set of actions
	 */
	public static Set<String> getAllowedActionsByUserPolicy(AmazonIdentityManagementClient iamClient, String userName) {
		Set<String> actionSet = new HashSet<>();
		actionSet.addAll(getAttachedUserPolicyActionSet(userName, iamClient));
		actionSet.addAll(getInlineUserPolicyActionSet(userName, iamClient));
		return actionSet;
	}
	
	/**
	 * Gets the attached policy.
	 *
	 * @param userName
	 *            the user name
	 * @param iamClient
	 *            the iam client
	 * @param actionSet
	 *            the action set
	 * @return the attached policy
	 */
	private static Set<String> getAttachedUserPolicyActionSet(String userName,
			AmazonIdentityManagementClient iamClient) {
		Set<String> actionSet = new HashSet<>();
		String docVersion = null;
		List<AttachedPolicy> attachedPolicies = getAttachedPolicyOfIAMUser(userName, iamClient);
		for (AttachedPolicy attachedPolicy : attachedPolicies) {
			List<PolicyVersion> policyVersions = iamClient
					.listPolicyVersions(new ListPolicyVersionsRequest().withPolicyArn(attachedPolicy.getPolicyArn()))
					.getVersions();

			for (PolicyVersion policyVersion : policyVersions) {
				if (policyVersion.getIsDefaultVersion()) {
					try {
						GetPolicyVersionRequest versionRequest = new GetPolicyVersionRequest();
						versionRequest.setPolicyArn(attachedPolicy.getPolicyArn());
						versionRequest.setVersionId(policyVersion.getVersionId());
						GetPolicyVersionResult versionResult = iamClient.getPolicyVersion(versionRequest);
						try {
							docVersion = URLDecoder.decode(versionResult.getPolicyVersion().getDocument(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.error(e.getMessage());
							throw new InvalidInputException(e.getMessage());
						}
						Policy policy = Policy.fromJson(docVersion);
						actionSet.addAll(getActionSet(policy));

					} catch (Exception e) {
						logger.error("Error in getting policy for base account in verify account", e.getMessage());
					}
				}
			}
		}
		return actionSet;
	}
	
	private static Set<String> getActionSet(Policy policy) {
		Set<String> actionsSet = new HashSet();
		for (Statement statement : policy.getStatements()) {
			if (statement.getEffect().equals(Effect.Allow)) {
				for (Action action : statement.getActions()) {
					actionsSet.add(action.getActionName());
				}
			}

		}
		return actionsSet;
	}

	
	/**
	 * Gets the inline user policy.
	 *
	 * @param userName
	 *            the user name
	 * @param amazonIdentityManagement
	 *            the amazon identity management
	 * @param actionSet
	 *            the action set
	 * @return the inline user policy
	 */
	private static Set<String> getInlineUserPolicyActionSet(String userName,
			AmazonIdentityManagementClient amazonIdentityManagement) {
		Set<String> actionSet = new HashSet<>();

		List<String> inlineUserPolicyNameList = new ArrayList<>();
		ListUserPoliciesRequest listUserPoliciesRequest = new ListUserPoliciesRequest();
		listUserPoliciesRequest.setUserName(userName);
		ListUserPoliciesResult listUserPoliciesResult = null;
		do {
			listUserPoliciesResult = amazonIdentityManagement.listUserPolicies(listUserPoliciesRequest);
			inlineUserPolicyNameList.addAll(listUserPoliciesResult.getPolicyNames());
			listUserPoliciesRequest.setMarker(listUserPoliciesResult.getMarker());
		} while (listUserPoliciesResult.isTruncated());

		for (String policyName : inlineUserPolicyNameList) {
			Policy policy = getInlineUserPolicy(userName, policyName, amazonIdentityManagement);
			actionSet.addAll(getActionSet(policy));
		}
		return actionSet;
	}
	
	/**
	 * Gets the inline user policy.
	 *
	 * @param userName
	 *            the user name
	 * @param policyName
	 *            the policy name
	 * @param amazonIdentityManagement
	 *            the amazon identity management
	 * @return the inline user policy
	 */
	private static Policy getInlineUserPolicy(String userName, String policyName,
			AmazonIdentityManagement amazonIdentityManagement) {
		Policy policy = new Policy();
		try {
			GetUserPolicyRequest policyRequest = new GetUserPolicyRequest();
			policyRequest.setUserName(userName);
			policyRequest.setPolicyName(policyName);
			GetUserPolicyResult policyResult = amazonIdentityManagement.getUserPolicy(policyRequest);
			String policyAsString = policyResult.getPolicyDocument();

			policyAsString = java.net.URLDecoder.decode(policyAsString, "UTF-8");
			policy = Policy.fromJson(policyAsString);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return policy;
	}
	
	/**
	 * This method will fetch the attached policy a particular role.
	 * 
	 * @param roleName
	 * @param iamClient
	 * @return list of AttachedPolicy
	 */
	public static List<AttachedPolicy> getAttachedPolicyOfIAMUser(String userName,
			AmazonIdentityManagementClient iamClient) throws RuleExecutionFailedExeption {
		ListAttachedUserPoliciesRequest attachedUserPoliciesRequest = new ListAttachedUserPoliciesRequest();
		attachedUserPoliciesRequest.setUserName(userName);
		ListAttachedUserPoliciesResult userPoliciesResult = iamClient
				.listAttachedUserPolicies(attachedUserPoliciesRequest);
		return userPoliciesResult.getAttachedPolicies();
	}

	
	/**
	 * This method will fetch the policy .
	 * 
	 * @param policyArn
	 * @param iamClient
	 * @return Set of actions
	 */
	public static Set<String> getAllowedActionsByRolePolicy(AmazonIdentityManagementClient iamClient, String roleName) throws Exception {
		Set<String> actionSet = new HashSet<>();
		actionSet.addAll(getAttachedRolePolicyActionSet(roleName, iamClient));
		actionSet.addAll(getInlineRolePolicyActionSet(roleName, iamClient));
		return actionSet;
	}
	
	
	/**
	 * Gets the attached policy.
	 *
	 * @param roleName
	 *            the role name
	 * @param iamClient
	 *            the iam client
	 * @param actionSet
	 *            the action set
	 * @return the attached policy
	 */
	private static Set<String> getAttachedRolePolicyActionSet(String roleName,
			AmazonIdentityManagementClient iamClient) throws AmazonIdentityManagementException, InterruptedException {
		Set<String> actionSet = new HashSet<>();
		String docVersion = null;
		List<AttachedPolicy> attachedPolicies = getAttachedPolicyOfIAMRole(roleName, iamClient);
		List<PolicyVersion> policyVersions = new ArrayList<>();
		for (AttachedPolicy attachedPolicy : attachedPolicies) {
			for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
				try {
					policyVersions = iamClient
							.listPolicyVersions(new ListPolicyVersionsRequest().withPolicyArn(attachedPolicy.getPolicyArn()))
							.getVersions();
				} catch (AmazonIdentityManagementException e) {
					if (e.getMessage().startsWith("Rate exceeded")) {
						Thread.sleep(5000);
						if (i == 2) {
							logger.error(e.getMessage());
							throw e;
						}
						logger.info("Retrying inside getAttachedRolePolicyActionSet for policy arn -{}", attachedPolicy.getPolicyArn());
						continue;
					}
					else{
						throw e;
					}
				}
				break;
			}

			for (PolicyVersion policyVersion : policyVersions) {
				if (policyVersion.getIsDefaultVersion()) {
					try {
						GetPolicyVersionRequest versionRequest = new GetPolicyVersionRequest();
						versionRequest.setPolicyArn(attachedPolicy.getPolicyArn());
						versionRequest.setVersionId(policyVersion.getVersionId());
						GetPolicyVersionResult versionResult=new GetPolicyVersionResult();
						for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
							try {
								versionResult = iamClient.getPolicyVersion(versionRequest);
							} catch (AmazonIdentityManagementException e) {
								if (e.getMessage().startsWith("Rate exceeded")) {
									Thread.sleep(5000);
									if (i == 2) {
										logger.error(e.getMessage());
										throw e;
									}
									logger.info("Retrying inside getAttachedRolePolicyActionSet for policy version-{}", policyVersion.getVersionId());
									continue;
								}
								else{
									throw e;
								}
							}
							break;
						}
						try {
							docVersion = URLDecoder.decode(versionResult.getPolicyVersion().getDocument(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.error(e.getMessage());
							throw new InvalidInputException(e.getMessage());
						}
						Policy policy = Policy.fromJson(docVersion);
						actionSet.addAll(getActionSet(policy));

					} catch (Exception e) {
						logger.error("Error in getting policy for base account in verify account", e.getMessage());
					}
				}
			}
		}
		return actionSet;
	}
	
	/**
	 * Gets the inline role policy.
	 *
	 * @param roleName
	 *            the role name
	 * @param amazonIdentityManagement
	 *            the amazon identity management
	 * @param actionSet
	 *            the action set
	 * @return the inline role policy
	 */
	private static Set<String> getInlineRolePolicyActionSet(String roleName,
			AmazonIdentityManagementClient amazonIdentityManagement) throws AmazonIdentityManagementException, InterruptedException, UnsupportedEncodingException {
		Set<String> actionSet = new HashSet<>();

		List<String> inlineRolePolicyNameList = new ArrayList<>();
		ListRolePoliciesRequest listRolePoliciesRequest = new ListRolePoliciesRequest();
		listRolePoliciesRequest.setRoleName(roleName);
		ListRolePoliciesResult listRolePoliciesResult = null;
		do {
			for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
				try {
					listRolePoliciesResult = amazonIdentityManagement.listRolePolicies(listRolePoliciesRequest);
				} catch (AmazonIdentityManagementException e) {
					if (e.getMessage().startsWith("Rate exceeded")) {
						Thread.sleep(5000);
						if (i == 2) {
							logger.error(e.getMessage());
							throw e;
						}
						logger.info("Retrying inside getInlineRolePolicyActionSet for roleName-{}", roleName);
						continue;
					}
					else{
						throw e;
					}
				}
				break;
			}
			inlineRolePolicyNameList.addAll(listRolePoliciesResult.getPolicyNames());
			listRolePoliciesRequest.setMarker(listRolePoliciesResult.getMarker());
		} while (listRolePoliciesResult.isTruncated());

		for (String policyName : inlineRolePolicyNameList) {
			Policy policy = getInlineRolePolicy(roleName, policyName, amazonIdentityManagement);
			actionSet.addAll(getActionSet(policy));
		}
		return actionSet;
	}
	
	/**
	 * This method will fetch the attached policy a particular role.
	 * 
	 * @param roleName
	 * @param iamClient
	 * @return list of AttachedPolicy
	 */
	public static List<AttachedPolicy> getAttachedPolicyOfIAMRole(final String roleName,
			AmazonIdentityManagementClient iamClient) throws AmazonIdentityManagementException, InterruptedException {
		ListAttachedRolePoliciesRequest attachedUserPoliciesRequest = new ListAttachedRolePoliciesRequest();
		attachedUserPoliciesRequest.setRoleName(roleName);
		for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
			try {
				ListAttachedRolePoliciesResult rolePoliciesResult = iamClient
						.listAttachedRolePolicies(attachedUserPoliciesRequest);
				return rolePoliciesResult.getAttachedPolicies();
			} catch (AmazonIdentityManagementException e) {
				if (e.getMessage().startsWith("Rate exceeded")) {
					Thread.sleep(5000);
					if (i == 2) {
						logger.error(e.getMessage());
						throw e;
					}
					logger.info("Retrying inside getInlineRolePolicyActionSet for roleName-{}", roleName);
				}
				else{
					throw e;
				}
			}
		}
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * Gets the inline role policy.
	 *
	 * @param roleName
	 *            the role name
	 * @param policyName
	 *            the policy name
	 * @param amazonIdentityManagement
	 *            the amazon identity management
	 * @return the inline role policy
	 */
	private static Policy getInlineRolePolicy(String roleName, String policyName,
			AmazonIdentityManagement amazonIdentityManagement) throws AmazonIdentityManagementException, InterruptedException, UnsupportedEncodingException {
		Policy policy = new Policy();
		for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++){
			try {
				GetRolePolicyRequest policyRequest = new GetRolePolicyRequest();
				policyRequest.setRoleName(roleName);
				policyRequest.setPolicyName(policyName);
				GetRolePolicyResult policyResult = amazonIdentityManagement.getRolePolicy(policyRequest);
				String policyAsString = policyResult.getPolicyDocument();
				policyAsString = java.net.URLDecoder.decode(policyAsString, "UTF-8");
				policy = Policy.fromJson(policyAsString);
			} catch (AmazonIdentityManagementException e) {
				if(e.getMessage().startsWith("Rate exceeded")){
					Thread.sleep(5000);
					if(i==2){
						logger.error(e.getMessage());
						throw e;
					}
					logger.info("Retrying inside getInlineRolePolicy for policyName-{} and roleName-{}",policyName,roleName);
					continue;
				}
				else{
					throw e;
				}
			}
			break;
		}
		return policy;
	}
	
	/**
	 * @param userName
	 * @param amazonIdentityManagement
	 * @return
	 * 
	 * return true if an inline policy of a user allows full administrative privileges
	 * 
	 */
	public static boolean isInlineUserPolicyWithFullAdminAccess(String userName,
			AmazonIdentityManagementClient amazonIdentityManagement) {

		List<String> inlineUserPolicyNameList = new ArrayList<>();
		ListUserPoliciesRequest listUserPoliciesRequest = new ListUserPoliciesRequest();
		listUserPoliciesRequest.setUserName(userName);
		ListUserPoliciesResult listUserPoliciesResult = null;
		do {
			listUserPoliciesResult = amazonIdentityManagement.listUserPolicies(listUserPoliciesRequest);
			inlineUserPolicyNameList.addAll(listUserPoliciesResult.getPolicyNames());
			listUserPoliciesRequest.setMarker(listUserPoliciesResult.getMarker());
		} while (listUserPoliciesResult.isTruncated());

		for (String policyName : inlineUserPolicyNameList) {
			Policy policy = getInlineUserPolicy(userName, policyName, amazonIdentityManagement);
			if(isStatementContainsFullAdminAccess(policy))
				return true;
				
		}
		return false;
	}
    
    /**
     * @param groupName
     * @param amazonIdentityManagement
     * @return
     * 
     * return true if an inline policy of a group allows full administrative privileges
     * 
     */
    public static boolean isInlineGroupPolicyWithFullAdminAccess(String groupName,
			AmazonIdentityManagementClient amazonIdentityManagement) {

		List<String> inlineGroupPolicyNameList = new ArrayList<>();
		ListGroupPoliciesRequest listGroupPoliciesRequest = new ListGroupPoliciesRequest();
		listGroupPoliciesRequest.setGroupName(groupName);
		ListGroupPoliciesResult listGroupPoliciesResult = null;
		do {
			listGroupPoliciesResult = amazonIdentityManagement.listGroupPolicies(listGroupPoliciesRequest);
			inlineGroupPolicyNameList.addAll(listGroupPoliciesResult.getPolicyNames());
			listGroupPoliciesRequest.setMarker(listGroupPoliciesResult.getMarker());
		} while (listGroupPoliciesResult.isTruncated());

		for (String policyName : inlineGroupPolicyNameList) {
			Policy policy = getInlineGroupPolicy(groupName, policyName, amazonIdentityManagement);
			if(isStatementContainsFullAdminAccess(policy))
				return true;
				
		}
		return false;
	}
    
    /**
     * @param groupName
     * @param policyName
     * @param amazonIdentityManagement
     * @return
     * 
     * Fetches inline policy details of a group using the groupname and policyname
     * 
     */
    private static Policy getInlineGroupPolicy(String groupName, String policyName,
			AmazonIdentityManagement amazonIdentityManagement) {
		Policy policy = new Policy();
		try {
			GetGroupPolicyRequest policyRequest = new GetGroupPolicyRequest();
			policyRequest.setGroupName(groupName);
			policyRequest.setPolicyName(policyName);
			GetGroupPolicyResult policyResult = amazonIdentityManagement.getGroupPolicy(policyRequest);
			String policyAsString = policyResult.getPolicyDocument();

			policyAsString = java.net.URLDecoder.decode(policyAsString, "UTF-8");
			policy = Policy.fromJson(policyAsString);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return policy;
	}
	
	/**
	 * Method to check if the policy statement contains {Effect : *, Action: *, Resource : *}
	 * 
	 * @param policyArn
	 * @param iamClient
	 * @return
	 */
	public static boolean isPolicyWithFullAdminAccess(String policyArn, AmazonIdentityManagementClient iamClient) throws AmazonIdentityManagementException,InterruptedException {
		List<PolicyVersion> policyVersions = new ArrayList<>();
		for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
			try {
				policyVersions = iamClient.listPolicyVersions(new ListPolicyVersionsRequest().withPolicyArn(policyArn)).getVersions();
			} catch (AmazonIdentityManagementException e) {
				if (e.getMessage().startsWith("Rate exceeded")) {
					Thread.sleep(5000);
					if (i == 2) {
						logger.error(e.getMessage());
						throw e;
					}
					logger.info("Retrying inside isPolicyWithFullAdminAccess for policy arn -{}", policyArn);
					continue;
				}
				else{
					throw e;
				}
			}
			break;
		}

		for (PolicyVersion policyVersion : policyVersions) {
			if (policyVersion.isDefaultVersion()) {
				String docVersion = null;
				Policy policy = new Policy();
				GetPolicyVersionResult versionResult=new GetPolicyVersionResult();
				try {
					GetPolicyVersionRequest versionRequest = new GetPolicyVersionRequest();
					versionRequest.setPolicyArn(policyArn);
					versionRequest.setVersionId(policyVersion.getVersionId());
					for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
						try {
							versionResult = iamClient.getPolicyVersion(versionRequest);
						} catch (AmazonIdentityManagementException e) {
							if (e.getMessage().startsWith("Rate exceeded")) {
								Thread.sleep(5000);
								if (i == 2) {
									logger.error(e.getMessage());
									throw e;
								}
								logger.info("Retrying inside isPolicyWithFullAdminAccess for policy arn,versionid -{}, {}", policyArn, policyVersion.getVersionId());
								continue;
							}
							else{
								throw e;
							}
						}
						break;
					}
					try {
						docVersion = URLDecoder.decode(versionResult.getPolicyVersion().getDocument(), "UTF-8");
						policy = Policy.fromJson(docVersion);
					} catch (UnsupportedEncodingException e) {
						logger.error(e.getMessage());
						throw new InvalidInputException(e.getMessage());
					}

				} catch (Exception e) {
					logger.error("Error in getting policy for base account in verify account", e.getMessage());
				}
				
				if(isStatementContainsFullAdminAccess(policy))
					return true;

			}

		}

		return false;
	}

	/**
	 * @param policy
	 * @return
	 * 
	 * return true if any of the statement in policy contains full administrative access
	 */
	private static boolean isStatementContainsFullAdminAccess(Policy policy) {
		if (null != policy && !CollectionUtils.isNullOrEmpty(policy.getStatements())) {

			for (Statement statement : policy.getStatements()) {

				if (statement.getEffect().equals(Effect.Allow)) {
					if (statement.getActions().stream()
							.filter(action -> action.getActionName().equalsIgnoreCase(PacmanRuleConstants.COLON_STAR))
							.count() > 0) {
						if (statement.getResources().stream()
								.filter(resource -> resource.getId().equalsIgnoreCase(PacmanRuleConstants.COLON_STAR))
								.count() > 0) {
							return true;
						}
					}
				}

			}

		}
		return false;
	}
	
	/**
	 * @param roleName
	 * @param amazonIdentityManagement
	 * @return
	 * 
	 * return true if an inline policy of a role allows full administrative privileges
	 * 
	 */
	public static boolean isInlineRolePolicyWithFullAdminAccess(String roleName,
			AmazonIdentityManagementClient amazonIdentityManagement) throws AmazonIdentityManagementException, InterruptedException, UnsupportedEncodingException {

		List<String> inlineRolePolicyNameList = new ArrayList<>();
		ListRolePoliciesRequest listRolePoliciesRequest = new ListRolePoliciesRequest();
		listRolePoliciesRequest.setRoleName(roleName);
		ListRolePoliciesResult listRolePoliciesResult = null;
		do {
			for(int i=0;i<PacmanSdkConstants.MAX_RETRY_COUNT;i++) {
				try {
					listRolePoliciesResult = amazonIdentityManagement.listRolePolicies(listRolePoliciesRequest);
				} catch (AmazonIdentityManagementException e) {
					if (e.getMessage().startsWith("Rate exceeded")) {
						Thread.sleep(5000);
						if (i == 2) {
							logger.error(e.getMessage());
							throw e;
						}
						logger.info("Retrying inside isInlineRolePolicyWithFullAdminAccess for rolename -{}", roleName);
						continue;
					}
					else{
						throw e;
					}
				}
				break;
			}
			listRolePoliciesRequest.setMarker(listRolePoliciesResult.getMarker());
		} while (listRolePoliciesResult.isTruncated());

		for (String policyName : inlineRolePolicyNameList) {
			Policy policy = getInlineRolePolicy(roleName, policyName, amazonIdentityManagement);
			if(isStatementContainsFullAdminAccess(policy))
				return true;
				
		}
		return false;
	}
	
	/**
	 * @param arn
	 * @param iamClient
	 * @return
	 * @throws RuleExecutionFailedExeption
	 * 
	 * Returns the role ids of all the IAM roles having 'arn' policy attached
	 * 
	 */
	public static Set<String> getSupportRoleByPolicyArn(String arn,
			AmazonIdentityManagementClient iamClient) throws RuleExecutionFailedExeption {

		ListEntitiesForPolicyRequest request = new ListEntitiesForPolicyRequest();
		ListEntitiesForPolicyResult respone = null;
		Set<String> roleIds = null;
		do {
			respone = iamClient.listEntitiesForPolicy(request.withPolicyArn(arn));
			if(respone != null && !CollectionUtils.isNullOrEmpty(respone.getPolicyRoles())) {
				roleIds = respone.getPolicyRoles().stream().map(role -> role.getRoleId()).collect(Collectors.toSet());
			}
			request.setMarker(respone.getMarker());
		} while (respone.isTruncated());
		return roleIds;
	}
	
	/**
	 * @param policyName
	 * @param iamClient
	 * @return
	 * @throws RuleExecutionFailedExeption
	 * 
	 * Returns the policy arn of the AWS managed policy with policyName
	 * 
	 */
	public static String getAwsManagedPolicyArnByName(String policyName,
			AmazonIdentityManagementClient iamClient) throws RuleExecutionFailedExeption {

		List<String> arn = null;
		ListPoliciesRequest request = new ListPoliciesRequest();
		ListPoliciesResult respone = null;
		do {
			respone = iamClient.listPolicies(request.withScope(PolicyScopeType.AWS));
			if (null != respone && !CollectionUtils.isNullOrEmpty(respone.getPolicies())) {
				arn = new ArrayList<>();
				arn.addAll(respone.getPolicies().stream()
						.filter(policy -> policy.getPolicyName().equalsIgnoreCase(policyName))
						.map(policy -> policy.getArn()).collect(Collectors.toList()));

			}
			request.setMarker(respone.getMarker());
		} while (respone.isTruncated() && CollectionUtils.isNullOrEmpty(arn));
		return !CollectionUtils.isNullOrEmpty(arn) ? arn.get(0) : null;
	}
	
	/**
	 * @param policies
	 * @param iamClient
	 * @return
	 * @throws RuleExecutionFailedExeption
	 * 
	 * Check the assumed policies of a role having any user or group as principal
	 * 
	 */
	public static boolean isSupportRoleAssumedByUserOrGroup(Set<String> policies,
			AmazonIdentityManagementClient iamClient) throws RuleExecutionFailedExeption {

		String docVersion = null;
		for (String policyDoc : policies) {
			try {
				docVersion = URLDecoder.decode(policyDoc, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage());
				throw new InvalidInputException(e.getMessage());
			}
			Policy policy = Policy.fromJson(docVersion);

			if (null != policy && !CollectionUtils.isNullOrEmpty(policy.getStatements())) {

				for (Statement statement : policy.getStatements()) {
					if (statement.getEffect().equals(Effect.Allow)) {

						if (statement.getPrincipals().stream().filter(
								principal -> principal.getId().contains("user") || principal.getId().contains("group"))
								.count() > 0) {
							return true;
						}
					}

				}

			}

		}

		return false;
	}

	public static boolean isUserHasInvalidPermission(String userName, AmazonIdentityManagementClient iamClient) {
		ListUserPoliciesResult listUserPoliciesResult = new ListUserPoliciesResult();
		ListUserPoliciesRequest listUserPoliciesRequest = new ListUserPoliciesRequest();

		List<AttachedPolicy> attachedPolicies = getAttachedPolicyOfIAMUser(userName, iamClient);

		listUserPoliciesRequest.setUserName(userName);
		listUserPoliciesResult = iamClient.listUserPolicies(listUserPoliciesRequest);

		if (!CollectionUtils.isNullOrEmpty(attachedPolicies)
				|| !CollectionUtils.isNullOrEmpty(listUserPoliciesResult.getPolicyNames())) {
			return true;
		}
		return false;
	}

	
}

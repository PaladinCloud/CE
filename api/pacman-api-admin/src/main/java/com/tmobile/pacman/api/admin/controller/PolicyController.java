package com.tmobile.pacman.api.admin.controller;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.CreateUpdatePolicyDetails;
import com.tmobile.pacman.api.admin.domain.Response;
import com.tmobile.pacman.api.admin.repository.service.PolicyService;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Policy API Controller
 */
@Api(value = "/policy")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'policy-management') or #oauth2.hasScope('API_OPERATION/READ')")
@RequestMapping("/policy")
public class PolicyController {

	/** The Constant logger. */
	private static final Logger log = LoggerFactory.getLogger(PolicyController.class);

	@Autowired
    private PolicyService policyService;

	/**
     * API to get all Policies
     *
     * @author 
     * @param page - zero-based page index.
     * @param size - the size of the page to be returned.
     * @param searchTerm - searchTerm to be searched.
     * @return All Policies details
     */
	@ApiOperation(httpMethod = "GET", value = "API to get all Policies", response = Page.class,  produces = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/list", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getPolicies(
			@ApiParam(value = "provide valid page number", required = true) @RequestParam("page") Integer page,
			@ApiParam(value = "provide valid page size", required = true) @RequestParam("size") Integer size,
			@ApiParam(value = "provide valid search term", required = false) @RequestParam(defaultValue="", name = "searchTerm", required = false) String searchTerm) {
		try {
			if(searchTerm != null && AdminConstants.AUTO_FIX_KEYWORD.equalsIgnoreCase(searchTerm)) {
				searchTerm = AdminConstants.AUTO_FIX_KEY;
			} else {
				searchTerm = searchTerm != null ? searchTerm.trim() : "";
			}
			return ResponseUtils.buildSucessResponse(policyService.getPolicies(searchTerm, page, size));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

	/**
     * API to get policy by id
     *
     * @author 
     * @param policyId - valid policy Id
     * @return Policies details
     */
	@ApiOperation(httpMethod = "GET", value = "API to get policy by id", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/details-by-id", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getPoliciesById(
			@ApiParam(value = "provide valid policy id", required = true) @RequestParam(defaultValue = "", name = "policyId", required = true) String policyId) {
		try {
			return ResponseUtils.buildSucessResponse(policyService.getByPolicyId(policyId));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

	/**
     * API to get AlexaKeywords
     *
     * @author 
     * @return All AlexaKeywords
     */
	@ApiOperation(httpMethod = "GET", value = "API to get alexa keywords", response = Response.class,  produces = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/alexa-keywords", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getAllAlexaKeywords() {
		try {
			return ResponseUtils.buildSucessResponse(policyService.getAllAlexaKeywords());
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

	/**
     * API to get all Policy Id's
     *
     * @author 
     * @return All Policy Id's
     */
	@ApiOperation(httpMethod = "GET", value = "API to get all Policy Id's", response = Response.class,  produces = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/policy-ids", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getAllPolicyIds() {
		try {
			return ResponseUtils.buildSucessResponse(policyService.getAllPolicyIds());
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

	/**
     * API to create new policy
     *
     * @author 
     * @param fileToUpload - valid executable policy jar file
     * @param createPolicyDetails - details for creating new Policy
     * @return Success or Failure response
     */
	@ApiOperation(httpMethod = "POST", value = "API to create new policy", response = Response.class, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@RequestMapping(path = "/create", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> createPolicy(@AuthenticationPrincipal Principal user,
			@ApiParam(value = "provide valid policy details", required = false) @RequestParam(defaultValue="", value = "file", required = false) MultipartFile fileToUpload, CreateUpdatePolicyDetails createPolicyDetails) {
		try {
			return ResponseUtils.buildSucessResponse(policyService.createPolicy(fileToUpload, createPolicyDetails, user.getName()));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

	/**
     * API to update new policy
     *
     * @author 
     * @param fileToUpload - valid executable policy jar file
     * @param updatePolicyDetails - details for updating existing policy
     * @return Success or Failure response
     */
	@ApiOperation(httpMethod = "POST", value = "API to update new policy", response = Response.class, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@RequestMapping(path = "/update", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> updatePolicy(@AuthenticationPrincipal Principal user,
			@ApiParam(value = "provide valid policy details", required = false) @RequestParam(value = "file", required = false) MultipartFile fileToUpload, CreateUpdatePolicyDetails updatePolicyDetails) {
		try {
			return ResponseUtils.buildSucessResponse(policyService.updatePolicy(fileToUpload, updatePolicyDetails, user.getName()));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

	/**
     * API to invoke policy
     *
     * @author 
     * @param policyId - valid policy Id
     * @param policyOptionalParams - valid policy optional parameters which need to be passed while invoking policy
     * @return Success or Failure response
     */
	@ApiOperation(httpMethod = "POST", value = "API to invoke policy", response = Response.class, consumes = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/invoke", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> invokePolicy(
			@ApiParam(value = "provide valid policy id", required = true) @RequestParam("policyId") String policyId,
			@ApiParam(value = "provide policy optional params", required = false) @RequestBody(required = false) List<Map<String, Object>> policyOptionalParams) {
		try {
			return ResponseUtils.buildSucessResponse(policyService.invokePolicy(policyId, policyOptionalParams));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

	/**
     * API to enable disable policy
     *
     * @author 
     * @param policyId - valid policy Id
     * @param user - userId who performs the action
     * @param action - valid action (disable/ enable)
     * @return Success or Failure response
     */
	@ApiOperation(httpMethod = "POST", value = "API to enable disable policy", response = Response.class, consumes = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/enable-disable", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> enableDisablePolicy(@AuthenticationPrincipal Principal user,
			@ApiParam(value = "provide valid policy id", required = true) @RequestParam("policyId") String policyId,
			@ApiParam(value = "provide valid action", required = true) @RequestParam("action") String action) {
		try {
			return ResponseUtils.buildSucessResponse(policyService.enableDisablePolicy(policyId, action, user.getName()));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}
	
	/**
	 * Gets the all Policy category.
	 *
	 * @return the all Policy category
	 */
	@ApiOperation(httpMethod = "GET", value = "API to get all Policy Category's", response = Response.class,  produces = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/categories", method = RequestMethod.GET,  produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getAllPolicyCategory() {
		try {
			return ResponseUtils.buildSucessResponse(policyService.getAllPolicyCategories());
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}
	
	/**
     * API to enable disable policy
     *
     * @author 
     * @param policyId - valid policy Id
     * @param user - userId who performs the action
     * @param action - valid action (disable/ enable)
     * @return Success or Failure response
     */
	@ApiOperation(httpMethod = "POST", value = "API to enable disable policy", response = Response.class, consumes = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(path = "/enable-disable-autofix", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> enableDisableAutoFix(@AuthenticationPrincipal Principal user,
			@ApiParam(value = "provide valid policy id", required = true) @RequestParam("policyId") String policyId,
			@ApiParam(value = "provide valid status", required = true) @RequestParam("autofixStatus") String action) {
		try {
			return ResponseUtils.buildSucessResponse(policyService.enableDisableAutofix(policyId, action, user.getName()));
		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
	}

}


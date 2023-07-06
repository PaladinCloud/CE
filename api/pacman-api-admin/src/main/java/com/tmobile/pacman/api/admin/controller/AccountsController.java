package com.tmobile.pacman.api.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.tmobile.pacman.api.admin.domain.*;
import com.tmobile.pacman.api.admin.factory.AccountFactory;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.admin.repository.service.AccountsService;
import com.tmobile.pacman.api.admin.repository.service.AssetGroupService;
import com.tmobile.pacman.api.admin.repository.service.UserPreferencesService;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

@Api(value = "/accounts", consumes = "application/json", produces = "application/json")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'account-management') or #oauth2.hasScope('API_OPERATION/READ')")
@RequestMapping("/accounts")
public class AccountsController {
    private static final Logger log = LoggerFactory.getLogger(AccountsController.class);

    @Autowired
    ActivityLoggingController activityLoggingController;

    @Autowired
    AssetGroupService assetGroupService;

    @Autowired
    UserPreferencesService userPreferencesService;


    @Value("${application.optionalAssetGroupList}")
	private String optionalAssetGroupList;


    @ApiOperation(httpMethod = "POST", value = "API to fetch list of accounts", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(path = "/list", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAccounts(@RequestBody PluginRequestBody requestBody){
        try {
            AccountsService accountsService= AccountFactory.getService(Constants.AWS);
            return ResponseUtils.buildSucessResponse(accountsService.getAllAccountsByFilter(requestBody));
        }catch (Exception exception){
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "GET", value = "API to fetch list of accounts", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(path = "/filter/attribute", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getPluginFilterVal(@ApiParam(value = "provide filter attribute", required = true) @RequestParam("attribute") String attribute){
        try {
            AccountsService accountsService= AccountFactory.getService(Constants.AWS);
            return  ResponseUtils.buildSucessResponse(accountsService.getPluginFilterVal(attribute));
        }catch (Exception exception){
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "DELETE", value = "API to delete account", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping(produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteAccount(@ApiParam(value = "provide account number", required = true) @RequestParam("accountId") String accountId,
                                                @ApiParam(value = "provide provider type", required = true) @RequestParam("provider") String type){
		try {
			AccountsService accountsService = AccountFactory.getService(type);
			AccountValidationResponse response = accountsService.deleteAccount(accountId);
			/*
			 * Disable asset group if no cloud type not exists
			 */
			List<String> optionalAssetList = null;
			if (optionalAssetGroupList != null) {
				optionalAssetList = Arrays.asList(optionalAssetGroupList.split(","));
			}
			if (optionalAssetList != null && optionalAssetList.size() > 0 && optionalAssetList.contains(type)) {
				List<AccountDetails> findOnlineAccounts = accountsService.findOnlineAccounts("configured", type);
				if (findOnlineAccounts == null || findOnlineAccounts.size() <= 0) {
					String updateAssetGroupStatus = assetGroupService.updateAssetGroupStatus(type, false, "admin");
					log.debug("AssetGoup  {}   status {}", type, updateAssetGroupStatus);
					Integer totalUsers = userPreferencesService.updateDefaultAssetGroup(type);
					log.debug("total user updated with default asset group {}", totalUsers);
				}
			}
			return ResponseUtils.buildSucessResponse(response);

		} catch (Exception exception) {
			log.error(UNEXPECTED_ERROR_OCCURRED, exception);
			return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
		}
    }

    @ApiOperation(httpMethod = "POST", value = "API to create account", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody final CreateAccountRequest accountDetails){
        AccountsService accountsService=null;
        try{
        	String pluginType = accountDetails.getPlatform();
            accountsService= AccountFactory.getService(pluginType);
            AccountValidationResponse accountValidationResponse = accountsService.addAccount(accountDetails);
			if (accountValidationResponse.getValidationStatus().equalsIgnoreCase("success")) {
				ObjectMapper oMapper = new ObjectMapper();
				Gson gson = new Gson();
				Map<String, Object> responseMap = oMapper.convertValue(accountValidationResponse, Map.class);
				responseMap.keySet().retainAll(Arrays.asList("accountId", "accountName", "type"));
				String acctDetails = gson.toJson(responseMap);
				/*
				 * By default Azure and GCP asset groups are disabled. Enabled after creating
				 * the Cloud Plugins .
				 */
				List<String> optionalAssetList = null;
				if (optionalAssetGroupList != null) {
					optionalAssetList = Arrays.asList(optionalAssetGroupList.split(","));
				}
				if (optionalAssetList != null && optionalAssetList.size() > 0
						&& optionalAssetList.contains(pluginType)) {
					String updateAssetGroupStatus = assetGroupService.updateAssetGroupStatus(pluginType, true, "admin");
					log.info("AssetGoup  {} status {}", pluginType, updateAssetGroupStatus);
				}

				invokeActivityLogging(acctDetails, accountDetails.getCreatedBy(),
						accountValidationResponse.getAccountId(), "create");
			}
            return ResponseUtils.buildSucessResponse(accountValidationResponse);
        }catch (Exception exception){
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            if(accountsService!=null) {
                accountsService.deleteAccount(accountDetails.getAccountId());
            }
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }
    @ApiOperation(httpMethod = "POST", value = "API to validate account configuration", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(path = "/validate", produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> validateAccount(@RequestBody final CreateAccountRequest accountDetails){
        try{
            AccountsService accountsService= AccountFactory.getService(accountDetails.getPlatform());
            return ResponseUtils.buildSucessResponse(accountsService.validate(accountDetails));
        }catch (Exception exception){
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "GET", value = "API to fetch base account details", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(path = "/baseAccount",produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getBaseAccount(){
        try {
            Map<String, String> baseAccount=new HashMap<>();
            baseAccount.put("accountId", System.getenv("COGNITO_ACCOUNT"));
            baseAccount.put("roleName", System.getenv("PALADINCLOUD_RO"));
            return ResponseUtils.buildSucessResponse(baseAccount);
        }catch (Exception exception){
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @Async
    private void invokeActivityLogging(String acctDetails, String acctCreatedBy, String accountId, String action){
        try{
            ActivityLogRequest activityLogRequest = new ActivityLogRequest();
            activityLogRequest.setAction(action);
            activityLogRequest.setOldState("NA");
            activityLogRequest.setUser(acctCreatedBy);
            activityLogRequest.setObject("Plugins");
            activityLogRequest.setObjectId(accountId);
            activityLogRequest.setNewState(acctDetails);
            activityLoggingController.logActivity(activityLogRequest);
        }
        catch(Exception exception){
            log.error("Could not save account created with id - {} to activity log!!", accountId);
        }
    }


}

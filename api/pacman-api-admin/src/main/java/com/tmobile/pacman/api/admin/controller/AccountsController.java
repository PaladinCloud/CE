package com.tmobile.pacman.api.admin.controller;

import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.admin.domain.PluginRequestBody;
import com.tmobile.pacman.api.admin.domain.Response;
import com.tmobile.pacman.api.admin.factory.AccountFactory;
import com.tmobile.pacman.api.admin.repository.service.AccountsService;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

@Api(value = "/accounts", consumes = "application/json", produces = "application/json")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'account-management') or #oauth2.hasScope('API_OPERATION/READ')")
@RequestMapping("/accounts")
public class AccountsController {
    private static final Logger log = LoggerFactory.getLogger(AccountsController.class);


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

    @ApiOperation(httpMethod = "DELETE", value = "API to delete account", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping(produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteAccount(@ApiParam(value = "provide account number", required = true) @RequestParam("accountId") String accountId,
                                                @ApiParam(value = "provide provider type", required = true) @RequestParam("provider") String type){
        try{
            AccountsService accountsService= AccountFactory.getService(type);
            return ResponseUtils.buildSucessResponse(accountsService.deleteAccount(accountId));
        }catch (Exception exception)
        {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "POST", value = "API to create account", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(produces =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody final CreateAccountRequest accountDetails){
        AccountsService accountsService=null;
        try{
            accountsService= AccountFactory.getService(accountDetails.getPlatform());
            return ResponseUtils.buildSucessResponse(accountsService.addAccount(accountDetails));
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


}

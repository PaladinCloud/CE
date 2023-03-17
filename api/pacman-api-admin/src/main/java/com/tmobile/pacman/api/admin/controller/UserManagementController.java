package com.tmobile.pacman.api.admin.controller;

import com.tmobile.pacman.api.admin.domain.CognitoUserDetails;
import com.tmobile.pacman.api.admin.domain.CreateCognitoUserDetails;
import com.tmobile.pacman.api.admin.domain.Response;
import com.tmobile.pacman.api.admin.repository.service.UserService;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

@Api(value = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'user-management') or #oauth2.hasScope('API_OPERATION/READ')")
@RequestMapping("/users")
public class UserManagementController {

    private static final Logger log = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    private UserService userService;


    @ApiOperation(httpMethod = "GET", value = "API to get user details from AWS cognito UserPool ", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping( produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getUsers(@RequestParam(defaultValue = "1", required = false) Integer cursor,
                                           @RequestParam(defaultValue = "50", required = false)  Integer limit,
                                           @RequestParam(required = false) String filter){
        try {
            if(cursor<1 || limit<1){
                String errorMsg="Cursor and limit can't be less than 1";
                return ResponseUtils.buildFailureResponse(new IllegalArgumentException(errorMsg),errorMsg, HttpStatus.BAD_REQUEST);
            }
            return  ResponseUtils.buildSucessResponse(userService.getAllUsers(cursor,limit,filter));
        }
        catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "PUT", value = "API to add roles to the user", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping(path = "/{username}/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> updateRoleMembership(@PathVariable final String username,@RequestBody final CognitoUserDetails cognitoUserDetails){
        try {
            cognitoUserDetails.setUsername(username);
            return  ResponseUtils.buildSucessResponse(userService.updateRoleMembership(cognitoUserDetails.getUsername(),cognitoUserDetails));
        }
        catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "PUT", value = "API to edit status of the user", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping(path = "{username}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> editUser(@PathVariable final String username){
        try {
            return  ResponseUtils.buildSucessResponse(userService.editStatusToUser(username));
        }
        catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "POST", value = "API to create a new user in userPool", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createUser(@RequestBody final CreateCognitoUserDetails userDetails){
        try {
            return  ResponseUtils.buildSucessResponse(userService.createUser(userDetails));
        }
        catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

    @ApiOperation(httpMethod = "DELETE", value = "API to delete an user from userPool", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping(path = "{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> deleteUser(@PathVariable final String username){
        try {
            return  ResponseUtils.buildSucessResponse(userService.deleteUser(username));
        }
        catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(new Exception(UNEXPECTED_ERROR_OCCURRED), exception.getMessage());
        }
    }

}


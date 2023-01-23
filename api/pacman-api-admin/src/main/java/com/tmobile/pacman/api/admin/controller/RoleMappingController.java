package com.tmobile.pacman.api.admin.controller;

import com.tmobile.pacman.api.admin.domain.Response;
import com.tmobile.pacman.api.admin.repository.service.RoleMappingService;
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

import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;


@Api(value = "/rolemapping", consumes = "application/json", produces = "application/json")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")

@RequestMapping("/rolemapping")
public class RoleMappingController {

    private static final Logger log = LoggerFactory.getLogger(RoleMappingController.class);

    @Autowired
    private RoleMappingService roleMappingService;

    @ApiOperation(httpMethod = "GET", value = "API to get the permission associated with given roles", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getRoleMapping(@ApiParam(value = "provide valid roles", required = true) @RequestParam("roles") String roles) {
        try {
            return ResponseUtils.buildSucessResponse(roleMappingService.getRoleCapabilityMapping(roles));

        } catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(exception, null, null);
        }
    }
}
package com.tmobile.pacman.api.admin.controller;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.admin.domain.ActivityLogRequest;
import com.tmobile.pacman.api.admin.domain.Response;
import com.tmobile.pacman.api.commons.repo.ElasticSearchRepository;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(value = "/activitylog", consumes = "application/json", produces = "application/json")
@RestController
@RequestMapping("/activitylog")
public class ActivityLoggingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLoggingController.class);
    private static final String dataSource = "activitylog";

    @Value("${activitylogging.for.login.logout:no}")
    private String loginFlagForActivityLogging;

    @Autowired
    ElasticSearchRepository elasticSearchRepository;


    @ApiOperation(httpMethod = "POST", value = "API to create activity log record", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/", method = RequestMethod.POST)
    public ResponseEntity<Object> logActivity(@RequestBody ActivityLogRequest request){
        try{
            LOGGER.info("inside ActivityLoggingController:::logActivity "+request.toString());
            List<String> missingAttributesList = new ArrayList<>();
            if(Strings.isNullOrEmpty(request.getObject())){
                missingAttributesList.add("object");
            }
            if(Strings.isNullOrEmpty(request.getObjectId())){
                missingAttributesList.add("objectId");
            }
            if(Strings.isNullOrEmpty(request.getUser())){
                missingAttributesList.add("user");
            }
            if(Strings.isNullOrEmpty(request.getAction())){
                missingAttributesList.add("action");
            }
            if(!missingAttributesList.isEmpty()){
                String missingAttrList = String.join(",",missingAttributesList);
                return ResponseUtils.buildFailureResponse(new Exception("Following attributes are not provided - "+missingAttrList),null, HttpStatus.BAD_REQUEST);
            }

            Map<String,Object> requestObject=request.getActivityLogDetails();
            if("Users".equalsIgnoreCase(request.getObject()) && ("login".equalsIgnoreCase(request.getAction()) || "logout".equalsIgnoreCase(request.getAction())) && !"yes".equalsIgnoreCase(loginFlagForActivityLogging)){
                return ResponseUtils.buildSucessResponse("Login/Logout is not be saved to activitylogging as related flag in configuration is turned off.");
            }
            else{
                boolean status= elasticSearchRepository.saveActivityLogToES(dataSource,requestObject);
                if(status)
                    return ResponseUtils.buildSucessResponse(new HashMap());
            }
        }
        catch(Exception exception){
            return ResponseUtils.buildFailureResponse(exception,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseUtils.buildFailureResponse(new Exception("Activity log not saved to ES."),null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}


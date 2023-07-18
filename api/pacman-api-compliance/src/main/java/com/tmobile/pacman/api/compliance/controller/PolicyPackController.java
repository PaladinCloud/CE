package com.tmobile.pacman.api.compliance.controller;

import com.google.common.base.Strings;
import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import com.tmobile.pacman.api.compliance.domain.PolicyPackDetails;
import com.tmobile.pacman.api.compliance.service.PolicyPackService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")
public class PolicyPackController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyPackController.class);
    private static final String DATA_TYPE_NOT_VALID="Data type is invalid. It should be either benchmark or category.";
    private static final String CATEGORY_NOT_PROVIDED="Category is not provided.";
    private static final String CATEGORY_NOT_VALID="Category is invalid and cannot have special characters.";

    private static final String BENCHMARK="benchmark";
    private static final String CATEGORY="category";
    private static final String CIS_CONTROLS = "cisControls";
    int NEG_THIRTY = -30;
    @Autowired
    PolicyPackService policyPackService;

    @ApiOperation(httpMethod = "GET", value = "API to get policy pack tree structure.", produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping(path = "/v1/allpolicypacks/{datatype}")
    @ResponseBody
    public ResponseEntity<Object>  getAllPolicyPacks(@ApiParam(value = "Datatype(benchmark/category)", required = true) @PathVariable("datatype") String dataType){
        try{
            if(!(dataType.equalsIgnoreCase(CATEGORY) || dataType.equalsIgnoreCase(BENCHMARK))){
                return ResponseUtils.buildFailureResponse(new Exception(DATA_TYPE_NOT_VALID),null, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> response = new HashMap<>();
            List<PolicyPackDetails> policyPackDetailsList = new ArrayList<>();
            policyPackDetailsList = policyPackService.getAllPolicyPacks(dataType);
            response.put("response", policyPackDetailsList);
            return ResponseUtils.buildSucessResponse(response);
        }
        catch(Exception exception){
            LOGGER.error(exception.getMessage());
            return ResponseUtils.buildFailureResponse(exception,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(httpMethod = "GET", value = "API to get CIS Controls details for each policy in a policypack", produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "/ciscontrols", method = RequestMethod.GET)
    public ResponseEntity<Object> getCISControlsByPolicyPack(@ApiParam(value = "Policy pack id.", required = true)
                                                                     @RequestParam("policypackid") String policyPackId) {
        try{
        	Map<String,Object> policyPackDetails = policyPackService.getPolicyPackDetailsByID(policyPackId);
        	List<Map<String,Object>> cisControlList = policyPackService.getCISControls(policyPackId);
            policyPackDetails.put(CIS_CONTROLS,cisControlList);
            return ResponseUtils.buildSucessResponse(policyPackDetails);
        }
        catch (Exception e){
            LOGGER.error(e.getMessage());
            return ResponseUtils.buildFailureResponse(e,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
   
    /**
     * API to get policy by ID
     *
     * @author 
     * @param policyID - valid policy ID
     * @return Policy standard details
     */
  	@ApiOperation(httpMethod = "GET", value = "API to get policy standard by policyID",  produces = MediaType.APPLICATION_JSON_VALUE)
  	@RequestMapping(path = "/v1/policystandard", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  	public ResponseEntity<Object> getPoliciesByUUID(
  			@ApiParam(value = "provide valid policy ID", required = true) @RequestParam(defaultValue = "", name = "policyID", required = true) String policyID) {
  		try {
  			return ResponseUtils.buildSucessResponse(policyPackService.getPolicyStandard(policyID));
  		} catch (Exception exception) {
  			LOGGER.error("Unexpected error occurred!!", exception);
  			return ResponseUtils.buildFailureResponse(new Exception("Unexpected error occurred!!"), exception.getMessage());
  		}
  	}  
}

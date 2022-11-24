package com.tmobile.pacman.api.compliance.controller;

import com.tmobile.pacman.api.commons.utils.ResponseUtils;
import com.tmobile.pacman.api.compliance.domain.PolicyPackDetails;
import com.tmobile.pacman.api.compliance.service.PolicyPackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PolicyPackController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyPackController.class);
    @Autowired
    PolicyPackService policyPackService;

    @GetMapping(path = "/v1/allPolicyPacks")
    @ResponseBody
    public ResponseEntity<Object>  getAllPolicyPacks(){
        try{
            Map<String, Object> response = new HashMap<>();
            List<PolicyPackDetails> policyPackDetailsList = new ArrayList<>();
            policyPackDetailsList = policyPackService.getAllPolicyPacks();
            response.put("response", policyPackDetailsList);
            return ResponseUtils.buildSucessResponse(response);
        }
        catch(Exception exception){
            LOGGER.error(exception.getMessage());
            return ResponseUtils.buildFailureResponse(exception,null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.compliance.repository.PolicyParamsRepository;
import com.tmobile.pacman.api.compliance.repository.model.PolicyParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class PolicyParamServiceImpl implements PolicyParamService, Constants {
    
	private static final Logger logger = LoggerFactory.getLogger(PolicyParamServiceImpl.class);


    @Autowired
    private PolicyParamsRepository policyParamsRepository;


    @Override
    public List<PolicyParams> getPolicyParamsByPolicyId(String policyId) {
        List<PolicyParams> policyParamsList=new ArrayList<>();
        Optional<List<PolicyParams>> policyParams = policyParamsRepository.findByPolicyId(policyId);
        if(policyParams.isPresent()){
            policyParamsList= policyParams.get();
        }else{
            logger.debug("Policy Params not found for policy:{}",policyId);
        }
        return policyParamsList;
    }

    @Override
    public PolicyParams getPolicyParamsByPolicyIdAndKey(String policyId, String policyParamKey) {
       return policyParamsRepository.findByPolicyIdAndKey(policyId, policyParamKey);
    }
}

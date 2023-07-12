package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.compliance.repository.model.PolicyParams;

import java.util.List;

public interface PolicyParamService {

    public List<PolicyParams> getPolicyParamsByPolicyId(String policyId);

    public PolicyParams getPolicyParamsByPolicyIdAndKey(String policyId, String policyParamKey);

}

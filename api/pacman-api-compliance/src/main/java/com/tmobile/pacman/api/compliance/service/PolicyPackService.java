package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.compliance.domain.PolicyPackDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PolicyPackService {
    public List<PolicyPackDetails> getAllPolicyPacks(String dataType);
    public List<Map<String, Object>> getCISControls(String policyPackId) throws Exception;
    public Map<String, Object> getPolicyPackDetailsByID(String policyPackId) throws Exception;
    public List<Map<String, Object>> getPolicyStandard(String policyID) throws Exception;
}

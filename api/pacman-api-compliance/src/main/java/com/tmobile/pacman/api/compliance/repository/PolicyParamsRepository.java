package com.tmobile.pacman.api.compliance.repository;

import com.tmobile.pacman.api.compliance.repository.model.PolicyParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyParamsRepository extends JpaRepository<PolicyParams, Long> {

    Optional<List<PolicyParams>> findByPolicyId(String policyId);

    PolicyParams findByPolicyIdAndKey(String policyId, String policyParamKey);
}

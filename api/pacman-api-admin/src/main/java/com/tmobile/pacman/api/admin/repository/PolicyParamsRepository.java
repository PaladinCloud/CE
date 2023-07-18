package com.tmobile.pacman.api.admin.repository;

import com.tmobile.pacman.api.admin.repository.model.PolicyParams;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyParamsRepository extends JpaRepository<PolicyParams, Long> {

    Optional<List<PolicyParams>> findByPolicyId(String policyId);

    Optional<List<PolicyParams>> findByPolicyIdAndIsEdit(String policyId, String isEdit);
}

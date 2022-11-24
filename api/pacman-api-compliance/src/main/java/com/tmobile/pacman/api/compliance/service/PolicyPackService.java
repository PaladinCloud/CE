package com.tmobile.pacman.api.compliance.service;

import com.tmobile.pacman.api.compliance.domain.PolicyPackDetails;

import java.util.List;

public interface PolicyPackService {
    public List<PolicyPackDetails> getAllPolicyPacks();
}

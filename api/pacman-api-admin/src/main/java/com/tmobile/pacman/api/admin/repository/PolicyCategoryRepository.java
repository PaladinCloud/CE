package com.tmobile.pacman.api.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tmobile.pacman.api.admin.repository.model.PolicyCategory;

/**
 * The Interface PolicyCategoryRepository.
 */
@Repository
public interface PolicyCategoryRepository extends JpaRepository<PolicyCategory, String> {

}

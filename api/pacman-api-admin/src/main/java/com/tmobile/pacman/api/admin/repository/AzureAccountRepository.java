package com.tmobile.pacman.api.admin.repository;

import com.tmobile.pacman.api.admin.repository.model.AzureAccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AzureAccountRepository extends JpaRepository<AzureAccountDetails, String> {
    @Query("SELECT distinct tenant from AzureAccountDetails where subscription like %:filterValue%")
    List<String> findTenantBySubscription(@Param("filterValue") String subscription);

    @Query("SELECT subscription from AzureAccountDetails where tenant like %:filterValue%")
    List<String> findSubscriptionByTenant(@Param("filterValue") String tenant);

    @Query("SELECT distinct subscription from AzureAccountDetails where subscription is NOT NULL ")
    List<String> findSubscriptions();

    @Query("SELECT ac from AzureAccountDetails ac where subscription is NOT NULL and subscriptionStatus='configured'")
    List<AzureAccountDetails> findConfiguredSubscriptions();
}

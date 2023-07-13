package com.tmobile.pacman.api.admin.repository;

import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountsRepository extends JpaRepository<AccountDetails, String> {

    @Query("SELECT a.accountId, a.accountName,a.assets, a.violations ,a.accountStatus, a.platform FROM AccountDetails a WHERE "+
            "LOWER(a.accountId) LIKE %:searchTerm% OR "
            + "LOWER(a.accountName) LIKE %:searchTerm% OR "
            + "LOWER(a.assets) LIKE %:searchTerm% OR "
            + "LOWER(a.violations) LIKE %:searchTerm% OR "
            + "LOWER(a.accountStatus) LIKE %:searchTerm% OR "
            + "LOWER(a.platform) LIKE %:searchTerm% ")
    Page<AccountDetails> findAll(Pageable pageable,@Param("searchTerm") String searchTerm);

    @Query("SELECT a.accountId, a.accountName,a.assets, a.violations ,a.accountStatus, a.platform FROM AccountDetails a WHERE a.accountId LIKE %:filterValue% ")
    Page<AccountDetails> findById(Pageable pageable,@Param("filterValue") String filterValue);

    @Query("SELECT a.accountId, a.accountName,a.assets, a.violations ,a.accountStatus, a.platform FROM AccountDetails a WHERE a.accountName LIKE %:filterValue% ")
    Page<AccountDetails> findByName(Pageable pageable,@Param("filterValue") String filterValue);

    @Query("SELECT a.accountId, a.accountName,a.assets, a.violations ,a.accountStatus, a.platform FROM AccountDetails a WHERE a.assets LIKE %:filterValue% ")
    Page<AccountDetails> findByAssets(Pageable pageable,@Param("filterValue") String filterValue);

    @Query("SELECT a.accountId, a.accountName,a.assets, a.violations ,a.accountStatus, a.platform FROM AccountDetails a WHERE a.violations LIKE %:filterValue% ")
    Page<AccountDetails> findByViolations(Pageable pageable,@Param("filterValue") String filterValue);

    @Query("SELECT a.accountId, a.accountName,a.assets, a.violations ,a.accountStatus, a.platform FROM AccountDetails a WHERE a.accountStatus LIKE %:filterValue% ")
    Page<AccountDetails> findByStatus(Pageable pageable,@Param("filterValue") String filterValue);

    @Query("SELECT a.accountId, a.accountName,a.assets, a.violations ,a.accountStatus, a.platform FROM AccountDetails a WHERE a.platform LIKE %:filterValue% ")
    Page<AccountDetails> findByPlatform(Pageable pageable,@Param("filterValue") String filterValue);

    @Query("select distinct accountId from AccountDetails where accountId is NOT NULL and platform !='azure'")
    List<String> findDistinctAccountId();

    @Query("SELECT CASE WHEN a.platform='azure' THEN azure.subscription ELSE a.accountId END AS accountId, " +
            "CASE WHEN a.platform='azure' THEN azure.subscriptionName ELSE a.accountName END AS accountName, " +
            "CASE WHEN a.platform='azure' THEN azure.assets ELSE a.assets END AS assets, " +
            "CASE WHEN a.platform='azure' THEN azure.violations ELSE a.violations END AS violations , " +
            "a.accountStatus, a.platform, a.createdBy, a.createdTime  FROM AccountDetails a LEFT OUTER JOIN AzureAccountDetails azure " +
            "ON azure.tenant=a.accountId WHERE "+
            "(a.accountId IN (:accountId) OR "
            + "azure.subscription IN (:accountId)) AND "
            + "a.accountName IN (:accountName) AND "
            + "a.assets IN (:assets) AND "
            + "a.violations IN (:violations) AND "
            + "a.accountStatus IN (:status) AND "
            + "a.platform IN (:platform) AND "
            + "a.createdBy IN (:createdBy) "
            +"AND "
            +"(LOWER(a.accountId) LIKE %:searchTerm% OR  "
            + "LOWER(a.accountName) LIKE %:searchTerm% OR "
            + "LOWER(a.assets) LIKE %:searchTerm% OR "
            + "LOWER(a.violations) LIKE %:searchTerm% OR "
            + "LOWER(a.accountStatus) LIKE %:searchTerm% OR "
            + "LOWER(a.platform) LIKE %:searchTerm% OR "
            + "LOWER(a.createdBy) LIKE %:searchTerm% OR "
            + "LOWER(a.createdTime) LIKE %:searchTerm% ) ")
    Page<AccountDetails> findAll(Pageable pageable,  @Param("searchTerm") String searchTerm, @Param("accountName")List<String> accountName, @Param("accountId")List<String> accountId,
                                 @Param("createdBy")List<String> createdBy, @Param("assets") List<String> assets,  @Param("violations") List<String> violations,
                                 @Param("status") List<String> status, @Param("platform") List<String> platform);

    @Query("select distinct accountName from AccountDetails where accountName is NOT NULL ")
    List<String> findDistinctAccountName();

    @Query("select distinct assets from AccountDetails where assets is NOT NULL ")
    List<String> findDistinctAssets();
    @Query("select distinct violations from AccountDetails where violations is NOT NULL ")
    List<String> findDistinctViolations();

    @Query("select distinct platform from AccountDetails where platform is NOT NULL ")
    List<String> findDistinctPlatform();

    @Query("select distinct accountStatus from AccountDetails where accountStatus is NOT NULL ")
    List<String> findDistinctAccountStatus();

    @Query("select distinct createdBy from AccountDetails where createdBy is NOT NULL ")
    List<String> findDistinctCreatedBy();

}

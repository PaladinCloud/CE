package com.tmobile.pacman.api.admin.repository;

import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}

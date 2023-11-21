package com.paladincloud.jobscheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.paladincloud.jobscheduler.repository.model.AccountDetails;

@Repository
public interface AccountsRepository extends JpaRepository<AccountDetails, String> {


    List<AccountDetails> findByAccountStatusAndPlatform(String accountStatus, String platform);
    List<AccountDetails> findByAccountStatus(String accountStatus);
    
    @Query(value = "select distinct platform from cf_Accounts where accountSTatus= :accountStatus and platform in "
    		+ "(select distinct dataSourceName from cf_Target where status ='enabled')", nativeQuery = true)
    List<String> getAccountNameByStatus(@Param("accountStatus")  String accountStatus);

}

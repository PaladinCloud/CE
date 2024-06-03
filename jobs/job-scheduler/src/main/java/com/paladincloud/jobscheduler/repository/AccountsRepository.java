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
    
    @Query(value = "SELECT DISTINCT platform FROM cf_Accounts WHERE accountStatus = 'configured' AND platform IN ( " +
            " SELECT DISTINCT SUBSTRING_INDEX(cfkey, '.', 1) AS `plugin` FROM pac_config_properties " +
            " WHERE application = 'application' AND cfkey IN (:pluginConfigList) " +
            " AND `value` IN ('true', '1'))", nativeQuery = true)
    List<String> getEnabledAccountNameByConfig(@Param("pluginConfigList") List<String> pluginConfigList);

}

package com.paladincloud.jobscheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paladincloud.jobscheduler.repository.model.AccountDetails;

@Repository
public interface AccountsRepository extends JpaRepository<AccountDetails, String> {


    List<AccountDetails> findByAccountStatusAndPlatform(String accountStatus, String platform);
    List<AccountDetails> findByAccountStatus(String accountStatus);

}

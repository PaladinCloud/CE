package com.paladincloud.jobscheduler.repository.service;


import java.util.List;
import com.paladincloud.jobscheduler.repository.model.AccountDetails;


public interface AccountsService {

	List<AccountDetails> getAccountDetailsByPlatFromandStatus(String status, String platform);
}

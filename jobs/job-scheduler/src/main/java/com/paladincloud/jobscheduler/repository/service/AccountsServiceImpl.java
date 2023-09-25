package com.paladincloud.jobscheduler.repository.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paladincloud.jobscheduler.repository.AccountsRepository;
import com.paladincloud.jobscheduler.repository.model.AccountDetails;

@Service
public class AccountsServiceImpl implements AccountsService {
	
    @Autowired
	AccountsRepository accountsRepository;
    
    @Override
    public List<AccountDetails> getAccountDetailsByPlatFromandStatus(String platForm, String status){
    	return accountsRepository.findByAccountStatusAndPlatform(status, platForm);
    }

}

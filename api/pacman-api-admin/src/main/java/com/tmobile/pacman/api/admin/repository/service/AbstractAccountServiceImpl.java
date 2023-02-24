package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.repository.AccountsRepository;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractAccountServiceImpl implements AccountsService{

    @Autowired
    AccountsRepository accountsRepository;
    @Override
    public  List<Map<String,String>> getAllAccounts(String columnName, int page, int size,String searchTerm,String sortOrder) {
        if(sortOrder.equalsIgnoreCase("desc")) {
            return convertToMap(accountsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, columnName)), searchTerm.toLowerCase()));
        }
        else {
            return convertToMap(accountsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, columnName)), searchTerm.toLowerCase()));
        }
    }
    private List<Map<String,String>> convertToMap(Page<AccountDetails> entities) {
        List accountDetailsList= entities.getContent();
        Long elements=entities.getTotalElements();
        List<Map<String,String>> convertAccountDetails=new ArrayList<>();
        for(int i=0;i<accountDetailsList.size();i++){
            Object[] ob= (Object[]) accountDetailsList.get(i);
            Map<String,String > accountMap=new HashMap<>();
            accountMap.put("accountId",ob[0].toString());
            accountMap.put("accountName",ob[1].toString());
            accountMap.put("assets",ob[2].toString());
            accountMap.put("violations",ob[3].toString());
            accountMap.put("accountStatus",ob[4].toString());
            accountMap.put("platform",ob[5].toString());
            convertAccountDetails.add(accountMap);
        }
        Map<String,String > totalElements=new HashMap<>();
        totalElements.put("totalElements",elements.toString());
        convertAccountDetails.add(totalElements);
        return convertAccountDetails;
    }
    @Override
    public  List<Map<String,String>> getAllAccountsByFilter(Integer page, Integer size, String filterName, String filterValue) {
        switch (filterName) {
            case "accountId":  return convertToMap(accountsRepository.findById(PageRequest.of(page, size), filterValue));
            case "accountName":  return convertToMap(accountsRepository.findByName(PageRequest.of(page, size), filterValue));
            case "assets":  return convertToMap(accountsRepository.findByAssets(PageRequest.of(page, size), filterValue));
            case "violations":  return convertToMap(accountsRepository.findByViolations(PageRequest.of(page, size), filterValue));
            case "accountStatus":  return convertToMap(accountsRepository.findByStatus(PageRequest.of(page, size), filterValue));
            case "platform":  return convertToMap(accountsRepository.findByPlatform(PageRequest.of(page, size), filterValue));
        }
        return new ArrayList<>();
    }

    public boolean deleteAccountFromDB(String accountId) {
         accountsRepository.deleteById(accountId);
         return true;
    }

    public boolean createAccountInDb(String accountId, String accountName, String platform) {
        AccountDetails accountDetails=new AccountDetails();
        accountDetails.setAccountId(accountId);
        accountDetails.setViolations("0");
        accountDetails.setAssets("0");
        accountDetails.setAccountName(accountName);
        accountDetails.setPlatform(platform);
        accountDetails.setAccountStatus("configured");
        accountsRepository.save(accountDetails);
        return true;
    }




}

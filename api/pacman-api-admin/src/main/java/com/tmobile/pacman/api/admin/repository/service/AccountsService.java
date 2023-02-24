package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import java.util.List;
import java.util.Map;


public interface AccountsService {

    List<Map<String,String>> getAllAccounts(String columnName, int page, int size,String searchTerm,String sortOrder);

    List<Map<String,String>> getAllAccountsByFilter(Integer page, Integer size, String filterName, String filterValue);

    abstract String serviceType();

    AccountValidationResponse validate(CreateAccountRequest accountData);

    AccountValidationResponse addAccount(CreateAccountRequest accountData);

    AccountValidationResponse deleteAccount(String accountId);
}

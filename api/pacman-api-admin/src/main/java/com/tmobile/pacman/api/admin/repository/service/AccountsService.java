package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.domain.AccountList;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.admin.domain.PluginRequestBody;

import java.util.List;


public interface AccountsService {

    AccountList getAllAccounts(String columnName, int page, int size, String searchTerm, String sortOrder);

    AccountList getAllAccountsByFilter(PluginRequestBody requestBody);
    List<String> getPluginFilterVal(String attribute);

    AccountList getAllAccountsByFilter(Integer page, Integer size, String filterName, String filterValue);

    abstract String serviceType();

    AccountValidationResponse validate(CreateAccountRequest accountData);

    AccountValidationResponse addAccount(CreateAccountRequest accountData);

    AccountValidationResponse deleteAccount(String accountId);
}

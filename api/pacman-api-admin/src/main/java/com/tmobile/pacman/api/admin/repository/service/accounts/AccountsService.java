package com.tmobile.pacman.api.admin.repository.service.accounts;

import com.tmobile.pacman.api.admin.domain.AccountList;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.CreateAccountRequest;
import com.tmobile.pacman.api.admin.domain.PluginParameters;
import com.tmobile.pacman.api.admin.domain.PluginRequestBody;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.commons.Constants;

import java.util.List;
import java.util.Map;


public interface AccountsService {

    AccountList getAllAccounts(String columnName, int page, int size, String searchTerm, String sortOrder);

    AccountList getAllAccountsByFilter(PluginRequestBody requestBody);

    List<String> getPluginFilterVal(String attribute);

    abstract String serviceType();

    AccountValidationResponse validate(CreateAccountRequest accountData);

    AccountValidationResponse addAccount(CreateAccountRequest accountData);

    AccountValidationResponse deleteAccount(PluginParameters parameters);

    List<AccountDetails> findOnlineAccounts(String status, String platform);

    void disableAssetGroup(String pluginName);

    void invokeNotificationAndActivityLogging(PluginParameters parameters, Constants.Actions action);
}

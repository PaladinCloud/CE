package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.domain.PluginResponse;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;

import java.util.List;

public interface PluginsService<T> {

    PluginResponse createPlugin(T request, String createdBy);

    PluginResponse deletePlugin(String accountId);

    PluginResponse validate(T request);

    List<AccountDetails> findOnlineAccounts(String configured, String type);
}

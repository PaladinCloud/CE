/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.pacman.api.admin.repository.service.accounts;

import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.*;
import com.tmobile.pacman.api.admin.repository.AccountsRepository;
import com.tmobile.pacman.api.admin.repository.AzureAccountRepository;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.admin.repository.service.ConfigPropertyService;
import com.tmobile.pacman.api.admin.util.AdminUtils;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractAccountServiceImpl implements AccountsService {

    protected static final String FAILURE = "Failure";
    protected static final String SUCCESS = "Success";
    protected static final String SECRET_ALREADY_EXIST_FOR_ACCOUNT = "Secret already exist for account";
    protected static final String PALADINCLOUD_RO = "PALADINCLOUD_RO";
    protected static final String ERROR_IN_ASSUMING_STS_FOR_BASE_ACCOUNT_ROLE = "Error in assuming sts role, check permission configured for base account role";

    protected static final String TRUE = "true";
    protected static final String FALSE = "false";
    protected static final String JOB_SCHEDULER = "job-scheduler";
    protected static final String STATUS_CONFIGURED = "configured";
    protected static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameters: ";

    private static final Logger logger = LoggerFactory.getLogger(AbstractAccountServiceImpl.class);

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    @Autowired
    protected CredentialProvider credentialProvider;

    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private AzureAccountRepository azureAccountRepository;
    @Autowired
    private ConfigPropertyService configPropertyService;

    @Override
    public AccountList getAllAccounts(String columnName, int page, int size, String searchTerm, String sortOrder) {
        if (sortOrder.equalsIgnoreCase("desc")) {
            return convertToMap(accountsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, columnName)), searchTerm.toLowerCase()));
        } else {
            return convertToMap(accountsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, columnName)), searchTerm.toLowerCase()));
        }
    }

    @Override
    public AccountList getAllAccountsByFilter(PluginRequestBody reqBody) {
        String search = StringUtils.isEmpty(reqBody.getSearchtext()) ? "" : reqBody.getSearchtext();
        Map<String, String> sortOrderFilter = reqBody.getSortFilter();
        String sortElement = AdminConstants.ACCOUNT_ID;
        String sortOrder = AdminConstants.ASC;
        if (sortOrderFilter != null && sortOrderFilter.containsKey(AdminConstants.SORT_ELEMENT)) {
            sortElement = sortOrderFilter.get(AdminConstants.SORT_ELEMENT);
            sortOrder = sortOrderFilter.containsKey(AdminConstants.SORT_ORDER) ? sortOrderFilter.get(AdminConstants.SORT_ORDER) : AdminConstants.ASC;
        }

        if (reqBody.getFilter() == null) {
            return convertToMap(accountsRepository.findAll(PageRequest.of(reqBody.getPage(), reqBody.getSize(),
                            Sort.by(sortOrder.equalsIgnoreCase(AdminConstants.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC, sortElement)),
                    search.toLowerCase()));
        }

        List<String> accountName = getFilterValue(reqBody, AdminConstants.ACCOUNT_NAME);
        List<String> accountId = getFilterValue(reqBody, AdminConstants.ACCOUNT_ID);
        List<String> createdBy = getFilterValue(reqBody, AdminConstants.CREATED_BY);
        List<String> asset = getFilterValue(reqBody, AdminConstants.ASSET);
        List<String> violations = getFilterValue(reqBody, AdminConstants.VIOLATIONS);
        List<String> status = getFilterValue(reqBody, AdminConstants.ACCOUNT_STATUS);
        List<String> platform = getFilterValue(reqBody, AdminConstants.PLATFORM);

        return convertToMap(accountsRepository.findAll(PageRequest.of(reqBody.getPage(), reqBody.getSize(), sortOrder.equalsIgnoreCase(AdminConstants.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC, sortElement),
                search, accountName, accountId, createdBy, asset, violations, status, platform));
    }

    @Override
    public List<String> getPluginFilterVal(String attribute) {
        try {
            switch (attribute) {
                case AdminConstants.ACCOUNT_ID:
                    List<String> accountList = accountsRepository.findDistinctAccountId();
                    accountList.addAll(azureAccountRepository.findSubscriptions());
                    return accountList;
                case AdminConstants.ACCOUNT_NAME:
                    return accountsRepository.findDistinctAccountName();
                case AdminConstants.ASSET:
                    return accountsRepository.findDistinctAssets();
                case AdminConstants.VIOLATIONS:
                    return accountsRepository.findDistinctViolations();
                case AdminConstants.STATUS:
                    return accountsRepository.findDistinctAccountStatus();
                case AdminConstants.CREATED_BY:
                    return accountsRepository.findDistinctCreatedBy();
                case AdminConstants.PLATFORM:
                    return accountsRepository.findDistinctPlatform();
                default:
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccountDetails> findOnlineAccounts(String status, String platform) {
        return accountsRepository.findByAccountStatusAndPlatform(status, platform);
    }

    private List<String> getFilterValue(PluginRequestBody reqBody, String accountName) {
        return ((reqBody.getFilter() != null) && (reqBody.getFilter().get(accountName) != null))
                ? (List<String>) reqBody.getFilter().get(accountName) : getPluginFilterVal(accountName);
    }

    private AccountList convertToMap(Page<AccountDetails> entities) {
        List accountDetailsList = entities.getContent();
        List<Map<String, String>> convertAccountDetails = new ArrayList<>();
        for (int i = 0; i < accountDetailsList.size(); i++) {
            Object[] ob = (Object[]) accountDetailsList.get(i);
            Map<String, String> accountMap = new HashMap<>();
            accountMap.put("accountId", ob[0].toString());
            accountMap.put("accountName", ob[1].toString());
            accountMap.put("assets", ob[2].toString());
            accountMap.put("violations", ob[3].toString());
            accountMap.put("accountStatus", ob[4].toString());
            accountMap.put("source", ob[5].toString());
            accountMap.put("createdBy", ob[6] != null ? ob[6].toString() : "");
            accountMap.put("createdTime", ob[7] != null ? formatCreatedTime(ob[7]) : "");
            convertAccountDetails.add(accountMap);
        }

        AccountList accountList = new AccountList();
        accountList.setResponse(convertAccountDetails);

        Long totalElements = entities.getTotalElements();
        accountList.setTotal(totalElements);

        return accountList;
    }

    protected AccountValidationResponse deleteAccountFromDB(String accountId) {
        AccountValidationResponse response = new AccountValidationResponse();
        response.setAccountId(accountId);
        try {
            accountsRepository.deleteById(accountId);
            response.setMessage("Account deleted successfully");
            response.setValidationStatus(SUCCESS);
        } catch (EmptyResultDataAccessException exception) {
            logger.error("Error in deleting account: {}", exception, exception.getMessage());
            response.setValidationStatus(FAILURE);
            response.setErrorDetails("Account doesn't exists");
            response.setMessage("Account deletion failed");
        }

        return response;
    }

    protected AccountValidationResponse createAccountInDb(String accountId, String accountName, String platform, String createdBy) {
        AccountValidationResponse response = new AccountValidationResponse();
        response.setType(platform);
        response.setAccountId(accountId);
        response.setAccountName(accountName);

        Optional<AccountDetails> account = accountsRepository.findById(accountId);
        if (account.isPresent()) {
            logger.error("Account already present:{}", account.get().getAccountId());
            response.setErrorDetails("Account already exists.");
            response.setValidationStatus(FAILURE);
            response.setMessage("Account already exists.");
        } else {
            AccountDetails accountDetails = new AccountDetails();
            accountDetails.setAccountId(accountId);
            accountDetails.setViolations("0");
            accountDetails.setAssets("0");
            accountDetails.setAccountName(accountName);
            accountDetails.setPlatform(platform);
            accountDetails.setAccountStatus(STATUS_CONFIGURED);
            accountDetails.setCreatedBy(createdBy);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            accountDetails.setCreatedTime(formatter.format(date));
            accountsRepository.save(accountDetails);
            response.setValidationStatus(SUCCESS);
            response.setMessage("Account added successfully.");
        }

        return response;
    }

    protected void updateConfigProperty(String key, String value, String application) {
        ConfigPropertyItem config = new ConfigPropertyItem();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setApplication(application);

        List<ConfigPropertyItem> configList = new ArrayList<>();
        configList.add(config);

        ConfigPropertyRequest configPropertyRequest = new ConfigPropertyRequest();
        configPropertyRequest.setConfigProperties(configList);
        configPropertyService.addUpdateProperties(configPropertyRequest, "", "",
                AdminUtils.getFormatedStringDate(DATE_FORMAT, new Date()), false);
    }

    private String formatCreatedTime(Object createdTimeObject) {
        String createdTimeString = createdTimeObject.toString();
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Set the input time zone to UTC
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        outputFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Set the output time zone to UTC
        try {
            Date date = inputFormat.parse(createdTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return "Error in FormatCreatedTime";
        }
    }

}

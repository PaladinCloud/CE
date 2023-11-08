package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.*;
import com.tmobile.pacman.api.admin.repository.AccountsRepository;
import com.tmobile.pacman.api.admin.repository.AzureAccountRepository;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.admin.util.AdminUtils;
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


public abstract class AbstractAccountServiceImpl implements AccountsService{

    @Autowired
    AccountsRepository accountsRepository;

    @Autowired
    AzureAccountRepository azureAccountRepository;

    @Autowired
    ConfigPropertyService configPropertyService;
    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";
    public static final String SECRET_ALREADY_EXIST_FOR_ACCOUNT = "Secret already exist for account";
    public static final String PALADINCLOUD_RO = "PALADINCLOUD_RO";
    public static final String ERROR_IN_ASSUMING_STS_FOR_BASE_ACCOUNT_ROLE = "Error in assuming sts role, check permission configured for base account role";

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String JOB_SCHEDULER = "job-scheduler";
    public static final String STATUS_CONFIGURED = "configured";


    private static final Logger logger=LoggerFactory.getLogger(AbstractAccountServiceImpl.class);
    @Override
    public AccountList getAllAccounts(String columnName, int page, int size, String searchTerm, String sortOrder) {
        if(sortOrder.equalsIgnoreCase("desc")) {
            return convertToMap(accountsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, columnName)), searchTerm.toLowerCase()));
        }
        else {
            return convertToMap(accountsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, columnName)), searchTerm.toLowerCase()));
        }
    }

    @Override
    public AccountList getAllAccountsByFilter(PluginRequestBody reqBody) {
        String search = StringUtils.isEmpty(reqBody.getSearchtext()) ? "" : reqBody.getSearchtext();
        Map<String, String> sortOrderFilter = reqBody.getSortFilter();
        String sortElement = AdminConstants.ACCOUNT_ID;
        String sortOrder = AdminConstants.ASC;
        if(sortOrderFilter != null &&  sortOrderFilter.containsKey(AdminConstants.SORT_ELEMENT)){
            sortElement=  sortOrderFilter.get(AdminConstants.SORT_ELEMENT);
            sortOrder = sortOrderFilter.containsKey(AdminConstants.SORT_ORDER) ? sortOrderFilter.get(AdminConstants.SORT_ORDER) :  AdminConstants.ASC;
        }
        if(reqBody.getFilter() == null){
            return convertToMap(accountsRepository.findAll(PageRequest.of(reqBody.getPage(), reqBody.getSize(),
                            Sort.by(sortOrder.equalsIgnoreCase(AdminConstants.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC, sortElement)),
                    search.toLowerCase()));
        }
        List<String> accountName = ((reqBody.getFilter() != null) && (reqBody.getFilter().get(AdminConstants.ACCOUNT_NAME) != null))
                ? (List<String>) reqBody.getFilter().get(AdminConstants.ACCOUNT_NAME) : getPluginFilterVal(AdminConstants.ACCOUNT_NAME);
        List<String> accountId = ((reqBody.getFilter() != null) && (reqBody.getFilter().get(AdminConstants.ACCOUNT_ID) != null))
                ? (List<String>) reqBody.getFilter().get(AdminConstants.ACCOUNT_ID) : getPluginFilterVal(AdminConstants.ACCOUNT_ID);
        List<String> createdBy =  ((reqBody.getFilter() != null) &&  (reqBody.getFilter().get(AdminConstants.CREATED_BY) != null))
                ? (List<String>) reqBody.getFilter().get(AdminConstants.CREATED_BY) : getPluginFilterVal(AdminConstants.CREATED_BY);
        List<String> asset = ((reqBody.getFilter() != null) && (reqBody.getFilter().get(AdminConstants.ASSET) != null))
                ? (List<String>) reqBody.getFilter().get(AdminConstants.ASSET) : getPluginFilterVal(AdminConstants.ASSET);
        List<String> violations = ((reqBody.getFilter() != null) && (reqBody.getFilter().get(AdminConstants.VIOLATIONS) != null))
                ? (List<String>) reqBody.getFilter().get(AdminConstants.VIOLATIONS) : getPluginFilterVal(AdminConstants.VIOLATIONS);
        List<String> status = ((reqBody.getFilter() != null) && (reqBody.getFilter().get(AdminConstants.ACCOUNT_STATUS) != null))
                ?  (List<String>) reqBody.getFilter().get(AdminConstants.ACCOUNT_STATUS) : getPluginFilterVal(AdminConstants.STATUS);
        List<String> platform = ((reqBody.getFilter() != null) && (reqBody.getFilter().get(AdminConstants.PLATFORM) != null))
                ?  (List<String>) reqBody.getFilter().get(AdminConstants.PLATFORM) : getPluginFilterVal(AdminConstants.PLATFORM);

        return convertToMap(accountsRepository.findAll(PageRequest.of(reqBody.getPage(), reqBody.getSize(), sortOrder.equalsIgnoreCase(AdminConstants.ASC) ? Sort.Direction.ASC : Sort.Direction.DESC, sortElement),
                search, accountName, accountId, createdBy, asset, violations, status, platform));
    }

    private AccountList convertToMap(Page<AccountDetails> entities) {
        AccountList accountList=new AccountList();
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
            accountMap.put("source",ob[5].toString());
            accountMap.put("createdBy",ob[6]!=null?ob[6].toString():"");
            accountMap.put("createdTime",ob[6]!=null?formatCreatedTime(ob[7]):"");
            convertAccountDetails.add(accountMap);
        }
        accountList.setResponse(convertAccountDetails);
        accountList.setTotal(elements);
        return accountList;
    }

    public AccountValidationResponse deleteAccountFromDB(String accountId) {
        AccountValidationResponse response=new AccountValidationResponse();
        response.setAccountId(accountId);
        try {
            accountsRepository.deleteById(accountId);
            response.setMessage("Account deleted successfully");
            response.setValidationStatus(SUCCESS);
        }catch (EmptyResultDataAccessException exception){
            logger.error("Error in deleting account: {}",exception);
            response.setValidationStatus(FAILURE);
            response.setErrorDetails("Account doesn't exists");
            response.setMessage("Account deletion failed");
        }
         return response;
    }

    public AccountValidationResponse createAccountInDb(String accountId, String accountName, String platform,String createdBy) {
        AccountDetails accountDetails=new AccountDetails();
        accountDetails.setAccountId(accountId);
        accountDetails.setViolations("0");
        accountDetails.setAssets("0");
        accountDetails.setAccountName(accountName);
        accountDetails.setPlatform(platform);
        accountDetails.setAccountStatus("configured");
        accountDetails.setCreatedBy(createdBy);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        accountDetails.setCreatedTime(formatter.format(date));
        AccountValidationResponse response=new AccountValidationResponse();
        response.setType(platform);
        response.setAccountId(accountId);
        response.setAccountName(accountName);

        Optional<AccountDetails> account = accountsRepository.findById(accountId);
        if(account.isPresent()){
            logger.error("Account already present:{}",account.get().getAccountId());
            response.setErrorDetails("Account already exists.");
            response.setValidationStatus(FAILURE);
            response.setMessage("Account already exists.");
        }else {
            accountsRepository.save(accountDetails);
            response.setValidationStatus(SUCCESS);
            response.setMessage("Account added successfully.");
        }
        return response;
    }
    public void updateConfigProperty(String key, String value, String application) {
        ConfigPropertyRequest configPropertyRequest=new ConfigPropertyRequest();
        ConfigPropertyItem config=new ConfigPropertyItem();
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setApplication(application);
        List<ConfigPropertyItem> configList=new ArrayList<>();
        configList.add(config);
        configPropertyRequest.setConfigProperties(configList);
        configPropertyService.addUpdateProperties(configPropertyRequest, "", "",
                AdminUtils.getFormatedStringDate(DATE_FORMAT, new Date()), false);
    }
    public String getSecretData(String secret){
        String jsonTemplate="{\"secretdata\": \"%s\"}";
        return String.format(jsonTemplate,secret);
    }

    @Override
    public List<String> getPluginFilterVal(String attribute){
        try{
            switch (attribute) {
                case AdminConstants.ACCOUNT_ID:
                    List<String> accountList= accountsRepository.findDistinctAccountId();
                    accountList.addAll(azureAccountRepository.findSubscriptions());
                    return accountList;
                case AdminConstants.ACCOUNT_NAME:  return accountsRepository.findDistinctAccountName();
                case AdminConstants.ASSET:  return accountsRepository.findDistinctAssets();
                case AdminConstants.VIOLATIONS:  return accountsRepository.findDistinctViolations();
                case AdminConstants.STATUS:  return accountsRepository.findDistinctAccountStatus();
                case AdminConstants.CREATED_BY: return accountsRepository.findDistinctCreatedBy();
                case AdminConstants.PLATFORM: return accountsRepository.findDistinctPlatform();
                default: return new ArrayList<String>();
            }
        }catch (Exception e){
            return new ArrayList<>();
        }
    }

    @Override
    public List<AccountDetails> findOnlineAccounts(String status, String platform){
        return accountsRepository.findByAccountStatusAndPlatform(status,platform);
    }

    private String formatCreatedTime(Object createdTimeObject) {
        if (createdTimeObject == null) {
            return "";
        }
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

    protected List<AccountDetails> getListAccountsByPlatform(String platform) {
        return accountsRepository.findByPlatform(platform);
    }

}

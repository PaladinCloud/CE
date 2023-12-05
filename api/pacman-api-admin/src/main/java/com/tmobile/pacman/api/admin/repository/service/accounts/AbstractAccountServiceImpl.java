package com.tmobile.pacman.api.admin.repository.service.accounts;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.amazonaws.services.secretsmanager.model.ResourceExistsException;
import com.tmobile.pacman.api.admin.common.AdminConstants;
import com.tmobile.pacman.api.admin.domain.AccountList;
import com.tmobile.pacman.api.admin.domain.AccountValidationResponse;
import com.tmobile.pacman.api.admin.domain.ConfigPropertyItem;
import com.tmobile.pacman.api.admin.domain.ConfigPropertyRequest;
import com.tmobile.pacman.api.admin.domain.PluginRequestBody;
import com.tmobile.pacman.api.admin.repository.AccountsRepository;
import com.tmobile.pacman.api.admin.repository.AzureAccountRepository;
import com.tmobile.pacman.api.admin.repository.model.AccountDetails;
import com.tmobile.pacman.api.admin.repository.service.*;
import com.tmobile.pacman.api.admin.util.AdminUtils;
import com.tmobile.pacman.api.commons.config.CredentialProvider;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractAccountServiceImpl implements AccountsService {

    @Autowired
    AccountsRepository accountsRepository;

    @Autowired
    AzureAccountRepository azureAccountRepository;
    @Autowired
    PacmanRdsRepository rdsRepository;
    @Autowired
    ConfigPropertyService configPropertyService;
    @Autowired
    protected CredentialProvider credentialProvider;
    @Autowired
    private DataCollectorSQSService dataCollectorSQSService;
    @Autowired
    protected AssetGroupService assetGroupService;
    @Autowired
    private UserPreferencesService userPreferencesService;
    @Value("${secret.manager.path}")
    private String secretManagerPrefix;
    public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
    public static final String SECRET_ALREADY_EXIST_FOR_ACCOUNT = "Secret already exist for account";
    public static final String PALADINCLOUD_RO = "PALADINCLOUD_RO";
    public static final String ERROR_IN_ASSUMING_STS_FOR_BASE_ACCOUNT_ROLE = "Error in assuming sts role, check permission configured for base account role";

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String JOB_SCHEDULER = "job-scheduler";
    public static final String STATUS_CONFIGURED = "configured";

    protected static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";
    protected static final String FAILURE = "FAILURE";
    protected static final String SUCCESS = "SUCCESS";

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
        Map<String, String> sortOrderFilter = reqBody.getSortFilter();
        String sortElement = AdminConstants.ACCOUNT_ID;
        String sortOrder = AdminConstants.ASC;
        if (sortOrderFilter != null && sortOrderFilter.containsKey(AdminConstants.SORT_ELEMENT)) {
            sortElement = sortOrderFilter.get(AdminConstants.SORT_ELEMENT).equalsIgnoreCase("assets") ||
                    sortOrderFilter.get(AdminConstants.SORT_ELEMENT).equalsIgnoreCase("violations")
                    ? "cast(" + sortOrderFilter.get(AdminConstants.SORT_ELEMENT) + " as decimal(10,2))" : sortOrderFilter.get(AdminConstants.SORT_ELEMENT);
            sortOrder = sortOrderFilter.containsKey(AdminConstants.SORT_ORDER) ? sortOrderFilter.get(AdminConstants.SORT_ORDER) : AdminConstants.ASC;
        }
        String accountQuery = fetchQueryForAccountsFilterApplied(reqBody, sortOrder, sortElement);
        List<Map<String, Object>> responseList = rdsRepository.getDataFromPacman(accountQuery);
        List<AccountDetails> accountDetailsList = new ArrayList<>();
        responseList.stream().forEach(obj -> {
            AccountDetails accountDetails = new AccountDetails(
                    (String) (obj.get("accountId") == null ? "N/A" : obj.get("accountId")),
                    (String) (obj.get("accountName") == null ? "N/A" : obj.get("accountName")),
                    (String) (obj.get("assets") == null ? "0" : obj.get("assets")),
                    (String) (obj.get("violations") == null ? "0" : obj.get("violations")),
                    (String) (obj.get("accountStatus") == null ? "N/A" : obj.get("accountStatus")),
                    (String) (obj.get("platform") == null ? "N/A" : obj.get("platform")),
                    (String) (obj.get("createdBy") == null ? "N/A" : obj.get("createdBy")),
                    (Timestamp) (obj.get("createdTime")));
            accountDetailsList.add(accountDetails);
        });
        return convertToMap(new PageImpl<>(accountDetailsList));
    }

    private String fetchQueryForAccountsFilterApplied(PluginRequestBody reqBody, String sortOrder, String sortElement) {
        String tempQuery = "SELECT CASE WHEN a.platform='azure' THEN azure.subscription ELSE a.accountId END AS accountId," +
                "CASE WHEN a.platform='azure' THEN azure.subscriptionName ELSE a.accountName END AS accountName, " +
                "CASE WHEN a.platform='azure' THEN azure.assets ELSE a.assets END AS assets, " +
                "CASE WHEN a.platform='azure' THEN azure.violations ELSE a.violations END AS violations , " +
                "CASE WHEN a.platform='azure' THEN azure.subscriptionStatus ELSE a.accountStatus END AS accountStatus," +
                "a.platform as platform , a.createdBy as createdBy, a.createdTime as createdTime  FROM cf_Accounts a LEFT OUTER JOIN cf_AzureTenantSubscription azure" +
                "ON azure.tenant=a.accountId) AS plugins";
        String query;
        if (reqBody.getAttributeName() == null || reqBody.getAttributeName().isEmpty()) {
            query = "SELECT accountId, accountName, assets, violations, accountStatus, platform, createdBy, createdTime FROM(";
        } else {
            query = "SELECT DISTINCT " + reqBody.getAttributeName() + " FROM(";
        }
        StringBuilder accountQuery = new StringBuilder(query + tempQuery);

        if (reqBody.getFilter() != null && !reqBody.getFilter().isEmpty()) {
            StringBuilder clauseStr = new StringBuilder(" WHERE ");
            for (Map.Entry<String, Object> entry : reqBody.getFilter().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    StringBuilder queryForAttribute = new StringBuilder("");
                    if (entry.getKey().toString().equalsIgnoreCase("assets") || entry.getKey().toString().equalsIgnoreCase("violations")) {
                        List<Map<String, Object>> rangeList = (List<Map<String, Object>>) entry.getValue();
                        String queryWithDiffRangeOfEachAttribute = rangeList.stream().map(range -> "CAST(" + entry.getKey() + " AS decimal(10,2)) BETWEEN " + range.get("min") + " AND " + range.get("max"))
                                .collect(Collectors.joining(" OR "));
                        queryForAttribute = new StringBuilder("(").append(queryWithDiffRangeOfEachAttribute).append(")");
                    } else {
                        List<String> inpValues = (List<String>) entry.getValue();
                        queryForAttribute = new StringBuilder("" + entry.getKey().toString() + " IN (" + createCombinedStrWithCommaDelimiter(inpValues) + ")");
                    }
                    accountQuery = accountQuery.append(clauseStr).append(queryForAttribute);
                    clauseStr = new StringBuilder(" AND ");
                }
            }
        }
        accountQuery = accountQuery.append(" ORDER BY " + sortElement + " " + sortOrder);
        if (reqBody.getSize() > 0) {
            accountQuery = accountQuery.append(" LIMIT " + reqBody.getPage() + ", " + reqBody.getSize());
        }
        return accountQuery == null ? "" : accountQuery.toString();
    }

    private AccountList convertToMap(Page<AccountDetails> entities) {
        AccountList accountList=new AccountList();
        List accountDetailsList= entities.getContent();
        Long elements=entities.getTotalElements();
        List<Map<String,String>> convertAccountDetails=new ArrayList<>();
        for(int i=0;i<accountDetailsList.size();i++){
            Map<String,String > accountMap=new HashMap<>();
            if(accountDetailsList.get(i) instanceof AccountDetails){
                AccountDetails accountDetails = (AccountDetails) accountDetailsList.get(i);
                accountMap.put("accountId", accountDetails.getAccountId());
                accountMap.put("accountName", accountDetails.getAccountName());
                accountMap.put("assets", accountDetails.getAssets());
                accountMap.put("violations", accountDetails.getViolations());
                accountMap.put("accountStatus", accountDetails.getAccountStatus());
                accountMap.put("source", accountDetails.getPlatform());
                accountMap.put("createdBy", accountDetails.getCreatedBy());
                accountMap.put("createdTime", accountDetails.getCreatedTime() != null ?formatCreatedTime(accountDetails.getCreatedTime()) : "");
            }
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
        accountDetails.setCreatedTime(Timestamp.valueOf(LocalDateTime.now(Clock.systemUTC())));
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return dtf.format(((Timestamp)createdTimeObject).toLocalDateTime());
    }

    protected List<AccountDetails> getListAccountsByPlatform(String platform) {
        return accountsRepository.findByPlatform(platform);
    }

    protected void sendSQSMessage(String pluginName, String accountId, String tenantId) {
        try {
            List<AccountDetails> onlineAccounts = findOnlineAccounts(STATUS_CONFIGURED, pluginName);
            /* Send SQS message to DataCollector SQS to trigger collector, mapper, shipper */
            dataCollectorSQSService.sendSQSMessage(pluginName, tenantId, onlineAccounts);
        } catch (ResourceExistsException ree) {
            logger.error("Secret key for plugin already exists", ree);
            deleteSecret(accountId, pluginName, tenantId);
            deleteAccountFromDB(accountId);
        } catch (Exception e) {
            logger.error("Error occured while creating secret key for {}", pluginName, e);
            deleteAccountFromDB(accountId);
        }
    }

    protected DeleteSecretResult deleteSecret(String accountId, String plugin, String tenantId) {
        BasicSessionCredentials credentials = credentialProvider.getBaseAccCredentials();
        Regions region = Regions.fromName(System.getenv("REGION"));
        AWSSecretsManager secretClient = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(region).build();
        DeleteSecretRequest deleteRequest = new DeleteSecretRequest().withSecretId(secretManagerPrefix + "/" +
                tenantId + "/" + plugin + "/" + accountId).withForceDeleteWithoutRecovery(true);
        DeleteSecretResult deleteResponse = secretClient.deleteSecret(deleteRequest);
        logger.info("Delete secret response: {} ", deleteResponse);
        return deleteResponse;
    }

    protected String getSecretId(String id, String pluginName, String tenantId) {
        return secretManagerPrefix + "/" + tenantId + "/" + pluginName + "/" + id;
    }

    public void disableAssetGroup(String pluginName) {
        try {
            List<AccountDetails> onlineAccounts = findOnlineAccounts("configured", pluginName);
            if (!Objects.isNull(onlineAccounts) && !onlineAccounts.isEmpty()) {
                logger.info("There are {} online account(s). " +
                        "Therefore, no updates to asset groups are required.", onlineAccounts.size());
                return;
            }
            boolean deleteAssetGroupStatus = assetGroupService.deleteAssetGroupByGroupName(pluginName);
            logger.info("AssetGroup deletion status is {} for {}", deleteAssetGroupStatus, pluginName);
            Integer totalUsers = userPreferencesService.updateDefaultAssetGroup(pluginName);
            logger.info("Total users have been updated with the default asset group. The new count is: {}", totalUsers);
            assetGroupService.removePluginTypeFromAllSources(pluginName);
        } catch (Exception e) {
            logger.error("Unable to disable asset groups for {}", pluginName, e);
        }
    }

    private static String createCombinedStrWithCommaDelimiter(List<String> list) {
        if(CollectionUtils.isEmpty(list)){
            return "";
        }
        return String.join(",", list
                .stream()
                .map(name -> ("\"" + name + "\""))
                .collect(Collectors.toList()));
    }
}

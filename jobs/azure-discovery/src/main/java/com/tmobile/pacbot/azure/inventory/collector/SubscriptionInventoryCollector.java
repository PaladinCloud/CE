package com.tmobile.pacbot.azure.inventory.collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.Permission;
import com.microsoft.azure.management.graphrbac.RoleDefinition;
import com.microsoft.azure.management.storage.StorageAccount;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.RoleDefinitionVH;
import com.tmobile.pacbot.azure.inventory.vo.StorageAccountActivityLogVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import com.tmobile.pacman.commons.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class SubscriptionInventoryCollector {

    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    private static final Logger log = LoggerFactory.getLogger(SubscriptionInventoryCollector.class);

    public List<SubscriptionVH> fetchSubscriptions(SubscriptionVH subscription){

        List<SubscriptionVH> subscriptionList = new ArrayList<>();

        SubscriptionVH subscriptionVH = new SubscriptionVH();
        subscriptionVH.setTenant(subscription.getTenant());
        subscriptionVH.setSubscriptionId(subscription.getSubscriptionId());
        subscriptionVH.setId(subscription.getSubscriptionId());
        subscriptionVH.setSubscriptionName(subscription.getSubscriptionName());
        subscriptionVH.setSubscription(subscription.getSubscriptionId());

        subscriptionVH.setStorageAccountLogList(fetchStorageAccountActivityLog(subscriptionVH));
        subscriptionVH.setRoleDefinitionList(fetchAzureRoleDefinition(subscriptionVH));


        subscriptionList.add(subscriptionVH);

        log.info("Size of subscriptions: {}", subscriptionList.size());
        return subscriptionList;
    }

    private List<StorageAccountActivityLogVH>fetchStorageAccountActivityLog(SubscriptionVH subscription){

        List<StorageAccountActivityLogVH>storageAccountActivityLogVHList=new ArrayList<>();

        try{
            String apiUrlTemplate="https://management.azure.com/%s/providers/Microsoft.Insights/diagnosticSettings?api-version=2021-05-01-preview";
            String accessToken = azureCredentialProvider.getToken(subscription.getTenant());
            Azure azure = azureCredentialProvider.authenticate(subscription.getTenant(),
                    subscription.getSubscriptionId());
            PagedList<StorageAccount> storageAccounts = azure.storageAccounts().list();
            String url = String.format(apiUrlTemplate, URLEncoder.encode("/subscriptions/"+subscription.getSubscriptionId(),java.nio.charset.StandardCharsets.UTF_8.toString()));
            log.info("The url is {}",url);

            String response = CommonUtils.doHttpGet(url, "Bearer", accessToken);
            log.info("Response is :{}",response);
            JsonObject responseObj = new JsonParser().parse(response).getAsJsonObject();
            JsonArray storageObjects = responseObj.getAsJsonArray("value");

            for(JsonElement storageObjElement:storageObjects){
                StorageAccountActivityLogVH storageAccountActivityLogVH=new StorageAccountActivityLogVH();
                JsonObject  storageObject = storageObjElement.getAsJsonObject();
                JsonObject properties = storageObject.getAsJsonObject("properties");
                log.debug("Properties data{}",properties);
                if(properties!=null) {
                    String storageAccountId=properties.get("storageAccountId").getAsString();
                    storageAccountActivityLogVH.setStorageAccountActivityLogContainerId(storageAccountId);
                    storageAccountActivityLogVH.setStorageAccountEncryptionKeySource(checkStorageAccountEncryptionKeySource(storageAccountId,storageAccounts));
                }
                storageAccountActivityLogVHList.add(storageAccountActivityLogVH);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return storageAccountActivityLogVHList;
    }

    private String checkStorageAccountEncryptionKeySource(String storageAccountId, PagedList<StorageAccount> storageAccounts) {

        String result="";

        for (StorageAccount storageAccount : storageAccounts) {
            if(storageAccountId.equalsIgnoreCase(storageAccount.id())){
                return storageAccount.encryptionKeySource().toString();
            }
        }

        return result;
    }

    private List<RoleDefinitionVH>fetchAzureRoleDefinition(SubscriptionVH subscription){
        List<RoleDefinitionVH>roleDefinitionVHList=new ArrayList<>();

        try{
            Azure azure = azureCredentialProvider.getClient(subscription.getTenant(),subscription.getSubscriptionId());

            PagedList<RoleDefinition>roleDefinitions=azure.accessManagement().roleDefinitions().listByScope(subscription.getSubscriptionId());

            for(RoleDefinition roleDefinition:roleDefinitions){
                RoleDefinitionVH roleDefinitionVH=new RoleDefinitionVH();

                String roleName=roleDefinition.roleName();
                log.debug("Role Name{}",roleName);
                roleDefinitionVH.setRoleName(roleName);

                Set<String>assignableScopes=roleDefinition.assignableScopes();
                log.debug("Assignable Scopes size{}",assignableScopes.size());
                roleDefinitionVH.setAssignableScopes(assignableScopes);

                Set<Permission>permissions= azure.accessManagement().roleDefinitions().getByScopeAndRoleName(subscription.getSubscriptionId(),roleName).permissions();
                log.debug("Permissions size{}",permissions.size());

                Iterator<Permission>permissionIterator=permissions.iterator();

                while(permissionIterator.hasNext()){

                  List<String>actions= permissionIterator.next().actions();
                  log.debug("Action size{}",actions.size());

                  roleDefinitionVH.setActions(actions);
                }


                roleDefinitionVHList.add(roleDefinitionVH);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return  roleDefinitionVHList;
    }
}


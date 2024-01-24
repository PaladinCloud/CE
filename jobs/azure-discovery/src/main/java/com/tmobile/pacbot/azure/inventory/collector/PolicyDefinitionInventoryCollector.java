package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.PolicyDefinition;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.PolicyDefinitionVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tmobile.pacbot.azure.inventory.util.InventoryConstants.REGION_GLOBAL;

@Component
@EnableCaching
public class PolicyDefinitionInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(PolicyDefinitionInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    @Cacheable("PolicyDefinitionVH")
    public List<PolicyDefinitionVH> collect(SubscriptionVH subscription) {
        List<PolicyDefinitionVH> policyDefinitionList = new ArrayList<>();
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<PolicyDefinition> policyDefinitions = azure.policyDefinitions().list();
        for (PolicyDefinition policyDefinition : policyDefinitions) {
            PolicyDefinitionVH policyDefinitionVH = new PolicyDefinitionVH();
            policyDefinitionVH.setId(policyDefinition.id());
            policyDefinitionVH.setName(policyDefinition.name());
            policyDefinitionVH.setDescription(policyDefinition.description());
            policyDefinitionVH.setDisplayName(policyDefinition.displayName());
            policyDefinitionVH.setPolicyType(policyDefinition.policyType().toString());
            policyDefinitionVH.setPolicyRule(policyDefinition.policyRule().toString());
            policyDefinitionVH.setSubscription(subscription.getSubscriptionId());
            policyDefinitionVH.setSubscriptionName(subscription.getSubscriptionName());
            policyDefinitionVH.setRegion(REGION_GLOBAL);
            policyDefinitionList.add(policyDefinitionVH);
        }
        log.info("Target Type : {}  Total: {} ", "Policy Definition", policyDefinitionList.size());
        return policyDefinitionList;
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        throw new UnsupportedOperationException();
    }
}

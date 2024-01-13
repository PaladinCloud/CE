package com.tmobile.pacbot.azure.inventory.collector;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Disk;
import com.tmobile.pacbot.azure.inventory.auth.AzureCredentialProvider;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import com.tmobile.pacbot.azure.inventory.vo.DataDiskVH;
import com.tmobile.pacbot.azure.inventory.vo.SubscriptionVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DiskInventoryCollector implements Collector {

    private static final Logger log = LoggerFactory.getLogger(DiskInventoryCollector.class);
    @Autowired
    AzureCredentialProvider azureCredentialProvider;

    @Override
    public List<? extends AzureVH> collect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends AzureVH> collect(SubscriptionVH subscription) {
        throw new UnsupportedOperationException();
    }

    public List<DataDiskVH> collect(SubscriptionVH subscription, Map<String, Map<String, String>> tagMap) {
        List<DataDiskVH> dataDiskList = new ArrayList<DataDiskVH>();
        Azure azure = azureCredentialProvider.getClient(subscription.getTenant(), subscription.getSubscriptionId());
        PagedList<Disk> dataDisks = azure.disks().list();

        for (Disk dataDisk : dataDisks) {
            DataDiskVH dataDiskVH = new DataDiskVH();
            dataDiskVH.setId(dataDisk.id());
            dataDiskVH.setIsAttachedToVirtualMachine(dataDisk.isAttachedToVirtualMachine());
            dataDiskVH.setKey(dataDisk.key());
            dataDiskVH.setName(dataDisk.name());
            dataDiskVH.setDiskInner(dataDisk.inner());
            dataDiskVH.setRegion(Util.getRegionValue(subscription, dataDisk.region().toString()));
            dataDiskVH.setResourceGroupName(dataDisk.resourceGroupName());
            dataDiskVH.setSizeInGB(dataDisk.sizeInGB());
            dataDiskVH.setTags(Util.tagsList(tagMap, dataDisk.resourceGroupName(), dataDisk.tags()));
            dataDiskVH.setType(dataDisk.type());
            dataDiskVH.setVirtualMachineId(dataDisk.virtualMachineId());
            dataDiskVH.setSubscription(subscription.getSubscriptionId());
            dataDiskVH.setSubscriptionName(subscription.getSubscriptionName());
            dataDiskList.add(dataDiskVH);
        }
        log.info("Target Type : {}  Total: {} ", "Disc", dataDiskList.size());
        return dataDiskList;
    }
}

package com.tmobile.pacbot.azure.inventory.vo;

import com.microsoft.azure.management.storage.StorageAccountEncryptionKeySource;

public class StorageAccountActivityLogVH extends AzureVH{

    private String storageAccountActivityLogContainerId;

    private String storageAccountEncryptionKeySource;



    public String getStorageAccountActivityLogContainerId() {
        return storageAccountActivityLogContainerId;
    }

    public void setStorageAccountActivityLogContainerId(String storageAccountActivityLogContainerId) {
        this.storageAccountActivityLogContainerId = storageAccountActivityLogContainerId;
    }

    public String getStorageAccountEncryptionKeySource() {
        return storageAccountEncryptionKeySource;
    }

    public void setStorageAccountEncryptionKeySource(String storageAccountEncryptionKeySource) {
        this.storageAccountEncryptionKeySource = storageAccountEncryptionKeySource;
    }


}

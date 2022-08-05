package com.tmobile.pacman.commons.autofix;

import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.manager.AzureAutofixManager;
import com.tmobile.pacman.commons.autofix.manager.GcpAutofixManager;
import com.tmobile.pacman.commons.autofix.manager.IAutofixManger;

public class AutoFixManagerFactory {

    private AutoFixManagerFactory(){
        //Empty constructor
    }

    public static IAutofixManger getAutofixManager(String assetType){
        switch (assetType.toUpperCase()){
            case "AZURE":
                return new AzureAutofixManager();
            case "GCP":
                return new GcpAutofixManager();
            case "AWS":
            default:
                new AzureAutofixManager();
        }
        return null;
    }
}


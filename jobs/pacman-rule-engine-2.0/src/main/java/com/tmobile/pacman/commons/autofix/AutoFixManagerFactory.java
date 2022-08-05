package com.tmobile.pacman.commons.autofix;

import com.tmobile.cloud.constants.PacmanRuleConstants;
import com.tmobile.pacman.common.PacmanSdkConstants;
import com.tmobile.pacman.commons.autofix.manager.AwsAutofixManager;
import com.tmobile.pacman.commons.autofix.manager.AzureAutofixManager;
import com.tmobile.pacman.commons.autofix.manager.GcpAutofixManager;
import com.tmobile.pacman.commons.autofix.manager.IAutofixManger;

public class AutoFixManagerFactory {

    private AutoFixManagerFactory(){
        //Empty constructor
    }

    public static IAutofixManger getAutofixManager(String assetType){
        IAutofixManger autofixManger=null;
        switch (assetType.toUpperCase()){
            case "AZURE":
                autofixManger= new AzureAutofixManager();
                break;
            case "GCP":
                autofixManger= new GcpAutofixManager();
                break;
            case "AWS":
            default:
                autofixManger=new AwsAutofixManager();
        }
        return autofixManger;
    }
}


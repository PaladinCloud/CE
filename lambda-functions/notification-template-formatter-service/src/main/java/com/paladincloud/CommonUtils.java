package com.paladincloud;

import static com.paladincloud.Constants.APPROVE_EXEMPTION_REQUEST;
import static com.paladincloud.Constants.CANCEL_EXEMPTION_REQUEST;
import static com.paladincloud.Constants.CREATE;
import static com.paladincloud.Constants.CREATE_EXEMPTION_REQUEST;
import static com.paladincloud.Constants.REVOKE;
import static com.paladincloud.Constants.REVOKE_EXEMPTION_REQUEST;

public class CommonUtils {

    private static String getViolationTemplateName(String channelName, String action){
        String templateName="";
        if("create".equalsIgnoreCase(action)){
            switch(channelName){
                case "slack":
                    templateName= "openViolationsSlackTemplate.html";
                    break;
                case "jira":
                    templateName= "openViolationsJiraTemplate.html";
                    break;
                default:
                    templateName= "openViolationsEmailTemplate.html";
                    break;
            }
        }
        else{
            switch(channelName){
                default:
                    templateName= "closeViolationsEmailTemplate.html";
                    break;
            }
        }
        return templateName;
    }

    private static String getExemptionTemplateName(String channelName, String action, String exemptionType){
        if("sticky".equalsIgnoreCase(exemptionType)){
            return getStickyExTemplateName(channelName,action);
        }
        else{
            return getIndividualExTemplateName(action);
        }
    }

    private static String getIndividualExTemplateName(String action) {
        switch (action.toLowerCase()) {
            case CREATE:
                return "createIndividualExEmailTemplate.html";
            default:
            case REVOKE:
                return "revokeIndividualExEmailTemplate.html";
            case CREATE_EXEMPTION_REQUEST:
                return "createExemptionRequestEmailTemplate.html";
            case REVOKE_EXEMPTION_REQUEST:
                return "revokeExemptionRequestEmailTemplate.html";
            case CANCEL_EXEMPTION_REQUEST:
                return "cancelExemptionRequestEmailTemplate.html";
            case APPROVE_EXEMPTION_REQUEST:
                return "approveExemptionRequestEmailTemplate.html";
        }
    }

    private static String getStickyExTemplateName(String channelName, String action) {
        String templateName="";
        if("create".equalsIgnoreCase(action)){
            switch(channelName){
                default:
                    templateName= "createStickyExEmailTemplate.html";
                    break;
            }
        }
        else if("update".equalsIgnoreCase(action)){
            switch(channelName){
                default:
                    templateName= "updateStickyExEmailTemplate.html";
                    break;
            }
        }
        else{
            switch(channelName){
                default:
                    templateName= "deleteStickyExEmailTemplate.html";
                    break;
            }
        }
        return templateName;
    }


    public static String getTemplateName(String channelName, String action, String notificationType, String exemptionType){

        switch(notificationType){
            case "violations":
                return getViolationTemplateName(channelName,action);
            case "exemptions":
                return getExemptionTemplateName(channelName,action,exemptionType);
            case "autofix":
                return getAutofixTemplateName(channelName,action);
        }
        return null;
    }

    private static String getAutofixTemplateName(String channelName, String action) {
        String templateName = "";
        switch(channelName){
            case "email":
                if(Constants.AutoFixAction.AUTOFIX_ACTION_EMAIL.toString().equalsIgnoreCase(action)) {
                    templateName= "autofixWarningNotification.html";
                }
                else if(Constants.AutoFixAction.AUTOFIX_ACTION_FIX.toString().equalsIgnoreCase(action)) {
                    templateName= "autofixAppliedNotification.html";
                }
                else if(Constants.AutoFixAction.AUTOFIX_ACTION_EXEMPTED.toString().equalsIgnoreCase(action)) {
                    templateName= "autofixExemptedForViolation.html";
                }
                break;
        }
        return templateName;
    }
}

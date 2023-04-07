package com.paladincloud.notification_log.common;

public interface Constants {

     String CONFIG_SERVICE_URL = "CONFIG_SERVICE_URL";

    String CONFIG_CREDENTIALS = "CONFIG_CREDENTIALS";
    String MISSING_CONFIGURATION = "Missing value in the env configuration";
    String NAME = "name";
    String SOURCE = "source";
    String MISSING_DB_CONFIGURATION = "Missing db configurations";
    /** The pacman host. */
    String PACMAN_HOST = "pacman.host";
    /** The pacman login user name. */
    String PACMAN_LOGIN_USER_NAME = "pacman.login.user.name";
    String PACMAN_LOGIN_PASSWORD = "pacman.login.password";
    String API_READ_SCOPE = "API_OPERATION/READ";
    String API_AUTH_INFO = "apiauthinfo";
    /** The auth header. */
    String AUTH_HEADER = "Authorization";
    
    
    enum NotificationTypes {
        EXEMPTIONS("Exemption"),
        VIOLATIONS("Violation"),
        AUTOFIX ("AutoFix"),
        ISSUE("Issue"),
        ACCOUNTNOTIFICATION("AccountNotification"),
    	SCHEDULEDCHANGE("ScheduledChange"),
    	INVESTIGATION("Investigation");
    	
    	private String value;
    	
    	NotificationTypes(String value){
    		this.value = value;
    	}
    	public String getValue() {
    		return value;
    	}
    	
    	
    }
}


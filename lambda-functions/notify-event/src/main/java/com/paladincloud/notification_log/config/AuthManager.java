package com.paladincloud.notification_log.config;

import java.util.HashMap;
import java.util.Map;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paladincloud.notification_log.common.Constants;
import com.paladincloud.notification_log.util.CommonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthManager {

    static final Logger LOGGER = LoggerFactory.getLogger(AuthManager.class);

    private static final String AUTH_API_URL = System.getenv("AUTH_API_URL");
    private static final String API_READ_SCOPE = "API_OPERATION/READ";
    private static final String API_AUTH_INFO = "apiauthinfo";

    private static AccessToken accessToken ;

    private AuthManager(){

    }
    private static void  authorise() throws Exception{


        try{
            Gson serializer = new GsonBuilder().create();
            String loginUrl = CommonUtils.getPropValue(Constants.PACMAN_HOST)+"/api/auth/user/login";
            Map<String,String> creds = new HashMap<>();
            creds.put("password", CommonUtils.getPropValue(Constants.PACMAN_LOGIN_PASSWORD));
            creds.put("username", CommonUtils.getPropValue(Constants.PACMAN_LOGIN_USER_NAME));
            String credentials = CommonUtils.getPropValue(API_AUTH_INFO);
            String response =null;
            if(credentials!=null){
                response = CommonUtils.postUrlEncoded(AUTH_API_URL+"/oauth2/token?grant_type=client_credentials&scope="+API_READ_SCOPE,
                        "",credentials,"Basic");
            }else {
                response = CommonUtils.doHttpPost(loginUrl, serializer.toJson(creds), new HashMap<>());
            }
           //.info("Called Authorise");

            if(null!=response && response.contains("error")){
                LOGGER.info("Login Url: {}",loginUrl);
                LOGGER.error("unexpected response from auth api: {}",response);
            }
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            String token = jsonObject.get("access_token").getAsString();
            String expiresIn = jsonObject.get("expires_in").getAsString(); // In seconds
            if( token!=null){
                long tokenExpiresAt = System.currentTimeMillis() + Long.valueOf(expiresIn)*1000 - (20*1000) ; // 20 second buffer
                accessToken = new AccessToken(token, tokenExpiresAt);
            }

        }catch (Exception e) {
            LOGGER.error("error while getting API token",e);
        }

    }

    public static String getToken(){
        if(!isTokenValid()){
            try {
                authorise();
            } catch (Exception e) {
              LOGGER.error("Authorisation Failed",e);
            }
        }
        if(accessToken!=null)
            return accessToken.getToken();
        else
            return "";
    }

    private static boolean isTokenValid(){
        return accessToken !=null && accessToken.getExpiresAt() > System.currentTimeMillis();
    }

}

class AccessToken {
    private String token;
    private long expiresAt;

    AccessToken(String token, long expiresAt){
        this.token = token;
        this.expiresAt = expiresAt;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public long getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    public String toString(){
        return "Token:"+token+" ,ExpiresIn (sec)"+ (expiresAt- System.currentTimeMillis())/1000;
    }

}

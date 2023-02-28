package com.tmobile.cso.pacman.aqua.jobs;

import com.google.gson.JsonObject;
import com.tmobile.cso.pacman.aqua.util.HttpUtil;
import com.tmobile.cso.pacman.aqua.util.Util;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AquaDataImporter {

  protected static final String BASE_API_URL ="https://api.cloudsploit.com";
      //System.getProperty("aqua_api_url");

  protected static final String userName ="mahidhar.jalumuru@zemosolabs.com";
      //System.getProperty("aqua_username");

  protected static final String password ="5761M&Mlu";
      //System.getProperty("aqua_password");

  abstract public Map<String, Object> execute();

  /** The api map. */
  Map<String, String> apiMap = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(AquaDataImporter.class);


  public AquaDataImporter() {
    apiMap = new HashMap<String, String>();
    apiMap.put("signIn",
        "/v2/signin");
    apiMap.put("image_vulnerabilities",
        "/api/v2/risks/vulnerabilities");
    apiMap.put("vm_vulnerabilities", "/api/v2/risks/functions/vulnerabilities");
    apiMap.put("hostassetcount", "/qps/rest/2.0/count/am/hostasset");
  }
  public String getBearerToken(){
    String token = null;
    String tokenUri = BASE_API_URL + apiMap.get("signIn");
    JsonObject inputObject = new JsonObject();
      inputObject.addProperty("email", userName);
    inputObject.addProperty("password", password);
    String input = inputObject.toString();
    try {
      String response = HttpUtil.post(tokenUri,input,null , null);
      Map data = (Map) Util.getJsonAttribute(response, "data");
      token = (String) data.get("token");
    } catch (Exception e) {
      LOGGER.error("error in fetching aqua bearer token {}",e);
    }
    return token;
  }
}

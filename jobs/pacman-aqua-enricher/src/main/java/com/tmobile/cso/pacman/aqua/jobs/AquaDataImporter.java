package com.tmobile.cso.pacman.aqua.jobs;

import com.google.gson.JsonObject;
import com.tmobile.cso.pacman.aqua.exception.AquaDataImportException;
import com.tmobile.cso.pacman.aqua.util.HttpUtil;
import com.tmobile.cso.pacman.aqua.util.Util;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AquaDataImporter {

  protected static final String BASE_API_URL = System.getProperty("aqua_api_url");

  protected static final String userName = System.getProperty("aqua_username");

  protected static final String password = System.getProperty("aqua_password");

  abstract public Map<String, Object> execute();

  /** The api map. */
  Map<String, String> apiMap = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(AquaDataImporter.class);


  protected AquaDataImporter() {
    apiMap = new HashMap<String, String>();
    apiMap.put("signIn",
        "/v2/signin");
    apiMap.put("image_vulnerabilities",
        "/api/v2/risks/vulnerabilities");
    apiMap.put("vm_vulnerabilities", "/api/v2/risks/functions/vulnerabilities");
    apiMap.put("hostassetcount", "/qps/rest/2.0/count/am/hostasset");
  }
  public String getBearerToken() throws AquaDataImportException {
    String token = null;
    String tokenUri = BASE_API_URL + apiMap.get("signIn");
    JsonObject inputObject = new JsonObject();
      inputObject.addProperty("email", userName);
    inputObject.addProperty("password", password);
    String input = inputObject.toString();
    try {
      String response = HttpUtil.post(tokenUri,input,null , null);
      Map<String,Object> data = (Map) Util.getJsonAttribute(response, "data");
      token = (String) data.get("token");
    } catch (Exception e) {
      throw new AquaDataImportException(e.getMessage());
    }
    return token;
  }
}

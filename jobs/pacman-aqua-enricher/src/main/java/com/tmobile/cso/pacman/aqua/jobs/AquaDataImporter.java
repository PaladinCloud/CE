package com.tmobile.cso.pacman.aqua.jobs;

import com.amazonaws.auth.BasicSessionCredentials;
import com.google.gson.JsonObject;
import com.tmobile.cso.pacman.aqua.auth.CredentialProvider;
import com.tmobile.cso.pacman.aqua.exception.AquaDataImportException;
import com.tmobile.cso.pacman.aqua.util.HttpUtil;
import com.tmobile.cso.pacman.aqua.util.Util;
import com.tmobile.pacman.commons.secrets.AwsSecretManagerUtil;
import java.util.HashMap;
import java.util.Map;

public abstract class AquaDataImporter {

  protected static String BASE_API_URL = null;

  AwsSecretManagerUtil secretManagerUtil=new AwsSecretManagerUtil();

  CredentialProvider credentialProvider=new CredentialProvider();

  protected String secretManagerPrefix=System.getProperty("secret.manager.path");

  /** The Constant DEFAULT_USER. */
  private static String DEFAULT_USER;

  /** The Constant DEFAULT_PASS. */
  private static String DEFAULT_PASS;

  protected static String AQUA_CLIENT_DOMAIN_URL;


  abstract public Map<String, Object> execute();

  protected  String baseAccount =System.getProperty("base.account");
  protected  String baseRegion =System.getProperty("base.region");
  protected  String roleName =System.getProperty("s3.role");

  /** The api map. */
  Map<String, String> apiMap = null;

  protected AquaDataImporter() {
    apiMap = new HashMap<String, String>();
    apiMap.put("signIn",
        "/v2/signin");
    apiMap.put("image_vulnerabilities",
        "/api/v2/risks/vulnerabilities");
    apiMap.put("vm_vulnerabilities", "/api/v2/risks/functions/vulnerabilities");
    apiMap.put("hostassetcount", "/qps/rest/2.0/count/am/hostasset");
    getAquaInfo();
  }

  private void getAquaInfo() {
    BasicSessionCredentials credential = credentialProvider.getBaseAccountCredentials(baseAccount, baseRegion, roleName);
    secretManagerPrefix="paladincloud/secret";
    String secretData=secretManagerUtil.fetchSecret(secretManagerPrefix+"/aqua",credential,baseRegion);
    Map<String, String> dataMap = Util.getJsonData(secretData);
    DEFAULT_USER=dataMap.get("apiusername");
    DEFAULT_PASS=dataMap.get("apipassword");
    BASE_API_URL=dataMap.get("aquaApiUrl");
    AQUA_CLIENT_DOMAIN_URL= dataMap.get("aquaClientDomainUrl");
  }

  public String getBearerToken() throws AquaDataImportException {
    String token = null;
    String tokenUri = BASE_API_URL + apiMap.get("signIn");
    JsonObject inputObject = new JsonObject();
    inputObject.addProperty("email", DEFAULT_USER);
    inputObject.addProperty("password", DEFAULT_PASS);

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

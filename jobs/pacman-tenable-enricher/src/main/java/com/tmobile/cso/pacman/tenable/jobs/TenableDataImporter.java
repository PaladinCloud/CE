package com.tmobile.cso.pacman.tenable.jobs;

import com.amazonaws.auth.BasicSessionCredentials;
import com.tmobile.cso.pacman.tenable.auth.CredentialProvider;
import com.tmobile.cso.pacman.tenable.util.Util;
import com.tmobile.pacman.commons.secrets.AwsSecretManagerUtil;
import java.util.HashMap;
import java.util.Map;

public abstract class TenableDataImporter {


  abstract public Map<String, Object> execute();

  Map<String, String> apiMap = null;

  AwsSecretManagerUtil secretManagerUtil = new AwsSecretManagerUtil();

  CredentialProvider credentialProvider = new CredentialProvider();

  protected String secretManagerPrefix = System.getProperty("secret.manager.path");

  protected String baseAccount = System.getProperty("base.account");

  protected String baseRegion = System.getProperty("base.region");

  protected String roleName = System.getProperty("s3.role");

  protected static String ACCESS_KEY;

  protected static String SECRET_KEY;

  protected static String USER_AGENT;

  protected static String TENABLE_API_URL;

  protected static String API_KEYS;




  protected TenableDataImporter() {
    apiMap = new HashMap<String, String>();
    apiMap.put("assetsCount",
        "/assets");


    apiMap.put("assetsExportTrigger",
        "/assets/export");
    apiMap.put("assetsExportStatus",
        "/assets/export/{export_id}/status");
    apiMap.put("assetsExport",
        "/assets/export/{export_id}/chunks/{chunk_id}");

    apiMap.put("vulnerabilityExportTrigger",
        "/vulns/export");
    apiMap.put("vulnerabilityExportStatus",
        "/vulns/export/{export_id}/status");
    apiMap.put("vulnerabilityExport",
        "/vulns/export/{export_id}/chunks/{chunk_id}");

    getTenableInfo();
  }


  private void getTenableInfo() {
    BasicSessionCredentials credential = credentialProvider.getBaseAccountCredentials(baseAccount, baseRegion, roleName);
    secretManagerPrefix = "paladincloud/secret";
    String secretData = secretManagerUtil.fetchSecret(secretManagerPrefix + "/tenable", credential, baseRegion);
    Map<String, String> dataMap = Util.getJsonData(secretData);
    ACCESS_KEY = dataMap.get("accessKey");
    SECRET_KEY = dataMap.get("secretKey");
    TENABLE_API_URL = dataMap.get("apiURL");
    USER_AGENT = dataMap.get("userAgent");
    API_KEYS = "accessKey="+ACCESS_KEY+";secretKey="+SECRET_KEY+";";
  }
}

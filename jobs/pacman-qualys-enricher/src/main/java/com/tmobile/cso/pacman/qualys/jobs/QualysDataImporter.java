package com.tmobile.cso.pacman.qualys.jobs;

import com.amazonaws.auth.BasicSessionCredentials;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tmobile.cso.pacman.qualys.auth.CredentialProvider;
import com.tmobile.cso.pacman.qualys.util.Util;
import com.tmobile.pacman.commons.secrets.AwsSecretManagerUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * The Class QualysDataImporter.
 */
public abstract class QualysDataImporter {


    AwsSecretManagerUtil secretManagerUtil=new AwsSecretManagerUtil();

    CredentialProvider credentialProvider=new CredentialProvider();

    protected String secretManagerPrefix=System.getProperty("secret.manager.path");

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(QualysDataImporter.class);
    
    /** The Constant DEFAULT_USER. */
    private static String DEFAULT_USER;
    
    /** The Constant DEFAULT_PASS. */
    private static String DEFAULT_PASS;
    
    /** The Constant UTF8. */
    private static final String UTF8 = "UTF-8";
    
    /** The api map. */
    protected Map<String, String> apiMap;

    /**
     * Gets the api map.
     *
     * @return the api map
     */
    public Map<String, String> getApiMap() {
        return apiMap;
    }

    /**
     * Sets the api map.
     *
     * @param apiMap the api map
     */
    public void setApiMap(Map<String, String> apiMap) {
        this.apiMap = apiMap;
    }

    /** The Constant BASE_API_URL. */
    protected static String BASE_API_URL;

    protected  String baseAccount =System.getProperty("base.account");
    protected  String baseRegion =System.getProperty("base.region");
    protected  String roleName =System.getProperty("s3.role");

    /**
     * Instantiates a new qualys data importer.
     */
    public QualysDataImporter() {
        apiMap = new HashMap<String, String>();
        apiMap.put("hostList",
                "/api/2.0/fo/asset/host?action=list&use_tags=1&tag_set_by=name&tag_set_include=Cloud%20Agent");
        apiMap.put("listKnowledgebase",
                "/api/2.0/fo/knowledge_base/vuln/?action=list&details=All&show_pci_reasons=1&show_supported_modules_info=1");
        apiMap.put("hostAssetSearch", "/qps/rest/2.0/search/am/hostasset");
        apiMap.put("hostassetcount", "/qps/rest/2.0/count/am/hostasset");
        getQualysInfo();


    }

    private void getQualysInfo() {
        BasicSessionCredentials credential = credentialProvider.getBaseAccountCredentials(baseAccount, baseRegion, roleName);
        secretManagerPrefix="paladincloud/secret";
        String secretData=secretManagerUtil.fetchSecret(secretManagerPrefix+"/qualys",credential,baseRegion);
        Map<String, String> dataMap = Util.getJsonData(secretData);
        DEFAULT_USER=dataMap.get("apiusername");
        DEFAULT_PASS=dataMap.get("apipassword");
        BASE_API_URL=dataMap.get("qualysApiUrl");
    }

    /**
     * Call api.
     *
     * @param uri the uri
     * @param httpMethod the http method
     * @param xmlToPost the xml to post
     * @param parameters the parameters
     * @return the string
     * @throws ClientProtocolException the client protocol exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("deprecation")
    protected static String callApi(String uri, String httpMethod, String xmlToPost,
            List<BasicNameValuePair> parameters) throws ClientProtocolException, IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        if ("GET".equals(httpMethod)) {
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("content-type", "application/xml");
            httpGet.addHeader("cache-control", "no-cache");
            httpGet.addHeader("Accept", "application/json");
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(DEFAULT_USER, DEFAULT_PASS);
            httpGet.addHeader(BasicScheme.authenticate(credentials, UTF8, false));
            httpGet.addHeader("X-Requested-With", "DEFAULT_USER");
            LOGGER.debug("GET API call URL:{}", uri);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            LOGGER.debug("Response code:{}",httpResponse.getStatusLine().getStatusCode());
            return EntityUtils.toString(httpResponse.getEntity());
        } else if ("POST".equals(httpMethod)) {

            HttpPost post = new HttpPost(uri);
            post.addHeader("content-type", "application/xml");
            post.addHeader("cache-control", "no-cache");
            post.addHeader("Accept", "application/json");
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(DEFAULT_USER, DEFAULT_PASS);
            post.addHeader(BasicScheme.authenticate(creds, UTF8, false));
            if (xmlToPost != null) {
                HttpEntity entity = new ByteArrayEntity(xmlToPost.getBytes(UTF8));
                post.setEntity(entity);
            }

            if (parameters != null) {
                post.setEntity(new UrlEncodedFormEntity(parameters));
            }
            LOGGER.debug("POST API call URL:{}, input XML:{}", uri, xmlToPost);
            HttpResponse response = httpClient.execute(post);
            LOGGER.debug("Response code:{}",response.getStatusLine().getStatusCode());
            return EntityUtils.toString(response.getEntity());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Builds the XML reader.
     *
     * @param xmlContent the xml content
     * @return the XML stream reader
     * @throws XMLStreamException the XML stream exception
     */
    protected XMLStreamReader buildXMLReader(final String xmlContent) throws XMLStreamException {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final StringReader reader = new StringReader(xmlContent);
        return inputFactory.createXMLStreamReader(reader);
    }

    /**
     * Gets the service response.
     *
     * @param result the result
     * @return the service response
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getServiceResponse(String result) {
        Map<String, Object> serviceResponse = new Gson().fromJson(result, new TypeToken<Map<String, Object>>() {
        }.getType());
        if (serviceResponse != null)
            return (Map<String, Object>) serviceResponse.get("ServiceResponse");
        return null;
    }

    /**
     * Gets the host data.
     *
     * @param uriPost the uri post
     * @param inputXml the input xml
     * @return the host data
     */
    @SuppressWarnings("unchecked")
    protected List<Map<String, Object>> getHostData(String uriPost, String inputXml) {
        LOGGER.debug("getHostdata API URL:{}, RequestXML:{}",uriPost,inputXml);
        String resultJson = null;
        int retryCnt = 3;
        for (int i = 1; i <= retryCnt; i++) { // Retry 2 times if error occurs
            try {
                resultJson = callApi(uriPost, "POST", inputXml, null);
                LOGGER.debug("Response JSON: {}",resultJson);
            } catch (IOException e) {
                if (i == retryCnt) {
                    LOGGER.error("Error in fetching host info: Request :{}", inputXml);
                }
            }
            if (resultJson != null) {
                Map<String, Object> resp = getServiceResponse(resultJson);
                LOGGER.debug("Service response from resultJaon:{}",resp);
                if (resp != null && "SUCCESS".equals(resp.get("responseCode"))) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) resp.get("data");
                    if (data != null) {
                        return data.stream().map(obj -> (Map<String, Object>) obj.get("HostAsset"))
                                .collect(Collectors.toList());
                    }
                    break;
                } else {
                    if (i == retryCnt) {
                        LOGGER.error("Error in fetching host info: Request :{}", inputXml);
                        LOGGER.error("Response : {}", new Gson().toJson(resultJson));
                    }
                }
            }
        }

        return null;
    }
}

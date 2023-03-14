package com.tmobile.cso.pacman.qualys.jobs;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import com.tmobile.cso.pacman.qualys.dto.KNOWLEDGEBASEVULNLISTOUTPUT;
import com.tmobile.cso.pacman.qualys.dto.Vuln;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmobile.cso.pacman.qualys.Constants;
import com.tmobile.cso.pacman.qualys.util.ElasticSearchManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static com.tmobile.cso.pacman.qualys.jobs.QualysDataImporter.callApi;


/**
 * The Class HostAssetsEsIndexer.
 */
@SuppressWarnings("unchecked")
public class HostAssetsEsIndexer implements Constants {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(HostAssetsEsIndexer.class);
    private static final String BASE_API_URL = System.getProperty("qualys_api_url");
    private static final String kbGetUri = BASE_API_URL+"/api/2.0/fo/knowledge_base/vuln/?action=list&ids=%s";

    /**
     * Post host asset to ES.
     *
     * @param qualysInfo the qualys info
     * @param type the type
     */
    public void postHostAssetToES(Map<String, Map<String, Object>> qualysInfo, String ds,String type,List<Map<String,String>> errorList) {
        LOGGER.info("Uploading");
        String index = ds+"_" + type;
        ElasticSearchManager.createType(index, "qualysinfo", type);
        ElasticSearchManager.createType(index, "vulninfo", type);

        String createTemplate = "{ \"index\" : { \"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\", \"_parent\" : \"%s\" } }%n";

        Iterator<Entry<String, Map<String, Object>>> it = qualysInfo.entrySet().iterator();
        int i = 0;
        StringBuilder createRequest = new StringBuilder();
        StringBuilder vulnRequest = new StringBuilder();

        while (it.hasNext()) {
            Entry<String, Map<String, Object>> entry = it.next();
            String parent = entry.getKey();
            Map<String, Object> asset = entry.getValue();
            String assetDoc = createESDoc(asset,errorList);
            createRequest.append(String.format(createTemplate, index, "qualysinfo", asset.get(DOC_ID), parent));
            createRequest.append(assetDoc + "\n");
            List<Map<String, Object>> vulnInfo = fetchVulnInfo(asset,errorList);
            if (!CollectionUtils.isNullOrEmpty(vulnInfo)) {
                for (Map<String, Object> vuln : vulnInfo) {
                    vulnRequest
                            .append(String.format(createTemplate, index, "vulninfo", vuln.get("@id"), parent));
                    vuln.remove("@id");
                    vulnRequest.append(createESDoc(vuln,errorList) + "\n");
                }
            }
            i++;

            if (i % 50 == 0) {
                bulkUpload(createRequest.toString(),errorList);
                bulkUpload(vulnRequest.toString(),errorList);
                createRequest = new StringBuilder();
                vulnRequest = new StringBuilder();
            }
        }

        if (createRequest.length() > 0) {
           
            bulkUpload(createRequest.toString(),errorList);
        }
        if (vulnRequest.length() > 0) {
            bulkUpload(vulnRequest.toString(),errorList);
        }

    }

    /**
     * Bulk upload.
     *
     * @param bulkRequest the bulk request
     */
    private void bulkUpload(String bulkRequest,List<Map<String,String>> errorList) {
        try {
            Response resp = ElasticSearchManager.invokeAPI("POST", "/_bulk", bulkRequest);
            String responseStr = EntityUtils.toString(resp.getEntity());
            if (responseStr.contains("\"errors\":true")) {
                LOGGER.error(responseStr);
            }
        } catch (IOException e) {
            LOGGER.error("BulkUpload Failed", e);
            Map<String,String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "BulkUpload Failed");
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
        }
    }

    /**
     * Creates the ES doc.
     *
     * @param asset the asset
     * @return the string
     */
    private String createESDoc(Object asset,List<Map<String,String>> errorList) {
        ObjectMapper objMapper = new ObjectMapper();
        try {
            return objMapper.writeValueAsString(asset);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unexpected Error:", e);
            Map<String,String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Unexpected Error:");
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
        }
        return null;
    }

    /**
     * Fetch vuln info.
     *
     * @param asset the asset
     * @return the list
     */
    private List<Map<String, Object>> fetchVulnInfo(Map<String, Object> asset,List<Map<String,String>> errorList) {
        List<Map<String, Object>> vulnInfoList = new ArrayList<>();
        try {
            Map<String, Map<String, Object>> vulnMap = (Map<String, Map<String, Object>>) asset.get("vuln");
            if (vulnMap != null) {
                List<Map<String, Object>> vulnList = (List<Map<String, Object>>) vulnMap.get("list");
                if (vulnList != null) {
                    for (Map<String, Object> hostvuln : vulnList) {
                        Map<String, Object> vuln = new HashMap<>((Map<String, Object>) hostvuln.get("HostAssetVuln"));
                        List<String> kbaseDetailsList = getKbDetailsOfQid(vuln.get("qid").toString());
                        if(!kbaseDetailsList.isEmpty() && kbaseDetailsList.get(0)!=null &&  Long.valueOf(kbaseDetailsList.get(0))>=3 && "Vulnerability".equals(kbaseDetailsList.get(1))) {
                                vuln.put(DOC_ID, asset.get(DOC_ID));
                                vuln.put("discoverydate", asset.get("discoverydate"));
                                vuln.put("severity", "S" + kbaseDetailsList.get(0));
                                vuln.put("@id", asset.get(DOC_ID).toString() + "_" + vuln.get("qid").toString());
                                vuln.put("latest", true);
                                vuln.put("_resourceid", asset.get("_resourceid"));

                                vuln.put("title", kbaseDetailsList.get(3)+";"+kbaseDetailsList.get(4)+";"+kbaseDetailsList.get(2));
                                Object firstFound = vuln.get("firstFound");
                                Object lastFound = vuln.get("lastFound");
        
                                Object _firstFound = null;
                                Object _lastFound = null;
                                vuln.put("_vulnage", Util.calculteAgeInDays(firstFound, lastFound));
        
                                if (firstFound != null) {
                                    _firstFound = firstFound;
                                }
        
                                if (lastFound != null) {
                                    _lastFound = lastFound;
                                } else {
                                    _lastFound = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date());
                                }
        
                                if (_firstFound == null) {
                                    _firstFound = _lastFound;
                                }
                                vuln.put("_firstFound", _firstFound);
                                vuln.put("_lastFound", _lastFound);
        
                                vulnInfoList.add(vuln);
                        }
                    }    
                }
            }
        } catch (Exception e) {
            LOGGER.error("fetchVulnInfo Failed", e);
            Map<String,String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Unexpected Error:");
            errorMap.put(ERROR_TYPE, FATAL);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
        }
        return vulnInfoList;
    }

    /**
     * getKbDetailsOfQid
     *
     * @param qid of violation reported by qualys.
     * @return  list, first element of list will be severity level, second element will be vulnerability type
     * and third element will be violation title.
     */
    private List<String> getKbDetailsOfQid(String qid) {
        List<String> kbDetailsList = new ArrayList<>();
        try {
            double dblValue = Double.valueOf(qid);
            long longValue=(long)dblValue;
            String resultXML = callApi(String.format(kbGetUri,String.valueOf(longValue)), "GET", null, null);
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            final StringReader reader = new StringReader(resultXML);
            XMLStreamReader xreader = inputFactory.createXMLStreamReader(reader);
            JAXBContext jaxbContext = JAXBContext.newInstance(KNOWLEDGEBASEVULNLISTOUTPUT.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            KNOWLEDGEBASEVULNLISTOUTPUT resp = (KNOWLEDGEBASEVULNLISTOUTPUT) jaxbUnmarshaller.unmarshal(xreader);
            KNOWLEDGEBASEVULNLISTOUTPUT.RESPONSE.VULNLIST vulnList = resp.getRESPONSE().getVULNLIST();
            if (vulnList != null && vulnList.getVULN() != null && vulnList.getVULN().get(0) != null) {
                KNOWLEDGEBASEVULNLISTOUTPUT.RESPONSE.VULNLIST.VULN vulnInfo = vulnList.getVULN().get(0);
                kbDetailsList.add(String.valueOf(vulnInfo.getSEVERITYLEVEL()));
                kbDetailsList.add(vulnInfo.getVULNTYPE());
                kbDetailsList.add(vulnInfo.getTITLE());
                kbDetailsList.add(vulnInfo.getCVELIST().getCVE().get(0).getID());
                kbDetailsList.add(vulnInfo.getCVELIST().getCVE().get(0).getURL());
            } else {
                return Collections.emptyList();
            }
        } catch (XMLStreamException exception) {
            LOGGER.debug("XMLStreamException occurred in getKbDetailsOfQid method of HostAssetsEsIndexer");
            return Collections.emptyList();
        } catch (JAXBException e) {
            LOGGER.debug("JAXBException occurred in getKbDetailsOfQid method of HostAssetsEsIndexer");
            return Collections.emptyList();
        } catch (ClientProtocolException e) {
            LOGGER.debug("ClientProtocolException occurred in getKbDetailsOfQid method of HostAssetsEsIndexer");
            return Collections.emptyList();
        } catch (IOException e) {
            LOGGER.debug("IOException occurred in getKbDetailsOfQid method of HostAssetsEsIndexer");
            return Collections.emptyList();
        } catch (Exception e) {
            LOGGER.debug("Exception occurred in getKbDetailsOfQid method of HostAssetsEsIndexer");
            return Collections.emptyList();
        }
        return kbDetailsList;
    }

    /**
     * Wrap up.
     *
     * @param type the type
     * @param CURR_DATE the curr date
     */
    public void wrapUp(String type, String CURR_DATE,List<Map<String,String>> errorList) {

        String index = "aws_" + type;
        ElasticSearchManager.refresh(index);
        String closeQidsJson = "{\"script\":{\"inline\": \"ctx._source._status='Closed';ctx._source._closedate='"
                + CURR_DATE
                + "';ctx._source.latest=false\"},\"query\": {\"bool\": {\"must\": [{ \"match\": {\"latest\":true}}], \"must_not\": [{\"match\": {\"discoverydate.keyword\":\""
                + CURR_DATE + "\"}}]}}}";
        try {
            ElasticSearchManager.invokeAPI("POST", "/"+index + "/vulninfo/" + "_update_by_query", closeQidsJson);
        } catch (IOException e) {
            LOGGER.error("wrapUp Failed", e);
            Map<String,String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "wrapUp Failed");
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
        }

        ElasticSearchManager.updateLatestStatus(index, "qualysinfo", CURR_DATE);
    }
}

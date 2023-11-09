/*******************************************************************************
 * Copyright 2023 Paladin Cloud, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.datashipper.entity;

import com.google.common.base.Strings;
import com.tmobile.cso.pacman.datashipper.config.ConfigManager;
import com.tmobile.cso.pacman.datashipper.dao.RDSDBManager;
import com.tmobile.cso.pacman.datashipper.error.ErrorManager;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import com.tmobile.cso.pacman.datashipper.util.Constants;
import com.tmobile.cso.pacman.datashipper.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class EntityManager implements Constants {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityManager.class);
    private static final String FIRST_DISCOVERED = "firstdiscoveredon";
    private static final String DISCOVERY_DATE = "discoverydate";
    private static final String PAC_OVERRIDE = "pac_override_";
    private final String s3Account = System.getProperty("base.account");
    private final String s3Region = System.getProperty("base.region");
    private final String s3Role = System.getProperty("s3.role");
    private final String bucketName = System.getProperty("s3");
    private final String dataPath = System.getProperty("s3.data");
    private final String attributesToPreserve = System.getProperty("shipper.attributes.to.preserve");
    private Map<String,String> accountIdNameMap = new HashMap<>();

    /**
     * Update on prem data.
     *
     * @param entity the entity
     */
    private static void updateOnPremData(Map<String, Object> entity) {
        entity.put("tags.Application", entity.get("u_business_service").toString().toLowerCase());
        entity.put("tags.Environment", entity.get("used_for"));
        entity.put("inScope", "true");
    }

    /**
     * Override.
     *
     * @param entity         the entity
     * @param overrideList   the override list
     * @param overrideFields the override fields
     */
    private static void override(Map<String, Object> entity, List<Map<String, String>> overrideList,
                                 List<Map<String, String>> overrideFields) {

        if (overrideList != null && !overrideList.isEmpty()) {
            overrideList.forEach(obj -> {
                String key = obj.get("fieldname");
                String value = obj.get("fieldvalue");
                if (null == value)
                    value = "";
                entity.put(key, value);
            });
        }

        // Add override fields if not already populated
        if (overrideFields != null && !overrideFields.isEmpty()) {
            String strOverrideFields = overrideFields.get(0).get("updatableFields");
            String[] _strOverrideFields = strOverrideFields.split(",");
            for (String _strOverrideField : _strOverrideFields) {
                if (!entity.containsKey(_strOverrideField)) {
                    entity.put(_strOverrideField, "");
                }

                String value = entity.get(_strOverrideField).toString();
                if (_strOverrideField.startsWith(PAC_OVERRIDE)) {
                    String originalField = _strOverrideField.replace(PAC_OVERRIDE, "");
                    String finalField = _strOverrideField.replace(PAC_OVERRIDE, "final_");
                    if (entity.containsKey(originalField)) { // Only if the
                        // field exists in
                        // source, we need
                        // to add
                        String originalValue = entity.get(originalField).toString();
                        if ("".equals(value)) {
                            entity.put(finalField, originalValue);
                        } else {
                            entity.put(finalField, value);
                        }
                    }

                }
            }
        }
    }

    /**
     * Upload entity data.
     *
     * @param datasource the datasource
     * @return the list
     */
    public List<Map<String, String>> uploadEntityData(String datasource) {
        List<Map<String, String>> errorList = new ArrayList<>();
        Map<String, String> types = ConfigManager.getTypesWithDisplayName(datasource);
        Iterator<Map.Entry<String, String>> itr = types.entrySet().iterator();
        String type = "";
        LOGGER.info("*** Start Colleting Entity Info ***");
        List<String> filters = new ArrayList<>(Collections.singletonList("_docid"));

        // Preserve attributes from current asset data if exists
        if (!Strings.isNullOrEmpty(attributesToPreserve)) {
            String[] attributes = attributesToPreserve.split(",");
            filters.addAll(Arrays.asList(attributes));
        }

        EntityAssociationManager childTypeManager = new EntityAssociationManager();
        ViolationAssociationManager violationAssociatManager = new ViolationAssociationManager();
        while (itr.hasNext()) {
            try {
                Map.Entry<String, String> entry = itr.next();
                type = entry.getKey();
                String displayName = entry.getValue();
                Map<String, Object> stats = new LinkedHashMap<>();
                String loaddate = new SimpleDateFormat("yyyy-MM-dd HH:mm:00Z").format(new java.util.Date());
                stats.put("datasource", datasource);
                stats.put("docType", type);
                stats.put("start_time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new java.util.Date()));
                LOGGER.info("Fetching {}", type);
                String indexName = datasource + "_" + type;
                Map<String, Map<String, String>> currentInfo = ESManager.getExistingInfo(indexName, type, filters);
                LOGGER.info("Existing no of docs : {}", currentInfo.size());

                List<Map<String, Object>> entities = fetchEntitiyInfoFromS3(datasource, type, errorList);
                List<Map<String, String>> tags = fetchTagsForEntitiesFromS3(datasource, type);

                LOGGER.info("Fetched from S3");
                if (!entities.isEmpty()) {
                    List<Map<String, String>> overridableInfo = RDSDBManager.executeQuery(
                            "select updatableFields  from cf_pac_updatable_fields where resourceType ='" + type + "'");
                    List<Map<String, String>> overrides = RDSDBManager.executeQuery(
                            "select _resourceid,fieldname,fieldvalue from pacman_field_override where resourcetype = '"
                                    + type + "'");
                    Map<String, List<Map<String, String>>> overridesMap = overrides.parallelStream()
                            .collect(Collectors.groupingBy(obj -> obj.get("_resourceid")));

                    String keys = ConfigManager.getKeyForType(datasource, type);
                    String idColumn = ConfigManager.getIdForType(datasource, type);
                    String[] keysArray = keys.split(",");

                    prepareDocs(currentInfo, entities, tags, overridableInfo, overridesMap, idColumn, keysArray, type, datasource, displayName);
                    Map<String, Long> errUpdateInfo = ErrorManager.getInstance(datasource).handleError(indexName, type, loaddate, errorList, true);
                    Map<String, Object> uploadInfo = ESManager.uploadData(indexName, type, entities, loaddate);
                    //ESManager.removeViolationForDeletedAssets(entities, indexName);
                    stats.putAll(uploadInfo);
                    stats.put("errorUpdates", errUpdateInfo);
                    errorList.addAll(childTypeManager.uploadAssociationInfo(datasource, type));
                    errorList.addAll(violationAssociatManager.uploadViolationInfo(datasource, type));

                } else {
                    Map<String, Long> errUpdateInfo = ErrorManager.getInstance(datasource).handleError(indexName, type, loaddate, errorList, true);
                    ESManager.refresh(indexName);
                    ESManager.updateLatestStatus(indexName, type, loaddate);
                    errorList.addAll(childTypeManager.uploadAssociationInfo(datasource, type));
                    stats.put("errorUpdates", errUpdateInfo);
                }
                stats.put("total_docs", entities.size());
                stats.put("end_time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new java.util.Date()));
                stats.put("newly_discovered", entities.stream().filter(entity -> entity.get(DISCOVERY_DATE).equals(entity.get(FIRST_DISCOVERED))).count());
                String statsJson = ESManager.createESDoc(stats);
                ESManager.invokeAPI("POST", "/datashipper/_doc", statsJson);
            } catch (Exception e) {
                LOGGER.error("Exception in collecting/uploading data for {}", type, e);
                Map<String, String> errorMap = new HashMap<>();
                errorMap.put(ERROR, "Exception in collecting/uploading data for " + type);
                errorMap.put(ERROR_TYPE, WARN);
                errorMap.put(EXCEPTION, e.getMessage());
                errorList.add(errorMap);
            }

        }
        LOGGER.info("*** End Colleting Entity Info ***");
        return errorList;
    }

    private List<Map<String, String>> fetchTagsForEntitiesFromS3(String datasource, String type) {
        List<Map<String, String>> tags = new ArrayList<>();
        try {
            tags = Util.fetchDataFromS3(s3Account, s3Region, s3Role, bucketName, dataPath + "/" + datasource + "-" + type + "-tags.data");
        } catch (Exception e) {
            // Do Nothing as there may not a tag file.
        }
        return tags;
    }

    private List<Map<String, Object>> fetchEntitiyInfoFromS3(String datasource, String type, List<Map<String, String>> errorList) {
        List<Map<String, Object>> entities = new ArrayList<>();
        try {
            entities = Util.fetchDataFromS3(s3Account, s3Region, s3Role, bucketName, dataPath + "/" + datasource + "-" + type + ".data");
        } catch (Exception e) {
            LOGGER.error("Exception in collecting data for {}", type, e);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put(ERROR, "Exception in collecting data for " + type);
            errorMap.put(ERROR_TYPE, WARN);
            errorMap.put(EXCEPTION, e.getMessage());
            errorList.add(errorMap);
        }
        return entities;
    }

    /**
     * Prepare docs.
     *
     * @param currentInfo     the current info
     * @param entities        the entities
     * @param tags            the tags
     * @param overridableInfo the overridable info
     * @param overridesMap    the overrides map
     * @param idColumn        the id column
     * @param _keys           the keys
     * @param _type           the type
     */
    private void prepareDocs(Map<String, Map<String, String>> currentInfo, List<Map<String, Object>> entities,
                             List<Map<String, String>> tags, List<Map<String, String>> overridableInfo,
                             Map<String, List<Map<String, String>>> overridesMap, String idColumn, String[] _keys, String _type, String dataSource, String displayName) {
        entities.parallelStream().forEach(entityInfo -> {
            String id = entityInfo.get(idColumn).toString();
            String docId = Util.concatenate(entityInfo, _keys, "_");
            String resourceName = ConfigManager.getResourceNameType(dataSource, _type);
            if (entityInfo.containsKey(resourceName)) {
                entityInfo.put("_resourcename", entityInfo.get(resourceName).toString());
            } else {
                entityInfo.put("_resourcename", id);
            }

            entityInfo.put("_resourceid", id);
            if ("aws".equalsIgnoreCase(dataSource)) {
                if (Arrays.asList(_keys).contains("accountid")) {
                    docId = dataSource + "_" + _type + "_" + docId;
                }
            }
            entityInfo.put("_docid", docId);
            entityInfo.put("_entity", "true");
            entityInfo.put("_entitytype", _type);
            entityInfo.put("targettypedisplayname", displayName);

            if (entityInfo.containsKey("subscriptionName")) {
                entityInfo.put("accountname", entityInfo.get("subscriptionName"));
            } else if (entityInfo.containsKey("projectName")) {
                entityInfo.put("accountname", entityInfo.get("projectName"));
            }
            if (entityInfo.containsKey("subscription")) {
                entityInfo.put("accountid", entityInfo.get("subscription"));
            } else if (entityInfo.containsKey("projectId")) {
                entityInfo.put("accountid", entityInfo.get("projectId"));
            }
            //For GCP CQ Collector accountName will be fetched from RDS using accountId
            if ("gcp".equalsIgnoreCase(dataSource)) {
                String projectId = String.valueOf(entityInfo.get("projectId"));
                if (null != projectId && !projectId.isEmpty()) {
                    boolean isAccountIdAlreadyExists = accountIdNameMap.containsKey(projectId);
                    String accountName = null;
                    String accountNameIdentifier = "accountName";
                    String singleQuote = "'";
                    //RDS Call will only be made if HashMap does not contain entry for accountId {ie accountName}
                    if (!isAccountIdAlreadyExists) {
                        LOGGER.info("RDS Call is invoked for fetching accountName for specific accountId");
                        String accountNameQueryStr = Constants.ACCOUNT_ID_SQL_QUERY + singleQuote + projectId + singleQuote;
                        LOGGER.debug("Printing accountNameQueryStr:{}", accountNameQueryStr);
                        List<Map<String, String>> accountNameMapList = RDSDBManager.executeQuery(accountNameQueryStr);
                        if (accountNameMapList != null && accountNameMapList.size() > 0) {
                            accountName = accountNameMapList.get(0).get(accountNameIdentifier);
                            accountIdNameMap.putIfAbsent(projectId, accountName);
                        }
                    }
                    accountName = accountIdNameMap.get(projectId);
                    //add to ES doc
                    entityInfo.put("accountname", accountName);

                }

            }
            entityInfo.put(Constants.DOC_TYPE, _type);
            entityInfo.put(_type + "_relations", _type);
            if (currentInfo != null && !currentInfo.isEmpty()) {
                Map<String, String> _currInfo = currentInfo.get(docId);
                if (_currInfo != null) {
                    if (_currInfo.get(FIRST_DISCOVERED) == null) {
                        _currInfo.put(FIRST_DISCOVERED, entityInfo.get(DISCOVERY_DATE).toString());
                    }
                    entityInfo.putAll(_currInfo);
                } else {
                    entityInfo.put(FIRST_DISCOVERED, entityInfo.get(DISCOVERY_DATE));
                }
            } else {
                entityInfo.put(FIRST_DISCOVERED, entityInfo.get(DISCOVERY_DATE));
            }

            tags.parallelStream().filter(tag -> Util.contains(tag, entityInfo, _keys)).forEach(_tag -> {
                String key = _tag.get("key");
                if (key != null && !"".equals(key)) {
                    entityInfo.put("tags." + key, _tag.get("value"));
                }
            });
            if ("onpremserver".equals(_type)) {
                updateOnPremData(entityInfo);

                if (overridesMap.containsKey(id) || !overridableInfo.isEmpty()) {
                    override(entityInfo, overridesMap.get(id), overridableInfo);
                }
            }

            if ("gcp".equalsIgnoreCase(entityInfo.get("_cloudType").toString()) && entityInfo.containsKey("tags") && entityInfo.get("tags") instanceof Map) {
                Map<String, Object> tagMap = (Map<String, Object>) entityInfo.get("tags");
                if (!tagMap.isEmpty()) {
                    tagMap.entrySet().stream().forEach(tagEntry -> {
                        entityInfo.put("tags." + tagEntry.getKey().substring(0, 1).toUpperCase() + tagEntry.getKey().substring(1), tagEntry.getValue());
                    });
                }
            }
        });
    }
}

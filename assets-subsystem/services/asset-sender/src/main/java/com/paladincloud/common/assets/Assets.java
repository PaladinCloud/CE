package com.paladincloud.common.assets;

import static java.util.Map.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paladincloud.common.AssetDocumentFields;
import com.paladincloud.common.assets.Assets.FilesAndTypes.SupportingType;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.aws.S3Helper;
import com.paladincloud.common.config.AssetTypes;
import com.paladincloud.common.config.ConfigConstants;
import com.paladincloud.common.config.ConfigConstants.Sender;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticBatch;
import com.paladincloud.common.search.ElasticBatch.BatchItem;
import com.paladincloud.common.search.ElasticSearchHelper;
import com.paladincloud.common.search.ElasticSearchHelper.HttpMethod;
import com.paladincloud.common.util.MapHelper;
import com.paladincloud.common.util.StringHelper;
import com.paladincloud.common.util.TimeHelper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Assets {

    private static final Logger LOGGER = LogManager.getLogger(Assets.class);
    private static final String OVERRIDE_PREFIX = "pac_override_";
    private static final String TYPE_ON_PREM_SERVER = "onpremserver";

    private final ElasticSearchHelper elasticSearch;
    private final AssetTypes assetTypes;
    private final S3Helper s3;
    private final DatabaseHelper database;

    @Inject
    public Assets(ElasticSearchHelper elasticSearch, AssetTypes assetTypes, S3Helper s3,
        DatabaseHelper database) {
        this.elasticSearch = elasticSearch;
        this.assetTypes = assetTypes;
        this.s3 = s3;
        this.database = database;
    }

    private static void setAssetDisplayName(Map<String, Object> doc) {
        var resourceGroupName = ObjectUtils.firstNonNull(
            doc.get(AssetDocumentFields.RESOURCE_GROUP_NAME), "").toString();
        var assetName = ObjectUtils.firstNonNull(doc.get(AssetDocumentFields.NAME), "").toString();
        String assetIdDisplayName;
        if (!resourceGroupName.isEmpty() && !assetName.isEmpty()) {
            assetIdDisplayName = STR."\{resourceGroupName}/\{assetName}";
        } else if (resourceGroupName.isEmpty()) {
            assetIdDisplayName = assetName;
        } else {
            assetIdDisplayName = resourceGroupName;
        }
        doc.put(AssetDocumentFields.ASSET_ID_DISPLAY_NAME, assetIdDisplayName.toLowerCase());

    }

    private static void addTags(Map<String, Object> doc) {
        //noinspection unchecked - this is validated to at least be a map
        var tagMap = (Map<String, Object>) doc.get(AssetDocumentFields.TAGS);
        if (!tagMap.isEmpty()) {
            tagMap.forEach((key, value) -> {
                var firstChar = key.substring(0, 1).toUpperCase();
                var remainder = key.substring(1);
                doc.put(STR."\{AssetDocumentFields.TAGS_PREFIX}\{firstChar}\{remainder}", value);
            });
        }
    }

    private static String assetsPathPrefix(String dataSource) {
        return STR."\{ConfigService.get(ConfigConstants.S3.DATA_PATH)}/\{dataSource}-";
    }

    private static boolean isTypeFile(String typeFromPath, String path, Set<String> types) {
        // The type must be in the types list AND must NOT be a supporting file
        return types.contains(typeFromPath) && path.endsWith(STR."-\{typeFromPath}.data");
    }

    private static boolean isSupportingTypeFile(String typeFromPath, String path,
        Set<String> types) {
        // A supporting type must be in the types list AND must not be a regular type file
        return types.contains(typeFromPath) && path.contains(STR."-\{typeFromPath}-");
    }

    private static boolean isTagsFile(String path) {
        return path.toLowerCase().endsWith("-tags.data");
    }

    /**
     * Given a path, parse out the probable primary type
     * <p>
     * From the path "s3:/some/paths/aws-ec2.data", this will return "ec2" And from
     * "s3:/some/paths/aws-ec2-ssminfo.data", this will return "ec2"
     *
     * @param path - and S3 path
     * @return - the likely type or null
     */
    private static String getPrimaryTypeFromPath(String path) {
        var fullType = getFullTypeFromPath(path);
        if (fullType == null) {
            return null;
        }
        var firstDash = fullType.indexOf('-');
        if (firstDash < 0) {
            return fullType;
        }
        return fullType.substring(0, firstDash);
    }

    /**
     * Given a path, parse out the probable type
     * <p>
     * From the path "s3:/some/paths/aws-ec2.data", this will return "ec2" And from
     * "s3:/some/paths/aws-ec2-ssminfo.data", this will return "ec2-ssminfo"
     *
     * @param path - and S3 path
     * @return - the likely type or null
     */
    private static String getFullTypeFromPath(String path) {
        if (path == null || !path.endsWith(".data")) {
            return null;
        }
        var lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0) {
            return null;
        }
        var firstDash = path.indexOf('-', lastSlash + 1);
        if (firstDash < 0) {
            return null;
        }
        var lastDot = path.substring(firstDash + 1).indexOf('.');
        if (lastDot < 0) {
            return null;
        }
        return path.substring(firstDash + 1, firstDash + lastDot + 1);
    }

    /**
     * Given a fullType ('ec2' or 'ec2-ssminfo'), return the supporting type. For ec2, null is
     * returned and for ec2-ssminfo, ssminfo is returned.
     *
     * @param fullType - the value from @link #getFullTypeFromPath
     * @return - either null or the supporting type
     */
    private static String getSupportingType(String fullType) {
        if (fullType == null) {
            return null;
        }

        var firstDash = fullType.indexOf('-');
        if (firstDash < 0) {
            return null;
        }

        return fullType.substring(firstDash + 1);
    }

    private static void updateOnPremData(Map<String, Object> entity) {
        entity.put(AssetDocumentFields.TAGS_APPLICATION,
            entity.get(AssetDocumentFields.U_BUSINESS_SERVICE).toString().toLowerCase());
        entity.put(AssetDocumentFields.TAGS_ENVIRONMENT, entity.get(AssetDocumentFields.USED_FOR));
        entity.put(AssetDocumentFields.IN_SCOPE, "true");
    }

    /**
     * Override.
     *
     * @param document       the entity
     * @param overrideList   the override list
     * @param overrideFields the override fields
     */
    private static void override(Map<String, Object> document,
        List<Map<String, String>> overrideList, List<Map<String, String>> overrideFields) {

        if (CollectionUtils.isNotEmpty(overrideList)) {
            overrideList.forEach(obj -> {
                String key = obj.get("fieldname");
                String value = obj.get("fieldvalue");
                if (null == value) {
                    value = "";
                }
                document.put(key, value);
            });
        }

        // Add override fields if not already populated
        if (CollectionUtils.isNotEmpty(overrideFields)) {
            String strOverrideFields = overrideFields.getFirst().get("updatableFields");
            String[] _strOverrideFields = strOverrideFields.split(",");
            for (String _strOverrideField : _strOverrideFields) {
                if (!document.containsKey(_strOverrideField)) {
                    document.put(_strOverrideField, "");
                }

                String value = document.get(_strOverrideField).toString();
                if (_strOverrideField.startsWith(OVERRIDE_PREFIX)) {
                    String originalField = _strOverrideField.replace(OVERRIDE_PREFIX, "");
                    String finalField = _strOverrideField.replace(OVERRIDE_PREFIX, "final_");
                    if (document.containsKey(originalField)) {
                        // Only if the field exists in source, we need to add
                        String originalValue = document.get(originalField).toString();
                        if ("".equals(value)) {
                            document.put(finalField, originalValue);
                        } else {
                            document.put(finalField, value);
                        }
                    }
                }
            }
        }
    }

    private List<Map<String, Object>> fetchFromS3(String bucket, String path, String dataSource,
        String type) {
        try {
            return s3.fetchData(bucket, path);
        } catch (IOException e) {
            throw new JobException(
                STR."Exception fetching asset data for \{dataSource} from \{type}; path=\{path}",
                e);
        }
    }

    private void setMissingAccountName(Map<String, Object> newDocument,
        Map<String, String> accountIdNameMap) {
        String accountId = Stream.of(newDocument.get(AssetDocumentFields.PROJECT_ID),
                newDocument.get(AssetDocumentFields.ACCOUNT_ID)).filter(Objects::nonNull)
            .map(String::valueOf).findFirst().orElse(null);
        if (StringUtils.isNotEmpty(accountId)) {
            if (!accountIdNameMap.containsKey(accountId)) {
                String accountNameQueryStr = STR."SELECT accountName FROM pacmandata.cf_Accounts WHERE accountId = '\{accountId}'";
                var accountNameMapList = database.executeQuery(accountNameQueryStr);
                if (!accountNameMapList.isEmpty()) {
                    accountIdNameMap.putIfAbsent(accountId,
                        accountNameMapList.getFirst().get("accountName"));
                }
            }
            newDocument.put(AssetDocumentFields.ACCOUNT_NAME, accountIdNameMap.get(accountId));
        }
    }

    public void upload(String dataSource) {
        var bucket = ConfigService.get(ConfigConstants.S3.BUCKET_NAME);
        var allFilenames = s3.listObjects(ConfigService.get(ConfigConstants.S3.BUCKET_NAME),
            assetsPathPrefix(dataSource));
        var types = assetTypes.getTypesWithDisplayName(dataSource);
        var fileTypes = FilesAndTypes.matchFilesAndTypes(allFilenames, types.keySet());
        if (!fileTypes.unknownFiles.isEmpty()) {
            LOGGER.warn("Unknown files: {}", fileTypes.unknownFiles);
        }

        if (types.isEmpty()) {
            LOGGER.info("There are no types to process for dataSource: {}", dataSource);
            return;
        }

        List<String> filters = new ArrayList<>(
            Collections.singletonList(AssetDocumentFields.DOC_ID));
        var preservedAttributes = Arrays.stream(
            StringHelper.split(ConfigService.get(Sender.ATTRIBUTES_TO_PRESERVE), ",",
                StringHelper.EMPTY_ARRAY)).toList();
        if (!preservedAttributes.isEmpty()) {
            filters.addAll(preservedAttributes);
        }

        LOGGER.info("Start processing Asset info");
        var loadError = new LoadErrors(s3, elasticSearch, bucket, fileTypes.loadErrors);
        try (var batchIndexer = new ElasticBatch(elasticSearch)) {
            Map<String, String> accountIdNameMap = new HashMap<>();
            for (Map.Entry<String, String> entry : fileTypes.typeFiles.entrySet()) {
                var type = entry.getKey();
                var filename = entry.getValue();
                try {
                    var displayName = types.get(type);
                    var startTime = ZonedDateTime.now();
                    var loadDate = TimeHelper.formatZeroSeconds(startTime);
                    var indexName = StringHelper.indexName(dataSource, type);

                    var existingDocuments = elasticSearch.getExistingDocuments(indexName, filters);
                    var newDocuments = fetchFromS3(bucket, filename, dataSource, type);
                    var tags = (fileTypes.tagFiles.containsKey(type)) ? fetchFromS3(bucket,
                        fileTypes.tagFiles.get(type), dataSource, type)
                        : new ArrayList<Map<String, Object>>();

                    LOGGER.info("For {}, {} assets and {} tags were fetched from S3 and {} "
                            + "assets were fetched from ElasticSearch", type, newDocuments.size(),
                        tags.size(), existingDocuments.size());

                    if (!newDocuments.isEmpty()) {
                        indexDocuments(batchIndexer, indexName, existingDocuments, newDocuments,
                            tags, loadDate, type, dataSource, displayName, accountIdNameMap);
                    }

                    var stats = generateStats(startTime, dataSource, type, newDocuments.size(),
                        newDocuments.stream().filter((e -> e.get(AssetDocumentFields.DISCOVERY_DATE)
                            .equals(e.get(AssetDocumentFields.FIRST_DISCOVERED)))).count());
                    batchIndexer.add(
                        BatchItem.documentEntry("datashipper", UUID.randomUUID().toString(),
                            stats));

                    batchIndexer.flush();
                    loadError.process(indexName, type, loadDate);
                    elasticSearch.refresh(indexName);
                    elasticSearch.markStaleDocuments(indexName, type,
                        AssetDocumentFields.LOAD_DATE_KEYWORD, loadDate);

                    uploadSupportingTypes(dataSource, indexName, bucket,
                        fileTypes.supportingTypes.get(type), loadDate);
                } catch (Exception e) {
                    throw new JobException(
                        STR."Failed uploading asset data for \{dataSource} and \{type}", e);
                }
            }
        } catch (Exception e) {
            throw new JobException(STR."Exception inserting asset data for \{dataSource}", e);
        }

        LOGGER.info("Finished processing asset data for {}", dataSource);
    }

    private Map<String, Object> generateStats(ZonedDateTime startTime, String dataSource,
        String type, long documentCount, long newlyDiscovered) {
        return new HashMap<>(Map.ofEntries(entry(AssetDocumentFields.DATA_SOURCE, dataSource),
            entry(AssetDocumentFields.DOC_TYPE, type),
            entry(AssetDocumentFields.START_TIME, TimeHelper.formatISO8601(startTime)),
            entry(AssetDocumentFields.END_TIME, TimeHelper.formatNowISO8601()),
            entry(AssetDocumentFields.TOTAL_DOCS, documentCount),
            entry(AssetDocumentFields.UPLOADED_DOC_COUNT, documentCount),
            entry(AssetDocumentFields.NEWLY_DISCOVERED, newlyDiscovered)));
    }

    private void uploadSupportingTypes(String dataSource, String indexName, String bucket,
        List<FilesAndTypes.SupportingType> supportingTypes, String loadDate) throws IOException {
        if (supportingTypes.isEmpty()) {
            return;
        }

        var firstSupportingType = supportingTypes.getFirst();
        updateTypeRelations(indexName, firstSupportingType.parentType, supportingTypes);
        var keys = Arrays.stream(
                assetTypes.getKeyForType(dataSource, firstSupportingType.parentType).split(","))
            .toList();
        for (var supportingType : supportingTypes) {
            var documents = fetchFromS3(bucket, supportingType.filePath, dataSource,
                supportingType.fullType);

            LOGGER.info("Processing supporting type: parent={} type={} path={} count={}",
                supportingType.parentType, supportingType.supportingType, supportingType.filePath,
                documents.size());

            var ec2Type = STR."\{supportingType.parentType}_\{supportingType.supportingType}";

            try (var batchIndexer = new ElasticBatch(elasticSearch)) {
                for (var document : documents) {
                    var parentId = StringHelper.concatenate(document, keys, "_");
                    if ("aws".equalsIgnoreCase(dataSource)) {
                        if (keys.contains(AssetDocumentFields.ACCOUNT_ID)) {
                            parentId = STR."\{indexName}_\{supportingType.parentType}_\{parentId}";
                        }
                    }
                    document.put(AssetDocumentFields.LOAD_DATE, loadDate);
                    document.put(AssetDocumentFields.DOC_TYPE, ec2Type);
                    var relations = new HashMap<>(
                        Map.ofEntries(entry("name", ec2Type), entry("parent", parentId)));
                    document.put(STR."\{supportingType.parentType}_relations", relations);

                    batchIndexer.add(BatchItem.routingEntry(indexName, parentId, document));
                }
            } catch (Exception e) {
                throw new JobException(
                    STR."Error uploading data for \{dataSource} \{supportingType.fullType}", e);
            }

            elasticSearch.deleteDocumentsWithoutValue(indexName, ec2Type,
                AssetDocumentFields.LOAD_DATE_KEYWORD, loadDate);
        }
    }

    private void updateTypeRelations(String indexName, String parentType,
        List<SupportingType> relatedTypes) throws IOException {
        var relations = getTypeRelations(indexName, parentType);
        var relationsList = new ArrayList<String>();
        if (relations.containsKey(parentType)) {
            var existing = relations.get(parentType);
            if (existing instanceof String) {
                relationsList.add((String) existing);
            } else {
                relationsList.addAll((List<String>) existing);
            }
        }

        var needUpdate = false;
        for (var relatedType : relatedTypes) {
            var newType = STR."\{relatedType.parentType}_\{relatedType.supportingType}";
            if (!relationsList.contains(newType)) {
                needUpdate = true;
                relationsList.add(newType);
            }
        }

        // If an updated is needed, update only the '_relations' mapping
        if (needUpdate) {
            relations.put(parentType, relationsList);
            var asString = new ObjectMapper().writeValueAsString(relations);
            var payload = STR."""
                {
                    "properties": {
                        "\{parentType}_relations": {
                            "type": "join",
                            "relations": \{asString}
                        }
                    }
                }
                """.trim();
            LOGGER.info("Updating types relations for {} to {}", parentType, relationsList);
            elasticSearch.invokeAndCheck(HttpMethod.PUT, STR."\{indexName}/_mapping", payload);
        }
    }

    private Map<String, Object> getTypeRelations(String indexName, String parentType)
        throws IOException {
        var objectMapper = new ObjectMapper();
        var response = elasticSearch.invokeAndCheck(HttpMethod.GET, STR."\{indexName}/_mapping",
            null);
        var document = objectMapper.readTree(response.getBody());
        var relations = document.at(
            STR."/\{indexName}/mappings/properties/\{parentType}_relations/relations");
        return objectMapper.convertValue(relations, Map.class);
    }

    private void indexDocuments(ElasticBatch batchIndexer, String indexName,
        Map<String, Map<String, String>> existingDocuments, List<Map<String, Object>> newDocuments,
        List<Map<String, Object>> tags, String loadDate, String type, String dataSource,
        String displayName, Map<String, String> accountIdNameMap) throws IOException {
        var updatableFields = database.executeQuery(
            STR."select updatableFields from cf_pac_updatable_fields where resourceType ='\{type}'");
        var overrides = database.executeQuery(
            STR."select _resourceid,fieldname,fieldvalue from pacman_field_override where resourcetype = '\{type}'");
        var overridesMap = overrides.parallelStream()
            .collect(Collectors.groupingBy(obj -> obj.get(AssetDocumentFields.RESOURCE_ID)));

        var keys = assetTypes.getKeyForType(dataSource, type).split(",");
        var idColumn = assetTypes.getIdForType(dataSource, type);

        prepareDocuments(existingDocuments, newDocuments, tags, loadDate, updatableFields,
            overridesMap, idColumn, keys, type, dataSource, displayName, accountIdNameMap);
        batchIndexer.add(newDocuments.stream().map(doc -> BatchItem.documentEntry(indexName,
            doc.get(AssetDocumentFields.DOC_ID).toString(), doc)).toList());

    }

    private void prepareDocuments(Map<String, Map<String, String>> existingDocuments,
        List<Map<String, Object>> newDocuments, List<Map<String, Object>> tags, String loadDate,
        List<Map<String, String>> updatableFields,
        Map<String, List<Map<String, String>>> overridesMap, String idColumn, String[] keys,
        String type, String dataSource, String displayName, Map<String, String> accountIdNameMap) {

        newDocuments.parallelStream().forEach(newDocument -> {
            var idColumnValue = newDocument.get(idColumn);
            if (idColumnValue == null) {
                return;
            }

            var id = idColumnValue.toString();
            if (id == null) {
                id = newDocument.get("id").toString();
            }
            var docId = StringHelper.concatenate(newDocument, Arrays.stream(keys).toList(), "_");
            var resourceName = assetTypes.getResourceNameType(dataSource, type);
            if (newDocument.containsKey(resourceName)) {
                newDocument.put(AssetDocumentFields.RESOURCE_NAME,
                    newDocument.get(resourceName).toString());
            } else {
                newDocument.put(AssetDocumentFields.RESOURCE_NAME, id);
            }
            newDocument.putIfAbsent(AssetDocumentFields.RESOURCE_ID, id);
            if ("aws".equalsIgnoreCase(dataSource)) {
                if (Arrays.asList(keys).contains(AssetDocumentFields.ACCOUNT_ID)) {
                    docId = STR."\{StringHelper.indexName(dataSource, type)}_\{docId}";
                }
            }
            newDocument.putIfAbsent(AssetDocumentFields.DOC_ID, docId);
            newDocument.putIfAbsent(AssetDocumentFields.ENTITY, "true");
            newDocument.put(AssetDocumentFields.ENTITY_TYPE, type);
            newDocument.put(AssetDocumentFields.TARGET_TYPE_DISPLAY_NAME, displayName);

            if (newDocument.containsKey(AssetDocumentFields.SUBSCRIPTION_NAME)) {
                newDocument.put(AssetDocumentFields.ACCOUNT_NAME,
                    newDocument.get(AssetDocumentFields.SUBSCRIPTION_NAME));
            } else if (newDocument.containsKey(AssetDocumentFields.PROJECT_NAME)) {
                newDocument.put(AssetDocumentFields.ACCOUNT_NAME,
                    newDocument.get(AssetDocumentFields.PROJECT_NAME));
            }
            if (newDocument.containsKey(AssetDocumentFields.SUBSCRIPTION)) {
                newDocument.put(AssetDocumentFields.ACCOUNT_ID,
                    newDocument.get(AssetDocumentFields.SUBSCRIPTION));
            } else if (newDocument.containsKey(AssetDocumentFields.PROJECT_ID)) {
                newDocument.put(AssetDocumentFields.ACCOUNT_ID,
                    newDocument.get(AssetDocumentFields.PROJECT_ID));
            }

            // For CQ Collector accountName will be fetched from RDS using accountId only if not set earlier
            if (("gcp".equalsIgnoreCase(dataSource) || "crowdstrike".equalsIgnoreCase(dataSource))
                && !newDocument.containsKey(AssetDocumentFields.ACCOUNT_NAME)) {
                setMissingAccountName(newDocument, accountIdNameMap);
            }

            newDocument.put(AssetDocumentFields.DOC_TYPE, type);
            newDocument.put(STR."\{type}\{AssetDocumentFields.RELATIONS}", type);
            if (!existingDocuments.isEmpty()) {
                Map<String, String> existingDoc = existingDocuments.get(docId);
                if (existingDoc != null) {
                    if (existingDoc.get(AssetDocumentFields.FIRST_DISCOVERED) == null) {
                        existingDoc.put(AssetDocumentFields.FIRST_DISCOVERED,
                            newDocument.get(AssetDocumentFields.DISCOVERY_DATE).toString());
                    }
                    newDocument.putAll(existingDoc);
                } else {
                    newDocument.put(AssetDocumentFields.FIRST_DISCOVERED,
                        newDocument.get(AssetDocumentFields.DISCOVERY_DATE));
                }
            } else {
                newDocument.put(AssetDocumentFields.FIRST_DISCOVERED,
                    newDocument.get(AssetDocumentFields.DISCOVERY_DATE));
            }

            tags.parallelStream().filter(tag -> MapHelper.containsAll(tag, newDocument, keys))
                .forEach(tag -> {
                    var key = tag.get("key").toString();
                    if (StringUtils.isNotBlank(key)) {
                        newDocument.put(STR."tags.\{key}", tag.get("value"));
                    }
                });

            if (TYPE_ON_PREM_SERVER.equals(type)) {
                updateOnPremData(newDocument);
                if (overridesMap.containsKey(id) || !updatableFields.isEmpty()) {
                    override(newDocument, overridesMap.get(id), updatableFields);
                }
            }

            if ("gcp".equalsIgnoreCase(newDocument.get(AssetDocumentFields.CLOUD_TYPE).toString())
                && newDocument.containsKey(AssetDocumentFields.TAGS) && newDocument.get(
                AssetDocumentFields.TAGS) instanceof Map) {
                addTags(newDocument);
            }

            if ("Azure".equalsIgnoreCase(
                (String) newDocument.get(AssetDocumentFields.CLOUD_TYPE))) {
                setAssetDisplayName(newDocument);
            }

            newDocument.put(AssetDocumentFields.LOAD_DATE, loadDate);
            newDocument.put(AssetDocumentFields.LATEST, true);
        });
    }

    static class FilesAndTypes {

        Map<String, String> tagFiles = new HashMap<>();
        // The supporting files, by type (ec2-ssminfo)
        Map<String, String> supportingFiles = new HashMap<>();
        // The filename for each primary type (ec2)
        Map<String, String> typeFiles = new HashMap<>();
        // Files that are not expected or known
        Set<String> unknownFiles = new HashSet<>();
        // The supporting types for each primary type (ec2 -> ec2-ssminfo)
        Map<String, List<SupportingType>> supportingTypes = new HashMap<>();
        // Load error files
        List<String> loadErrors = new ArrayList<>();

        static FilesAndTypes matchFilesAndTypes(List<String> allFilenames,
            Set<String> primaryTypes) {
            var ft = new FilesAndTypes();
            for (var filename : allFilenames) {
                if (filename.toLowerCase().endsWith("-loaderror.data")) {
                    ft.loadErrors.add(filename);
                    continue;
                }
                var primaryType = getPrimaryTypeFromPath(filename);
                var fullType = getFullTypeFromPath(filename);
                if (isTagsFile(filename)) {
                    ft.tagFiles.put(fullType, filename);
                } else if (isTypeFile(fullType, filename, primaryTypes)) {
                    ft.typeFiles.put(fullType, filename);
                } else if (isSupportingTypeFile(primaryType, filename, primaryTypes)) {
                    ft.supportingFiles.put(fullType, filename);
                    var list = ft.supportingTypes.getOrDefault(primaryType, new ArrayList<>());
                    var supportingType = getSupportingType(fullType);
                    list.add(new SupportingType(primaryType, supportingType, fullType, filename));
                    ft.supportingTypes.put(primaryType, list);
                } else {
                    ft.unknownFiles.add(filename);
                }
            }
            return ft;
        }

        static class SupportingType {

            String parentType;
            String supportingType;
            String fullType;
            String filePath;

            SupportingType(String parentType, String supportingType, String fullType,
                String filePath) {
                this.parentType = parentType;
                this.supportingType = supportingType;
                this.fullType = fullType;
                this.filePath = filePath;
            }
        }
    }
}

package com.paladincloud.common.assets;

import static java.util.Map.entry;

import com.paladincloud.common.AssetDocumentFields;
import com.paladincloud.common.aws.Database;
import com.paladincloud.common.aws.S3;
import com.paladincloud.common.config.AssetTypes;
import com.paladincloud.common.config.ConfigConstants;
import com.paladincloud.common.config.ConfigConstants.Sender;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.search.ElasticBatch;
import com.paladincloud.common.search.ElasticBatch.BatchItem;
import com.paladincloud.common.search.ElasticSearch;
import com.paladincloud.common.util.MapExtras;
import com.paladincloud.common.util.StringExtras;
import com.paladincloud.common.util.TimeFormatter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Assets {

    private static final Logger LOGGER = LogManager.getLogger(Assets.class);
    private static final String OVERRIDE_PREFIX = "pac_override_";
    private static final String TYPE_ON_PREM_SERVER = "onpremserver";

    private final ElasticSearch elasticSearch;
    private final AssetTypes assetTypes;
    private final S3 s3;
    private final Database database;

    @Inject
    public Assets(ElasticSearch elasticSearch, AssetTypes assetTypes, S3 s3, Database database) {
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

    private List<Map<String, Object>> fetchFromS3(String bucket, String path,
        String dataSource, String type) {
        try {
            return s3.fetchData(bucket, path);
        } catch (IOException e) {
            throw new JobException(
                STR."Exception fetching asset data for \{dataSource} from \{type}; path=\{path}",
                e);
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

    private void setMissingAccountName(Map<String, Object> newDocument,
        Map<String, String> accountIdNameMap) {
        String accountId = Stream.of(newDocument.get(AssetDocumentFields.PROJECT_ID),
                newDocument.get(AssetDocumentFields.ACCOUNT_ID)).filter(Objects::nonNull)
            .map(String::valueOf).findFirst().orElse(null);
        if (StringUtils.isNotEmpty(accountId)) {
            if (!accountIdNameMap.containsKey(accountId)) {
                LOGGER.info("querying accountName for specific accountId");
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
        // TODO: DON'T CHECK THIS DEV HACK IN!
        types = types.entrySet().stream().filter(e -> e.getKey().equals("cloudfunction"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        LOGGER.error("LIMITED TYPES TO {}", types);

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
            StringExtras.split(ConfigService.get(Sender.ATTRIBUTES_TO_PRESERVE), ",",
                StringExtras.EMPTY_ARRAY)).toList();
        if (!preservedAttributes.isEmpty()) {
            filters.addAll(preservedAttributes);
        }

        LOGGER.info("Start collecting Asset info");
        // TODO: How are supporting types handled?
        //      They're missing displayName
        //      The associated ElasticSearch index needs to be created if it's missing (the earlier
        //          index creation depends on types (from the ConfigService API).
        //          Perhaps move index creation here? All missing ones could be created before
        //          any indexing. Perhaps there's a way to batch check for existing indexes?
        try (var batchIndexer = new ElasticBatch(elasticSearch)) {
            Map<String, String> accountIdNameMap = new HashMap<>();
            // TODO: Currently, this does primary types & related tags only
            for (Map.Entry<String, String> entry : fileTypes.typeFiles.entrySet()) {
                var type = entry.getKey();
                var filename = entry.getValue();
                try {
                    var supportingTypes = fileTypes.supportingTypes.get(type);
                    if (CollectionUtils.isNotEmpty(supportingTypes)) {
                        LOGGER.warn("Supporting types for {} are not being processed: {}", type,
                            supportingTypes);
                    }
                    var displayName = types.get(type);
                    var now = ZonedDateTime.now();
                    var loadDate = TimeFormatter.formatZeroSeconds(now);
                    Map<String, Object> stats = new java.util.HashMap<>(
                        Map.ofEntries(entry(AssetDocumentFields.DATA_SOURCE, dataSource),
                            entry(AssetDocumentFields.DOC_TYPE, type),
                            entry(AssetDocumentFields.START_TIME,
                                TimeFormatter.formatISO8601(now))));

                    var indexName = StringExtras.indexName(dataSource, type);
                    var existingDocuments = elasticSearch.getExistingDocuments(indexName, filters);
                    var newDocuments = fetchFromS3(bucket, filename, dataSource, type);
                    var tags = (fileTypes.tagFiles.containsKey(type)) ? fetchFromS3(bucket,
                        fileTypes.tagFiles.get(type), dataSource, type)
                        : new ArrayList<Map<String, Object>>();
                    LOGGER.info("For {}, {} assets and {} tags were fetched from S3 and {} "
                            + "assets were fetched from ElasticSearch", type, newDocuments.size(),
                        tags.size(), existingDocuments.size());
                    if (newDocuments.isEmpty()) {
                        // ERROR condition - update elastic index, it looks like
                        throw new RuntimeException("Handle no discovered assets");
                    } else {
                        var updatableFields = database.executeQuery(
                            STR."select updatableFields  from cf_pac_updatable_fields where resourceType ='\{type}'");
                        var overrides = database.executeQuery(
                            STR."select _resourceid,fieldname,fieldvalue from pacman_field_override where resourcetype = '\{type}'");
                        var overridesMap = overrides.parallelStream().collect(
                            Collectors.groupingBy(obj -> obj.get(AssetDocumentFields.RESOURCE_ID)));

                        var keys = assetTypes.getKeyForType(dataSource, type).split(",");
                        var idColumn = assetTypes.getIdForType(dataSource, type);

                        prepareDocuments(existingDocuments, newDocuments, tags, loadDate,
                            updatableFields, overridesMap, idColumn, keys, type, dataSource,
                            displayName, accountIdNameMap);
                        // TODO: ErrorManager handleError
                        batchIndexer.add(newDocuments.stream().map(doc -> new BatchItem(indexName,
                            doc.get(AssetDocumentFields.DOC_ID).toString(), doc)).toList());
                    }

                    stats.put(AssetDocumentFields.TOTAL_DOCS, newDocuments.size());
                    stats.put(AssetDocumentFields.END_TIME, TimeFormatter.formatNowISO8601());
                    stats.put(AssetDocumentFields.NEWLY_DISCOVERED, newDocuments.stream().filter(
                        (e -> e.get(AssetDocumentFields.DISCOVERY_DATE)
                            .equals(e.get(AssetDocumentFields.FIRST_DISCOVERED)))).count());
                    batchIndexer.add(
                        new BatchItem("datashipper", UUID.randomUUID().toString(), stats));

                } catch (Exception e) {
                    throw new JobException(
                        STR."Failed uploading asset data for \{dataSource} and \{type}", e);
                }
            }
        } catch (Exception e) {
            throw new JobException(STR."Exception inserting asset data for \{dataSource}", e);
        }

        LOGGER.info("Finished collecting asset data for {}", dataSource);
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
            var docId = StringExtras.concatenate(newDocument, keys, "_");
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
                    docId = STR."\{StringExtras.indexName(dataSource, type)}_\{docId}";
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

            tags.parallelStream().filter(tag -> MapExtras.containsAll(tag, newDocument, keys))
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

    private static class FilesAndTypes {

        Map<String, String> tagFiles = new HashMap<>();
        // The supporting files, by type (ec2-ssminfo)
        Map<String, String> supportingFiles = new HashMap<>();
        // The filename for each primary type (ec2)
        Map<String, String> typeFiles = new HashMap<>();
        // Files that are not expected or known
        Set<String> unknownFiles = new HashSet<>();
        // The supporting types for each primary type (ec2 -> ec2-ssminfo)
        Map<String, List<String>> supportingTypes = new HashMap<>();

        static FilesAndTypes matchFilesAndTypes(List<String> allFilenames,
            Set<String> primaryTypes) {
            var ft = new FilesAndTypes();
            for (var filename : allFilenames) {
                var primaryType = getPrimaryTypeFromPath(filename);
                var fullType = getFullTypeFromPath(filename);
                if (isTagsFile(filename)) {
                    ft.tagFiles.put(fullType, filename);
                } else if (isTypeFile(fullType, filename, primaryTypes)) {
                    ft.typeFiles.put(fullType, filename);
                } else if (isSupportingTypeFile(primaryType, filename, primaryTypes)) {
                    ft.supportingFiles.put(fullType, filename);
                    var list = ft.supportingTypes.getOrDefault(primaryType, new ArrayList<>());
                    list.add(fullType);
                    ft.supportingTypes.put(primaryType, list);
                } else {
                    ft.unknownFiles.add(filename);
                }
            }
            return ft;
        }
    }
}

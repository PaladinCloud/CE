package com.paladincloud.common.assets;

import static java.util.Map.entry;

import com.paladincloud.common.AssetDocumentFields;
import com.paladincloud.common.assets.FilesAndTypes.SupportingType;
import com.paladincloud.common.aws.DatabaseHelper;
import com.paladincloud.common.config.AssetTypes;
import com.paladincloud.common.config.ConfigConstants;
import com.paladincloud.common.config.ConfigConstants.Sender;
import com.paladincloud.common.config.ConfigService;
import com.paladincloud.common.errors.JobException;
import com.paladincloud.common.mapper.MapperRepository;
import com.paladincloud.common.search.ElasticBatch.BatchItem;
import com.paladincloud.common.util.JsonHelper;
import com.paladincloud.common.util.StringHelper;
import com.paladincloud.common.util.TimeHelper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Assets {

    private static final Logger LOGGER = LogManager.getLogger(Assets.class);

    private static final String DATA_SHIPPER_INDEX = "datashipper";

    private final AssetTypes assetTypes;
    private final AssetRepository assetRepository;
    private final MapperRepository mapperRepository;
    private final DatabaseHelper databaseHelper;

    @Inject
    public Assets(AssetRepository assetRepository, AssetTypes assetTypes,
        MapperRepository mapperRepository, DatabaseHelper databaseHelper) {
        this.assetRepository = assetRepository;
        this.assetTypes = assetTypes;
        this.mapperRepository = mapperRepository;
        this.databaseHelper = databaseHelper;
    }

    private static String assetsPathPrefix(String dataSource) {
        return STR."\{ConfigService.get(ConfigConstants.S3.DATA_PATH)}/\{dataSource}-";
    }

    private List<Map<String, Object>> fetchMapperFiles(String bucket, String path,
        String dataSource, String type) {
        try {
            return mapperRepository.fetchFile(bucket, path);
        } catch (IOException e) {
            throw new JobException(
                STR."Exception fetching asset data for \{dataSource} from \{type}; path=\{path}",
                e);
        }
    }

    public void process(String dataSource) {
        var bucket = ConfigService.get(ConfigConstants.S3.BUCKET_NAME);
        var allFilenames = mapperRepository.listFiles(bucket, assetsPathPrefix(dataSource));
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
        filters.addAll(Arrays.stream(
            StringHelper.split(ConfigService.get(Sender.ATTRIBUTES_TO_PRESERVE), ",",
                StringHelper.EMPTY_ARRAY)).toList());

        LOGGER.info("Start processing Asset info");

        var typeToError = loadTypeErrors(bucket, fileTypes.loadErrors);
        try (var batchIndexer = assetRepository.createBatch()) {
            fileTypes.typeFiles.forEach((type, filename) -> {
                try {
                    var displayName = types.get(type);
                    var startTime = ZonedDateTime.now();
                    var indexName = StringHelper.indexName(dataSource, type);

                    var existingAssets = assetRepository.getLatestAssets(indexName, filters);
                    var latestAssets = fetchMapperFiles(bucket, filename, dataSource, type);
                    var tags = (fileTypes.tagFiles.containsKey(type)) ? fetchMapperFiles(bucket,
                        fileTypes.tagFiles.get(type), dataSource, type)
                        : new ArrayList<Map<String, Object>>();

                    LOGGER.info("For {}, {} assets and {} tags were fetched from S3 and {} "
                            + "assets were fetched from ElasticSearch", type, latestAssets.size(),
                        tags.size(), existingAssets.size());

                    var docIdFields = Arrays.stream(
                        assetTypes.getKeyForType(dataSource, type).split(",")).toList();
                    var idColumn = assetTypes.getIdForType(dataSource, type);

                    // Merge stored assets and mapped assets
                    var assetCreator = AssetDocumentHelper.builder().loadDate(startTime)
                        .idField(idColumn).docIdFields(docIdFields).dataSource(dataSource)
                        .displayName(displayName).tags(tags).type(type)
                        .accountIdToNameFn(this::accountIdToName);
                    var mergeResponse = MergeAssets.process(assetCreator.build(), existingAssets,
                        latestAssets);
                    LOGGER.info(
                        "Merged mapper assets for {}; {} were updated, {} were added and {} were deleted",
                        type, mergeResponse.getUpdatedAssets().size(),
                        mergeResponse.getNewAssets().size(),
                        mergeResponse.getRemovedAssets().size());

                    // Each document needs to be updated, regardless of which state it is in
                    mergeResponse.getAllAssets().forEach((_, value) -> {
                        try {
                            batchIndexer.add(BatchItem.documentEntry(indexName,
                                STR."\{dataSource}_\{value.getDocId()}", JsonHelper.toJson(value)));
                        } catch (IOException e) {
                            throw new JobException("Failed converting asset to JSON", e);
                        }
                    });

                    var stats = generateStats(startTime, dataSource, type, latestAssets.size(),
                        mergeResponse.getNewAssets().size());
                    batchIndexer.add(
                        BatchItem.documentEntry(DATA_SHIPPER_INDEX, UUID.randomUUID().toString(),
                            JsonHelper.toJson(stats)));

                    batchIndexer.flush();

                    var loadDate = TimeHelper.formatZeroSeconds(startTime);
                    assetRepository.processLoadErrors(indexName, type, loadDate, typeToError);

                    uploadSupportingTypes(dataSource, indexName, bucket,
                        fileTypes.supportingTypes.getOrDefault(type, Collections.emptyList()),
                        loadDate);
                } catch (Exception e) {
                    throw new JobException(
                        STR."Failed uploading asset data for \{dataSource} and \{type}", e);
                }
            });
        } catch (Exception e) {
            throw new JobException(STR."Exception inserting asset data for \{dataSource}", e);
        }

        LOGGER.info("Finished processing asset data for {}", dataSource);
    }

    private String accountIdToName(String accountId) {
        String accountNameQueryStr = STR."SELECT accountName FROM pacmandata.cf_Accounts WHERE accountId = '\{accountId}'";
        var accountNameMapList = databaseHelper.executeQuery(accountNameQueryStr);
        if (!accountNameMapList.isEmpty()) {
            return accountNameMapList.getFirst().get("accountName");
        }
        return null;
    }

    private Map<String, List<Map<String, Object>>> loadTypeErrors(String bucket,
        List<String> filenames) {
        Map<String, List<Map<String, Object>>> typeToError;
        if (filenames.size() > 1) {
            throw new JobException(STR."Cannot handle more than one load error file: \{filenames}");
        }
        if (filenames.isEmpty()) {
            typeToError = new HashMap<>();
        } else {
            try {
                var documents = mapperRepository.fetchFile(bucket, filenames.getFirst());
                typeToError = documents.parallelStream()
                    .collect(Collectors.groupingBy(d -> d.get("type").toString()));
            } catch (IOException e) {
                throw new JobException("Exception fetching error file", e);
            }
        }
        return typeToError;
    }

    private ShipperStatsDTO generateStats(ZonedDateTime startTime, String dataSource, String type,
        long documentCount, long newlyDiscovered) {
        return new ShipperStatsDTO(dataSource, type, startTime, ZonedDateTime.now(), documentCount,
            documentCount, newlyDiscovered);
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
            var documents = fetchMapperFiles(bucket, supportingType.filePath, dataSource,
                supportingType.fullType);

            LOGGER.info("Processing supporting type: parent={} type={} path={} count={}",
                supportingType.parentType, supportingType.supportingType, supportingType.filePath,
                documents.size());

            var ec2Type = STR."\{supportingType.parentType}_\{supportingType.supportingType}";

            try (var batchIndexer = assetRepository.createBatch()) {
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

            assetRepository.deleteAssetsWithoutValue(indexName, ec2Type,
                AssetDocumentFields.asKeyword(AssetDocumentFields.LOAD_DATE), loadDate);
        }
    }

    private void updateTypeRelations(String indexName, String parentType,
        List<SupportingType> relatedTypes) throws IOException {
        var relations = assetRepository.getTypeRelations(indexName, parentType);
        var relationsList = new ArrayList<String>();
        if (relations.containsKey(parentType)) {
            var existing = relations.get(parentType);
            if (existing instanceof String) {
                relationsList.add((String) existing);
            } else {
                @SuppressWarnings("unchecked") List<String> list = (List<String>) existing;
                relationsList.addAll(list);
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
            assetRepository.updateTypeRelations(indexName, parentType, relations);
        }
    }
}

package com.paladincloud.common.assets;

import static java.util.Map.entry;

import com.paladincloud.common.AssetDocumentFields;
import com.paladincloud.common.util.MapHelper;
import com.paladincloud.common.util.StringHelper;
import com.paladincloud.common.util.TimeHelper;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Use the Builder to set properties, and {@link #createFrom(Map)} to convert from a mapped data
 * object to an AssetDTO. Used by @see MergeAssets when processing assets from the mapper.
 */
@Builder
public class AssetDocumentHelper {

    static private Map<String, String> accountIdNameMap = new HashMap<>();
    @NonNull
    private ZonedDateTime loadDate;
    @Getter
    @NonNull
    private String idField;
    @NonNull
    private List<String> docIdFields;
    @NonNull
    private String dataSource;
    @NonNull
    private String displayName;
    @NonNull
    private String type;
    @NonNull
    private List<Map<String, Object>> tags;
    @NonNull
    private Function<String, String> accountIdToNameFn;
    private String resourceNameField;

    /**
     * Given mapper data, create an Asset document from it
     *
     * @param data - the mapper created data
     * @return - AssetDTO, intended for manipulation & serialization
     */
    public AssetDTO createFrom(Map<String, Object> data) {
        var idValue = data.getOrDefault(idField, "").toString();
        if (idValue.isEmpty()) {
            return null;
        }

        var docId = StringHelper.concatenate(data, docIdFields, "_");
        if ("aws".equalsIgnoreCase(dataSource)) {
            if (docId.contains(AssetDocumentFields.ACCOUNT_ID)) {
                docId = STR."\{StringHelper.indexName(dataSource, type)}_\{docId}";
            }
        }

        var dto = new AssetDTO();

        // Set some common properties, which are type safe and require function calls rather
        // than map puts. These properties are removed from 'data' (the mapper data) in order
        // to decrease confusion between an additional property and a typed property.
        Map<String, DtoSetter> fieldSetterMap = Map.ofEntries(
            entry(AssetDocumentFields.ACCOUNT_ID, v -> dto.setAccountId(v.toString())),
            entry(AssetDocumentFields.ACCOUNT_NAME, v -> dto.setAccountName(v.toString())),
            entry(AssetDocumentFields.CLOUD_TYPE,
                v -> dto.setCloudType(v.toString().toLowerCase())),
            entry(AssetDocumentFields.CSPM_SOURCE, v -> dto.setCspmSource(v.toString())),
            entry(AssetDocumentFields.DISCOVERY_DATE,
                v -> dto.setDiscoveryDate(TimeHelper.parseDiscoveryDate(v.toString()))),
            entry(AssetDocumentFields.NAME, v -> dto.setName(v.toString())),
            entry(AssetDocumentFields.REPORTING_SOURCE, v -> dto.setReportingSource(v.toString())));

        fieldSetterMap.forEach((key, value) -> {
            var fieldValue = getAndRemove(key, data);
            if (fieldValue != null) {
                value.set(fieldValue);
            }
        });

        // Set the remaining mapper properties
        dto.setDocId(docId);
        dto.setEntity(true);
        dto.setReportingSource(dataSource);

        // Populate the dto with all existing mapper values. Some of these may get overwritten
        data.forEach(dto::addAdditionalProperty);

        // Set common asset properties
        dto.setEntityType(type);
        dto.setTargetTypeDisplayName(displayName);
        dto.setDocType(type);

        if (dto.isOwner()) {
            dto.addAdditionalProperty(STR."\{type}\{AssetDocumentFields.RELATIONS}", type);
        }
        dto.setResourceName(data.getOrDefault(resourceNameField, idValue).toString());
        dto.setResourceId(data.getOrDefault(AssetDocumentFields.RESOURCE_ID, idValue).toString());

        if (data.containsKey(AssetDocumentFields.SUBSCRIPTION_NAME)) {
            dto.setAccountName(data.get(AssetDocumentFields.SUBSCRIPTION_NAME).toString());
        } else if (data.containsKey(AssetDocumentFields.PROJECT_NAME)) {
            dto.setAccountName(data.get(AssetDocumentFields.PROJECT_NAME).toString());
        }

        if (data.containsKey(AssetDocumentFields.SUBSCRIPTION)) {
            dto.setAccountId(data.get(AssetDocumentFields.SUBSCRIPTION).toString());
        } else if (data.containsKey(AssetDocumentFields.PROJECT_ID)) {
            dto.setAccountId(data.get(AssetDocumentFields.PROJECT_ID).toString());
        }

        dto.setFirstDiscoveryDate(dto.getDiscoveryDate());

        tags.parallelStream().filter(tag -> MapHelper.containsAll(tag, data, docIdFields))
            .forEach(tag -> {
                var key = tag.get("key").toString();
                if (StringUtils.isNotBlank(key)) {
                    dto.addAdditionalProperty(STR."tags.\{key}", tag.get("value"));
                }
            });

        // For CQ Collector accountName will be fetched from RDS using accountId only if not set earlier
        if (("gcp".equalsIgnoreCase(dataSource) || "crowdstrike".equalsIgnoreCase(dataSource))
            && dto.getAccountName() == null) {
            setMissingAccountName(dto, data);
        }

        if ("gcp".equalsIgnoreCase(
            data.getOrDefault(AssetDocumentFields.CLOUD_TYPE, "").toString())) {
            addTags(data, dto);
        }

        if ("Azure".equalsIgnoreCase(dto.getCloudType())) {
            dto.setAssetIdDisplayName(getAssetIdDisplayName(data));
        }

        dto.setLoadDate(loadDate);
        dto.setLatest(true);
        return dto;
    }

    /**
     * Update an existing Asset with fields from the latest mapper data.
     *
     * @param data - the mapper data
     * @param dto  - the existing AssetDTO
     */
    public void updateFrom(Map<String, Object> data, AssetDTO dto) {
        var idValue = data.getOrDefault(idField, "").toString();

        // One time only, existing assets in ElasticSearch must be updated to include new fields
        if (StringUtils.isEmpty(dto.getCspmSource())) {
            dto.setCspmSource(data.getOrDefault(AssetDocumentFields.CSPM_SOURCE, "").toString());
        }
        if (StringUtils.isEmpty(dto.getReportingSource())) {
            dto.setReportingSource(
                data.getOrDefault(AssetDocumentFields.REPORTING_SOURCE, "").toString());
        }

        // Update all fields the user has control over.
        if (data.containsKey(AssetDocumentFields.NAME)) {
            dto.setName(data.get(AssetDocumentFields.NAME).toString());
        }
        dto.setLoadDate(loadDate);
        dto.setLatest(true);

        dto.setResourceName(data.getOrDefault(resourceNameField, idValue).toString());
        if (data.containsKey(AssetDocumentFields.ACCOUNT_NAME)) {
            dto.setAccountName(data.get(AssetDocumentFields.ACCOUNT_NAME).toString());
        }

        if (data.containsKey(AssetDocumentFields.SUBSCRIPTION_NAME)) {
            dto.setAccountName(data.get(AssetDocumentFields.SUBSCRIPTION_NAME).toString());
        } else if (data.containsKey(AssetDocumentFields.PROJECT_NAME)) {
            dto.setAccountName(data.get(AssetDocumentFields.PROJECT_NAME).toString());
        }

        // The display name comes out of our database, but could potentially change with an update.
        // Hence, it gets updated here.
        dto.setTargetTypeDisplayName(displayName);
        if ("Azure".equalsIgnoreCase(dto.getCloudType())) {
            dto.setAssetIdDisplayName(getAssetIdDisplayName(data));
        }

        if ("gcp".equalsIgnoreCase(
            data.getOrDefault(AssetDocumentFields.CLOUD_TYPE, "").toString())) {
            addTags(data, dto);
        }
    }

    /**
     * Update AssetDTO fields to indicate the asset has been removed - it's no longer an existing
     * asset.
     *
     * @param dto - the existing AssetDTO that is to be removed.
     */
    public void remove(AssetDTO dto) {
        dto.setLatest(false);
    }

    private Object getAndRemove(String key, Map<String, Object> data) {
        if (data.containsKey(key)) {
            var value = data.get(key);
            data.remove(key);
            return value;
        }
        return null;
    }

    private String getAssetIdDisplayName(Map<String, Object> data) {
        var resourceGroupName = ObjectUtils.firstNonNull(
            data.get(AssetDocumentFields.RESOURCE_GROUP_NAME), "").toString();
        var assetName = ObjectUtils.firstNonNull(data.get(AssetDocumentFields.NAME), "").toString();
        String assetIdDisplayName;
        if (!resourceGroupName.isEmpty() && !assetName.isEmpty()) {
            assetIdDisplayName = STR."\{resourceGroupName}/\{assetName}";
        } else if (resourceGroupName.isEmpty()) {
            assetIdDisplayName = assetName;
        } else {
            assetIdDisplayName = resourceGroupName;
        }
        return assetIdDisplayName.toLowerCase();
    }

    private void setMissingAccountName(AssetDTO dto, Map<String, Object> data) {
        String accountId = Stream.of(data.get(AssetDocumentFields.PROJECT_ID),
                data.get(AssetDocumentFields.ACCOUNT_ID)).filter(Objects::nonNull).map(String::valueOf)
            .findFirst().orElse(null);
        if (StringUtils.isNotEmpty(accountId)) {
            if (!accountIdNameMap.containsKey(accountId)) {
                var accountName = accountIdToNameFn.apply(accountId);
                if (accountName != null) {
                    accountIdNameMap.put(accountId, accountName);
                }
            }
            dto.setAccountName(accountIdNameMap.get(accountId));
        }
    }

    private void addTags(Map<String, Object> data, AssetDTO dto) {
        var tagData = data.get(AssetDocumentFields.TAGS);
        if (tagData instanceof Map) {
            @SuppressWarnings("unchecked") var tagMap = (Map<String, Object>) tagData;
            if (!tagMap.isEmpty()) {
                tagMap.forEach((key, value) -> {
                    var firstChar = key.substring(0, 1).toUpperCase();
                    var remainder = key.substring(1);
                    var upperCaseStart = STR."\{firstChar}\{remainder}";
                    dto.addAdditionalProperty(STR."\{AssetDocumentFields.asTag(upperCaseStart)}",
                        value);
                });
            }
        }
    }


    interface DtoSetter {

        void set(Object value);
    }
}

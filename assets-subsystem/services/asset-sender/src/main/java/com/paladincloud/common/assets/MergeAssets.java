package com.paladincloud.common.assets;

import com.paladincloud.common.errors.JobException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class MergeAssets {
    private final Map<String, AssetDTO> updatedAssets = new HashMap<>();
    private final Map<String, AssetDTO> removedAssets = new HashMap<>();
    private final Map<String, AssetDTO> newAssets = new HashMap<>();

    private MergeAssets() {
    }

    public Map<String, AssetDTO> getAllAssets() {
        var allAssets = new HashMap<>(getUpdatedAssets());
        allAssets.putAll(getNewAssets());
        allAssets.putAll(getRemovedAssets());
        return allAssets;
    }

    /**
     * Given the existing repository documents, merge in the associated new/latest documents. The
     * existing documents will be updated in place. In addition, this instance will track and
     * provide the new document ids and the deleted document ids.
     *
     * @param existingAssets - the documents in the repository (OpenSearch)
     * @param latestAssets - the mapper documents
     * @return - A MergeAssets instance
     */
    static public MergeAssets process(AssetDocumentHelper assetHelper, Map<String, AssetDTO> existingAssets, List<Map<String, Object>> latestAssets) {
        var response = new MergeAssets();

        latestAssets.forEach(latestDoc -> {
            var id = latestDoc.getOrDefault(assetHelper.getIdField(), "").toString();
            if (id.isEmpty()) {
                throw new JobException(STR."Asset missing the id field '\{assetHelper.getIdField()}'");
            }
            if (!existingAssets.containsKey(id)) {
                response.newAssets.put(id, assetHelper.createFrom(latestDoc));
            } else {
                var asset = existingAssets.get(id);
                response.updatedAssets.put(id, asset);
                assetHelper.updateFrom(latestDoc, asset);
            }
        });

        existingAssets.forEach( (key, value) -> {
            if (!response.updatedAssets.containsKey(key)) {
                response.removedAssets.put(key, value);
                assetHelper.remove(value);
            }
        });
        return response;
    }
}

package com.paladincloud.common.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilesAndTypes {

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

    static public FilesAndTypes matchFilesAndTypes(List<String> allFilenames,
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

    static class SupportingType {

        String parentType;
        String supportingType;
        String fullType;
        String filePath;

        SupportingType(String parentType, String supportingType, String fullType, String filePath) {
            this.parentType = parentType;
            this.supportingType = supportingType;
            this.fullType = fullType;
            this.filePath = filePath;
        }
    }
}

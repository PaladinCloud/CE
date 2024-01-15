/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
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
package com.tmobile.pacbot.azure.inventory.file;

import com.tmobile.pacbot.azure.inventory.util.InventoryConstants;
import com.tmobile.pacbot.azure.inventory.util.TargetTypesConstants;
import com.tmobile.pacbot.azure.inventory.vo.AzureVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.tmobile.pacbot.azure.inventory.util.Constants.ERROR_PREFIX;

/**
 * The Class FileManager.
 */
public class FileManager {

    private static final Logger log = LoggerFactory.getLogger(FileManager.class);

    /**
     * Instantiates a new file manager.
     */
    private FileManager() { }

    /**
     * Initialise.
     *
     * @param folderName the folder name
     */
    public static void initialise(String folderName) {
        FileGenerator.folderName = folderName;
        boolean isCreated = new File(folderName).mkdirs();
        if (!isCreated) {
            log.error(ERROR_PREFIX + "Failed to create file in S3 in path {}", folderName);
            System.exit(1); // We want to exit if the S3 folder is not created
        }

        TargetTypesConstants.TARGET_TYPES_TO_COLLECT.forEach(type -> {
            try {
                FileGenerator.writeToFile(getFilenameFromTargetType(type), InventoryConstants.OPEN_ARRAY, false);
            } catch (IOException e) {
                // We want to continue if the file is not created
                log.error(ERROR_PREFIX + "Failed to initialize write file in S3 in path {}", folderName, e);
            }
        });
    }

    public static void finalise() {
        TargetTypesConstants.TARGET_TYPES_TO_COLLECT.forEach(type -> {
            try {
                FileGenerator.writeToFile(getFilenameFromTargetType(type), InventoryConstants.CLOSE_ARRAY, true);
            } catch (IOException e) {
                log.error(ERROR_PREFIX + "Failed to finalize write file in S3 in path {}", FileGenerator.folderName, e);
            }
        });
    }

    public static void generateTargetTypeFile(List<? extends AzureVH> map, String targetType) {
        FileGenerator.generateJson(map, getFilenameFromTargetType(targetType));
    }

    private static String getFilenameFromTargetType(String targetType) {
        return "azure-" + targetType + ".data";
    }
}

package com.tmobile.pacbot.gcp.inventory.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.tmobile.pacbot.gcp.inventory.vo.GCPVH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The Class FileGenerator.
 */
public class FileGenerator {

    /**
     * Instantiates a new file generator.
     */
    private FileGenerator() {

    }

    /**
     * The folder name.
     */
    protected static String folderName;

    /**
     * The Constant DELIMITER.
     */
    public static final String DELIMITER = "`";

    /**
     * The Constant LINESEPARATOR.
     */
    public static final String LINESEPARATOR = "\n";

    public static final String COMMA = ",";

    /**
     * The current date.
     */
    protected static String discoveryDate = new SimpleDateFormat("yyyy-MM-dd HH:00:00Z").format(new java.util.Date());

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(FileGenerator.class);

    /**
     * Write to file.
     *
     * @param filename the filename
     * @param data     the data
     * @param appendto the appendto
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeToFile(String filename, String data, boolean appendto) throws IOException {
        log.debug("Write to File : {}", filename);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(folderName + File.separator + filename, appendto))) {
            bw.write(data);
            bw.flush();
        } catch (IOException e) {
            log.error("Write to File :{} failed", filename, e);
            throw e;
        }
    }


    /**
     * Gets the line data.
     *
     * @param fieledNames the fieled names
     * @param obj         the obj
     * @return the line data
     */


    protected static boolean generateJson(List<? extends GCPVH> assetList, String fileName) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        StringBuilder sb = new StringBuilder();

        for (GCPVH asset : assetList) {
            asset.setDiscoverydate(discoveryDate);
            try {
                if (sb.length() == 0 && new File(folderName + File.separator + fileName).length() < 2) {
                    sb.append(objectMapper.writeValueAsString(asset));
                } else {
                    sb.append(COMMA + LINESEPARATOR).append(objectMapper.writeValueAsString(asset));
                }
            } catch (Exception e) {
                log.error("Error in generateJson ", e);
                return false;
            }
        }

        try {
            writeToFile(fileName, sb.toString(), true);
        } catch (IOException e) {
            log.error("Error in generateJson ", e);
            return false;
        }
        return true;
    }

}

package com.tmobile.pacbot.gcp.inventory.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.tmobile.pacbot.gcp.inventory.vo.*;;

/**
 * The Class FileManager.
 */
public class FileManager {

    /**
     * Instantiates a new file manager.
     */
    private FileManager() {

    }

    /**
     * Initialise.
     *
     * @param folderName the folder name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void initialise(String folderName) throws IOException {
        FileGenerator.folderName = folderName;
        new File(folderName).mkdirs();

        FileGenerator.writeToFile("gcp-vminstance.data", "[", false);
    }

    public static void finalise() throws IOException {

        FileGenerator.writeToFile("gcp-vminstance.data", "]", true);
    }

    public static void generateVMFiles(List<VirtualMachineVH> vmMap) throws IOException {

        FileGenerator.generateJson(vmMap, "gcp-vminstance.data");
    }

}

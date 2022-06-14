/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
	 * @param folderName
	 *            the folder name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
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

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
package com.paladincloud.service;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tmobile.pacman.api.commons.Constants;
import com.tmobile.pacman.api.commons.repo.PacmanRdsRepository;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class DownloadFileService.
 */

public class DownloadFileService implements Constants {

    private S3Service s3Service = new S3Service();

    public void downloadData(final JsonArray response, final String fileFormat, String serviceName) throws Exception {

        byte[] byteArray = null;
        List<String> columnList = getColumnNames(response);
        if (!StringUtils.isEmpty(fileFormat) && "excel".equalsIgnoreCase(fileFormat)) {
            byteArray = getFileBytes(response, serviceName, columnList);
        } else {
            byteArray = generateCsvFile(response, columnList);
        }
        columnList = new ArrayList<>();
        s3Service.uploadByteArray(byteArray, fileFormat, serviceName);
    }

    /**
     * Gets the column names.
     *
     * @param response the response
     * @return List<String>
     */
    private List<String> getColumnNames(JsonArray response) {
        List<String> columns = new ArrayList<>();
        if (response.size() > 0) {
            JsonObject columnsObj = response.get(0).getAsJsonObject();

            String key = null;
            Iterator<String> it = columnsObj.keySet().iterator();

            while (it.hasNext()) {
                key = it.next();
                if (!"nonDisplayableAttributes".equals(key)) {
                    columns.add(key);
                }
            }
        }
        return columns;
    }

    /**
     * Generate csv file.
     *
     * @param issueDetails the issue details
     * @param columns      the columns
     * @return the byte[]
     * @throws Exception the exception
     */
    @SuppressWarnings("resource")
    private byte[] generateCsvFile(JsonArray issueDetails, List<String> columns) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(baos));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
        List<String> headers = Lists.newArrayList();

        for (String clm : columns) {
            headers.add(clm.replaceAll("_", ""));
        }

        csvPrinter.printRecord(headers.toArray());
        for (int columnIndex = 0; columnIndex < issueDetails.size(); columnIndex++) {
            JsonObject issueDetail = issueDetails.get(columnIndex).getAsJsonObject();
            List<String> rows = Lists.newArrayList();

            for (String clm : columns) {
                if (issueDetail.has(clm) && !issueDetail.get(clm).isJsonNull()) {
                    if (issueDetail.get(clm) instanceof JsonObject) {
                        rows.add(issueDetail.get(clm).toString());
                    } else {
                        rows.add(issueDetail.get(clm).getAsString());
                    }
                } else {
                    rows.add(null);
                }
            }
            csvPrinter.printRecord(rows.toArray());
        }
        writer.flush();
        writer.close();
        columns = new ArrayList<>();
        return baos.toByteArray();
    }

    /**
     * Gets the file bytes.
     *
     * @param issueDetails the issue details
     * @param methodType   the method type
     * @param columns      the columns
     * @return the file bytes
     * @throws WriteException the write exception
     * @throws IOException    Signals that an I/O exception has occurred.
     */
    private byte[] getFileBytes(JsonArray issueDetails, String methodType, List<String> columns) throws WriteException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableWorkbook workbook = Workbook.createWorkbook(baos);
        WritableSheet writablesheet = workbook.createSheet(methodType, 0);
        formatWritableSheet(writablesheet, columns);
        addWritableSheetCells(writablesheet, issueDetails, columns);
        workbook.write();
        workbook.close();
        return baos.toByteArray();
    }

    /**
     * Adds the writable sheet cells.
     *
     * @param writablesheet the writablesheet
     * @param issueDetails  the issue details
     * @param columns       the columns
     * @throws WriteException the write exception
     */
    private void addWritableSheetCells(WritableSheet writablesheet, JsonArray issueDetails, List<String> columns) throws WriteException {
        WritableFont cellFont = new WritableFont(WritableFont.createFont("Calibri"), TWELVE);
        cellFont.setColour(Colour.BLACK);
        WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
        cellFormat.setBackground(Colour.WHITE);
        cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.GRAY_25);
        for (int columnIndex = 0; columnIndex < issueDetails.size(); columnIndex++) {
            JsonObject issueDetail = issueDetails.get(columnIndex).getAsJsonObject();
            addCell(writablesheet, issueDetail, cellFormat, columnIndex, columns);
        }
    }

    /**
     * Adds the cell.
     *
     * @param writablesheet the writablesheet
     * @param issueDetail   the issue detail
     * @param cellFormat    the cell format
     * @param columnIndex   the column index
     * @param columns       the columns
     * @throws WriteException the write exception
     */
    private void addCell(WritableSheet writablesheet, JsonObject issueDetail, WritableCellFormat cellFormat,
                         int columnIndex, List<String> columns) throws WriteException {
        int rowIndex = 0;

        for (String clm : columns) {
            if (issueDetail.has(clm)) {
                if (issueDetail.get(clm).isJsonNull()) {
                    writablesheet.addCell(new Label(rowIndex++, 1 + columnIndex, "No Data", cellFormat));
                } else {
                    writablesheet.addCell(new Label(rowIndex++, 1 + columnIndex, issueDetail.get(clm).getAsString(),
                            cellFormat));
                }

            }
        }
    }

    /**
     * Format writable sheet.
     *
     * @param writablesheet the writable sheet
     * @param columns       the columns
     * @throws WriteException the write exception
     */
    private void formatWritableSheet(WritableSheet writablesheet, List<String> columns) throws WriteException {
        WritableFont cellFonts = new WritableFont(WritableFont.createFont("Calibri"), ELEVEN, WritableFont.BOLD, false,
                UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        WritableCellFormat cellFormats = new WritableCellFormat(cellFonts);
        cellFormats.setBorder(Border.ALL, BorderLineStyle.THIN);
        cellFormats.setBackground(Colour.WHITE);
        int labelIndex = 0;
        for (String clm : columns) {
            writablesheet.addCell(new Label(labelIndex, 0, clm.replaceAll("_", ""), cellFormats));
            CellView cell = writablesheet.getColumnView(labelIndex);
            cell.setAutosize(true);
            writablesheet.setColumnView(labelIndex, cell);
            labelIndex++;
        }
    }

}

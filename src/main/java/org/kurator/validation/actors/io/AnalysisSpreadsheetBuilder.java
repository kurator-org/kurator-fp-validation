/*
 * Copyright (c) 2014 President and Fellows of Harvard College
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of Version 2 of the GNU General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.kurator.validation.actors.io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.kurator.akka.KuratorActor;

import java.io.*;
import java.util.*;

/**
 * Generates a spreadsheet from analysis results.
 *
 * @author lowery
 */
public class AnalysisSpreadsheetBuilder extends KuratorActor {
    private static final String INPUT_FILE_PROPERTY = "postprocess.inputFile";
    private static final String OUTPUT_FILE_PROPERTY = "postprocess.outputFile";
    private static final String ACTIONABLE_ITEMS_ONLY_PROPERTY = "postprocess.actionableItemsOnly";

    private static final int MAX_ROWS = 65535;

    private List<Map<String, Object>> summary = new ArrayList<Map<String, Object>>();

    private String[] actorNames = { "ScientificNameValidator", "DateValidator", "GeoRefValidator", "BasisOfRecordValidator" };

    private Map<String, String> validationStateTextMappings = new HashMap<String, String>() {{
        put("UNABLE_DETERMINE_VALIDITY", "don't know");
        put("CURATED", "we have proposed this change");
        put("CORRECT", "no change needed; looks good to us");
        put("FILLED_IN", "no value was present, we have proposed one");
        put("UNABLE_CURATE", "there seems to be a problem, but we don't know how to solve it");
    }};

    private Map<String, String> recordColumnMap = new LinkedHashMap<String, String>() {{
        put("collectionCode", "Collection Code");
        put("institutionCode", "Institution Code");
        put("catalogNumber", "Catalog Number");
        put("id", "Id");
        put("occurrenceId", "occurrence Id");
        put("recordedBy", "Collector");
        put("eventDate", "Date Collected");
        put("identifiedBy", "Determiner");
        put("scientificName", "Scientific Name");
        put("scientificNameAuthorship", "Scientific Name Authorship");
        put("taxonID", "Taxon Id");
        put("family", "Family");
        put("country", "Country");
        put("stateProvince", "State/Province");
        put("county", "County");
        put("locality", "Locality");
        put("decimalLatitude", "Decimal Latitude");
        put("decimalLongitude", "Decimal Longitude");
        put("georeferenceSources", "Georeference Sources");
        put("coordinateUncertaintyInMeters", "Coordinate Uncertainty In Meters");
        put("geodeticDatum", "Geodetic Datum");
        put("ownerInstitutionCode", "Owner Institution Code");
        put("startDayOfYear", "Start Day Of Year");
        put("month", "Month");
        put("day", "Day");
        put("year", "Year");
        put("basisOfRecord", "Basis of Record");
        put("modified", "Modified");
    }};

    private Map<String, String> actorDetailsColumnMap = new LinkedHashMap<String, String>(recordColumnMap) {{
        put("Actor Result", "Actor Result");
        put("Comment", "Provenance"); //JSON key is always "Comment" so we have to keep it and adjust col header here
        put("Source", "Source");
    }};

    private HSSFWorkbook wb;
    private Map<String, HSSFCellStyle> validationStateStyles;
    private HSSFSheet recordSheet;
    private Map<String, HSSFSheet> actorDetailsSheets;

    private int recordNum = 0;

    public boolean actionableItemsOnly;
    public String filePath;

    @Override
    protected void onInitialize() throws Exception {
        this.wb = new HSSFWorkbook();

        initStyles();
        initSheets();
    }

    @Override
    protected void onData(Object value) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        LinkedHashMap result = mapper.readValue((String)value, new TypeReference<LinkedHashMap>() {});

        Map record = (LinkedHashMap)result.get("Record");
        Map markers = (LinkedHashMap)result.get("Markers");
        addRecord(record, markers, recordNum+1);

        ArrayList actorDetails = (ArrayList) result.get("ActorDetails");
        addActorDetails(actorDetails, record, recordNum+1);

        recordNum++;
    }

    @Override
    protected void onEnd() throws Exception {
        // last step is to auto-size columns
        int numSheets = wb.getNumberOfSheets();
        int maxCols = actorDetailsColumnMap.size() < recordColumnMap.size() ?
                recordColumnMap.size() : actorDetailsColumnMap.size();

        for (int i = 0; i < numSheets; i++) {
            autoSizeColumns(wb.getSheetAt(i), maxCols);
        }

        File file = File.createTempFile("output_", ".xls");
        wb.write(new FileOutputStream(file));

        wb.close();

        publishArtifact("output_xls", file.getAbsolutePath());
    }

    private void initFirstSheet(long count) {
        StringBuffer stringBuffer = new StringBuffer();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/analysis.txt")));

            String line = null;

            while ((line = bufferedReader.readLine()) != null) {

                stringBuffer.append(line).append("\n");
            }

            stringBuffer.append("\nTotal record count: " + count + " occurrence records.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Add list of sources to first page

        HSSFSheet sheet = wb.createSheet("Description");

        sheet.setColumnWidth(0, 18000);
        HSSFRow row = sheet.createRow(0);

        HSSFCell cell = row.createCell(0);

        CellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        cell.setCellStyle(style);
        cell.setCellValue(stringBuffer.toString());
    }

    /**
     * Process a single result and add it to the summary list for post processing. If we are only dealing with
     * actionable items we check to see if a record is actionable and omit records that are not.
     *
     * @param result a single analysis result
     */
    private void processResult(LinkedHashMap result) {
        Map record = (LinkedHashMap)result.get("Record");
        boolean isActionable = checkActionable(record);

        if (!actionableItemsOnly || actionableItemsOnly && isActionable) {
            summary.add(result);
        }
    }

    /**
     * Check if a particular record is actionable or not. Actionable items are those that require further action
     * such as records that have been marked "CURATED" or "UNABLE_CURATE".
     *
     * @param record
     * @return true if actionable, false otherwise
     */
    private boolean checkActionable(Map record) {
        LinkedHashMap validationState = (LinkedHashMap)record.get("ValidationState");
        for (Object value: validationState.values()) {
            if ((((String)value).equalsIgnoreCase("CURATED") || ((String)value).equalsIgnoreCase("UNABLE_CURATE"))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Given a key that may occur in a map with string keys, but might have different
     * case or spacing, find a matching key in the map ignoring case and spacing.
     *
     * @param key
     * @param record map assumed to be keyed with strings
     * @return key or a case insentitive match to key found in the keys of map.
     */
    protected String normalizeKey(String key, Map record) {
        if (!record.containsKey(key)) {
            // handle case variation in terms
            Set recordKeys = record.keySet();
            Iterator i = recordKeys.iterator();
            boolean found = false;
            while (i.hasNext() && !found) {
                String recordKey = i.next().toString();
                if (key.replace(" ", "").toLowerCase().equals(recordKey.replace(" ", "").toLowerCase())) {
                    key = recordKey;
                    found = true;
                }
            }
            if (!found) {
                if (key.toLowerCase().trim().equals("source")) {
                    key = "Source";
                }
                if (key.toLowerCase().trim().equals("actor result")) {
                    key = "Actor Result";
                }
                if (key.toLowerCase().trim().equals("comment")) {
                    key = "Comment";
                }
            }
        }
        return key;
    }

    /**
     * Add the details for an actor run for a particular record to the spreadsheet.
     *
     * @param actorDetails the actor details part of the analysis result
     * @param record the record with changes applied
     * @param rowIndex the row to add actor details to
     */
    private void addActorDetails(ArrayList<LinkedHashMap> actorDetails, Map record, int rowIndex) {
        for (LinkedHashMap detail: actorDetails) {
            String actorName = (String)detail.get("Actor Name");

            // get the actor details sheet for the current actor and create a new row

            HSSFSheet sheet = actorDetailsSheets.get(actorName);
            HSSFRow row = sheet.createRow(rowIndex);

            Map validationState = (LinkedHashMap)detail.get("ValidationState");

            int colIndex = 0;
            for (String key : actorDetailsColumnMap.keySet()) {
                if (!record.containsKey(key) && !validationStateTextMappings.containsKey(key) && !actorDetailsColumnMap.containsKey(key)) {
                    // handle case variation in terms
                    key = this.normalizeKey(key, record);
                }

                HSSFCell cell = row.createCell(colIndex);

                // Actor details may not contain values for all the columns, only those that are relevant to that
                // particular actor (scientificName and scientificNameAuthorship for the ScientificNameValidator
                // for example). If there is no value for the current column, obtain a value from the full record.
                if (detail.containsKey(key) && detail.get(key) != null && !((String)detail.get(key)).isEmpty()) {
                    if (validationStateTextMappings.containsKey(detail.get(key))) {
                        cell.setCellValue(validationStateTextMappings.get((String) detail.get(key)));
                        cell.setCellStyle(validationStateStyles.get((String) detail.get(key)));
                    } else {
                        String value = (String) detail.get(key);
                        // TODO: Fix this upstream - remove overloading of source with initial values.
                        if (key.toLowerCase().trim().equals("source") && value!=null && value.contains("|")) {
                            // Crude hack, strip out leading overloaded key:value# from before first source.
                            value = value.substring(value.indexOf('|'));
                        } else if (key.toLowerCase().trim().equals("source") && value!=null && !value.contains("|") && value.trim().endsWith("#")) {
                            // Crude hack, strip out the loading overloaded key:value# when no source was added
                            value = "";
                        }
                        cell.setCellValue(processDetailString(value));
                    }
                } else {
                    if (validationStateTextMappings.containsKey(detail.get(key))) {
                        cell.setCellValue(validationStateTextMappings.get((String) record.get(key)));
                        cell.setCellStyle(validationStateStyles.get((String) detail.get(key)));
                    } else {
                        cell.setCellValue((String) record.get(key));
                    }
                }

                // Lastly apply the style based on validation state
                if (validationState.containsKey(key)) {
                    String value = (String)validationState.get(key);
                    row.getCell(colIndex).setCellStyle(validationStateStyles.get(value));
                }

                colIndex++;
            }
        }
    }

    /**
     * For now we use this to preprocess the strings but this should really be done in analysis.
     *
     * @param s
     * @return
     */
    private String processDetailString(String s) {
        if (s.startsWith("CORRECT:") || s.startsWith("UNABLE_TO_CURATE:") ||
                s.startsWith("UNABLE_DETERMINE_VALIDITY_OF:") || s.startsWith("FILLED_IN:")) {
            return s.substring(s.indexOf(':') + 2);
        }

        return s;
    }

    /**
     * Add the record from the analysis result to the spreadsheet.
     *
     * @param record the record with changes applied
     * @param markers contains the information about each actor run
     * @param rowIndex the row to add to
     */
    private void addRecord(Map record, Map markers, int rowIndex) {
        HSSFRow row = recordSheet.createRow(rowIndex);

        Map validationState = (LinkedHashMap)record.get("ValidationState");

        int colIndex = 0;
        for (String key : recordColumnMap.keySet()) {

            if (!record.containsKey(key)) {
                // handle case variation in terms
                key = this.normalizeKey(key, record);
            }
            // set the cell value for each column of the record
            HSSFCell cell = row.createCell(colIndex);
            cell.setCellValue((String) record.get(key));

            // apply the appropriate style (background color) based on values for validation state

            if (validationState.containsKey(key)) {
                String value = (String)validationState.get(key);
                row.getCell(colIndex).setCellStyle(validationStateStyles.get(value));
            }

            colIndex++;
        }

        // the last few columns of the record sheet contain information about each actor run

        for (String actorName : actorNames) {
            String marker = (String)markers.get(actorName);
            if (marker!=null) {
                row.createCell(colIndex).setCellValue(validationStateTextMappings.get(marker));
                row.getCell(colIndex).setCellStyle(validationStateStyles.get(marker));
            }
            colIndex++;
        }
    }

    /**
     * Initialize styles to be used when generating the spreadsheet. Each background color maps to
     * a particular validation state (CORRECT, CURATED, UNABLE_CURATE, UNABLE_DETERMINE_VALIDITY, etc)
     */
    private void initStyles() {
        HSSFPalette palette = wb.getCustomPalette();

        HSSFColor red = palette.findSimilarColor(255, 145, 145);
        HSSFColor green = palette.findSimilarColor(156, 255, 153);
        HSSFColor yellow = palette.findSimilarColor(255, 248, 153);
        HSSFColor yellow4 = palette.findSimilarColor(230, 230, 76);
        HSSFColor grey = palette.findSimilarColor(204, 204, 204);
        HSSFColor sun4 = palette.findSimilarColor(204, 204, 255);

        HSSFCellStyle unableCurateCellStyle = wb.createCellStyle();
        unableCurateCellStyle.setFillForegroundColor(red.getIndex());
        unableCurateCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFCellStyle correctCellStyle = wb.createCellStyle();
        correctCellStyle.setFillForegroundColor(green.getIndex());
        correctCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFCellStyle curatedCellStyle = wb.createCellStyle();
        curatedCellStyle.setFillForegroundColor(yellow.getIndex());
        curatedCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFCellStyle filledinCellStyle = wb.createCellStyle();
        filledinCellStyle.setFillForegroundColor(yellow4.getIndex());
        filledinCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        HSSFCellStyle unableDetermineValidityCellStyle = wb.createCellStyle();
        unableDetermineValidityCellStyle.setFillForegroundColor(grey.getIndex());
        unableDetermineValidityCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        validationStateStyles = new HashMap<String, HSSFCellStyle>();

        validationStateStyles.put("UNABLE_DETERMINE_VALIDITY", unableDetermineValidityCellStyle);
        validationStateStyles.put("CURATED", curatedCellStyle);
        validationStateStyles.put("CORRECT", correctCellStyle);
        validationStateStyles.put("FILLED_IN", filledinCellStyle);
        validationStateStyles.put("UNABLE_CURATE", unableCurateCellStyle);
    }

    private void initSheets() {
        recordSheet = wb.createSheet("Analysis Results");
        HSSFRow header = recordSheet.createRow(0);

            // initialize record sheets

            int columnIndex = 0;
            for (String columnName : recordColumnMap.values()) {
                header.createCell(columnIndex++).setCellValue(columnName);
            }

            for (String actorName : actorNames) {
                header.createCell(columnIndex++).setCellValue(actorName);
            }


        // initialize actor details sheets
        actorDetailsSheets = new HashMap<String, HSSFSheet>();

        for (String actorName : actorNames) {
                HSSFSheet sheet = wb.createSheet(actorName);
                HSSFRow detailHeader = sheet.createRow(0);

                int detailColumnIndex = 0;
                for (String columnName : actorDetailsColumnMap.values()) {
                    detailHeader.createCell(detailColumnIndex++).setCellValue(columnName);
                }

            actorDetailsSheets.put(actorName, sheet);
        }
    }

    /**
     * Helper method for saving the spreadsheet to a file.
     *
     * @param outFile the path to the output file
     */
    private void save(String outFile) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(outFile));
            wb.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { out.close(); } catch (IOException e) { }
        }
    }

    private void autoSizeColumns(HSSFSheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
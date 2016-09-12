package org.kurator.validation.actors.io;


import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.filteredpush.kuration.util.SpecimenRecordTypeConf;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.kurator.akka.KuratorActor;
import org.kurator.validation.data.AnalysisSummary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
*/
public class AnalysisSummaryTranslator extends KuratorActor {

    private int validCount = 0;

    //private Map<String, SpecimenRecord> _OriginalRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, SpecimenRecord> _validatedRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, HashMap> _recordMarkersMap = new HashMap<String, HashMap>();
    private HashMap<String, HashSet<String>> highlightedLabelsMap = new HashMap<String, HashSet<String>>();
    //private HashMap<String, HashSet> _recordDetailsMap = new HashMap<String, HashSet>();

    private String overallLabel = "Kuration Workflow";

    @Override
    protected void onStart() throws Exception {
        constructMaps();
    }

    @Override
    protected void onData(Object value) throws Exception {
        SpecimenRecord record = (SpecimenRecord) value;
        //System.out.println("record.prettyPrint() = " + record.prettyPrint());

        Set<Map<String, String>> actorSet = new HashSet<Map<String, String>>();

        //System.out.println("inputSpecimenRecord = " + record.toString());

        //detect what actor has modified the record, check condition is temp now
        if (record.get("geoRefStatus") != null) {
            Map<String, String> actorStatusMap = new HashMap<String, String>();
            actorStatusMap.put("actor", "GeoRefValidator");
            actorStatusMap.put("status", record.get("geoRefStatus"));
            actorStatusMap.put("comment", record.get("geoRefComment"));
            actorStatusMap.put("source", record.get("geoRefSource"));
            actorSet.add(actorStatusMap);
        }
        if (record.get(SpecimenRecord.SciName_Status_Label) != null) {
            Map<String, String> actorStatusMap = new HashMap<String, String>();
            actorStatusMap.put("actor", "ScientificNameValidator");
            actorStatusMap.put("status", record.get(SpecimenRecord.SciName_Status_Label));
            actorStatusMap.put("comment", record.get(SpecimenRecord.SciName_Comment_Label));
            actorStatusMap.put("source", record.get(SpecimenRecord.SciName_Source_Label));
            actorSet.add(actorStatusMap);
        }
        if (record.get("flwtStatus") != null) {
            Map<String, String> actorStatusMap = new HashMap<String, String>();
            actorStatusMap.put("actor", "FloweringTimeValidator");
            actorStatusMap.put("status", record.get("flwtStatus"));
            actorStatusMap.put("comment", record.get("flwtComment"));
            actorStatusMap.put("source", record.get("flwtSource"));
            actorSet.add(actorStatusMap);
        }
        if (record.get(SpecimenRecord.eventDate_Status_Label) != null) {
            Map<String, String> actorStatusMap = new HashMap<String, String>();
            actorStatusMap.put("actor", "EventDateValidator");
            actorStatusMap.put("status", record.get(SpecimenRecord.eventDate_Status_Label));
            actorStatusMap.put("comment", record.get(SpecimenRecord.eventDate_Comment_Label));
            actorStatusMap.put("source", record.get(SpecimenRecord.eventDate_Source_Label));
            actorSet.add(actorStatusMap);
        }         
        if (record.get("dateStatus") != null) {
            Map<String, String> actorStatusMap = new HashMap<String, String>();
            actorStatusMap.put("actor", "DateValidator");
            actorStatusMap.put("status", record.get("dateStatus"));
            actorStatusMap.put("comment", record.get("dateComment"));
            actorStatusMap.put("source", record.get("dateSource"));
            actorSet.add(actorStatusMap);
        }
        if (record.get("borStatus") != null) {
            Map<String, String> actorStatusMap = new HashMap<String, String>();
            actorStatusMap.put("actor", "BasisOfRecordValidator");
            actorStatusMap.put("status", record.get("borStatus"));
            actorStatusMap.put("comment", record.get("borComment"));
            actorStatusMap.put("source", record.get("borSource"));
            actorSet.add(actorStatusMap);
        }

        //remove a set of added fields in the record which have been read before
        HashSet<String> removeLables = new HashSet<String>();
        removeLables.add("geoRefStatus");removeLables.add("geoRefComment");removeLables.add("geoRefSource");
        removeLables.add(SpecimenRecord.SciName_Status_Label);removeLables.add(SpecimenRecord.SciName_Comment_Label);removeLables.add(SpecimenRecord.SciName_Source_Label);
        removeLables.add("flwtStatus");removeLables.add("flwtComment");removeLables.add("flwtSource");
        removeLables.add(SpecimenRecord.eventDate_Status_Label);removeLables.add(SpecimenRecord.eventDate_Comment_Label);removeLables.add(SpecimenRecord.eventDate_Source_Label);
        removeLables.add("dateStatus");removeLables.add("dateComment");removeLables.add("dateSource");
        removeLables.add("borStatus");removeLables.add("borComment");removeLables.add("borSource");
        for (String label:removeLables){
            if (record.keySet().contains(label)) record.remove(label);
        }

        //start
        HashMap<String, String> markers = new HashMap<String, String>();    //Construct the appended field (markers) of summary

        HashSet<HashMap> detailSet = new HashSet<HashMap>();
        Iterator it =  actorSet.iterator();
        while (it.hasNext()){
            HashMap<String, String> eachActorStatusMap = (HashMap)it.next();

            //set the markers first
            String marker = null;
            if(eachActorStatusMap.get("status").equals(CurationComment.CORRECT.toString())){
                marker = "CORRECT";
            }else if(eachActorStatusMap.get("status").equals(CurationComment.CURATED.toString())) {
                marker = "CURATED";
            }else if(eachActorStatusMap.get("status").equals(CurationComment.FILLED_IN.toString())){
                marker = "FILLED_IN";
            }else if(eachActorStatusMap.get("status").equals(CurationComment.UNABLE_CURATED.toString())){
                marker = "UNABLE_CURATE";
            }else if(eachActorStatusMap.get("status").equals(CurationComment.UNABLE_DETERMINE_VALIDITY.toString())){
                marker = "UNABLE_DETERMINE_VALIDITY";  //highlight = "UNABLE_CURATE_FIELD";
            }else {
                System.out.println(" start comment type is wrong");
            }

            markers.put(eachActorStatusMap.get("actor"), marker);
            HashSet<String> highlightLabels = highlightedLabelsMap.get(eachActorStatusMap.get("actor"));


            //TODO: may have problem if original record arrives late
            detailSet.add(constructDetail(record, eachActorStatusMap, marker, highlightLabels));
            //highlights.put(record.get("actor"),highlight);
        }

        //calculate the overall marker, comparing the new one and the one in the record
        String wfmarker = "CORRECT";
        for (String lable : markers.keySet()){
            String mk = markers.get(lable);
            if (mk.equals("UNABLE_CURATE") || wfmarker.equals("UNABLE_CURATE")){
                wfmarker = "UNABLE_CURATE";
            }else {
                if (mk.equals("UNABLE_DETERMINE_VALIDITY") || wfmarker.equals("UNABLE_DETERMINE_VALIDITY")){
                    wfmarker = "UNABLE_DETERMINE_VALIDITY";
                }else {
                    if (mk.equals("CURATED") && wfmarker.equals("CURATED")) {
                        wfmarker = "CURATED";
                    } else wfmarker = "CORRECT";
                }
            }
        }

        markers.put(overallLabel, wfmarker);           //put the overall marker in the same map
        //_recordMarkersMap.put(record.get("id"), markers);
        //_recordDetailsMap.put(record.get("id"), detailSet);
        //_validatedRecordMap.put(record.get("id"), record);

        //add validationStatus in record
        HashMap validationState = new HashMap<String, String>();
        for (HashMap item: detailSet){
            validationState.putAll((Map) item.get("ValidationState"));
        }

        //record.put("ValidationState", validationState);

        //BasicDBObject recordObject = new BasicDBObject("Record", record);

        HashMap<String, Object> modifiedRecord = new HashMap<String, Object>();
        for (String label : record.keySet()){
            modifiedRecord.put(label,record.get(label));
        }
        modifiedRecord.put("ValidationState", validationState);

        AnalysisSummary summary = new AnalysisSummary(modifiedRecord, markers, detailSet);
        broadcast(summary);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    private HashMap constructDetail(SpecimenRecord record, HashMap<String, String> actorDetail, String marker, HashSet highlights){
        HashMap<String, Object> detailRecord = new HashMap<String, Object>();
        HashMap<String, String> validationState = new HashMap<String, String>();
        //SpecimenRecord validatedRecord = _validatedRecordMap.get(record.get("id"));
        //todo:assume origianl record and validated record have the same arity

        detailRecord.put("Actor Name", actorDetail.get("actor"));
        detailRecord.put("Comment", actorDetail.get("comment"));
        //get the original value
        HashMap<String, String> originMap = new HashMap<String, String>();

        /*//old code that put old valud in the source field
        if(source != null && source.contains("#")){
            //not sure how to handle the case source = "...:...#", i.e., no source case
            if(source.substring(source.length()-1,source.length()).equals("#"))  source += " ";
            String[] sub = source.split("#");
            for(int i = 0; i < sub.length-1; i++){
                //to handle null value
                String[] each = sub[i].split(":");
                try {
                    if (each.length > 1) originMap.put(each[0], each[1]);
                    else originMap.put(each[0], null);
                } catch(ArrayIndexOutOfBoundsException e){
                    System.out.println("source = " + source);
                }
            }
            detailRecord.put("Source", sub[sub.length-1]);
        }else{
            detailRecord.put("Source", actorDetail.get("source"));
        }*/

        if(record.containsKey(SpecimenRecord.Original_SciName_Label)) originMap.put(SpecimenRecord.dwc_scientificName, record.get(SpecimenRecord.Original_SciName_Label));
        if(record.containsKey(SpecimenRecord.Original_Authorship_Label)) originMap.put(SpecimenRecord.dwc_scientificNameAuthorship, record.get(SpecimenRecord.Original_Authorship_Label));
        if(record.containsKey(SpecimenRecord.Original_EventDate_Label)) originMap.put(SpecimenRecord.dwc_eventDate, record.get(SpecimenRecord.Original_EventDate_Label));
        if(record.containsKey(SpecimenRecord.Original_Latitude_Label)) originMap.put(SpecimenRecord.dwc_decimalLatitude, record.get(SpecimenRecord.Original_Latitude_Label));
        if(record.containsKey(SpecimenRecord.Original_Longitude_Label)) originMap.put(SpecimenRecord.dwc_decimalLongitude, record.get(SpecimenRecord.Original_Longitude_Label));
        if(record.containsKey(SpecimenRecord.Original_BasisOfRecord_Label)) originMap.put(SpecimenRecord.dwc_basisOfRecord, record.get(SpecimenRecord.Original_BasisOfRecord_Label));

        detailRecord.put("Source", actorDetail.get("source"));
        detailRecord.put("Actor Result", marker);

        // TODO: Need to replace these magic strings with referenced constants.
        for (String label : record.keySet()) {
            if (highlights.contains(label)){
                validationState.put(label, marker);
                //four different cases have different content
                if (marker.equals("CORRECT")) detailRecord.put(label, "CORRECT: " + record.get(label));
                //no original record is available
                //else if (marker.equals("CURATED")) detailRecord.put(label, "yellow: WAS: " + record.get(label) + " CHANGED TO: "  + record.get("label"));
                else if (marker.equals("CURATED") || marker.equals("FILLED_IN")){
                    //System.out.println("source = " + actorDetail.get("source"));
                    //System.out.println("marker = " + marker);
                    //System.out.println("originMap = " + originMap);
                    //System.out.println("record = " + record);
                    //System.out.println("label = " + label);
                    //System.out.println("originMap(label) = " + originMap.get(label));
                    if (originMap.containsKey(label) && !originMap.get(label).equals(record.get(label))) {
                    	String oldVal =  originMap.get(label);
                    	if (oldVal==null || oldVal.length()==0) {
                    		oldVal = "EMPTY";
                    	}
                        detailRecord.put(label, "WAS: " + oldVal + "; CHANGED TO: " + record.get(label));
                    } else {
                        detailRecord.put(label, "CORRECT: " + record.get(label));
                    }
                }
                else if (marker.equals("UNABLE_CURATE")) detailRecord.put(label, "UNABLE_TO_CURATE: " + record.get(label));
                else if (marker.equals("UNABLE_DETERMINE_VALIDITY")) detailRecord.put(label, "UNABLE_DETERMINE_VALIDITY_OF: " + record.get(label));
                else System.out.println("detail comment ("+marker+") type is not known");
            }else{
                //detailRecord.put(label, "");
            }

        }

        detailRecord.put("ValidationState", validationState);
        return detailRecord;
    }

    private void constructMaps() {

        String latitudeLabel;
        String longitudeLabel;
        String floweringTimeLabel;
        String scientificNameLabel;
        String scientificNameAuthorLabel;
        String eventDateLabel;
        String basisOfRecordLabel;

        SpecimenRecordTypeConf specimenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

        scientificNameLabel = specimenRecordTypeConf.getLabel("ScientificName");
        if(scientificNameLabel == null) scientificNameLabel = "scientificName";

        // TODO: The repeated scientificNameLabel = ... statements below feel like a copy/paste error.
        scientificNameAuthorLabel = specimenRecordTypeConf.getLabel("ScientificNameAuthorship");
        if(scientificNameAuthorLabel == null) scientificNameLabel = "scientificNameAuthorship";

        floweringTimeLabel = specimenRecordTypeConf.getLabel("ReproductiveCondition");
        if(floweringTimeLabel == null) scientificNameLabel = "reproductiveCondition";

        longitudeLabel = specimenRecordTypeConf.getLabel("DecimalLongitude");
        if(longitudeLabel == null) scientificNameLabel = "decimalLongitude";

        latitudeLabel = specimenRecordTypeConf.getLabel("DecimalLatitude");
        if(latitudeLabel == null) scientificNameLabel = "decimalLatitude";

        eventDateLabel = specimenRecordTypeConf.getLabel("EventDate");
        if(eventDateLabel == null) scientificNameLabel = "eventDate";

        basisOfRecordLabel = specimenRecordTypeConf.getLabel("BasisOfRecord");
        if(basisOfRecordLabel == null) scientificNameLabel = "basisOfRecord";


        HashSet<String> fset = new HashSet<String>();
        fset.add(floweringTimeLabel);
        highlightedLabelsMap.put("FloweringTimeValidator", fset);

        HashSet<String> gset = new HashSet<String>();
        gset.add(latitudeLabel);
        gset.add(longitudeLabel);
        highlightedLabelsMap.put("GeoRefValidator", gset);

        HashSet<String> sset = new HashSet<String>();
        sset.add(scientificNameLabel);
        sset.add(scientificNameAuthorLabel);
        highlightedLabelsMap.put("ScientificNameValidator", sset);
        
        HashSet<String> edset = new HashSet<String>();
        edset.add(eventDateLabel);
        highlightedLabelsMap.put("EventDateValidator", edset);        

        HashSet<String> dset = new HashSet<String>();
        dset.add(eventDateLabel);
        highlightedLabelsMap.put("DateValidator", dset);
        
        HashSet<String> borset = new HashSet<String>();
        borset.add(basisOfRecordLabel);
        highlightedLabelsMap.put("BasisOfRecordValidator", borset);        
        
    }


}

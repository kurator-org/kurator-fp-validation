package org.kurator.validation.data;

import org.filteredpush.kuration.util.SpecimenRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lowery on 7/11/16.
 */
public class AnalysisSummary {
    private HashMap<String, Object> record;
    private HashMap<String, String> markers;
    private HashSet<HashMap> detailSet;

    public AnalysisSummary(HashMap<String, Object> record, HashMap<String, String> markers, HashSet<HashMap> detailSet) {
        this.record = record;
        this.markers = markers;
        this.detailSet = detailSet;
    }

    public HashMap<String, Object> getRecord() {
        return record;
    }

    public HashMap<String, String> getMarkers() {
        return markers;
    }

    public HashSet<HashMap> getDetailSet() {
        return detailSet;
    }

    public JSONObject toJsonObject() {
        //add validationStatus in record
        HashMap validationState = new HashMap<String, String>();
        for (HashMap item : detailSet) {
            validationState.putAll((Map) item.get("ValidationState"));
        }

        //record.put("ValidationState", validationState);

        //BasicDBObject recordObject = new BasicDBObject("Record", record);

        HashMap<String, Object> modifiedRecord = new HashMap<String, Object>();
        for (String label : record.keySet()) {
            modifiedRecord.put(label, record.get(label));
        }
        modifiedRecord.put("ValidationState", validationState);

        JSONObject obj = new JSONObject();
        obj.put("Record", modifiedRecord);
        obj.put("Markers", markers);

        JSONArray detailList = new JSONArray();
        for (HashMap item : detailSet) {
            detailList.add(item);
        }
        obj.put("ActorDetails", detailList);

        return obj;
    }
}

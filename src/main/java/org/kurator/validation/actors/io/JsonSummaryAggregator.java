package org.kurator.validation.actors.io;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.messages.EndOfStream;
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
public class JsonSummaryAggregator extends KuratorActor {

    int invoc = 0;
    private int reportSize = 1000;
    //private final OutputStreamWriter ost;

    public String filePath;
    private File file;

    private int validCount = 0;
    private OutputStreamWriter _outputFile;
    private boolean firstRecord = true;

    //private Map<String, SpecimenRecord> _OriginalRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, SpecimenRecord> _validatedRecordMap = new HashMap<String, SpecimenRecord>();
    //private Map<String, HashMap> _recordMarkersMap = new HashMap<String, HashMap>();
    private HashMap<String, HashSet<String>> highlightedLabelsMap = new HashMap<String, HashSet<String>>();
    //private HashMap<String, HashSet> _recordDetailsMap = new HashMap<String, HashSet>();

    private String overallLabel = "Kuration Workflow";

    @Override
    protected void onStart() throws Exception {
        if (filePath == null) {
            file = File.createTempFile("output_", ".json");
        } else {
            file = new File(filePath);
        }
        _outputFile = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        _outputFile.write("[");
    }

    @Override
    protected void onData(Object value) throws Exception {
        if (value instanceof AnalysisSummary) {
            AnalysisSummary summary = (AnalysisSummary) value;

            try {
                //_outputFile.write("["+obj.toString()+"]");

                if (firstRecord) {
                    firstRecord = false;
                } else {
                    _outputFile.write(",\n");
                }

                String json = summary.toJsonObject().toJSONString();
                _outputFile.write(json);

                _outputFile.flush();
                    /*
                    JSONObject obj1 = new JSONObject();
                    obj.put("name", "mkyong.com");

                    FileWriter file = new FileWriter("/home/tianhong/data/test2.json");
                    file.write(obj1.toJSONString());
                    file.flush();
                    file.close();*/

            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            validCount++;
        }
    }

    @Override
    protected void onEndOfStream(EndOfStream eos) throws Exception {
        Map<String, String> outputs = new HashMap<String, String>();
        outputs.put("outputfile", file.getAbsolutePath());
        outputs.put("workspace", file.getParent());
        broadcast(outputs);

        super.onEndOfStream(eos);
    }

    @Override
    protected void onEnd() throws Exception {
        try {
            _outputFile.write("]");
            _outputFile.close();

            publishArtifact("output_json", file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Wrote out " + validCount + " records");
    }
}

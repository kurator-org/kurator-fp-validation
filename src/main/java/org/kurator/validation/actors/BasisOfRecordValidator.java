package org.kurator.validation.actors;

import org.filteredpush.kuration.interfaces.IStringValidationService;
import org.filteredpush.kuration.services.BasisOfRecordValidationService;
import org.filteredpush.kuration.util.*;
import org.kurator.akka.KuratorActor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Created by lowery on 6/12/16.
 */
public class BasisOfRecordValidator extends KuratorActor {
    private String singleServiceClassQN = "org.filteredpush.kuration.services.BasisOfRecordValidationService";

    private String basisOfRecordLabel;

    IStringValidationService basisOfRecordValidationService = null;
    private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
    private LinkedHashMap<String, TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String, TreeSet<SpecimenRecord>>();

    @Override
    protected void onInitialize() throws Exception {
        try {
            //initialize required label
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            basisOfRecordLabel = speicmenRecordTypeConf.getLabel("BasisOfRecord");
            if (basisOfRecordLabel == null) {
                throw new CurationException(getClass().getName() + " failed since the basisOfRecord label of the SpecimenRecordType is not set.");
            }

            //resolve service
            basisOfRecordValidationService = (IStringValidationService) new BasisOfRecordValidationService();

        } catch (CurationException e) {
            e.printStackTrace();
        }

        // handleScopeStart
        inputObjList.clear();
        inputDataMap.clear();
    }

    @Override
    protected void onData(Object value) throws Exception {
        if (value instanceof SpecimenRecord) {
            //if(dataLabelStr.equals(getCurrentToken().getLabel().toString())){

            SpecimenRecord inputSpecimenRecord = (SpecimenRecord) value;

            //System.err.println("datestart#"+inputSpecimenRecord.get("oaiid").toString() + "#" + System.currentTimeMillis());

            String basisOfRecord = inputSpecimenRecord.get(basisOfRecordLabel);
            //System.err.println("servicestart#"+ inputSpecimenRecord.get("oaiid").toString() + "#" + ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000);
            basisOfRecordValidationService.validateString(basisOfRecord);
            //System.err.println("servicesend#"+ inputSpecimenRecord.get("oaiid").toString() + "#" + ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000);

            CurationStatus curationStatus = basisOfRecordValidationService.getCurationStatus();

            //System.out.println("curationStatus = " + curationStatus);
            //System.out.println("basisOfRecordValidationService.getComment() = " + basisOfRecordValidationService.getComment());
            //System.out.println("basisOfRecordValidationService.getServiceName() = " + basisOfRecordValidationService.getServiceName());

            if (curationStatus == CurationComment.CURATED || curationStatus == CurationComment.FILLED_IN) {
                //replace the old value if curated
                //inputSpecimenRecord.put("eventDate", String.valueOf(basisOfRecordValidationService.getCorrectedDate()));
                String originalBasis = inputSpecimenRecord.get(SpecimenRecord.dwc_basisOfRecord);
                String newBasis = basisOfRecordValidationService.getCorrectedValue();
                if (originalBasis != null && originalBasis.length() != 0 && !originalBasis.equals(newBasis)) {
                    inputSpecimenRecord.put(SpecimenRecord.Original_BasisOfRecord_Label, originalBasis);
                    inputSpecimenRecord.put(SpecimenRecord.dwc_basisOfRecord, newBasis);
                }
            }

            CurationCommentType curationComment = CurationComment.construct(curationStatus, basisOfRecordValidationService.getComment(), basisOfRecordValidationService.getServiceName());
            updateAndSendRecord(inputSpecimenRecord, curationComment);
        }
    }

    private void updateAndSendRecord(SpecimenRecord result, CurationCommentType comment) {
        if (comment != null) {
            result.put(SpecimenRecord.borRef_Comment_Label, comment.getDetails());
            result.put(SpecimenRecord.borRef_Status_Label, comment.getStatus());
            result.put(SpecimenRecord.borRef_Source_Label, comment.getSource());
        } else {
            result.put(SpecimenRecord.borRef_Comment_Label, "None");
            result.put(SpecimenRecord.borRef_Status_Label, CurationComment.CORRECT.toString());
            result.put(SpecimenRecord.borRef_Source_Label, comment.getSource());
        }

        broadcast(result);
    }
}

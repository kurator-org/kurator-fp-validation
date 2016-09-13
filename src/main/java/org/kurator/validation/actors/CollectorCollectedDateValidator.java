package org.kurator.validation.actors;

/*
 * This code is an adaptation of akka.fp.InternalDateValidator
 * in the FP-Akka package as of 29Oct2014 (where Internal references internal to 
 * one record, in contrast to comparison of collecting events accross 
 * records.  Renamed to match comparison of collector against date collected.
 * 
 */

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

import org.filteredpush.kuration.interfaces.IExternalDateValidationService;
import org.filteredpush.kuration.interfaces.IInternalDateValidationService;
import org.filteredpush.kuration.services.InternalDateValidationService;
import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.CurationCommentType;
import org.filteredpush.kuration.util.CurationException;
import org.filteredpush.kuration.util.CurationStatus;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.filteredpush.kuration.util.SpecimenRecordTypeConf;
import org.kurator.akka.KuratorActor;

public class CollectorCollectedDateValidator extends KuratorActor {
    private String singleServiceClassQN = "org.filteredpush.kuration.services.InternalDateValidationService";

    private String collectorLabel;
    private String yearCollectedLabel;
    private String monthCollectedLabel;
    private String dayCollectedLabel;
    private String eventDateLabel;
    private String startDayOfYearLabel;
    private String verbatimEventDateLabel;
    private String modifiedLabel;

    IInternalDateValidationService singleDateValidationService = null;

    private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
    private LinkedHashMap<String, TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String, TreeSet<SpecimenRecord>>();

    @Override
    public void onInitialize() {
        try {
            //initialize required label
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            collectorLabel = speicmenRecordTypeConf.getLabel("RecordedBy");
            if (collectorLabel == null) {
                throw new CurationException(getName() + " failed since the RecordedBy label of the SpecimenRecordType is not set.");
            }

            yearCollectedLabel = speicmenRecordTypeConf.getLabel("YearCollected");
            if (yearCollectedLabel == null) {
                throw new CurationException(getName() + " failed since the YearCollected label of the SpecimenRecordType is not set.");
            }

            monthCollectedLabel = speicmenRecordTypeConf.getLabel("MonthCollected");
            if (monthCollectedLabel == null) {
                throw new CurationException(getName() + " failed since the MonthCollected label of the SpecimenRecordType is not set.");
            }

            dayCollectedLabel = speicmenRecordTypeConf.getLabel("DayCollected");
            if (dayCollectedLabel == null) {
                throw new CurationException(getName() + " failed since the DayCollected label of the SpecimenRecordType is not set.");
            }

            eventDateLabel = speicmenRecordTypeConf.getLabel("EventDate");
            if (eventDateLabel == null) {
                throw new CurationException(getName() + " failed since the eventDate label of the SpecimenRecordType is not set.");
            }

            startDayOfYearLabel = speicmenRecordTypeConf.getLabel("StartDayOfYear");
            if (startDayOfYearLabel == null) {
                throw new CurationException(getName() + " failed since the startDayOfYearLabel label of the SpecimenRecordType is not set.");
            }

            verbatimEventDateLabel = speicmenRecordTypeConf.getLabel("VerbatimEventDate");
            if (verbatimEventDateLabel == null) {
                throw new CurationException(getName() + " failed since the verbatimEventDate label of the SpecimenRecordType is not set.");
            }

            modifiedLabel = speicmenRecordTypeConf.getLabel("Modified");
            if (modifiedLabel == null) {
                throw new CurationException(getName() + " failed since the modified label of the SpecimenRecordType is not set.");
            }

            //resolve service
            this.singleServiceClassQN = singleServiceClassQN;
            singleDateValidationService = (IInternalDateValidationService) Class.forName(this.singleServiceClassQN).newInstance();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (CurationException e) {
            e.printStackTrace();
        }

        // handleScopeStart
        inputObjList.clear();
        inputDataMap.clear();
    }

    public String getName() {
        return "CollectionEventOutlierFinder";
    }

    @Override
    public void onData(Object value) throws Exception {

        if (value instanceof SpecimenRecord) {

            SpecimenRecord inputSpecimenRecord = (SpecimenRecord) value;


            //System.err.println("datestart#"+inputSpecimenRecord.get("oaiid").toString() + "#" + System.currentTimeMillis());

            String eventDate = inputSpecimenRecord.get(eventDateLabel);
            String collector = inputSpecimenRecord.get(collectorLabel);
            String year = inputSpecimenRecord.get(yearCollectedLabel);
            String month = inputSpecimenRecord.get(monthCollectedLabel);
            String day = inputSpecimenRecord.get(dayCollectedLabel);
            String startDayOfYear = inputSpecimenRecord.get(startDayOfYearLabel);
            String verbatimEventDate = inputSpecimenRecord.get(verbatimEventDateLabel);
            String modified = inputSpecimenRecord.get(modifiedLabel);

            //System.err.println("servicestart#"+ inputSpecimenRecord.get("oaiid").toString() + "#" + ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000);
            singleDateValidationService.validateDate(eventDate, verbatimEventDate, startDayOfYear, year, month, day, modified, collector);
            //System.err.println("servicesend#"+ inputSpecimenRecord.get("oaiid").toString() + "#" + ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()/1000);

            CurationCommentType curationComment = null;
            CurationStatus curationStatus = singleDateValidationService.getCurationStatus();

            //System.out.println("curationStatus = " + curationStatus);
            //System.out.println("singleDateValidationService.getComment() = " + singleDateValidationService.getComment());
            //System.out.println("singleDateValidationService.getServiceName() = " + singleDateValidationService.getServiceName());

            if (curationStatus == CurationComment.CURATED) {
                //replace the old value if curated
                //inputSpecimenRecord.put("eventDate", String.valueOf(singleDateValidationService.getCorrectedDate()));
                String originalDate = inputSpecimenRecord.get(SpecimenRecord.dwc_eventDate);
                String newDate = singleDateValidationService.getCorrectedDate();
                if(originalDate != null && originalDate.length() != 0 &&  !originalDate.equals(newDate)){
                    inputSpecimenRecord.put(SpecimenRecord.Original_EventDate_Label, originalDate);
                    inputSpecimenRecord.put(SpecimenRecord.dwc_eventDate, newDate);
                }
            }
            if (curationStatus == CurationComment.FILLED_IN) {
                //provide the filled in value
                //inputSpecimenRecord.put("eventDate", String.valueOf(singleDateValidationService.getCorrectedDate()));
                String originalDate = inputSpecimenRecord.get(SpecimenRecord.dwc_eventDate);
                String newDate = singleDateValidationService.getCorrectedDate();
                if(originalDate != null && originalDate.length() != 0 &&  !originalDate.equals(newDate)){
                    inputSpecimenRecord.put(SpecimenRecord.Original_EventDate_Label, originalDate);
                    inputSpecimenRecord.put(SpecimenRecord.dwc_eventDate, newDate);
                } else if(originalDate == null || originalDate.length() == 0) { 
                    inputSpecimenRecord.put(SpecimenRecord.Original_EventDate_Label, "");
                    inputSpecimenRecord.put(SpecimenRecord.dwc_eventDate, newDate);
                }
            }

            curationComment = CurationComment.construct(curationStatus, singleDateValidationService.getComment(), singleDateValidationService.getServiceName());

            updateAndSendRecord(inputSpecimenRecord, curationComment);
        }
    }

    private void updateAndSendRecord(SpecimenRecord result,
            CurationCommentType comment) {

        if (comment != null) {
            result.put(SpecimenRecord.date_Comment_Label, comment.getDetails());
            result.put(SpecimenRecord.date_Status_Label, comment.getStatus());
            result.put(SpecimenRecord.date_Source_Label, comment.getSource());
        } else {
            result.put(SpecimenRecord.date_Comment_Label, "None");
            result.put(SpecimenRecord.date_Status_Label, CurationComment.CORRECT.toString());
            result.put(SpecimenRecord.date_Source_Label, comment.getSource());
        }

        broadcast(result);
    }

}

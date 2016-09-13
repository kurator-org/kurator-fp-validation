/** EventDateValidator.java
 * 
 * Copyright 2016 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kurator.validation.actors;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.filteredpush.kuration.util.CurationComment;
import org.filteredpush.kuration.util.CurationCommentType;
import org.filteredpush.kuration.util.CurationException;
import org.filteredpush.kuration.util.CurationStatus;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.filteredpush.kuration.util.SpecimenRecordTypeConf;
import org.filteredpush.kuration.validators.DateValidator;
import org.kurator.akka.KuratorActor;
import org.kurator.akka.data.CurationStep;

/**
 * Validation for dwc:Event terms, including comparison of eventDate with atomic
 * components and population of an empty eventDate from atomic components. 
 * 
 */
public class EventDateValidator extends KuratorActor {
    private String singleServiceClassQN = "org.filteredpush.kuration.services.EventDateValidationService";

    private String eventDateLabel;
    private String eventTimeLabel;
    private String yearLabel;
    private String monthLabel;
    private String dayLabel;
    private String startDayOfYearLabel;
    private String endDayOfYearLabel;
    private String verbatimEventDateLabel;

    private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
    private LinkedHashMap<String, TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String, TreeSet<SpecimenRecord>>();

    @Override
    public void onInitialize() {
        try {
            //initialize required labels
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

			eventDateLabel = speicmenRecordTypeConf.getLabel("EventDate");
			if (eventDateLabel == null) {
				throw new CurationException(getName() + " failed since the eventDate label of the SpecimenRecordType is not set.");
			}

			eventTimeLabel = speicmenRecordTypeConf.getLabel("EventTime");
			if (eventTimeLabel == null) {
				throw new CurationException(getName() + " failed since the eventTime label of the SpecimenRecordType is not set.");
			} 

			yearLabel = speicmenRecordTypeConf.getLabel("YearCollected");
			if (yearLabel == null) {
				throw new CurationException(getName() + " failed since the YearCollected label of the SpecimenRecordType is not set.");
			}

			monthLabel = speicmenRecordTypeConf.getLabel("MonthCollected");
			if (monthLabel == null) {
				throw new CurationException(getName() + " failed since the MonthCollected label of the SpecimenRecordType is not set.");
			}

			dayLabel = speicmenRecordTypeConf.getLabel("DayCollected");
			if (dayLabel == null) {
				throw new CurationException(getName() + " failed since the DayCollected label of the SpecimenRecordType is not set.");
			}

			startDayOfYearLabel = speicmenRecordTypeConf.getLabel("StartDayOfYear");
			if (startDayOfYearLabel == null) {
				throw new CurationException(getName() + " failed since the startDayOfYearLabel label of the SpecimenRecordType is not set.");
			}

			endDayOfYearLabel = speicmenRecordTypeConf.getLabel("EndDayOfYear");
			if (endDayOfYearLabel == null) {
				throw new CurationException(getName() + " failed since the endDayOfYearLabel label of the SpecimenRecordType is not set.");
			}                

			verbatimEventDateLabel = speicmenRecordTypeConf.getLabel("VerbatimEventDate");
			if (verbatimEventDateLabel == null) {
				throw new CurationException(getName() + " failed since the verbatimEventDate label of the SpecimenRecordType is not set.");
			}

        } catch (CurationException e) {
            e.printStackTrace();
        }

        // handleScopeStart
        inputObjList.clear();
        inputDataMap.clear();
    }

    public String getName() {
        return "EventDateValidator";
    }

    @Override
    public void onData(Object value) throws Exception {

        if (value instanceof SpecimenRecord) {

            SpecimenRecord inputSpecimenRecord = (SpecimenRecord) value;

			String eventDate = inputSpecimenRecord.get(eventDateLabel);
			String eventTime = inputSpecimenRecord.get(eventTimeLabel);
			String year = inputSpecimenRecord.get(yearLabel);
			String month = inputSpecimenRecord.get(monthLabel);
			String day = inputSpecimenRecord.get(dayLabel);
			String startDayOfYear = inputSpecimenRecord.get(startDayOfYearLabel);
			String endDayOfYear = inputSpecimenRecord.get(endDayOfYearLabel);
			String verbatimEventDate = inputSpecimenRecord.get(verbatimEventDateLabel);

			CurationStep result = DateValidator.validateEventConsistency(eventDate, year, month, day, startDayOfYear, endDayOfYear, eventTime, verbatimEventDate);

			List<String> curationComments = result.getCurationComments();
			StringBuffer comments = new StringBuffer();
			Iterator<String> i = curationComments.iterator();
			String separator = "";
			while (i.hasNext()) { 
				comments.append(separator).append(i.next());
				if (separator.length()==0) { separator = " | "; }
			}
			List<String> curationStates = result.getCurationStates();
			Map<String,String> finalElementValues = result.getFinalElementValues();
			String specification = result.getValidationMethodSpecification();

			CurationCommentType curationComment = null;
			// treat curation status as the last in the list of curation states.
			CurationStatus curationStatus = new CurationStatus(curationStates.get(curationStates.size()-1));

			if (curationStates.contains(CurationComment.FILLED_IN)) {
				//provide the filled in value 
				String originalDate = inputSpecimenRecord.get(SpecimenRecord.dwc_eventDate);
				String newDate = finalElementValues.get("eventDate");
				if(originalDate != null && originalDate.length() != 0 &&  !originalDate.equals(newDate)){
					inputSpecimenRecord.put(SpecimenRecord.Original_EventDate_Label, originalDate);
					inputSpecimenRecord.put(SpecimenRecord.dwc_eventDate, newDate);
				} else if(originalDate == null || originalDate.length() == 0) { 
					inputSpecimenRecord.put(SpecimenRecord.Original_EventDate_Label, "");
					inputSpecimenRecord.put(SpecimenRecord.dwc_eventDate, newDate);
				}
			}                

			// Curated follows (and trumps) filled in here where only one curation status is used.
			if (curationStatus.equals(CurationComment.CURATED)) {
				//replace the old value if curated
				String originalDate = inputSpecimenRecord.get(SpecimenRecord.dwc_eventDate);
				String newDate = finalElementValues.get("eventDate");
				if(originalDate != null && originalDate.length() != 0 &&  !originalDate.equals(newDate)){
					inputSpecimenRecord.put(SpecimenRecord.Original_EventDate_Label, originalDate);
					inputSpecimenRecord.put(SpecimenRecord.dwc_eventDate, newDate);
				}
			}

			curationComment = CurationComment.construct(curationStatus, comments.toString(), DateValidator.class.getName());

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
            result.put(SpecimenRecord.date_Source_Label, singleServiceClassQN);
        }

        broadcast(result);
    }

}

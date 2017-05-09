/** SingleCriterionStreamFilter.java
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

import org.filteredpush.kuration.util.*;
import org.kurator.akka.KuratorActor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Given a filter term and a filter condition, pass through only SpecimeRecords 
 * where the filter term is present and the value of the filter term matches in 
 * a case insensitive way the provided filter condition.
 * 
 * @author mole
 *
 */
public class SingleCriterionStreamFilter extends KuratorActor {
    private String singleServiceClassQN = "org.kurator.validation.actors.SingleCriterionStreamFilter";
    
    public String filterTerm = null;
    public String filterCondition = null;
    
    private String filterTermLabel = null;

    private LinkedList<SpecimenRecord> inputObjList = new LinkedList<SpecimenRecord>();
    private LinkedHashMap<String, TreeSet<SpecimenRecord>> inputDataMap = new LinkedHashMap<String, TreeSet<SpecimenRecord>>();

    @Override
    protected void onInitialize() throws Exception {
        try {
            //initialize required label
            SpecimenRecordTypeConf speicmenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

            filterTermLabel = speicmenRecordTypeConf.getLabel(filterTerm);
            if (filterTermLabel == null) {
                throw new CurationException(getClass().getName() + " failed since the filterTerm label " + filterTerm +" of the SpecimenRecordType is not set.");
            }

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
        	boolean send = false;
        	if (this.filterTerm!=null && this.filterCondition!=null) { 
        		String termValue = ((SpecimenRecord) value).get(filterTerm);
        		if (termValue.trim().equalsIgnoreCase(filterCondition.trim())) { 
        			send = true;
        		}
        	}
            if (send) { 
        	    broadcast(value);
            }
        }
    }

}
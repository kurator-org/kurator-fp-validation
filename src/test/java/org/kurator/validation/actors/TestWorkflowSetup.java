/**
 * 
 */
package org.kurator.validation.actors;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kurator.akka.ActorConfig;
import org.kurator.akka.PythonActor;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.actors.CsvFileReader;
import org.kurator.akka.actors.CsvFileWriter;
import org.kurator.validation.IgnoreInIntegration;
import org.kurator.validation.actors.io.AnalysisSpreadsheetBuilder;
import org.kurator.validation.actors.io.AnalysisSummaryTranslator;
import org.kurator.validation.actors.io.JsonSummaryAggregator;

/**
 * @author mole
 *
 */
@Category(IgnoreInIntegration.class)
public class TestWorkflowSetup {

	@Test
	public void testAddActorsToWorkflow() {

		try { 
		        WorkflowRunner wr = new WorkflowRunner();

		        ActorConfig csvReader = wr.actor(CsvFileReader.class)
		                .param("recordClass", "org.kurator.validation.data.OrderedSpecimenRecord");

		        ActorConfig eventDateValidator = wr.actor(EventDateValidator.class)
		                .listensTo(csvReader);
		        
		        ActorConfig dateValidator = wr.actor(CollectorCollectedDateValidator.class)
		                .listensTo(eventDateValidator);

		        ActorConfig borValidator = wr.actor(BasisOfRecordValidator.class)
		                .listensTo(dateValidator);
		        
		        ActorConfig geoValidator = wr.actor(GEORefValidator.class)
		                .listensTo(borValidator);
		        
		        ActorConfig sciNameValidator = wr.actor(ScientificNameValidator.class)
		        		.param("authorityName", "WoRMS")
		                .listensTo(geoValidator);
		        
		        ActorConfig csvWriter = wr.actor(CsvFileWriter.class)
		                .listensTo(sciNameValidator);
		        
		        ActorConfig translator = wr.actor(AnalysisSummaryTranslator.class)
		                .listensTo(sciNameValidator);
		        
		        ActorConfig spreadhsheetWriter = wr.actor(AnalysisSpreadsheetBuilder.class)
		                .listensTo(translator);
		        
		        ActorConfig summaryWriter = wr.actor(JsonSummaryAggregator.class)
		                .listensTo(translator);
		        
		} catch (Exception e) { 
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testAddPythonActor() {
		try { 
			WorkflowRunner wr = new WorkflowRunner();

	        ActorConfig csvReader = wr.actor(PythonActor.class)
	                .param("module", "kurator_fp.outcome_stats")
	                .param("onData", "outcomestats");
	        
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
}

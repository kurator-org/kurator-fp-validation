package org.kurator.validation.actors;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
		TestCsvSpecimenFileWriter.class,
		TestInternalDateValidation.class, 
		TestInternalDateValidator.class,
		TestScientificNameValidator.class, 
		TestWorkflowSetup.class
		})
public class AllTests {

}

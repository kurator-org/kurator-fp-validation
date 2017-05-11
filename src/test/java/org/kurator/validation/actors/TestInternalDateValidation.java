package org.kurator.validation.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.experimental.categories.Category;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.YamlFileWorkflowRunner;
import org.kurator.validation.IntegrationTest;

@Category(IntegrationTest.class)
public class TestInternalDateValidation extends KuratorAkkaTestCase {

    static final String RESOURCE_PATH = "classpath:/org/kurator/validation/workflows/";

    private OutputStream outputBuffer;
    private Writer bufferWriter;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        outputBuffer = new ByteArrayOutputStream();
        bufferWriter = new OutputStreamWriter(outputBuffer);
    }

    public void testInternalDataValidation_OneSpecimenRecord() throws Exception {

        YamlFileWorkflowRunner wr = new YamlFileWorkflowRunner();
        wr.yamlFile(RESOURCE_PATH + "internal_date_validation.yaml");
        wr.apply("in", "src/test/resources/org/kurator/validation/data/one_specimen_record.csv");
        wr.apply("writer", bufferWriter);
        
        wr.run();

        String expected =
        		"country,CollectionCode,year,scientificName,scientificNameAuthorship,InstitutionCode,county,fieldNumber,decimalLatitude,eventDateStatus,eventDateComment,catalogNumber,eventDateSource,dateSource,origialEventDate,day,reproductiveCondition,locality,decimalLongitude,stateProvince,geodeticDatum,dateStatus,dateComment,recordedBy,month,DatasetName,Id,family,eventDate" + EOL +
                "United States,FilteredPush,2007,Taraxacum erythrospermum,auct.,DAV,Chelan,126,47.1384,\"Filled in \",dwc:eventDate does not contain a value. | dwc:eventDate does not contain a value. | dwc:eventDate specifies a date to a day or less.,100001,org.filteredpush.kuration.validators.DateValidator,eventDate:null# | year:2007# | month:6# | day:29# | Harvard List of Botanists | FilteredPush Entomologists List,,29,Flower:March;April;May;June;July;August,Wenatchee National Forest. South Cle Elum Ridge.,-120.9263,Washington,WGS84,\"Filled in \",Constructed event date from atomic parts | eventDate is consistent with atomic fields | Unable to find collector Megan A. Jensen in Harvard list of botanists. | Unable to get the Life span data of collector:Megan A. Jensen | Unable to lookup a lifespan for the collector Megan A. Jensen,Megan A. Jensen,6,SPNHCDEMO,926137834,Asteraceae,2007-06-29\n";
        /*		
        		"country,CollectionCode,year,scientificName,scientificNameAuthorship,InstitutionCode,county,fieldNumber,decimalLatitude,eventDateStatus,eventDateComment,catalogNumber,eventDateSource,dateSource,origialEventDate,day,reproductiveCondition,locality,decimalLongitude,stateProvince,geodeticDatum,dateStatus,dateComment,recordedBy,month,DatasetName,Id,family,eventDate" + EOL +
        		"United States,FilteredPush,2007,Taraxacum erythrospermum,auct.,DAV,Chelan,126,47.1384,\"Filled in \",dwc:eventDate does not contain a value. | dwc:eventDate specifies a date to a day or less.,100001,org.filteredpush.kuration.validators.DateValidator,eventDate:null# | year:2007# | month:6# | day:29# | Harvard List of Botanists | FilteredPush Entomologists List,,29,Flower:March;April;May;June;July;August,Wenatchee National Forest. South Cle Elum Ridge.,-120.9263,Washington,WGS84,\"Filled in \",Constructed event date from atomic parts | eventDate is consistent with atomic fields | Unable to find collector Megan A. Jensen in Harvard list of botanists. | Unable to get the Life span data of collector:Megan A. Jensen | Unable to lookup a lifespan for the collector Megan A. Jensen,Megan A. Jensen,6,SPNHCDEMO,926137834,Asteraceae,2007-6-29\n";
        		*/
        /*
            "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,eventDate,dateComment,dateStatus,dateSource" + EOL +
            "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,2007-6-29,Constructed event date from atomic parts | eventDate is consistent with atomic fields | Unable to find collector Megan A. Jensen in Harvard list of botanists. | Unable to get the Life span data of collector:Megan A. Jensen | Unable to lookup a lifespan for the collector Megan A. Jensen,Filled in,eventDate:null# | year:2007# | month:6# | day:29# | Harvard List of Botanists | FilteredPush Entomologists List" + EOL;
          */

        assertEquals(expected, outputBuffer.toString());
    }
}
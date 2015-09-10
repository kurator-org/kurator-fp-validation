package org.kurator.validation.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.kurator.akka.ActorConfig;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.actors.CsvFileReader;
import org.kurator.akka.actors.CsvFileWriter;

public class TestScientificNameValidator extends KuratorAkkaTestCase {

    private WorkflowRunner wr;
    private OutputStream outputBuffer;
    private ActorConfig csvReader;
    private ActorConfig csvWriter;
    private ActorConfig sciNameValidator;
    private Writer bufferWriter;

    @Override
    public void setUp() throws Exception {

        super.setUp();

        outputBuffer = new ByteArrayOutputStream();
        bufferWriter = new OutputStreamWriter(outputBuffer);

        wr = new WorkflowRunner();

        csvReader = wr.actor(CsvFileReader.class)
                .param("recordClass", "org.kurator.validation.data.OrderedSpecimenRecord");

        sciNameValidator = wr.actor(ScientificNameValidator.class)
                .listensTo(csvReader);

        csvWriter = wr.actor(CsvFileWriter.class)
                .listensTo(sciNameValidator);
    }

    public void testScientficNameValidator_OneRecord() throws Exception {

        csvReader.param("filePath", "src/test/resources/org/kurator/validation/data/one_specimen_record.csv" );
        csvWriter.param("outputWriter", bufferWriter);

        wr.run();

        String expected =
            "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,GUID,scinComment,scinStatus,scinSource" + EOL +
            "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,http://api.gbif.org/v1/species/5393872,\"| can't construct sciName from atomic fields | No match found in Catalog of Life. |  | The provided name: Taraxacum+erythrospermum has a match in the GlobalNames Resolver | No match found in Catalog of Life. | Fail to access GNI service | Got a valid result from GBIF checklistbank Backbone | The original SciName and Authorship are curated |  Authorship: Author Dissimilar Similarity: 0.14285714285714285 | Retaining original authorship string 'auct.' = of authors, meaning not intended as in the sense of Andrz. ex Besser\",NotCurated,scientificName:Taraxacum erythrospermum#scientificNameAuthorship:auct.# | Catalog Of Life | Catalog Of Life | Global Name Resolver | Catalog Of Life | Global Name Index | GBIF CheckListBank Backbone" + EOL;

        assertEquals(expected, outputBuffer.toString());
    }

    public void testScientficNameValidator_EightRecords() throws Exception {

       csvReader.param("filePath", "src/test/resources/org/kurator/validation/data/eight_specimen_records.csv" );
       csvWriter.param("outputWriter", bufferWriter);

       wr.run();

       String expected =
           "catalogNumber,recordedBy,fieldNumber,year,month,day,decimalLatitude,decimalLongitude,geodeticDatum,country,stateProvince,county,locality,family,scientificName,scientificNameAuthorship,reproductiveCondition,InstitutionCode,CollectionCode,DatasetName,Id,GUID,scinComment,scinStatus,scinSource" + EOL +
           "100001,Megan A. Jensen,126,2007,6,29,47.1384,-120.9263,WGS84,United States,Washington,Chelan,Wenatchee National Forest. South Cle Elum Ridge.,Asteraceae,Taraxacum erythrospermum,auct.,Flower:March;April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,926137834,http://api.gbif.org/v1/species/5393872,\"| can't construct sciName from atomic fields | No match found in Catalog of Life. |  | The provided name: Taraxacum+erythrospermum has a match in the GlobalNames Resolver | No match found in Catalog of Life. | Fail to access GNI service | Got a valid result from GBIF checklistbank Backbone | The original SciName and Authorship are curated |  Authorship: Author Dissimilar Similarity: 0.14285714285714285 | Retaining original authorship string 'auct.' = of authors, meaning not intended as in the sense of Andrz. ex Besser\",NotCurated,scientificName:Taraxacum erythrospermum#scientificNameAuthorship:auct.# | Catalog Of Life | Catalog Of Life | Global Name Resolver | Catalog Of Life | Global Name Index | GBIF CheckListBank Backbone" + EOL +
           "100002,G. Rink,2503,2003,7,27,-37.25,-108.68,WGS84,United States,Colorado,,Yucca House National Monument,Asteraceae,Acroptilon repens,(L.) DC.,Flower:March;April;May;June;July;August;September,DAV,FilteredPush,SPNHCDEMO,925533578,| can't construct sciName from atomic fields | Found and resolved synonym Acroptilon repens  in Catalog of Life service. |  Authorship: Exact Match Similarity: 1.0,Valid,scientificName:Acroptilon repens#scientificNameAuthorship:(L.) DC.# | Catalog Of Life | Catalog Of Life" + EOL +
           "100003,Mark Elvin,2938,1990,5,11,34.0,-117.0,WGS84,United States,California,San Bernardino,400 m north of Cushenbury Springs,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940716,\"| can't construct sciName from atomic fields | More than one (2) match in Catalog of Life service, may be homonym or hemihomonym. | Found accepted name Cirsium mohavense (Greene) Petr. in Catalog of Life service. |  Authorship: Exact Match Similarity: 1.0\",Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.# | Catalog Of Life | Catalog Of Life" + EOL +
           "100004,Mark Elvin,3000,1990,5,21,37.0,-118.0,WGS84,United States,California,,Northern end of The Owens Valle Bishop,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940765,\"| can't construct sciName from atomic fields | | can't construct sciName from atomic fields | More than one (2) match in Catalog of Life service, may be homonym or hemihomonym. | Found accepted name Cirsium mohavense (Greene) Petr. in Catalog of Life service. |  Authorship: Exact Match Similarity: 1.0\",Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.# | Catalog Of Life | scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.# | Catalog Of Life | Catalog Of Life" + EOL +
           "100005,Mark Elvin,2940,1990,5,12,34.0,-117.0,WGS84,United States,California,San Bernardino,l mi. NW of Lucerne Valley town center,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940674,\"| can't construct sciName from atomic fields | | can't construct sciName from atomic fields | More than one (2) match in Catalog of Life service, may be homonym or hemihomonym. | Found accepted name Cirsium mohavense (Greene) Petr. in Catalog of Life service. |  Authorship: Exact Match Similarity: 1.0\",Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.# | Catalog Of Life | scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.# | Catalog Of Life | Catalog Of Life" + EOL +
           "100006,Mark Elvin,1940,1990,5,20,36.0,-118.0,WGS84,United States,California,Kern,Weldon Rancheria,Asteraceae,Cirsium mohavense,(Greene) Petr.,Flower:June;July;August;September;October;November,DAV,FilteredPush,SPNHCDEMO,1024940053,\"| can't construct sciName from atomic fields | | can't construct sciName from atomic fields | More than one (2) match in Catalog of Life service, may be homonym or hemihomonym. | Found accepted name Cirsium mohavense (Greene) Petr. in Catalog of Life service. |  Authorship: Exact Match Similarity: 1.0\",Valid,scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.# | Catalog Of Life | scientificName:Cirsium mohavense#scientificNameAuthorship:(Greene) Petr.# | Catalog Of Life | Catalog Of Life" + EOL +
           "100007,Mark Elvin,606,1990,5,16,21.312,-157.8055,WGS84,United States,Hawaii,Honolulu,Honolulu 3115 Kaloaluiki Place,Asteraceae,Tragopogon porrifolius,L.,Flower:April;May;June;July;August,DAV,FilteredPush,SPNHCDEMO,927140834,| can't construct sciName from atomic fields | Found accepted name Tragopogon porrifolius L. in Catalog of Life service. |  Authorship: Exact Match Similarity: 1.0,Valid,scientificName:Tragopogon porrifolius#scientificNameAuthorship:L.# | Catalog Of Life | Catalog Of Life" + EOL +
           "100008,Joseph P. Tracy,107702,1973,7,31,40.0,-104.0,WGS84,United States,California,,Carlotta Northern Coast Ranges,Asteraceae,Logfia gallica,(L.) Coss. & Germ.,Flower:July;August; September,DAV,FilteredPush,SPNHCDEMO,127140835,| can't construct sciName from atomic fields | Found and resolved synonym Logfia gallica  in Catalog of Life service. |  Authorship: Author Dissimilar Similarity: 0.7333333333333333,Curated,scientificName:Logfia gallica#scientificNameAuthorship:Coss. & Germ.# | Catalog Of Life | Catalog Of Life" + EOL;

       assertEquals(expected, outputBuffer.toString());
   }
}

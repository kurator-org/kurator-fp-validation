# kurator-fp-validation
Actors and workflows derived from FP-Akka package at http://sourceforge.net/p/filteredpush/svn/HEAD/tree/trunk/FP-Akka

## Prerequisites ##

### Clone and build the FP-CurationServices project ###

    $ git clone https://github.com/FilteredPush/FP-KurationServices.git
    $ cd FP-KurationServices
    $ mvn clean install

### Clone and build the kurator-akka dependencies ###

Build the kurator-akka project first and kurator-validation second. 

    $ git clone https://github.com/kurator-org/kurator-akka.git
    $ git clone https://github.com/kurator-org/kurator-validation.git
    
    $ cd kurator-akka
    $ mvn clean install
    
    $ cd ../kurator-validation
    $ mvn clean install

### Install Jython and set environment variables ###

Download the Jython 2.7.1.b3 installer jar. Run the installer from the command line in the kurator home directory.

    $ java -jar jython-installer-2.7.1b3.jar

Select the standard installation when prompted (option 2) and when asked to provide the target directory enter "jython2.7.1b3". This will install jython to "/home/kurator/jython2.7.1b3".

Set the JYTHON_HOME environment variable to point the directory you installed to:

    $ export JYTHON_HOME=/path/to/jython
    
Lastly, the JYTHON_PATH environment variable should point to the directory containing python packages for workflow actors. (for example the packages directory found in the root of this project)

    $ export JYTHON_PATH=/path/to/packages
    
### Build kurator-fp-validation and run workflows ###

Build this project via maven:

    $ mvn clean package
    
This will produce a jar-with-dependencies in the target directory of this project. Run this jar from the command-line via the following:

    $ java -jar kurator-fp-validation-1.0.0-jar-with-dependencies.jar -f packages/kurator_fp/workflows/outcome_stats.yaml -p workspace=./dwca_date_validator_workspace -p configfile=packages/kurator-fp/config/stats.ini inputfile=packages/kurator-fp/data/occurrence_qc.json -p outputfile=outcomeStats.xlsx -l DEBUG
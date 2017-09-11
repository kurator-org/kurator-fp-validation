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

Download the [Jython 2.7.1.b3 installer jar](http://search.maven.org/remotecontent?filepath=org/python/jython-installer/2.7.1b3/jython-installer-2.7.1b3.jar). Run the installer from the command line in the kurator home directory.

    $ java -jar jython-installer-2.7.1b3.jar

Select the standard installation when prompted (option 2) and when asked to provide the target directory enter "jython2.7.1b3". This will install jython to "/home/kurator/jython2.7.1b3".

Set the JYTHON_HOME environment variable to point the directory you installed to:

    $ export JYTHON_HOME=/path/to/jython
    
Lastly, the JYTHON_PATH environment variable should point to the directory containing python packages for workflow actors. (for example the packages directory found in the root of this project)

    $ export JYTHON_PATH=/path/to/packages
    
### Pip install outcomeStats.py dependencies ###

Use the pip installer bundled with jython to install python dependencies for the Outcome Stats workflow:

    $ ./pip install --upgrade pip

    $ cd $JYHTON_HOME/bin
    $ ./pip install unidecode
    $ ./pip install unicodecsv
    $ ./pip install chardet
    $ ./pip install xlsxwriter
    $ ./pip install ConfigParser
    
An additional change to the configparser is required, the backports package directory is missing an __init__.py file after installing. Manually adding this file fixes the issue:

    $ cd $JYHTON_HOME/Lib/site-packages/backports
    $ touch __init__.py
    
### Build kurator-fp-validation and run workflows ###

Build this project via maven:

    $ mvn clean package
    
This will produce a jar-with-dependencies in the target directory of this project. Run this jar from the command-line via the following:

    $ java -jar target/kurator-fp-validation-1.0.0-jar-with-dependencies.jar -f packages/kurator_fp/workflows/outcome_stats.yaml -p configfile=packages/kurator_fp/config/stats.ini -p inputfile=packages/kurator_fp/data/occurrence_qc.json -p outputfile=outcomeStats.xlsx -l DEBUG
    
### Run outcomestats workflow from release jar ###

First create a local git clone of this project and switch to the outcomestats branch via:

    $ git clone https://github.com/kurator-org/kurator-fp-validation.git
    $ cd kurator-fp-validation
    $ git fetch
    $ git checkout outcomestats

Set an environment variable, KURATORHOME, that points to the project root directory (kurator-fp-validation) and add the packages subdirectory to the PYTHONPATH environment variable:

    $ export KURATORHOME=/home/lowery/kurator-fp-validation
    $ export PYTHONPATH=$KURATORHOME/packages
    
Next, download a copy of the latest fp-validation release of the kurator-akka jar: https://github.com/kurator-org/kurator-fp-validation/releases/download/v1.0.1/kurator-fp-validation-1.0.1-SNAPSHOT-jar-with-dependencies.jar

Run with the following arguments to test the outcomestats workflow:

    $ cd ~/Downloads
    $ java -Djava.library.path=$KURATORHOME/lib/native -jar kurator-fp-validation-1.0.1-SNAPSHOT-jar-with-dependencies.jar -f $KURATORHOME/packages/kurator_fp/workflows/outcome_stats.yaml -p configfile=$KURATORHOME/packages/kurator_fp/stats.ini -p inputfile=$KURATORHOME/packages/kurator_fp/occurrence_qc.json -p outputfile=stats.xlsx

This should create a new workspace in the current directory that contains a single outputfile, stats.xlsx. Example output for a successful run shown below:

    Started MakeWorkspace
    MakeWorkspace options: {}
    WrapUp options: {'outputfile': './workspace_c3661654-9731-11e7-a6a7-1c1b0d64ce0b/stats.xlsx', 'success': 1, 'artifacts': {'stats_report': './workspace_c3661654-9731-11e7-a6a7-1c1b0d64ce0b/stats.xlsx'}, 'workspace': './workspace_c3661654-9731-11e7-a6a7-1c1b0d64ce0b', 'message': 'Successfully generated outcome stats report from occurrence json', 'inputfile': '/home/lowery/kurator-fp-validation/packages/kurator_fp/occurrence_qc.json'}
    ### Finished Outcome Stats Workflow ###


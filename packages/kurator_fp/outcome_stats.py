#!/usr/bin/env python

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

__author__ = "Robert A. Morris"
__copyright__ = "Copyright 2016 President and Fellows of Harvard College"
__version__ = "outcome_stats.py 2016-07-06T16:15:37-0400"

from dwca_utils import response
from dwca_utils import setup_actor_logging
import os
import logging
import uuid
from OutcomeStats import *
from OutcomeFormats import *

def outcomestats(options):
    """Generic actor showing patterns for logging, input dictionary, and output dictionary
       with artifacts.
    options - a dictionary of parameters
        loglevel - level at which to log (e.g., DEBUG) (optional)
        workspace - path to a directory for the outputfile (optional)
        inputfile - full path to the input file (required)
        outputfile - name of the output file, without path (optional)
    returns a dictionary with information about the results
        workspace - actual path to the directory where the outputfile was written
        outputfile - actual full path to the output file
        success - True if process completed successfully, otherwise False
        message - an explanation of the results
        artifacts - a dictionary of persistent objects created
    """
    setup_actor_logging(options)
    print options
    logging.debug( 'Started %s' % __version__ )
    logging.debug( 'options: %s' % options )

    # Make a list for the response
    returnvars = ['workspace', 'outputfile', 'success', 'message', 'artifacts']

    # Make a dictionary for artifacts left behind
    artifacts = {}

    # outputs
    workspace = None
    outputfile = None
    success = False
    message = None

    #abspath = os.path.abspath(__file__)
    #dname = os.path.dirname(abspath)
    #os.chdir(dname)

    #print dname
    #print options['configfile']

    # inputs
    try:
        workspace = options['workspace']
    except:
        workspace = None

    if workspace is None or len(workspace)==0:
        workspace = './'

    try:
        inputfile = options['inputfile']
    except:
        inputfile = None

    if inputfile is None or len(inputfile)==0:
        message = 'No input file given'
        returnvals = [workspace, outputfile, success, message, artifacts]
        logging.debug('message:\n%s' % message)
        return response(returnvars, returnvals)

    if os.path.isfile(inputfile) == False:
        message = 'Input file not found'
        returnvals = [workspace, outputfile, success, message, artifacts]
        logging.debug('message:\n%s' % message)
        return response(returnvars, returnvals)

    try:
        outputfile = options['outputfile']
    except:
        outputfile = None
    if outputfile is None or len(outputfile)==0:
        outputfile='outcomeStats_'+str(uuid.uuid1())+'.xlsx'

    try:
        configfile = options['configfile']
    except:
        configfile = None
    if configfile is None or len(configfile)==0:
        message = 'No config file given'
        returnvals = [workspace, outputfile, success, message, artifacts]
        logging.debug('message:\n%s' % message)

    # Construct the output file path in the workspace
    outputfile = '%s/%s' % (workspace.rstrip('/'), outputfile)

    # Do the actual work now that the preparation is complete
    success = stats_to_xlsx(inputfile, outputfile, configfile)

    # Add artifacts to the output dictionary if all went well
    if success==True:
        artifacts['output_file'] = outputfile

    # Prepare the response dictionary
    returnvals = [workspace, outputfile, success, message, artifacts]
    logging.debug('Finishing %s' % __version__)
    return response(returnvars, returnvals)

def stats_to_xlsx(inputfile, outputfile, configfile):
    """Generic function with input and output.
    parameters:
        inputfile - the full path to the input file
        outputfile - the full path to the output file
        outputfile - the full path to the config file
    returns:
        success - True if the task is completed, otherwise False
    """
    # Check for required values
    if inputfile is None or len(inputfile)==0:
        logging.debug('No input file given in do_stuff()')
        return False

    if outputfile is None or len(outputfile)==0:
        logging.debug('No output file given in do_stuff()')
        return False

        # load entire jason file. (Note: syntactically it is a Dictionary !!! )
    with open(inputfile) as data_file:
        fpAkkaOutput = json.load(data_file)

        ###### In this test, both normalized and non-normalized statistics are shown
    origin1 = [0, 0]  # Validator names, from which cell addr set below has names for non-normalized data
    origin2 = [5, 0]  # Validator names, from which cell addr set below has names for non-normalized data
    workbook = xlsxwriter.Workbook(outputfile)  # xlsxwriter model of an xlsx spreadsheet
    worksheet = workbook.add_worksheet()  # should supply worksheet name, else defaults
    #   stats = OutcomeStats(workbook,worksheet,data_file,outfile,configFile,origin1,origin2)
    stats = OutcomeStats(configfile)
    worksheet.set_column(0, len(stats.getOutcomes()), 3 + stats.getMaxLength())
    #   print(stats.getOutcomes())
    outcomeFormats = OutcomeFormats({})
    formats = outcomeFormats.initFormats(workbook)  # shouldn't be attr of main class
    ###################################################
    #####createStats and stats2XLSX comprise the main #
    # processor filling the spreadheet cells       ####
    ###################################################
    # if stats are normalized, results are divided by number of records
    # otherwise, cells show total of the number of each outcome in the appropriate column
    normalized = True
    validatorStats = stats.createStats(fpAkkaOutput, ~normalized)
    validatorStatsNormalized = stats.createStats(fpAkkaOutput, normalized)

    outcomes = stats.getOutcomes()
    #   print("outcomes=", outcomes)
    validators = stats.getValidators()
    stats.stats2XLSX(workbook, worksheet, formats, validatorStats, origin1, outcomes, validators)
    stats.stats2XLSX(workbook, worksheet, formats, validatorStatsNormalized, origin2, outcomes, validators)

    workbook.close()

    # Success
    return True

def _getoptions():
    """Parse command line options and return them."""
    parser = argparse.ArgumentParser()

    help = 'full path to the input file (required)'
    parser.add_argument("-i", "--inputfile", help=help)

    help = 'directory for the output file (optional)'
    parser.add_argument("-w", "--workspace", help=help)

    help = 'output file name, no path (optional)'
    parser.add_argument("-o", "--outputfile", help=help)

    help = 'output file name, no path (optional)'
    parser.add_argument("-c", "--configfile", help=help)

    help = 'log level (e.g., DEBUG, WARNING, INFO) (optional)'
    parser.add_argument("-l", "--loglevel", help=help)

    return parser.parse_args()

def main():
    options = _getoptions()
    optdict = {}

    if options.inputfile is None or len(options.inputfile)==0:
        s =  'syntax:\n'
        s += 'python outcome_stats.py'
        s += ' -i ./data/eight_specimen_records.csv'
        s += ' -o test_ccber_mammals_dwc_archive.zip'
        s += ' -w ./workspace'
        s += ' -l DEBUG'
        print '%s' % s
        return

    optdict['inputfile'] = options.inputfile
    optdict['outputfile'] = options.outputfile
    optdict['workspace'] = options.workspace
    optdict['configfile'] = options.configfile
    optdict['loglevel'] = options.loglevel
    print 'optdict: %s' % optdict

    # Append distinct values of to vocab file
    response=dostuffer(optdict)
    print '\nresponse: %s' % response

if __name__ == '__main__':
    main()

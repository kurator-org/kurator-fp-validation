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

from actor_decorator import python_actor

from OutcomeStats import *
from OutcomeFormats import *

@python_actor
###def outcomestats(inputfile, outputfile, configfile, origincolumn, originrow):
def outcomestats(inputfile, outputfile, configfile, originrow, origincolumn):
    # load entire jason file. (Note: syntactically it is a Dictionary !!! )
    with open(inputfile) as data_file:
        fpAkkaOutput = json.load(data_file)

        ###### In this test, both normalized and non-normalized statistics are shown
###    origin1 = [0, 0]  # Validator names, from which cell addr set below has names for non-normalized data
###    origin2 = [5, 0]  # Validator names, from which cell addr set below has names for non-normalized data
    
###    origin1 = [origincolumn,originrow]
    origin1 = [origincolumn,originrow]
    workbook = xlsxwriter.Workbook(outputfile)  # xlsxwriter model of an xlsx spreadsheet
    worksheet = workbook.add_worksheet()  # should supply worksheet name, else defaults
    #   stats = OutcomeStats(workbook,worksheet,data_file,outfile,configFile,origin1,origin2)
    stats = OutcomeStats(configfile)
###    worksheet.set_column(0, len(stats.getOutcomes()), 3 + stats.getMaxLength())
###    worksheet.set_column(origincolumn, len(stats.getOutcomes()), 3 + stats.getMaxLength())
    worksheet.set_column(origincolumn, len(stats.getOutcomes()), 3 + stats.getMaxLength())
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
###    stats.stats2XLSX(workbook, worksheet, formats, validatorStatsNormalized, origin2, outcomes, validators)

    workbook.close()

def _getoptions():
    ''' Parse command line options and return them.'''
    parser = argparse.ArgumentParser()

    help = 'full path to the input file (required)'
    parser.add_argument("-i", "--inputfile", help=help)

    help = 'directory for the output file (optional)'
    parser.add_argument("-w", "--workspace", help=help)

    help = 'output file name, no path (required)'
    parser.add_argument("-o", "--outputfile", help=help)

    help = 'config file name, no path (required)'
    parser.add_argument("-c", "--configfile", help=help)

    help = 'log level (e.g., DEBUG, WARNING, INFO) (optional)'
    parser.add_argument("-l", "--loglevel", help=help)

    help = 'log level (e.g., DEBUG, WARNING, INFO) (optional)'
    parser.add_argument("-ocol", "--origincolumn", help=help)

    help = 'log level (e.g., DEBUG, WARNING, INFO) (optional)'
    parser.add_argument("-orow", "--originrow", help=help)

    return parser.parse_args()

def main():
    options = _getoptions()
    optdict = {}

###    if options.inputfile is None or len(options.inputfile)==0:
###        s =  'syntax:\n'
###        s += 'python outcome_stats.py'
###        s += ' -i ./data/occurrence_qc.json'
###        s += ' -o outcomeStats.xlsx'
###        s += ' -w ./'
###        s += ' -c ./config/stats.ini'
###        s += ' -l DEBUG'
###     s += ' -ocol origincolumn'
###        s += ' -orow origin'
###        print '%s' % s
###        return

#    options.inputfile= './data/occurrence_qc.json'
#    optdict['inputfile'] = options.inputfile
    ocol = 4
    orow = 8
    optdict['inputfile'] = './data/occurrence_qc.json'
    optdict['outputfile'] = 'outcomeStats.xlsx'
    optdict['workspace'] = './'
    optdict['configfile'] = './config/stats.ini'
    optdict['loglevel'] = 'DEBUG'
    optdict['origincolumn'] = ocol
    optdict['originrow'] = orow
    print ('optdict: %s' % optdict)

    # Append distinct values of to vocab file
    response=outcomestats(optdict)
    print ('\nresponse: %s' % response)

if __name__ == '__main__':
    main()

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
__version__ = "OutcomeStats.py 2017-03-20T21:54:18-04:00"

import json
import configparser
import argparse
#from Args import Args #local file Args.py
from actor_decorator import python_actor
from collections import OrderedDict
import numpy as np
import sys
import BreakPoint as bp

class XOutcomeStats:
   def __init__(self, dict):
     self.dict= dict
#     print(self.dict)
     infile = dict.get('infile')
           # convert fpAkka post processor json to python dict
     with open(infile) as data_file:
        self.fpa=json.load(data_file) #python form of fpAkka post processor json
     self.validators = dict.get('validators')
     self.outcomes = dict.get('outcomes')
#     print("L36 outcomes=", self.outcomes, "validators=", self.validators)

    # fpa = dict['fpa']
    # return fpa
 #    print("L37 outcomes=", self.outcomes, "validators=", self.validators)     
     #print('fpa=',fpa)
        # return self.fpa

   def getFpa(self) :
      return self.fpa
   
   def getDict(self) :
      return self.dict
   
   def getOutcomes(self) :
      return self.outcomes
   def getValidators(self) :
      return self.validators
   def getMaxLength(self):
      return self.maxlength

   def initStats(self,validators, outcomes) :
      stats = np.zeros((len(validators), len(outcomes)), dtype=np.int32)
#      print("outcomes=", outcomes, "self.outcomes=", self.outcomes)
#      for outcome in outcomes:
#          stats[outcome] = 0
    #      print("L55 outcome=",outcome, "stats=", stats)
#      sys.exit()
      return stats

def startup(dict) :
      infile = dict.get('inputfile')
           # convert fpAkka post processor json to python dict
      with open(infile) as data_file:
        fpa=json.load(data_file) #python form of fpAkka post processor json
#      validators = dict.get('validators')
#      outcomes = dict.get('outcomes')
      return fpa   #making a copy??? OK???

def XinitValidatorStats(validators, outcomes) :
      stats = np.zeros((len(validators), len(outcomes)))
      bp.breakpoint("L69 in initValidatorStats stats=",stats, True)
#      for v in validators :
#         stats[v] = self.initStats(validators, outcomes) #already wrong here
      return stats
   

def updateValidatorStats(fpa, stats, validators, outcomes, record)  :
#   print("L111 stats=",stats)
 #  print("L94 in updateValidatorStats called by outcomeStatsOnData")
   print("L96 stats in=", stats)
#   sys.exit()
   data=fpa[record]["Markers"]
#   print("L114 data=",data, "validators=", validators, "outcomes=", outcomes)
#   print("L115 validator indices="
   for i in range(len(validators)) :
      validator = validators[i]
      outcome = data.get(validator)
      outcomeIndex = outcomes.index(outcome)
      validatorIndex = validators.index(validator) # that is i
     # print("i= ", i, "validator=",validator, "outcome=", outcome, "outcomeIndex=", outcomeIndex)
      #row = validators.index(validator)
      # row = i, col = outcomeIndex
#      print("Line 106 in updateValidatorStats:", i,outcomeIndex)
      z=np.zeros((len(validators), len(outcomes)), dtype=np.int32) #constant?
      z.itemset((i,outcomeIndex),1)
#      print("L109 in updateValidatorStats z=",z)
      stats  = stats+z
#      print ("L111 stats on next record=",stats)
#      bp.breakpoint("At data eval", stats, False)
  # print(stats)
  # sys.exit()
#   for data_k, data_v in data.items() :
#      for stats_k, stats_v in stats: #stats.items() :
#         if (stats_k == data_k):
#            stats[stats_k][data_v] += 1
#   bp.breakpoint("At end of updateValidatorStats() stats=", stats, False)
   print ("L120 stats out on next record=",stats)
#   sys.exit()
   return stats

   
   dict = {'infile': infile, 'validators':validators, 'outcomes':outcomes}
#   print("L123 dict[validators]=",dict['validators'])
   outcomestats=OutcomeStats(dict)  #fpAkka postprocessor as python
#   print("L125 dict[validators]=",dict['validators'])
   fpa = outcomestats.getDict()['fpa']
#   print("L132 fpa=",fpa)
#   sys.exit()
   normalize = False

      # return theStats as a Dict
   aaa = OutcomeStats(dict)
#   stats = outcomestats.createStats(fpa, normalize)
   stats = aaa.initStats(validators, outcomes)
       #now fill
   #print("in OutcomeStatsOnData L150 about to call updateValidatorStats loop on  ", len(fpa), "records")
#   for record in range(len(fpa)):
#         updateValidatorStats(fpa, stats,validators, outcomes, record) 

   print("L160 stats=", stats)
  # sys.exit()
   #updateValidatorStats(fpa, theStats, record)
#   stats = fillStats(fpa,stats,validators, outcomes)
   return stats


def main():
   optdict = {'inputfile':'occurrence_qc.json' }
#   stats = outcomestatsOnData(optdict)
   validators = ("ScientificNameValidator","DateValidator",  "GeoRefValidator","BasisOfRecordValidator") #row order in output
   outcomes = ("CORRECT","CURATED","FILLED_IN", "UNABLE_DETERMINE_VALIDITY",  "UNABLE_CURATE") #col order in output
   stats = np.zeros((len(validators), len(outcomes)))
   infile = optdict.get('inputfile')
   dict = {'infile': infile, 'validators':validators, 'outcomes':outcomes}
#   print("L123 dict[validators]=",dict['validators'])
#   outcomestats=OutcomeStats(dict)  #fpAkka postprocessor as python
   fpa = startup(optdict)
   for record in range(len(fpa)):
         stats=updateValidatorStats(fpa, stats,validators, outcomes, record) 
#   print("fpa=",fpa)
#   sys.exit()
#   stats = updateValidatorStats(fpa, stats, validators, outcomes, record)
#   stats = fillStats(fpa, stats, validators, outcomes)
   print("in main, stats=", stats)
 #  bp.breakpoint("In main() stats=", stats, False)
   

if __name__ == '__main__':
   main()

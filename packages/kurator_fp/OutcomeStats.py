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
__version__ = "OutcomeStats.py 2017-03-13T21:45:08-04:00"

import json
import configparser
import argparse
#from Args import Args #local file Args.py
from actor_decorator import python_actor
from collections import OrderedDict
import numpy as np
import sys
import BreakPoint as bp

class OutcomeStats:
   def __init__(self, dict):
     self.dict= dict
#     print(self.dict)
     infile = dict.get('infile')
           # convert fpAkka post processor json to python dict
     with open(infile) as data_file:
        fpa=json.load(data_file) #python form of fpAkka post processor json
     self.validators = dict.get('validators')
     self.outcomes = dict.get('outcomes')
#     print("L36 outcomes=", self.outcomes, "validators=", self.validators)

     dict['fpa'] = fpa
    # return fpa
 #    print("L37 outcomes=", self.outcomes, "validators=", self.validators)     
     #print('fpa=',fpa)
        # return self.fpa

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
   


  ### def initValidatorStats(self,validators, outcomes) :
##      stats = np.zeros((len(validators), len(outcomes)))
##      bp.breakpoint("L69 in initValidatorStats stats=",stats, True)
##      for v in validators :
##         stats[v] = self.initStats(validators, outcomes) #already wrong here
##      return stats
   
   
   #typed parameter requires python3
def xfillStats(fpa, normalize=False):
      print("L88")
      validatorStats = initValidatorStats(validators, outcomes) #already wrong
      print("L78 validatorStats=", validatorStats)
      sys.exit()
      for record in range(len(fpa)):
         updateValidatorStats(fpa, validatorStats, record) 
      if normalize == True :
         normalizeStats(fpa,validatorStats)
      return validatorStats

def fillStats(fpa, normalize=False):
     # print("L88")
      validatorStats = initValidatorStats(validators, outcomes) #already wrong
      print("L78 validatorStats=", validatorStats)
      sys.exit()
      for record in range(len(fpa)):
         updateValidatorStats(fpa, validatorStats, record) 
      if normalize == True :
         normalizeStats(fpa,validatorStats)
      return validatorStats
   
def normalizeStats(fpa,stats):
      #fpa is dict loaded from FP-Akka json output
      #divide outcome counts by occurrence counts
   count=len(fpa)
   count_f= float(count)
   #   if (count <= 0) return(-1)
   for validator,outcomes in stats.items():
      stat=stats[validator]
      for k,v in stat.items():
         v = v/count_f
         stat[k] = format(v, '.4f')
   return stats
   
def updateValidatorStats(fpa, stats, validators, outcomes, record)  :
   print("L100 stats=",stats)
#   sys.exit()
   data=fpa[record]["Markers"]
#   print("L114 data=",data, "validators=", validators, "outcomes=", outcomes)
#   print("L115 validator indices="
   for i in range(len(validators)) :
      validator = validators[i]
      outcome = data.get(validator)
      outcomeIndex = outcomes.index(outcome)
      print("i= ", i, "validator=",validator, "outcome=", outcome, "outcomeIndex=", outcomeIndex)
   sys.exit()
#   for data_k, data_v in data.items() :
#      for stats_k, stats_v in stats.items() :
#         if (stats_k == data_k):
#            stats[stats_k][data_v] += 1
   bp.breakpoint("At end of updateValidatorStats() stats=", stats, True)
   return stats

   
#@python_actor
   #return the outcome stats as a python dictionary
def outcomestatsOnData(optdict):
   from Args import Args
 #  args=Args('occurrence_qc.json', 'outcomeStats.xlsx', 'stats.ini')
#   args = Args('stats.ini')
###   parser = argparse.ArgumentParser(description='Stats arguments')
###   config = configparser.ConfigParser()
###   config.sections()
###   configFile = args.getConfigfile()
###   configFile='stats.ini'
###   config.read(configFile)
###   validators =eval( config['DEFAULT']['validators'])
#   maxlength= max(len(s) for s in validators)
###   outcomes = eval(config['DEFAULT']['outcomes'])
   
   infile = 'occurrence_qc.json' #FPAkka postprocessor output
   validators = ("ScientificNameValidator","DateValidator",  "GeoRefValidator","BasisOfRecordValidator") #row order in output
   outcomes = ("CORRECT","CURATED","FILLED_IN", "UNABLE_DETERMINE_VALIDITY",  "UNABLE_CURATE") #col order in output

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
#   theStats = outcomestats.createStats(fpa, normalize)
   theStats = aaa.initStats(validators, outcomes)
       #now fill
   for record in range(len(fpa)):
         updateValidatorStats(fpa, theStats,validators, outcomes, record) 

   print("L143 theSats=", theStats)
   sys.exit()
   #updateValidatorStats(fpa, theStats, record) 
   theStats = fillStats(fpa,theStats,  normalize=False)
   return theStats


def main():
   optdict = {'inputfile':'occurrence_qc.json', }
   stats = outcomestatsOnData(optdict)
   bp.breakpoint("In main() stats=", stats, True)
   

if __name__ == '__main__':
   main()

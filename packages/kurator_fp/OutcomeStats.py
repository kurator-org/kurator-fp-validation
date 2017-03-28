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
__version__ = "OutcomeStats.py 2017-03-25T09:53:53-04:00"

import json
import configparser
import argparse
from actor_decorator import python_actor
import numpy as np
import sys
import BreakPoint as bp


def startup(dict) :
      infile = dict.get('inputfile')
           # convert fpAkka post processor json to python dict
      with open(infile) as data_file:
        fpa=json.load(data_file) #python form of fpAkka post processor json
#      validators = dict.get('validators')
#      outcomes = dict.get('outcomes')
      return fpa   #making a copy??? OK???

   

def updateValidatorStats(fpa, stats, validators, outcomes, record)  :
#   print("L40 stats in=", stats)
   data=fpa[record]["Markers"]
   for i in range(len(validators)) :
      validator = validators[i]
      outcome = data.get(validator)
      outcomeIndex = outcomes.index(outcome)
      validatorIndex = validators.index(validator) 
      #row = validators.index(validator)
      # row = i, col = outcomeIndex
      z=np.zeros((len(validators), len(outcomes)), dtype=np.int32) #constant?
      z.itemset((i,outcomeIndex),1)
      stats  = stats+z
#   print ("L120 stats out on next record=",stats)
   return stats
   
  

def main():
   optdict = {'inputfile':'occurrence_qc.json' }
   validators = ("ScientificNameValidator","DateValidator",  "GeoRefValidator","BasisOfRecordValidator") #row order in output
   outcomes = ("CORRECT","CURATED","FILLED_IN", "UNABLE_DETERMINE_VALIDITY",  "UNABLE_CURATE") #col order in output
   stats = np.zeros((len(validators), len(outcomes)), dtype=np.int32)
   infile = optdict.get('inputfile')
   dict = {'infile': infile, 'validators':validators, 'outcomes':outcomes}
#   print("L123 dict[validators]=",dict['validators'])
#   outcomestats=OutcomeStats(dict)  #fpAkka postprocessor as python
   fpa = startup(optdict)
   for record in range(len(fpa)):
         stats=updateValidatorStats(fpa, stats,validators, outcomes, record) 
   print("in main, stats=")
   print(stats)

   

if __name__ == '__main__':
   main()

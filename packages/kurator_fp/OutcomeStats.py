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
__version__ = "OutcomeStats.py 2017-04-11T22:29:26-04:00"

import json
import configparser
import argparse
from actor_decorator import python_actor
import numpy as np
import sys



def startup(dict) :
      infile = dict.get('inputfile')
           # convert fpAkka post processor json to python dict
      with open(infile) as data_file:
        fpa=json.load(data_file) #python form of fpAkka post processor json
      return fpa   #making a copy??? OK???

def updateValidatorStats(fpa, stats, validators, outcomes, record)  :
   data=fpa[record]["Markers"]
   for i in range(len(validators)) :
      validator = validators[i]
      outcome = data.get(validator)
      outcomeIndex = outcomes.index(outcome)
      validatorIndex = validators.index(validator) 
      z=np.zeros((len(validators), len(outcomes)), dtype=np.int32) #constant?
      z.itemset((i,outcomeIndex),1)
      stats  = stats+z
   return stats

def getStats() :
   import Config
#   import OutcomeStats as ocstats
   config = Config.config('stats.ini')
   validators = eval(config['validators'])
   outcomes = eval(config['outcomes'])
   optdict = {'inputfile':'occurrence_qc.json' }
   stats = np.zeros((len(validators), len(outcomes)), dtype=np.int32)
   infile = optdict.get('inputfile')
#   dict = {'infile': infile, 'validators':validators, 'outcomes':outcomes}
   fpa = startup(optdict)
   for record in range(len(fpa)):
         stats = updateValidatorStats(fpa, stats,validators, outcomes, record) 
   statsAsPythonLisTuples = nmpyArrayToPythonTuple(stats)
   return statsAsPythonLisTuples

    #convenience methods
def nmpyArrayToPythonList(array):
      try:
            return array.tolist()
      except TypeError:
            return array

def pythonListToNmpy(list):     #should check that the Python is a list?
      try:
            return(np.array(list))
      except TypeError:
            return list
      

def nmpyArrayToPythonTuple(array):
      try:
            return tuple(nmpyArrayToPythonTuple(i) for i in array)
      except TypeError:
            return array

def pythonTupleToNmpy(tuple):
      try:
            return np.asarray(tuple)
      except TypeError:
            return tuple

def pythonTupleToJson(tuple):
      try:
            return json.dump(tuple)
      except TypeError:
            return tuple

def numpyArrayToJson(array, json_file=None):
      import codecs
      try:
            if json_file is None:
                  jason_file = stats.json
            np_array_to_list = array.tolist()
            b = np_array_to_list
            json.dump(b, codecs.open(json_file, 'w', encoding='utf-8'), sort_keys=True, indent=4)
      except TypeError:
            return array

def main():
   import Config
   config = Config.config('stats.ini')
   validators = eval(config['validators'])
   outcomes = eval(config['outcomes'])
   optdict = {'inputfile':'occurrence_qc.json' }
   stats = np.zeros((len(validators), len(outcomes)), dtype=np.int32)
   infile = optdict.get('inputfile')
#   dict = {'infile': infile, 'validators':validators, 'outcomes':outcomes}
   fpa = startup(optdict)
   for record in range(len(fpa)):
         stats=updateValidatorStats(fpa, stats,validators, outcomes, record) 
   print("in main, stats as numpy 2d array :")
   print(stats)
   print("in main, stats as python list of lists:")
   statsAsPythonList = nmpyArrayToPythonList(stats)
   print(statsAsPythonList)
   print("zz=")
   print(pythonTupleToNmpy(statsAsPythonList))

   print("in main, statsAsPython to nmpy array:")
   print(pythonListToNmpy(statsAsPythonList))


   statstpl=nmpyArrayToPythonTuple(stats)
#   print("statstupl:",statstpl)

   numpyArrayToJson(stats, "stats.json")
   sys.exit()
   print(pythonTupleToNmpy(statstpl))
   print("in main, stats to python tuple:")
   print(nmpyArrayToPythonTuple(stats))

   print(json.dumps(statstpl))
if __name__ == '__main__':
   main()

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
__version__ = "OutcomeStats.py 2017-04-27T11:09:22-04:00"

import json
import Config
import configparser
import argparse
from actor_decorator import python_actor
import numpy as np
import sys
import codecs
import OptDict


def startup(dict) :
      infile = dict.get('inputfile')
           # convert fpAkka post processor json to python dict
           #infile must be a "file-like object"
           #See https://docs.python.org/2/glossary.html#term-file-object
      with open(infile) as data_file:
        fpa=json.load(data_file) #python form of fpAkka post processor json
#
#      print("fpa is json", is_json(fpa))
      return fpa   #making a copy??? OK???


def getStatsAsNmpyArray(validators, outcomes, optdict):
   config = Config.config('stats.ini')
   validators = eval(config['validators'])
   outcomes = eval(config['outcomes'])
   stats = np.zeros((len(validators), len(outcomes)), dtype=np.int32)
   fpa = startup(optdict)
   for record in range(len(fpa)): #one record at a time
         stats=updateValidatorStats(fpa, stats,validators, outcomes, record) 
   return(stats)

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

def getStats(optdict) :
   config = Config.config('stats.ini')
   validators = eval(config['validators'])
   outcomes = eval(config['outcomes'])
   stats = np.zeros((len(validators), len(outcomes)), dtype=np.int32)
   infile = optdict.get('inputfile')
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

def numpyArrayToJsonFile(array, json_file=None):
      try:
            if json_file is None:
                  jason_file = stats.json
            np_array_to_list = array.tolist()
            b = np_array_to_list
#            print("nmpy:", b, type(b))
            json.dump(b, codecs.open(json_file, 'w', encoding='utf-8'), sort_keys=True, indent=4)
      except TypeError:
            return array

def labelsToJson(labels,labelname, json_file=None):
#            print("inLabelsToJson:", labels, type(labels))
            labelList=list(labels)
            json_string = ""
            if True: #json_file is None:
                  filename = labelname +".json"
            return json_string

def obj_dict(obj):
       return obj.__dict__

# from stackoverflow.com how-do-i-check-if-a-string-is-valid-json-in-python
# cc-by-3.0
def is_json(myjson): 
  try:
    json_object = json.loads(myjson)
  except ValueError, e:
    return False
  return True

def labelsToJson(labels,labelname):
            labelList=list(labels)
            json_string = ""
            json_string =json.dumps(labelList)
            return json_string

def jsonToFile(jsonStr, filename):
      theFile = open(filename, 'w') #should test success
      if is_json(jsonStr):
            theFile.write(jsonStr)
            theFile.close()  #make unwriteable???
            return True
      else :
            return False


def main():
   config = Config.config('stats.ini')
   validators = eval(config['validators'])
   outcomes = eval(config['outcomes'])
#   optdict = {'inputfile':'occurrence_qc.json', 'outputfile':'stats.json' }
   optdict = OptDict.getOptDict()
        #returns Nmpy array of stats data only
   stats = getStatsAsNmpyArray(validators, outcomes, optdict)
        # returns a Python list of lists generated by the Nmpy array
   statsAsPythonList = nmpyArrayToPythonList(stats)
        # inverse serialization of nmpyArrayToPythonList(...)
   pythonListToNmpy(statsAsPythonList)
   
        #produces a python  tuple of tuples from nmpy array
   statstpl=nmpyArrayToPythonTuple(stats)
        #produces a nmpy array from Python list of lists
   pythonTupleToNmpy(statsAsPythonList)
        #inverse serialization of nmpyArray from tuple of tuples
   pythonTupleToNmpy(statstpl)

        #produces a Json file from numpy array of stats
   numpyArrayToJsonFile(stats, optdict['outputfile'])

       ##provide and serialize labels of validators and outcomes
       # provide for validators
   ltvj=labelsToJson(validators,"validators")
   jsonToFile(ltvj, "validators.json")
       # provide for outcomes
   ltoj=labelsToJson(outcomes, "outcomes.json")
   jsonToFile(ltoj, "outcomes.json")
      ## json i/o sanity check for outcomes
   jsonFile = open("outcomes.json",'r')
   lbls = jsonFile.read()
  # print("lbls:", lbls, type(lbls))
   jsonFile.close()
   print(is_json (lbls))
   ### In linux examine *.json e.g. with "more" and check dates
   ### of all of {stats,outcomes,validators}.json

   # David: aggregates the three results into a single json file
   jsonDict = { 'validators': validators, 'outcomes': outcomes, 'stats': stats.tolist() }

   with open('results.json', 'w') as outfile:
       json.dump(jsonDict, outfile)

if __name__ == '__main__':
   main()

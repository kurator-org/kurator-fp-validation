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
__version__ = "OutcomeStats.py 2017-03-04T10:28:51-05:00"

import json
import configparser
import Args

class OutcomeStats:
   def __init__(self, dict):
     self.dict= dict
     infile = dict.get('infile')
           # convert fpAkka post processor json to python dict
     with open(infile) as data_file:
        fpa=json.load(data_file) #python form of fpAkka post processor json
     self.validators = dict.get('validators')
     self.outcomes = dict.get('outcomes')
   #   print("outcomes=", outcomes)

     dict['fpa'] = fpa
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

   def initStats(self,outcomes) :
      stats = {}
      for outcome in outcomes:
          stats[outcome] = 0
      return stats
   
   def initValidatorStats(self,validators, outcomes) :
      stats = {}
      for v in validators :
         stats[v] = self.initStats(outcomes)
      return stats
   
   def updateValidatorStats(self,fpa, stats, record)  :
      data=fpa[record]["Markers"]
      for data_k, data_v in data.items() :
         for stats_k, stats_v in stats.items() :
            if (stats_k == data_k):
               stats[stats_k][data_v] += 1
      return stats
   
   #typed parameter requires python3
   def createStats(self, fpa, normalize):
      validatorStats = self.initValidatorStats(self.validators, self.outcomes)
      for record in range(len(fpa)):
         self.updateValidatorStats(fpa, validatorStats, record) 
      if normalize == True :
         self.normalizeStats(fpa,validatorStats)
      return validatorStats
   
   def normalizeStats(self,fpa,stats):
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
   #         print("yy:",stats[validator])
   #   print("in normalize stats=",stats)
      return stats
   
   
   
def main():
   validators = ("ScientificNameValidator","DateValidator",  "GeoRefValidator","BasisOfRecordValidator") #row order in output
   outcomes = ("CORRECT","CURATED","FILLED_IN", "UNABLE_DETERMINE_VALIDITY",  "UNABLE_CURATE") 
   infile = 'occurrence_qc.json' #FPAkka postprocessor output

   dict = {'infile': infile, 'validators':validators, 'outcomes':outcomes} 
   outcomestats=OutcomeStats(dict)  #fpAkka postprocessor as python 
   fpa = outcomestats.getDict()['fpa'] 
   normalize = False

      # return theStats as a Dict
   theStats = outcomestats.createStats(fpa, normalize)
   print("L112 theStats=", theStats)
if __name__ == '__main__':
   main()

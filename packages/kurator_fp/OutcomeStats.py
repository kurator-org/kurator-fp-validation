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
__version__ = "OutcomeStats.py 2016-07-02T17:37:34-0400"

import json
import xlsxwriter
import configparser
import Args

class OutcomeStats:
#   def __init__(self, workbook, worksheet,infile, outfile, configFile, origin1, origin2):
   def __init__(self, configfile):

      #with open(args.getInfile()) as data_file:
      #           fpAkkOutput=json.load(data_file)

      #with open(args.getInfile()) as data_file:
      #   self.fpa=json.load(data_file)

      config = configparser.ConfigParser()
      config.sections()
#      self.configFile =configFile
      self.configFile = configfile
#      self.configFile='stats.ini'
      config.read(self.configFile)
      self.validators =eval( config['DEFAULT']['validators'])
      self.maxlength= max(len(s) for s in self.validators)
      self.outcomes = eval(config['DEFAULT']['outcomes'])
      self.max1= max(len(s) for s in self.validators)
      self.max2= max(len(t) for t in self.outcomes)
      self.maxlength = max(self.max1,self.max2)
      #self.fpa = {}
      #infile = 'occurrence_qc.json' #for now

      #self.numRecords = len(self.fpa)

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
   #   print("in updateValidatorStats[",record,"]")
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
   
   def stats2CSV(self, stats, outfile, outcomes, validators):
      import csv
      with open(outfile, 'w') as csvfile:
         o=list(outcomes)
         o.insert(0,"Validator")
         fieldnames=tuple(o)
         writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
         writer.writeheader()
         for v in validators:
            row = stats[v]
            row['Validator'] = v
            writer.writerow(row)
   
   def initWorkbook(outfile):
      """
      Returns a workbook to be written to **outfile**
      """
      workbook = xlsxwriter.Workbook(outfile)
      return workbook
   
   #doesn't belong in this class
   
   def stats2XLSX(self, workbook, worksheet, formats, stats, origin, outcomes, validators):
   #   print("fmts=",formats)
      bold = workbook.add_format({'bold': True})
   #   print("stats=",stats)
   #   print("outcomes=", outcomes)
   #   print(origin)
      worksheet.write(origin[0],origin[1],"Validator",bold)
      for str in outcomes :
         col=1+origin[1]+outcomes.index(str) #insure order is as in outcomes list
         worksheet.write(origin[0],col, str, bold) #write col header
      for k, v in stats.items():
   #      print("key=",k,"val=", v)
         row = 1+origin[0]+validators.index(k) #put rows in order of the validators list
   #      print("row=",row)
         worksheet.write(row,0,k) #write validator name
         #write data for each validator in its own row
         for outcome, statval in v.items():
            col=1+outcomes.index(outcome) #put cols in order of the outcomes list
            worksheet.write(row, col, statval,formats.get(outcome))
   
def main():
   from Args import Args
   print("OutcomeStats.main()")
#   print(type(self.outcomeFormats()))
   args=Args('occurrence_qc.json', 'outcomeStats.xlsx', 'stats.ini')
   workbook = xlsxwriter.Workbook(args.getOutfile())
   worksheet = workbook.add_worksheet()
   origin1 = [0,0]
   origin2 = [5,0]

   stats=OutcomeStats(args)

if __name__ == "__main__" :
   print("hello")
   main()

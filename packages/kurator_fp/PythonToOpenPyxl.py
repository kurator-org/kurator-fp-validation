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
__version__ = "PythonToOpenPyxl.py 2017-04-03T16:36:18-04:00"

import json
import configparser
import argparse
from actor_decorator import python_actor
import numpy as np
from openpyxl import Workbook
import sys

def wbInit():  #should call only once?
    wb = Workbook()
    return  wb

def getWorksheet(wb, sheet=None):
    if sheet is None:
        return wb.active
    else:
        return sheet

#def wsInit(name):
def getStats() :
   import Config
   import OutcomeStats as ocstats
   config = Config.config('stats.ini')
   validators = eval(config['validators'])
   outcomes = eval(config['outcomes'])
   optdict = {'inputfile':'occurrence_qc.json' }
   stats = np.zeros((len(validators), len(outcomes)), dtype=np.int32)
   infile = optdict.get('inputfile')
#   dict = {'infile': infile, 'validators':validators, 'outcomes':outcomes}
   fpa = ocstats.startup(optdict)
   for record in range(len(fpa)):
         stats=ocstats.updateValidatorStats(fpa, stats,validators, outcomes, record) 
   statsAsPythonLisTuples = ocstats.nmpyArrayToPythonTuple(stats)
   return statsAsPythonLisTuples
    
def main():
    wb = Workbook()
    ws = wb.active
    statsAsTuples = getStats()
#    print("stats as tuples:")
#    print(statsAsTuples)
    for index in range(len(statsAsTuples)):
        rownum=index
        theRowTuple = statsAsTuples[index]
        for index in range(len(theRowTuple)):
            colnum = index
            value = theRowTuple[index]
            ws.cell(column=colnum+1,row=rownum+1,value=value)
       # print("ty=",type(row))
#        for cell in row:
     #       cell.value = statsAsTuples(row,cell)
 #           cell.value = 'hello'
          #  cell.value = tuple(row for row in (1,2,3))
###        for cell in ws.iter_cols(min_col=1, max_col=5):
#            print("cell:")
#            print(cell)
    wb.save('stats.xlsx')
    
if __name__ == "__main__" :
   main()


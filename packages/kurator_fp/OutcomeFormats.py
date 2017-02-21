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
__version__ = "OutcomeFormats.py 2017-02-20T18:52:21-0500"

import json
import sys
#import xlsxwriter
from openpyxl.styles import NamedStyle, PatternFill, Fill, Border, Side, Alignment, Protection, Font, GradientFill, Alignment
from openpyxl import Workbook

import argparse

class OutcomeFormats:
   """Class supporting xlsx cell formats for a set of Kurator Quality Control *outcomes*
   """
   def __init__(self, outcomes):
      self.outcomes = outcomes
      
   def getFormats(self):
      return formats

   def setFormats(self, formats):
      return {}
   
#   def initFormats(self, workbook, worksheet):
   def initFormats(self, formatdict):  #ignore forrmatdict for now
      ### should get names of formats from something reachable from frmtdict
      ### perhaps via config/stats.ini
      formatGrnFill=PatternFill("solid", fgColor='00FF00') #lite green
      formatRedFill=PatternFill("solid", fgColor='FF0000')
      formatMusFill=PatternFill("solid", fgColor='DDDD00') #mustard
      formatYelFill=PatternFill("solid", fgColor='FFFF00')
      formatGryFill=PatternFill("solid", fgColor='888888')


      formatXFill=''
      #next make a dict out of the set of openpyxl styles
      self.formats={'UNABLE_DETERMINE_VALIDITY':formatGryFill, 'CURATED':formatYelFill, 'UNABLE_CURATE':formatRedFill, 'CORRECT':formatGrnFill, 'FILLED_IN':formatMusFill}

      

      return self.formats

def main():
   print("OutcomeFormats.main()")
   formatdict = {}
   frm = OutcomeFormats(formatdict)
   formats = frm.initFormats(formatdict)
   formatGrnFill=PatternFill("solid", fgColor='00FF00') #lite green
   formatRedFill=PatternFill("solid", fgColor='FF0000')
   formatMusFill=PatternFill("solid", fgColor='DDDD00') #mustard
   formatYelFill=PatternFill("solid", fgColor='FFFF00')
   formatGryFill=PatternFill("solid", fgColor='888888')
   formatsDict={'UNABLE_DETERMINE_VALIDITY':formatGryFill, 'CURATED':formatYelFill, 'UNABLE_CURATE':formatRedFill, 'CORRECT':formatGrnFill, 'FILLED_IN':formatMusFill}
   grnFill = NamedStyle(name='grnFill')

   wb = Workbook()
   ws = wb.active
   originrow = 4
   origincol = 2

#   outcomes = eval(config['DEFAULT']['outcomes'])
#   max1= max(len(s) for s in validators)
#   max2= max(len(t) for t in outcomes)
#   maxlength = max(max1,max2)

   fdictlen = len(formatsDict)
#   print(fdictlen)
   theOutcomes = formatsDict.keys()
   max2= max(len(t) for t in theOutcomes)
   max1=max2  #for now
   print(max2)
   

   
   outcomeIndex = 0

   # set outcome names as headers 
   #
#   font = Font(bold=True)
   for col in range(1, 1+fdictlen):
      value = theOutcomes[outcomeIndex] 
      cell = ws.cell(column=col+origincol, row=originrow, value=value)
      cell.font = Font(bold=True)
      outcomeIndex += 1

      #set column width based on length of column header text
      #which is taken from outcome names
   dims = {}
   emwidth = 4 #for now
   for row in ws.rows:
      for cell in row:
        if cell.value:
            dims[cell.column] = emwidth + max((dims.get(cell.column, 0), len(cell.value)))
        #    print ("L92", cell.value, dims[cell.column])
   for col, value in dims.items():
      ws.column_dimensions[col].width = value
 

#   sys.exit()
###   outcomeCORRECTcell = ws['B2']
###   outcomeCORRECTcell.value = "CORRECT"
###   wb.add_named_style(grnFill)
###   ws['B2'].style = grnFill
   wb.save("outcomestyled.xlsx")
   
#   print("L69 formatsDict=",formatsDict)
#   import statstest
#   exec(open("statstest.py").read())
if __name__ == "__main__" :
   main()


   

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
__version__ = "OutcomeFormats.py 2017-02-27T10:51:47-0500"

import json
import sys
from collections import OrderedDict
import argparse

from openpyxl.styles import NamedStyle, PatternFill, Fill, Border, Side, Alignment, Protection, Font, GradientFill, Alignment
from openpyxl import Workbook
from openpyxl.utils import get_column_letter
from FormatUtils import style_range

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
      formatYelFill=PatternFill("solid", fgColor='DDDD00')
      formatGryFill=PatternFill("solid", fgColor='888888')


      formatXFill=''
      #next make a dict out of the set of openpyxl styles
      self.formats={'UNABLE_DETERMINE_VALIDITY':formatGryFill, 'CURATED':formatYelFill, 'UNABLE_CURATE':formatRedFill, 'CORRECT':formatGrnFill, 'FILLED_IN':formatMusFill}

      

      return self.formats
def main():
   print("OutcomeFormats.main()")
   theformatdict = {}
   frm = OutcomeFormats(theformatdict)
   formats = frm.initFormats(theformatdict)
#   print("L61 formats type=", type(formats))
   outcomeDict = OrderedDict(sorted(formats.items(),key=lambda t: t[0]))
   formatGrnFill=PatternFill("solid", fgColor='00FF00') #lite green
   formatRedFill=PatternFill("solid", fgColor='FF0000')
   formatMusFill=PatternFill("solid", fgColor='DDDD00') #mustard
   formatYelFill=PatternFill("solid", fgColor='222200')
   formatGryFill=PatternFill("solid", fgColor='888888')
#   formatDict = OrderedDict(sorted(theformatdict.items(),key=lambda t: t[0]))
#   print("L67 outComeDictKeys=", outcomeDict.keys())

   theDict = {'CORRECT':formatGrnFill, 'CURATED':formatYelFill,'FILLED IN':formatMusFill, 'UNABLE CURATE':formatRedFill,'UNABLE\nDETERMINE\nVALIDITY':formatGryFill}
   formatsDict = OrderedDict(sorted(theDict.items(),key=lambda t: t[0]))
#   print("L69 formatsDictKeys=", formatsDict.keys())
   wb = Workbook()
   ws = wb.active
   originrow = 4
   rd = ws.row_dimensions[originrow]
   rd.height = 12 #points; could do better if can compute out
   origincol = 2
   #headerColAl = Alignment()
   #headerColAl.vert = Alignment.VERT_CENTER

#   outcomes = eval(config['DEFAULT']['outcomes'])
#   max1= max(len(s) for s in validators)
#   max2= max(len(t) for t in outcomes)
#   maxlength = max(max1,max2)

   fdictlen = len(formatsDict)
#   print(fdictlen)
   theOutcomes = formatsDict.keys()
   max2 = len(max(formatsDict))
   max1=max2  #for now
#   print(max2, max3)
   

   

    ############################################
    # set kurator outcome names as headers     #
    ############################################
   outcomeIndex = 0
   for col in range(1, 1+fdictlen):
      value = theOutcomes[outcomeIndex]
      cell = ws.cell(column=col+origincol, row=originrow, value=value)
      thecol=col+origincol
      thecolletter = get_column_letter(thecol)
#      print("L139 value = ", value, "thecolletter=",thecolletter, "col=", col, "cell=", cell)
#      cell.style.alignment.wrap_text = True
      cell.font = Font(bold=True)
      outcomeIndex += 1

   ##########################################################
   # set column width based on length of column header text #
   # which is taken from outcome names                      #
   ##########################################################   
   colwidthDict = {}
   emwidth = 12 #for now
   for row in ws.rows:
      for cell in row:
        if cell.value:
            colwidthDict[cell.column] = emwidth + max((colwidthDict.get(cell.column, 0), len(cell.value)))
#            print ("L92", cell.value, colwidthDict[cell.column])
   maxcolwidth = max(colwidthDict.keys())
   ww = colwidthDict
   alignment=Alignment(horizontal='general',
                       vertical='top',
                       text_rotation=0,
                       wrap_text=True,
                       shrink_to_fit=False,
                       indent=0)
   maxwidth = 0
   for col, value in colwidthDict.items():
      tt = ws.column_dimensions[col].width = 16 #value
      if value > maxwidth :
         maxwidth = value
   for row in ws.rows:
      for outcomename in row:
        if outcomename.value:
            cc = colwidthDict[outcomename.column] = emwidth + maxwidth
#            print("L142= outcomename=",outcomename, "cc=",cc)


   ##########################################################
   # make validator column from validator names           ###
   # providing space for longest name                     ###
   ##########################################################
   validators = ("ScientificNameValidator","DateValidator",  "GeoRefValidator","BasisOfRecordValidator") #row order in desired output. should get from app args
   numvalidators = len(validators) #number of data rows, one for each validator
   row = 1+originrow
   maxValidatorLen = len(max(validators))
   for i in range(0,numvalidators):
      value = validators[i]
      ws.cell(row=i+originrow+1, column=origincol, value=value)
   
   validatorCol = get_column_letter(origincol) #get column name id spreadsheet form
   ws.column_dimensions[validatorCol].width = maxValidatorLen #long enough for longest name
     
      #################################################
      # height of name row. Tricky in openpyxl        #
      # so use a typographically reasonable constant. #
      # Should document the typography more.          #
      #################################################
   ws.row_dimensions[originrow].height = 3*emwidth+12 #
   
      ###########################################################
      # provide openpyxl styles which are constant on the parts #
      # where data will go                                      #
      ###########################################################      
   thin = Side(border_style="thin", color="000000")
   border = Border(top=thin, left=thin, right=thin, bottom=thin)
   font = Font(b=True, color="000000")
   al = Alignment(horizontal="center", vertical="top", wrap_text=True)

      ###########################################################
      # make cell color extracted from outcome index            #
      ###########################################################      
   theFills = [PatternFill("solid", fgColor="00FF00"), PatternFill("solid", fgColor="FFFF00"), PatternFill("solid", fgColor="DDDD00"), PatternFill("solid", fgColor="FF0000"), PatternFill("solid", fgColor="BBBBBB")]
   j=0
   for col in range(1+origincol,1+origincol+len(formatsDict)):
      colname = get_column_letter(col)
      theFill = theFills[j]
#      print("L179 j=",j, "theFill=", theFill)
      j = j+1
      for row in range(1+originrow,1+originrow+numvalidators):
         cellname = colname+str(row)
         theRange=cellname+":"+cellname
         theCell = ws[cellname]
            ### commit style via style_range(...)
         style_range(ws,theRange,border=border, fill=theFill, font=font, alignment=al)

   wb.save("outcomestyled.xlsx")
   
if __name__ == "__main__" :
   main()

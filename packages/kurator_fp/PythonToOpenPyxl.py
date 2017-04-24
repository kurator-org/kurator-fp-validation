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
__version__ = "PythonToOpenPyxl.py 2017-04-24T17:30:55-04:00"

import json
import Config
import configparser
import argparse
#from actor_decorator import python_actor
import numpy as np
from openpyxl import Workbook
#from openpyxl.styles import.colors
from openpyxl.styles import PatternFill, Border, Side, Alignment, Protection, Font
import OutcomeStats as ocstats
import sys

def wbInit():  #should call only once?
    wb = Workbook()
    return  wb

def getWorksheet(wb, sheet=None):
    if sheet is None:
        return wb.active
    else:
        return sheet
def getOutcomeColors():
  #  config = Config.config('styles.ini')
  #   outcomeFills = eval(config['outcomeFills'])
  #    print (outcomeFills)
  #    sys.exit()
    #To do : get outcome names from Config
    grnFill=PatternFill("solid", fgColor='00FF00') #lite green
    redFill=PatternFill("solid", fgColor='FF0000')
    musFill=PatternFill("solid", fgColor='DDDD00') #mustard
    yelFill=PatternFill("solid", fgColor='FFFF00')
    gryFill=PatternFill("solid", fgColor='888888')

    colors = {"CORRECT":grnFill,"CURATED":yelFill,
              "FILLED_IN":musFill,"UNABLE_DETERMINE_VALIDITY":redFill,
              "UNABLE_CURATE":gryFill}
    return colors


def vtl(string):
    return len(string)

        
def setColumnStyles(ws, optdict):
    thin   = Side(border_style="thin",   color="000000")
    double = Side(border_style="double", color="000000")
    border = Border(top=double,left=thin,right=thin,bottom=double)
#    outcomes = ("CORRECT","CURATED","FILLED_IN", "UNABLE_DETERMINE_VALIDITY", "UNABLE_CURATE")
#    validators = ("ScientificNameValidator","DateValidator",
         #         "GeoRefValidator","BasisOfRecordValidator")
    validators = eval(Config.config("stats.ini")['validators'])
    outcomes = eval(Config.config("stats.ini")['outcomes'])
    outcomes2 = eval(Config.config("stats.ini")['outcomesFolded'])

    colOrigin = optdict['colOrigin']
    rowOrigin = optdict['rowOrigin']
    maxvtl =  0 #max validator typographic length 
    for index in range(len(validators)): #enter validator labels and
                                         #find typographically longest
        value = validators[index]
        thecell=ws.cell(column=colOrigin, row=rowOrigin+index+1,
                        value=value)
           #next find the currently largest validator name string
        vtl = len(value)
        if vtl > maxvtl :
            maxvtl = vtl
        #reset column width to current value of maxvtl
    ws.column_dimensions[thecell.column].width = maxvtl


    maxotl = 0 #max outcome typographic length;
#    print(len(outcomes))
    for index in range(len(outcomes)):
        value = outcomes[index]
        otl = len(value)
        if otl>maxotl:
            maxotl = otl
#    print ("maxotl:", maxotl)
#    print("outcomes2:",outcomes2)
    for index in range(len(outcomes)):
       # outcomes2=("CORRECT","CURATED","FILLED_\nIN", "UNABLE_\nDETERMINE_\nVALIDITY", "UNABLE_\nCURATE")
#        value = outcomes[index]
        value = outcomes2[index]
#        print("index,value:",index,value)
        otl = len(value) #GAAK! this is character count!
        alignment=Alignment(horizontal='general',
                     vertical='justify',
                     text_rotation=0,
                     wrap_text=True,
                     shrink_to_fit=False,
                     indent=0)
#        print (index, value, otl)
        thecell = ws.cell(column=colOrigin+index+1,row=rowOrigin,value=value)
        thecell.alignment = alignment
        thecell_col = thecell.column
        
 #       print("thecell_col:", thecell_col)
        ws.column_dimensions[thecell_col].width = maxotl
    #    ws.column_dimensions[thecell_col].height = 20
        
#    thecell = ws.cell(column=colOrigin,row=,rowOrigin, )
    rd = ws.row_dimensions[rowOrigin]
    rd.height = 45
        # print(thecell)
    statsAsTuples = ocstats.getStats(optdict)


    for index in range(len(statsAsTuples)):
        rownum=index
        row = rownum+1+rowOrigin
        theRowTuple = statsAsTuples[index]
        for index in range(len(theRowTuple)):
            colnum = index
            column = colnum+1+colOrigin
            value = theRowTuple[index]
            thecell = ws.cell(column=column,row=row,value=value)
            outcome = outcomes[colnum]
            thecell.fill = getOutcomeColors()[outcome]
            thecell.border = border

def main():
    pyxlConfig = Config.config('stats.ini')
#    outcomeFills = eval(cfgopyxl['outcomeFills'])
#    print ("outcomeFills:",outcomeFills)
#    print("pyxlConfig:")
#    print(pyxlConfig)
#    sys.exit()
    wb = Workbook()
    ws = wb.active
    optdict = {'inputfile':'occurrence_qc.json', 'outputfile':'stats.json', 'rowOrigin':2, 'colOrigin':3 }
    setColumnStyles(ws, optdict)
#            print (border)
    wb.save('stats.xlsx')

if __name__ == "__main__" :
   main()


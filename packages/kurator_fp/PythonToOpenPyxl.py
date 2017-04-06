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
__version__ = "PythonToOpenPyxl.py 2017-04-06T18:14:03-04:00"

import json
import configparser
import argparse
from actor_decorator import python_actor
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

#def wsInit(name):



def getOutcomeColors():

    grnFill=PatternFill("solid", fgColor='00FF00') #lite green
    redFill=PatternFill("solid", fgColor='FF0000')
    musFill=PatternFill("solid", fgColor='DDDD00') #mustard
    yelFill=PatternFill("solid", fgColor='FFFF00')
    gryFill=PatternFill("solid", fgColor='888888')

    colors = {"CORRECT":grnFill,"CURATED":yelFill,
              "FILLED_IN":musFill,"UNABLE_DETERMINE_VALIDITY":redFill,
              "UNABLE_CURATE":gryFill}
    return colors

def main():
    wb = Workbook()
    ws = wb.active
    colors = getOutcomeColors()
  #  border = Border(left=Side(border_style=None, color='FFFFFF'),
   #                    right=Side(border_style=None, color='FFFFFF'),
   #                   bottom=Side(border_style=None, color='FFFFFF'),
   #                   top=Side(border_style=None, color='FFFFFF'))
    thin = Side(border_style="thin", color="000000")
    double = Side(border_style="double", color="000000")

    border = Border(top=double, left=thin, right=thin,bottom=double)                
    outcomes = ("CORRECT","CURATED","FILLED_IN", "UNABLE_DETERMINE_VALIDITY",  "UNABLE_CURATE")
#    style_range(ws, 'A1:E4', theborder)
    statsAsTuples = ocstats.getStats()
    for index in range(len(statsAsTuples)):
        rownum=index
        theRowTuple = statsAsTuples[index]
        for index in range(len(theRowTuple)):
            colnum = index
            value = theRowTuple[index]
            thecell = ws.cell(column=colnum+1,row=rownum+1,value=value)
            outcome = outcomes[colnum]
            thecell.fill = getOutcomeColors()[outcome]
            thecell.border = border

#            print (border)
    wb.save('stats.xlsx')
    
if __name__ == "__main__" :
   main()


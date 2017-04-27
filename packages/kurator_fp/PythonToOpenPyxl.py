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
__version__ = "PythonToOpenPyxl.py 2017-04-27T11:09:22-04:00"

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
import OptDict
import sys

def wbInit():  #should call only once?
    wb = Workbook()
    return  wb

def getWorksheet(wb, sheet=None):
    if sheet is None:
        return wb.active
    else:
        return sheet




def vtl(string):
    return len(string)

        
def setColumnStyles(ws, optdict):
    thin   = Side(border_style="thin",   color="000000")
    double = Side(border_style="double", color="000000")
    border = Border(top=double,left=thin,right=thin,bottom=double)

    outcomes =   optdict['outcomes']
    validators = optdict['validators']
    outcomes2 =  optdict['outcomesFolded']
    colOrigin =  optdict['colOrigin']
    rowOrigin =  optdict['rowOrigin']
    outcomeFills = optdict['outcomeFills']
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
    for index in range(len(outcomes)):
        value = outcomes[index]
        otl = len(value)
        if otl>maxotl:
            maxotl = otl
    for index in range(len(outcomes)):
        value = outcomes2[index]
        otl = len(value) #GAAK! this is character count!
        alignment=Alignment(horizontal='general',
                     vertical='justify',
                     text_rotation=0,
                     wrap_text=True,
                     shrink_to_fit=False,
                     indent=0)
        thecell = ws.cell(column=colOrigin+index+1,row=rowOrigin,value=value)
        thecell.alignment = alignment
        thecell_col = thecell.column
        
        ws.column_dimensions[thecell_col].width = maxotl

    rd = ws.row_dimensions[rowOrigin]
    rd.height = 45
    statsAsTuples = ocstats.getStats(optdict) #from OpenStats module
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
            thecell.fill =   PatternFill("solid", fgColor=outcomeFills[outcome])
            thecell.border = border

def main():
    wb = Workbook()
    ws = wb.active
    optdict = OptDict.getOptDict(rowOrigin=7) #defaults to stats.ini
    setColumnStyles(ws, optdict)
#            print (border)
    wb.save('stats.xlsx')

if __name__ == "__main__" :
   main()


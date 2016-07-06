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
__version__ = "OutcomeFormats.py 2016-07-02T17:37:34-0400"

import json
import sys
import xlsxwriter
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
   
   def initFormats(self, workbook):
      formatGrnFill=workbook.add_format()
      formatRedFill=workbook.add_format()
      formatYelFill=workbook.add_format()
      formatMusFill=workbook.add_format()
      formatGryFill=workbook.add_format()
      formatGrnFill.set_bg_color('#00FF00') #lite green
      formatRedFill.set_bg_color('#FF0000')
      formatMusFill.set_bg_color('#DDDD00') #mustard
      formatYelFill.set_bg_color('#FFFF00')
      formatGryFill.set_bg_color('#888888')
      formatXFill=''
      self.formats={'UNABLE_DETERMINE_VALIDITY':formatGryFill, 'CURATED':formatYelFill, 'UNABLE_CURATE':formatRedFill, 'CORRECT':formatGrnFill, 'FILLED_IN':formatMusFill}
      return self.formats

def main():
   print("OutcomeFormats.main()")
   import statstest
   exec(open("statstest.py").read())
if __name__ == "__main__" :
   main()


   

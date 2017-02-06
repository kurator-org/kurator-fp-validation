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
__version__ = "OutcomeFormats.py 2017-01-31T14:47:24-0500"

import json
import sys
#import xlsxwriter
from openpyxl.styles import PatternFill, Fill, Border, Side, Alignment, Protection, Font
from openpyxl import Workbook
import argparse

def style_range(ws, cell_range, fill=None):
    """
    Adapted from https://openpyxl.readthedocs.io/en/default/styles.html#cell-styles
    :param ws: A worksheet instance
    :param range: An excel range to style (e.g. A1:F20)
    :param fill: An openpyxl PatternFill
    """

    cell_range = 'B2:F4'
    wb = Workbook()
    ws = wb.active
    fill = PatternFill("solid", fgColor="00DD00")
    rows = ws[cell_range]
    for row in rows:
        for c in row:
            c.fill = fill
    wb.save("styled.xlsx")

def main():
    print("In OpenpyxlStyle.main")
    style_range(None,None,None)
    print("Open styled.xlsx with a spreadsheet reader")
if __name__ == "__main__" :
   main()

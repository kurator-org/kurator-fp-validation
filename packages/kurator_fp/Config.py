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
__version__ = "Config.py 2017-04-23T17:15:28-04:00"

import sys
import ConfigParser

def config(configFileName=None) :  #default stats.ini
      if configFileName is None:
         configFileName ="stats.ini"
      parser = ConfigParser.ConfigParser()
      ccc = parser.read(configFileName)
      vvv = parser.get('DEFAULT','validators')
      ooo = parser.get('DEFAULT','outcomes')
      dict = {'validators':vvv, 'outcomes':ooo}
      try:
            fff = parser.get('OPENPYXL','outcomeFills')
            dict['outcomeFills']=fff
#      except parser.NoSectionError: No section 'OPENPYXL':
      except Exception: # parser.NoSectionError:
            print("NSE:")
      return dict

   #Helper function borrowed from
   #   https://wiki.python.org/moin/ConfigParserExamples
def ConfigSectionMap(section):
    dict1 = {}
    options = Config.options(section)
    for option in options:
        try:
            dict1[option] = Config.get(section, option)
            if dict1[option] == -1:
                DebugPrint("skip: %s" % option)
        except:
            print("exception on %s!" % option)
            dict1[option] = None
    return dict1

def main():
   import pprint
   cc = config("stats.ini")
   print("cc=")
   print(cc)

   
#   outcomeFills = ConfigSectionMap("OPENPYXL")['outcomeFills']
#   print(outcomeFills)

def xmain():
      print("main")
      
if __name__ == "__main__" :
   main()


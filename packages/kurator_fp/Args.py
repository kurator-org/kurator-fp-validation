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
__version__ = "Args.py 2017-03-04T22:05:21-05:00"

import argparse

class Args :
#   def __init__(self, infile, outfile, configFile):
   def __init__(self, configFile):   
      self.parser = argparse.ArgumentParser()
#      self.parser.add_argument('--i',default='occurrence_qc.json', help="Defaults to occurrence_qc.json if '--i' absent")
#      self.parser.add_argument('--o',default='outcomeStats.xlsx', help="Defaults to outcomeStats.xlsx if '--o' absent")
      self.parser.add_argument('--c',default='stats.ini', help="Defaults to stats.ini if --c absent")
      self.args = self.parser.parse_args()
 #     self.infile = infile
#      self.outfile = outfile
      self.configFile = configFile
      
   def getArgs(self):
      return self.args

   def getInfile(self):
       return self.infile

   def getOutfile(self):
       return self.outfile

   def getConfigfile(self):
       return self.configFile

def main():
   import pprint
#   print("Args.main()")
   args=Args('occurrence_qc.json', 'outcomeStats.xlsx', 'stats.ini')
   print("a=",args)
   pprint.pprint(args.getArgs())
   print("infile=",args.getInfile(), "outfile=", args.getOutfile(), "configFile=", args.getConfigfile())
#   print(args.i)



if __name__ == "__main__" :
   main()


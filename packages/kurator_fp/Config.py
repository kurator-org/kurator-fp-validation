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
__version__ = "Config.py 2017-03-25T15:55:59-04:00"

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
#      print(dict)
#      sys.exit()
      return dict


def main():
   import pprint
   cc = config("stats.ini")
   print("cc=")
   print(cc)

if __name__ == "__main__" :
   main()


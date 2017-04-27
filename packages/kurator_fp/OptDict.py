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
__version__ = "OptDict.py 2017-04-27T11:09:22-04:00"

import Config
import sys
def getOptDict(configFile=None, rowOrigin=None, colOrigin=None ):
    import Config
    if configFile is None:
        configFile = "stats.ini"
    if rowOrigin is None:
        rowOrigin = 1
    if colOrigin is None:
        colOrigin = 1
        
    config = Config.config(configFile) #should catch file system exceptions?
    outcomeFills = eval(config['outcomeFills'])
    outcomesFolded = eval(config['outcomesFolded'])    
    outcomes = eval(config['outcomes'])
    validators = eval(config['validators'])
    dict = {'inputfile':'occurrence_qc.json', 'outputfile':'stats.json', 'rowOrigin':rowOrigin, 'colOrigin':colOrigin, 'outcomeFills':outcomeFills,  'outcomesFolded':outcomesFolded, 'outcomes':outcomes, 'validators':validators}
    return dict

def main():
    optdict = getOptDict(rowOrigin=3)
    print optdict

if __name__ == "__main__" :
   main()

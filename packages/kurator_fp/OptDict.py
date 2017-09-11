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
from collections import MutableMapping
import Config
import pprint

__author__ = "Robert A. Morris, David B. Lowery"
__copyright__ = "Copyright 2017 President and Fellows of Harvard College"
__version__ = "OptDict.py 2017-05-24T11:09:22-04:00"

class OptDict(MutableMapping):
    """ OptDict object to be used as actor input as well as the actor output. This type extends 
        MutableMapping and instances of OptDict can be used in place of a python dict.
        Provides methods for actor specific dictionary entries.
        
     See: https://docs.python.org/3/library/collections.abc.html#module-collections.abc"""

    def __init__(self, workspace, inputfile, outputfile):
        """ Create an options dictionary containing the supplied values for each 
            of the required parameters (workspace, inputfile and outputfile)"""

        self.optdict = {
            'workspace': workspace,
            'inputfile': inputfile,
            'outputfile': outputfile,
            'artifacts': {}
        }

        # TODO: check if workspace dir, inputfile and output file exist

    def configure(self, configFile='stats.ini'):
        """ Load values from the config file into the options dictionary. Accepts the
            the name of the config file as an argument (defaults to stats.ini)"""

        config = Config.config(configFile)

        self.optdict.update({
            'outcomeFills': eval(config['outcomeFills']),
            'outcomesFolded': eval(config['outcomesFolded']),
            'outcomes': eval(config['outcomes']),
            'validators': eval(config['validators'])
        });

    def origin(self, rowOrigin=1, colOrigin=1):
        """ Set row and column origin indicies as parameter values in the options dictionary.
            Defaults to rowOrigin=1 and colOrigin=1 if None value is supplied"""

        self.optdict.update({
            'rowOrigin': rowOrigin,
            'colOrigin': colOrigin
        });

    def status(self, success, message):
        """ Add values indicating success or failure (True or False) and a message describing
            the result conditions to the options dictionary. Use this to set these values
            for an OptDict that will serve as the response"""

        self.optdict.update({
            'success': success,
            'message': message
        });

    def publish(self, label, filename):
        """ Publish an artifact file after it has been saved to the filesystem. An artifact entry consists
            of a label and the path to the file. This lets the workflow engine communicate to client 
            applications information about what was produced and where to find the results."""

        artifacts = self.optdict['artifacts'];
        artifacts[label] = filename;

    # Python equivalent to Java toString().
    # will "pretty print" the internal optdict
    def __str__(self):
        pp = pprint.PrettyPrinter(indent=4)
        return pp.pformat(self.optdict);

    # The following are part of the interface that allows this
    # class to be used as an implementation of python dict
    def __getitem__(self, key):
        return self.optdict[key]

    def __setitem__(self, key, value):
        self.optdict[key] = value;

    def __delitem__(self, key):
        del key

    def __iter__(self):
        return iter(self.optdict)

    def __len__(self):
        return len(self.optdict)

def main():
    # initialize with required values for workspace, inputfile, outputfile
    optdict = OptDict('./outcome_stats_workspace', 'occurrence_qc.json', 'stats.xlsx');

    # load configuration from file
    optdict.configure('stats.ini');

    # set origins
    optdict.origin(1, 1);

    # publish an artifact
    optdict.publish('stats_xlsx', 'stats.xlsx');

    # pretty print the optdict
    print optdict

    # surprise: an object of type OptDict is also a dict ;)
    # example below is of accessing a value by key
    print 'value of inputfile before change: ' + optdict['inputfile']

    # example of set value with dict syntax
    optdict['inputfile'] = 'some_new_file.txt'
    print 'value of inputfile after change: ' + optdict['inputfile']

if __name__ == "__main__" :
   main()

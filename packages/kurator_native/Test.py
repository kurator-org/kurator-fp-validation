import numpy as np
import sys

def test(optdict):
    print "optdict: " + str(optdict)

    #arr = np.array([6, 7, 8])
    print sys.api_version

    return {
        'test': 'Hello World!',
        'test2': "Goodbye world!",
        'workspace': optdict['workspace']
    }

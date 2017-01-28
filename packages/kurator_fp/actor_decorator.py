from dwca_utils import response
from dwca_utils import setup_actor_logging
import os
import logging
import uuid
import inspect

def python_actor(do_stuff):
    def do_stuffer(options):
        print ("path" + os.getcwd())
        setup_actor_logging(options)

        #logging.debug('Started %s' % __version__)
        logging.debug('options: %s' % options)

        # Make a list of keys in the response dictionary
        returnvars = ['workspace', 'outputfile', 'success', 'message', 'artifacts']

        ### Standard outputs ###
        success = False
        message = None

        ### Custom outputs ###

        # Make a dictionary for artifacts left behind
        artifacts = {}

        ### Establish variables ###
        inputfile = None
        outputfile = None

        ### Required inputs ###
        try:
            workspace = options['workspace']
        except:
            workspace = './'

        try:
            inputfile = options['inputfile']
        except:
            pass

        if inputfile is None or len(inputfile) == 0:
            #message = 'No input file given. %s' % __version__
            message = 'No input file given.'
            returnvals = [workspace, outputfile, success, message, artifacts]
            logging.debug('message:\n%s' % message)
            return response(returnvars, returnvals)

        if os.path.isfile(inputfile) == False:
            #message = 'Input file %s not found. %s' % (inputfile, __version__)
            message = 'Input file %s not found.' % inputfile
            returnvals = [workspace, outputfile, success, message, artifacts]
            logging.debug('message:\n%s' % message)
            return response(returnvars, returnvals)

        try:
            outputfile = options['outputfile']
        except:
            pass

        if outputfile is None or len(outputfile) == 0:
            outputfile = 'dwca_' + str(uuid.uuid1()) + '.zip'

        # Construct the output file path in the workspace
        outputfile = '%s/%s' % (workspace.rstrip('/'), outputfile)

        ### Optional inputs ###
        params = []
        argspec = inspect.getargspec(do_stuff)

        for arg in argspec.args:
            if arg == 'inputfile':
                params.append(inputfile)
            elif arg == 'outputfile':
                params.append(outputfile)
            elif arg == 'workspace':
                params.append(workspace)

            else:
                params.append(options[arg])

        # Do the actual work now that the preparation is complete
        success = do_stuff(*params)

        # Add artifacts to the output dictionary if all went well
        if success == True:
            artifacts['template_output_file'] = outputfile

        # Prepare the response dictionary
        returnvals = [workspace, outputfile, success, message, artifacts]
        #logging.debug('Finishing %s' % __version__)
        logging.debug('Finishing')
        return response(returnvars, returnvals)
    return do_stuffer

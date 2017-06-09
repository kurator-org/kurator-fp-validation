from array import array

int32 = int

class ndarray(object):
    '''See: https://docs.scipy.org/doc/numpy/reference/generated/numpy.ndarray.html'''

    def __init__(self, shape, dtype=float):
        print "implement me: ndarray.__init__"

    def tolist(self):
        '''See: https://docs.scipy.org/doc/numpy/reference/generated/numpy.ndarray.tolist.html'''
        print "implement me: ndarray.tolist"
        return [[0, 2, 10, 0, 0], [6, 0, 0, 6, 0], [10, 0, 0, 2, 0], [0, 0, 0, 0, 12]]

    def itemset(self, *args):
        '''See: https://docs.scipy.org/doc/numpy/reference/generated/numpy.ndarray.itemset.html'''
        print "implement me: ndarray.itemset"

    def __add__(self, other):
        '''See: https://docs.python.org/2/reference/datamodel.html#object.__add__'''
        print "implement me: ndarray.__add__"
        return ndarray((5,5), int)

    def __iter__(self):
        '''See: https://stackoverflow.com/questions/19151/build-a-basic-python-iterator'''
        return self

    def next(self):
        '''See: https://stackoverflow.com/questions/19151/build-a-basic-python-iterator'''
        print "implement me: ndarray.next"
        raise StopIteration


def zeros(shape, dtype=None):
    '''See: https://docs.scipy.org/doc/numpy/reference/generated/numpy.zeros.html'''
    print "implement me: zeros"
    return ndarray(shape, dtype)

def array(array_like):
    '''See: https://docs.scipy.org/doc/numpy/reference/generated/numpy.array.html'''
    print "implement me: array"
    return ndarray((5,5), int)

def asarray(array_like):
    '''https://docs.scipy.org/doc/numpy/reference/generated/numpy.asarray.html'''
    array(array_like)
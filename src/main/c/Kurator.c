#include <Python.h>
#include "Kurator.h"
#include <dlfcn.h>

void test() {
    printf("Hello world!");
}

JNIEXPORT void JNICALL Java_Kurator_sayHello(JNIEnv *env, jobject obj) {
    PyObject *pName, *pModule, *pDict, *pFunc;
    PyObject *pArgs, *pValue;
    int i;

    dlopen("libpython2.7.so", RTLD_LAZY | RTLD_GLOBAL);

    //if (argc < 3) {
    //    fprintf(stderr,"Usage: call pythonfile funcname [args]\n");
    //    return 1;
    //}

    Py_Initialize();
    pName = PyString_FromString("hello.hello");
    // TODO Error checking of pName

    pModule = PyImport_Import(pName);
    Py_DECREF(pName);

    if (pModule != NULL) {
        pFunc = PyObject_GetAttrString(pModule, "hello");
        /* pFunc is a new reference */

        if (pFunc && PyCallable_Check(pFunc)) {
            pArgs = PyTuple_New(1);

            //pDict = PyDict_New();
            pValue = PyString_FromString("David Lowery");

//            for (i = 0; i < argc - 3; ++i) {
//                pValue = PyInt_FromLong(atoi(argv[i + 3]));
//                if (!pValue) {
//                    Py_DECREF(pArgs);
//                    Py_DECREF(pModule);
//                    fprintf(stderr, "Cannot convert argument\n");
//                    return 1;
//            }

            PyTuple_SetItem(pArgs, 0, pValue);

            pValue = PyObject_CallObject(pFunc, pArgs);
            Py_DECREF(pArgs);
            if (pValue != NULL) {
                printf("Result of call: %ld\n", PyInt_AsLong(pValue));
                Py_DECREF(pValue);
            }
            else {
                Py_DECREF(pFunc);
                Py_DECREF(pModule);
                PyErr_Print();
                fprintf(stderr,"Call failed\n");
                //return 1;
            }
        }
        else {
            if (PyErr_Occurred())
                PyErr_Print();
            //fprintf(stderr, "Cannot find function \"%s\"\n", argv[2]);
        }
        Py_XDECREF(pFunc);
        Py_DECREF(pModule);
    }
    else {
        PyErr_Print();
        //fprintf(stderr, "Failed to load \"%s\"\n", argv[1]);
        //return 1;
    }
    Py_Finalize();
    //return 0;
}
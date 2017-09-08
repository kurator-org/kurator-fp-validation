CFLAGS=-I/usr/include/python2.7 -I/usr/include/x86_64-linux-gnu/python2.7  -fno-strict-aliasing -Wdate-time -D_FORTIFY_SOURCE=2 -g -fstack-protector-strong -Wformat -Werror=format-security  -DNDEBUG -g -fwrapv -O2 -Wall -Wstrict-prototypes
LDFLAGS=-L/usr/lib/python2.7/config-x86_64-linux-gnu -L/usr/lib -lpython2.7 -lpthread -ldl  -lutil -lm  -Xlinker -export-dynamic -Wl,-O1 -Wl,-Bsymbolic-functions

JNIINC=-I/usr/lib/jvm/java-8-oracle/include/ -I/usr/lib/jvm/java-8-oracle/include/linux

OPTS=-shared -fPIC
PROGRAMS=org_kurator_validation_actors_PythonInterpreter
all: $(PROGRAMS)

org_kurator_validation_actors_PythonInterpreter.o: org_kurator_validation_actors_PythonInterpreter.c
	gcc -c $(JNIINC) $(CFLAGS) $(OPTS) org_kurator_validation_actors_PythonInterpreter.c
org_kurator_validation_actors_PythonInterpreter: org_kurator_validation_actors_PythonInterpreter.o
	gcc org_kurator_validation_actors_PythonInterpreter.o $(LDFLAGS) $(OPTS) -o libkurator.so
clean:
	rm -f $(PROGRAMS) *.o *.pyc core

CFLAGS=-I/usr/include/python2.7 -I/usr/include/x86_64-linux-gnu/python2.7  -fno-strict-aliasing -Wdate-time -D_FORTIFY_SOURCE=2 -g -fstack-protector-strong -Wformat -Werror=format-security  -DNDEBUG -g -fwrapv -O2 -Wall -Wstrict-prototypes
LDFLAGS=-L/usr/lib/python2.7/config-x86_64-linux-gnu -L/usr/lib -lpython2.7 -lpthread -ldl  -lutil -lm  -Xlinker -export-dynamic -Wl,-O1 -Wl,-Bsymbolic-functions

JNIINC=-I/usr/lib/jvm/java-8-oracle/include/ -I/usr/lib/jvm/java-8-oracle/include/linux

OPTS=-shared -fPIC
PROGRAMS=Kurator 
all: $(PROGRAMS)

Kurator.o: Kurator.c
	gcc -c $(JNIINC) $(CFLAGS) $(OPTS) Kurator.c
Kurator: Kurator.o
	gcc Kurator.o $(LDFLAGS) $(OPTS) -o libkurator.so
clean:
	rm -f $(PROGRAMS) *.o *.pyc core

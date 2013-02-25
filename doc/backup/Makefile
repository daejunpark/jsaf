# Replace the Java and Yices home directories below with the directories used in your system

JAVA_HOME = /usr/lib/jvm/java-6-sun
YICES_HOME = ../../third_party/yices/linux/yices-1.0.36

yices: YicesSolver.c++
	gcc -fPIC JNIYices.c++ YicesSolver.c++ -Wl,-soname,libyicesSafe.so -o ./libyicesSafe.so -shared -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux -I$(YICES_HOME)/include -L$(YICES_HOME)/lib -lgmp -lyices -lstdc++

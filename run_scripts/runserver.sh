#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=.

echo 'Compiling...'
javac ResInterface/ResourceManager.java
javac -Xlint ResImpl/ResourceManagerImpl.java

echo 'Generating jar file(s)...'
jar cvf ResInterface.jar ResInterface/*.class

echo 'Running RM server...' 
java -Djava.security.policy=java.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/servercode/ ResImpl.ResourceManagerImpl

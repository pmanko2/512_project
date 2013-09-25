#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/comp512/servercode

echo 'Compiling...'
javac ~/comp512/servercode/ResInterface/ResourceManager.java
javac -Xlint ~/comp512/servercode/ResImpl/ResourceManagerImpl.java

echo 'Generating jar file(s)...'
jar cvf ~/comp512/servercode/ResInterface.jar ~/comp512/servercode/ResInterface/*.class

echo 'Running RM server...' 
java -Djava.security.policy=/home/2011/nwebst1/comp512/servercode/server.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/servercode/ ResImpl.ResourceManagerImpl && exit


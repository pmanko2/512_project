#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/comp512

echo 'Compiling...'
javac ~/comp512/ResInterface/ResourceManager.java
javac -Xlint ~/comp512/ResImpl/ResourceManagerImpl.java

echo 'Generating jar file(s)...'
jar cvf ~/comp512/ResInterface.jar ~/comp512/ResInterface/*.class

echo 'Running RM server...' 
java -Djava.security.policy=/home/2011/nwebst1/comp512/server.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/ ResImpl.ResourceManagerImpl && exit


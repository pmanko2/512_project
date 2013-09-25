#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/comp512/servercode:~/comp512/servercode/ResInterface.jar

echo 'Compiling...'
javac ~/comp512/servercode/ResImpl/MiddlewareImpl.java

echo 'Running Middleware server...'
java -Djava.security.policy=/home/2011/nwebst1/comp512/servercode/server.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/servercode/ ResImpl/MiddlewareImpl 7707 lab2-10.cs.mcgill.ca lab2-11.cs.mcgill.ca lab2-12.cs.mcgill.ca && exit

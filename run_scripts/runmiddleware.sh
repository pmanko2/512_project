#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=.:./ResInterface.jar

echo 'Compiling...'
javac MiddlewareImpl.java

echo 'Running Middleware server...'
java -Djava.security.policy=middle.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/middleware/ MiddlewareImpl

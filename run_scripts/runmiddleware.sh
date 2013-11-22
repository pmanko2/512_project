#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/comp512:~/comp512/TransactionManager:~/comp512/LockManager/:~/comp512/LockManager/XObj.class:~/comp512/ResInterface.jar

echo 'Compiling...'
javac ~/comp512/TransactionManager/*.java
javac ~/comp512/LockManager/LockManager.java
javac ~/comp512/ResImpl/MiddlewareImpl.java

echo 'Running Middleware server...'
java -Djava.security.policy=/home/2011/nwebst1/comp512/server.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/ ResImpl/MiddlewareImpl 7707 lab2-1.cs.mcgill.ca lab2-15.cs.mcgill.ca lab2-14.cs.mcgill.ca && exit


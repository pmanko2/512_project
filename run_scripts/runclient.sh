#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=.:./ResInterface.jar

echo 'Compiling...'
javac -Xlint client.java

echo 'Running client...'
java -Djava.security.policy=java.policy client teaching

#!/bin/bash

echo 'Establishing classpath...'
export CLASSPATH=~/comp512/Client/:~/comp512/Client/ResInterface.jar:~/comp512

echo 'Compiling...'
javac -Xlint ~/comp512/Client/Client.java

echo 'Running Client...'
java -Djava.security.policy=/home/2011/nwebst1/comp512/Client/client.policy Client.Client teaching

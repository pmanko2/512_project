#!/bin/bash

#If you don't know how, use the following website to allow to login from your laptop
#without typing a password
#http://www.linuxproblem.org/art_9.html

#Info for servers
SERVER_HOSTS="lab2-10.cs.mcgill.ca lab2-11.cs.mcgill.ca lab2-12.cs.mcgill.ca"
MIDDLEWARE_HOST="teaching.cs.mcgill.ca"
USERNAME=nwebst1

#kill processes started on each machine
cleanup()
{
    CLEANUP_SCRIPT="ps -u nwebst1 | grep -ie java | awk '{print \$1}' | xargs kill -9"

    for HOSTNAME in ${SERVER_HOSTS} ; do
        printf "\nCleaning up at ${HOSTNAME}...\n\n"
        ssh -l ${USERNAME} ${HOSTNAME} "${CLEANUP_SCRIPT}"
        sleep 5
    done

    printf "\nCleaning up at ${MIDDLEWARE_HOST}...\n\n"
    ssh -l ${USERNAME} ${MIDDLEWARE_HOST} "${CLEANUP_SCRIPT}"
    sleep 5

    printf "\n*************************"
    printf "CLEANUP COMPLETE"
    printf "*************************\n\n\n"
}

control_c()
{
    printf "\n*************************"
    printf "SHUTTING DOWN"
    printf "*************************\n"
    cleanup
    exit $?
}

#trap keyboard interrupt
trap control_c SIGINT

start()
{
    SERVER_SCRIPT="~/comp512/servercode/runserver.sh && exit"
    MIDDLEWARE_SCRIPT="~/comp512/servercode/runmiddleware.sh && exit"

    #start up RM servers
    for HOSTNAME in ${SERVER_HOSTS} ; do
        printf "\nConnecting to ${HOSTNAME}...\n\n"
        ssh -l ${USERNAME} ${HOSTNAME} "${SERVER_SCRIPT}" &
        sleep 5
    done

    #start up Middleware Server
    printf "\nConnecting to ${MIDDLEWARE_HOST}...\n\n"
    ssh -l ${USERNAME} ${MIDDLEWARE_HOST} "${MIDDLEWARE_SCRIPT}" &

    sleep 5
    printf "\n\n PRESS CONTROL-C TO QUIT\n\n"

    while :
    do
        sleep 1
    done
}

start
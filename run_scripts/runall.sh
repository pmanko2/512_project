#!/bin/bash

#If you don't know how, use the following website to allow to login from your laptop
#without typing a password
#http://www.linuxproblem.org/art_9.html

#Info for servers
SERVER_HOSTS="lab2-1.cs.mcgill.ca lab2-5.cs.mcgill.ca lab2-7.cs.mcgill.ca"
MIDDLEWARE_HOST="teaching.cs.mcgill.ca"
USERNAME=nwebst1

#kill processes started on each machine
#<<COMMENT
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
    sleep 3

    printf "\n*************************"
    printf "CLEANUP COMPLETE"
    printf "*************************\n\n\n"
}
#COMMENT
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
    SERVER_SCRIPT="~/comp512/runserver.sh && exit"
    MIDDLEWARE_SCRIPT="~/comp512/runmiddleware.sh && exit"

	SERVER_SCRIPT_NEW=("~/comp512/runserver_flights.sh" "~/comp512/runserver_cars.sh" "~/comp512/runserver_hotels.sh");

	index=0

    #start up RM servers
    for HOSTNAME in ${SERVER_HOSTS} ; do
        printf "\nConnecting to ${HOSTNAME}...\n\n"
        ssh -l ${USERNAME} ${HOSTNAME} "${SERVER_SCRIPT_NEW[index]}" &
        sleep 5
	index=$index+1
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

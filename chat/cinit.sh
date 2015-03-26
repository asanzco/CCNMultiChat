#!/bin/sh

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 username password" >&2
  exit 1
fi

clear

export USERNAME=$1
export SSHPASS=$2

Init() {
    echo 'Initializing ccnd in' $1 '.....'
    sshpass -e ssh -l $USERNAME $1 ccndstop
    sshpass -e ssh -l $USERNAME $1 ccndstart &
}

AddRoute() {
    echo 'Adding forwarding route' $3 'to ccnd in' $1 'to' $2 '.....'
    sshpass -e ssh -l $USERNAME $1 ccndc add $3 udp $2

    if $4; then
        echo 'Adding forwarding route' $3 'to ccnd in' $2 'to' $1 '.....'
        sshpass -e ssh -l $USERNAME $2 ccndc add $3 udp $1
    fi
}

Stop() {
    echo 'Stopping ccnd in ' $1 '.....'
    sshpass -e ssh -l $USERNAME $1 ccndstop
}


# Init daemons
Init epsilon01.aulas.gsyc.es
Init epsilon02.aulas.gsyc.es
Init epsilon03.aulas.gsyc.es
Init epsilon04.aulas.gsyc.es
Init epsilon05.aulas.gsyc.es

# Topology
AddRoute epsilon01.aulas.gsyc.es epsilon02.aulas.gsyc.es ccnx:/ true
AddRoute epsilon02.aulas.gsyc.es epsilon03.aulas.gsyc.es ccnx:/ true
AddRoute epsilon02.aulas.gsyc.es epsilon04.aulas.gsyc.es ccnx:/ true
AddRoute epsilon04.aulas.gsyc.es epsilon05.aulas.gsyc.es ccnx:/ true

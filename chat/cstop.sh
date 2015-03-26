#!/bin/sh

if [ "$#" -ne 2 ]; then
  echo "Usage: $0 username password" >&2
  exit 1
fi

clear

export USERNAME=$1
export SSHPASS=$2

Stop() {
    echo 'Stopping ccnd in' $1 '.....'
    sshpass -e ssh -l $USERNAME $1 ccndstop
}

# Init daemons
Stop epsilon01.aulas.gsyc.es
Stop epsilon02.aulas.gsyc.es
Stop epsilon03.aulas.gsyc.es
Stop epsilon04.aulas.gsyc.es
Stop epsilon05.aulas.gsyc.es

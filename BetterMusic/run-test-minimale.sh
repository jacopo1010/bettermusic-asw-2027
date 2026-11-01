#!/bin/bash

# Script per avviare il test minimale con REST e Kafka

conta_recensioni_seguite() {
    curl -s localhost:8080/recensioni-seguite/recensioniseguite/$1 | jq 'if type == "array" then length else -1 end' # -1 indica un errore
}

verifica() {
    alice=$(conta_recensioni_seguite Alice)
    bob=$(conta_recensioni_seguite Bob)
    carlo=$(conta_recensioni_seguite Carlo)

    if [ "$alice" -eq "$2" ] && [ "$bob" -eq "$3" ] && [ "$carlo" -eq "$4" ]; then
        esito="OK"
    else
        esito="FAIL"
    fi

    echo "$1: Alice=$alice Bob=$bob Carlo=$carlo (attesi $2 $3 $4) $esito"
}

# Inizializzazione db
./do-init-bettermusic-db.sh > /dev/null 2>&1
verifica "dopo init" 5 6 5

# Update albums
./do-update-albums.sh > /dev/null 2>&1
verifica "dopo update album" 5 6 5

# Update recensioni
./do-update-recensioni.sh > /dev/null 2>&1
verifica "dopo update recensioni" 6 8 7

#Update connessioni
./do-update-connessioni.sh > /dev/null 2>&1
verifica "dopo update connessioni" 7 5 8
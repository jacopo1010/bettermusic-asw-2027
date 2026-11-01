#!/bin/bash

# Script per avviare il test minimale con REST e Kafka

conta_recensioni_seguite() {
    curl -s localhost:8080/recensioni-seguite/recensioniseguite/$1 | jq 'if type == "array" then length else -1 end' # -1 indica un errore
}

# Funzione per verificare che i risultati ottenuti siano corretti
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

# Funzione che attende l'avvio di un servizio prima di iniziare i test
attendi_avvio_servizio() {
    tentativi=0
    while [ "$(curl -s -o /dev/null -w "%{http_code}" localhost:8080/$1)" != "200" ]; do
        tentativi=$((tentativi + 1))
        if [ "$tentativi" -ge 60 ]; then
            echo "ERRORE: $1 non risponde dopo 2 minuti, guarda app.log"
            exit 1
        fi 
        sleep 2
    done
    echo "$1 pronto"
}

# Avvio del test nella modalità scelta
MODE=${1:-rest}

case "$MODE" in
    rest)
        IMPL=RecensioniSeguiteServiceRestBasedImpl
        ;;
    kafka)
        IMPL=RecensioniSeguiteServiceKafkaBasedImpl
        ;;
    *)
        echo "Uso: $0 [rest|kafka]"
        exit 1
        ;;
esac

echo "--- Test minimale in modalità $MODE ($IMPL) ---"

echo "Arresto dell'applicazione..."
./stop-bettermusic.sh > /dev/null 2>&1

echo "Avvio dell'applicazione..."
ASW_RECENSIONISEGUITESERVICE_IMPLEMENTATION=$IMPL ./start-bettermusic.sh > app.log 2>&1

echo "Attesa dell'avvio dei servizi..."
attendi_avvio_servizio album/album
attendi_avvio_servizio recensioni/recensioni
attendi_avvio_servizio connessioni/connessioni
attendi_avvio_servizio recensioni-seguite/recensioniseguite/Alice # utente a caso sennò non funziona

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

# Messaggio per arrestare l'applicazione
echo "per arrestare l’applicazione: ./stop-bettermusic.sh"
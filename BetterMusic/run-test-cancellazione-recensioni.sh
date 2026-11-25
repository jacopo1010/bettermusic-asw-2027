#!/bin/bash

# Script per avviare il test della cancellazione delle recensioni con REST e Kafka
#
# Parte dallo stato finale del test minimale (Alice=7 Bob=5 Carlo=8), in cui:
#   Alice segue gli artisti Pink Floyd, The Beatles, Bruce Springsteen
#   Bob segue i recensori Woody, RollingStone
#   Carlo segue i generi Rock, Pop, Jazz
# e poi cancella alcune recensioni, verificando che le recensioni seguite si aggiornino.

conta_recensioni_seguite() {
    curl -s localhost:8080/recensioni-seguite/recensioniseguite/$1 | jq 'if type == "array" then length else -1 end' # -1 indica un errore
}

# Funzione per verificare che i risultati ottenuti siano corretti
verifica() {
    # in modalità kafka le repliche locali si aggiornano in modo asincrono: lascia il tempo di propagare gli eventi
    if [ "$MODE" = "kafka" ]; then
        sleep $ATTESA_PROPAGAZIONE_EVENTI
    fi

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

# Funzione che trova l'id di una recensione, dati recensore, titolo e artista dell'album
# (così il test non dipende dall'ordine in cui vengono assegnati gli id)
id_recensione() {
    id_album=$(curl -s -G localhost:8080/album/cercaalbum --data-urlencode "titolo=$2" --data-urlencode "artista=$3" | jq '.id')
    curl -s localhost:8080/recensioni/cercarecensioni/album/$id_album | jq --arg r "$1" '.[] | select(.recensore == $r) | .id'
}

# Funzione che cancella una recensione (segnala solo se il codice HTTP non è quello atteso)
cancella_recensione() {
    codice=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE localhost:8080/recensioni/recensioni/$1)
    if [ "$codice" != "$2" ]; then
        echo "DELETE /recensioni/$1: HTTP $codice (atteso $2) FAIL"
    fi
}

KAFKA_COMPOSE=kafka/docker/docker-compose.yml
ATTESA_PROPAGAZIONE_EVENTI=5 # secondi

# Avvio del test nella modalità scelta
MODE=${1:-rest}

case "$MODE" in
    rest)
        IMPL=RecensioniSeguiteServiceRestBasedImpl
        IMPL_RECENSIONI=RecensioniServiceRestBasedImpl
        ;;
    kafka)
        IMPL=RecensioniSeguiteServiceKafkaBasedImpl
        IMPL_RECENSIONI=RecensioniServiceKafkaBasedImpl
        ;;
    *)
        echo "Uso: $0 [rest|kafka]"
        exit 1
        ;;
esac

echo "--- Test cancellazione recensioni in modalità $MODE (recensioni-seguite: $IMPL, recensioni: $IMPL_RECENSIONI) ---"

echo "Arresto dell'applicazione..."
./stop-bettermusic.sh > /dev/null 2>&1

echo "Avvio dell'applicazione..."
if ! ASW_RECENSIONISEGUITESERVICE_IMPLEMENTATION=$IMPL ASW_RECENSIONISERVICE_IMPLEMENTATION=$IMPL_RECENSIONI ./start-bettermusic.sh > app.log 2>&1; then
    echo "ERRORE: avvio dell'applicazione fallito:"
    grep ERRORE app.log
    exit 1
fi

echo "Attesa dell'avvio dei servizi..."
attendi_avvio_servizio album/album
attendi_avvio_servizio recensioni/recensioni
attendi_avvio_servizio connessioni/connessioni
attendi_avvio_servizio recensioni-seguite/recensioniseguite/Alice # utente a caso sennò non funziona

# in modalità kafka, attende che i consumer si uniscano ai gruppi prima di inizializzare i dati
if [ "$MODE" = "kafka" ]; then
    echo "Attesa che i consumer Kafka si uniscano ai gruppi..."
    sleep 15
fi

# Preparazione dei dati: stesso stato finale del test minimale
./do-init-albums.sh > /dev/null 2>&1
if [ "$MODE" = "kafka" ]; then sleep $ATTESA_PROPAGAZIONE_EVENTI; fi
./do-init-recensioni.sh > /dev/null 2>&1
./do-init-connessioni.sh > /dev/null 2>&1
./do-update-albums.sh > /dev/null 2>&1
if [ "$MODE" = "kafka" ]; then sleep $ATTESA_PROPAGAZIONE_EVENTI; fi
./do-update-recensioni.sh > /dev/null 2>&1
./do-update-connessioni.sh > /dev/null 2>&1
verifica "prima delle cancellazioni" 7 5 8

# Woody / The Dark Side of the Moon: la seguono Alice (Pink Floyd), Bob (Woody), Carlo (Rock)
cancella_recensione $(id_recensione "Woody" "The Dark Side of the Moon" "Pink Floyd") 200
verifica "dopo cancellazione 1" 6 4 7

# OndaRock / Thriller: la segue solo Carlo (Pop), Bob non segue più OndaRock
cancella_recensione $(id_recensione "OndaRock" "Thriller" "Michael Jackson") 200
verifica "dopo cancellazione 2" 6 4 6

# RollingStone / Kind of Blue: la seguono Bob (RollingStone) e Carlo (Jazz)
cancella_recensione $(id_recensione "RollingStone" "Kind of Blue" "Miles Davis") 200
verifica "dopo cancellazione 3" 6 3 5

# Woody / The Wall: la seguono Alice (Pink Floyd) e Bob (Woody), non Carlo (Progressive Rock)
id=$(id_recensione "Woody" "The Wall" "Pink Floyd")
cancella_recensione $id 200
verifica "dopo cancellazione 4" 5 2 5

# di nuovo Woody / The Wall, ormai inesistente: risponde 404 e non cambia nulla
cancella_recensione $id 404
verifica "dopo cancellazione inesistente" 5 2 5

# Messaggio per arrestare l'applicazione
echo "per arrestare l’applicazione: ./stop-bettermusic.sh"
echo "per arrestare kafka: docker compose -f $KAFKA_COMPOSE down -v"

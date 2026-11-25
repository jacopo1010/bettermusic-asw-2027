#!/bin/bash

# Script per avviare l'applicazione BetterMusic

# Funzione che riavvia Kafka da zero (senza i dati di esecuzioni precedenti) e ne attende l'avvio
riavvia_kafka() {
    docker compose -f $KAFKA_COMPOSE down -v > /dev/null 2>&1
    docker compose -f $KAFKA_COMPOSE up -d > /dev/null 2>&1
    tentativi=0
    until docker compose -f $KAFKA_COMPOSE exec -T kafka kafka-topics.sh --bootstrap-server localhost:9092 --list > /dev/null 2>&1; do
        tentativi=$((tentativi + 1))
        if [ "$tentativi" -ge 30 ]; then
            echo "ERRORE: Kafka non risponde dopo 1 minuto, guarda: docker compose -f $KAFKA_COMPOSE logs kafka"
            exit 1
        fi
        sleep 2
    done
    echo "Kafka pronto"
}
KAFKA_COMPOSE=kafka/docker/docker-compose.yml

echo Starting BETTERMUSIC

# Controllo che Docker sia attivo
if ! docker info > /dev/null 2>&1; then
    echo "ERRORE: Docker non è avviato, avvia Docker Desktop e riprova"
    exit 1
fi

echo "Riavvio di Kafka (topic vuoti)..."
riavvia_kafka

java -Xms64m -Xmx128m -jar album/build/libs/album.jar &
java -Xms64m -Xmx128m -jar recensioni/build/libs/recensioni.jar &
java -Xms64m -Xmx128m -jar connessioni/build/libs/connessioni.jar &
java -Xms64m -Xmx128m -jar recensioni-seguite/build/libs/recensioni-seguite.jar &
# oppure: 
# ASW_RECENSIONISEGUITESERVICE_IMPLEMENTATION=RecensioniSeguiteServiceRestBasedImpl java -Xms64m -Xmx128m -jar recensioni-seguite/build/libs/recensioni-seguite.jar &

java -Xms64m -Xmx128m -jar api-gateway/build/libs/api-gateway.jar &

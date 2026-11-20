# DOCUMENTAZIONE — BetterMusic

Registro sempre aggiornato delle modifiche al codice (Attività 1: comunicazione asincrona con Kafka).

## Scopo e regola

Registra **ogni modifica** al codice — sia applicata dall'assistente, sia osservata nei commit dei colleghi — con:
1. **cosa** cambia e **dove**; 2. **chi** e **quando** (commit); 3. **scelte** e **motivazione**;
4. **come** è stata applicata; 5. **basi teoriche** e **riferimenti**.

## Convenzioni di lavoro

- **Commit/PR: nessuna firma come co-autore**.
- Lavoro sui consumatori sul branch **`implementazione-kafka-consumer`** (creato da `master` a `3c62c84`).
- Due copie sincronizzate via GitHub: **WSL** (principale) e **Windows** `C:\dev\bettermusic-asw-2027` (per l'assistente).
- Piano di riferimento: `~/.commandcode/plans/kafka-producer-consumer-bettermusic.md`.

---

## Storico del repository (da `git log`)

| Commit | Autore | Contenuto |
| --- | --- | --- |
| `f77e9fd` | jaorr | Import iniziale del progetto |
| `be5358a` | jaorr | Fix dipendenze/Lombok |
| `0ad4dd2` | Sara | Dipendenze e file Gradle |
| `ffb59e2`→`d356b04` | Sara | `run-test-minimale.sh` REST/Kafka |
| `c2bb09d` | Sara | `AlbumCreatoEvent`, `AlbumDomainEvent` |
| `d5e71bc` | Sara | dipendenza `spring-kafka` |
| `4777868` | Sara | config Kafka nei `application.yml` |
| `6a1749f` | Sara | `AlbumEventPublisher` + `AlbumEventKafkaPublisher` |
| `853f077` | Sara | pubblicazione `AlbumCreatoEvent` in `createAlbum` |
| `3395745` | Sara | funzione `riavvia_kafka` |
| `89b99a0` | jaorr | "Init Kafka" |
| `f309958` | jaorr | produttore connessioni + inizio produttore recensioni |
| `3c62c84` | jaorr | "Fine producer in recensioni" (consumer da fare) |

**Basi teoriche**: pattern eventi di dominio + canale Kafka e API Producer/Consumer di "Spring for Apache Kafka"
(PDF professore `asw840-kafka.pdf`): `DomainEvent`, `...EventChannel`, `EventPublisher`/`EventKafkaPublisher`,
`@KafkaListener` in `messagelistener.kafka`.

---

## Modifiche dell'assistente

Approccio scelto: **impatto minimo sul codice dei colleghi**. I **consumer** usano i **topic e gli eventi esistenti**
(i file dei produttori non vengono modificati); le uniche modifiche su `recensioni` sono lo split REST/Kafka e la replica album,
necessari per la modalità Kafka.

| # | Cosa / Dove | Scelta e motivazione | Come applicata | Basi teoriche |
| --- | --- | --- | --- | --- |
| A1 | Branch `implementazione-kafka-consumer` | Isolare il lavoro dei consumatori | `git checkout -b` da `3c62c84` | convenzione branch del gruppo |
| A2 | `recensioni-seguite/.../domain/{Album,Recensione,Connessione}.java` → `@Entity` + `{Album,Recensione,Connessione}Repository.java` | Recensioni-seguite deve rispondere senza REST: serve una **replica locale** | Annotate `@Entity` (id dagli eventi, `@ElementCollection` per i generi) + repository `CrudRepository` | pattern repliche/`Event Sourcing` (PDF); convenzione JPA del repo (HSQLDB) |
| A3 | `recensioni-seguite/.../domain/ReplicheService.java` | Applicare gli eventi alle repliche (upsert per id) | Metodi `onAlbumCreato/onRecensioneCreata/onConnessioneCreata/onConnessioneCancellata` | handler dominio (`onCommand`/`onEvent` nel PDF) |
| A4 | `recensioni-seguite/.../messagelistener/kafka/{Album,Recensioni,Connessioni}EventKafkaListener.java` | Consumare i 3 topic e aggiornare le repliche | `@KafkaListener(topics=...)` (topic esistenti) + `record.value()` → `ReplicheService` | PDF: consumer in `messagelistener.kafka` |
| A5 | `recensioni-seguite/.../domain/RecensioniSeguiteServiceKafkaBasedImpl.java` | Calcolare le recensioni seguite **solo** dalle repliche locali (zero REST) | Stessa logica di `...RestBasedImpl` ma su repository | PDF (architettura esagonale: porti/adattatori) |
| A6 | `recensioni/.../domain/{RecensioniServiceRestBasedImpl,RecensioniServiceKafkaBasedImpl}.java`, `rest/RecensioniController.java`, `config/AutowireCandidateResolverConfigurer.java` | Selezionare REST vs Kafka da configurazione (API REST invariata) e **correggere** l'ordine `save`→`publish` (prima invertito: id nullo nell'evento) | Due `@Service` + `@Qualifier("${asw.recensioniservice.implementation}")` + configurer; rimosso `RecensioniServiceImpl` | PDF: `save(...)` poi `publish(event)`; pattern qualifier di recensioni-seguite |
| A7 | `recensioni/.../domain/Album.java` (@Entity) + `AlbumReplicaRepository.java` + `messagelistener/kafka/AlbumEventKafkaListener.java` | In modalità Kafka recensioni risolve l'album dalla **replica locale** | Replica album aggiornata da `album-event-channel` | PDF: `@KafkaListener` + replica locale |
| A8 | `recensioni{, -seguite}/src/main/resources/application.yml` | Configurare i consumer | `group-id`, `auto-offset-reset=earliest`, `JsonDeserializer`, `trusted.packages=asw.bettermusic.*`; proprietà `asw.recensioniservice.implementation` | PDF / Spring for Apache Kafka |
| A9 | `run-test-minimale.sh` | Il test in modalità Kafka deve far girare **entrambi** i servizi in versione Kafka | Impostate **due** variabili all'avvio: `ASW_RECENSIONISEGUITESERVICE_IMPLEMENTATION=$IMPL` **e** `ASW_RECENSIONISERVICE_IMPLEMENTATION=$IMPL_RECENSIONI` (valori impostati nel `case rest|kafka`) | selezione da configurazione (come da consegna) |

**Non toccati** (allineamento cosmetico rimandato, come richiesto): classi `...EventChannel` (i topic restano le stringhe già usate dai produttori), rinomino del package eventi `recensioni.producerevent`, e l'uso dell'interfaccia in `ConnessioniServiceImpl`.

---

## Stato / note

- **Build: OK.** `gradle build` lato Windows (JDK 21 + Gradle 8.14.6): compilati tutti i moduli e generati i jar
  (`album.jar`, `recensioni.jar`, `connessioni.jar`, `recensioni-seguite.jar`, `api-gateway.jar` + i `*-api.jar`).
- **Modalità Kafka**: attivata da **entrambe** le proprietà —
  `ASW_RECENSIONISEGUITESERVICE_IMPLEMENTATION=RecensioniSeguiteServiceKafkaBasedImpl` **e**
  `ASW_RECENSIONISERVICE_IMPLEMENTATION=RecensioniServiceKafkaBasedImpl`. Ora `run-test-minimale.sh` le imposta tutte e due (vedi A9).
- **Line endings**: nella clone WSL e Windows gli `.sh` erano in CRLF (per `core.autocrlf=true` di Git for Windows);
  sistemato **in locale** (`core.autocrlf false` + re-checkout), senza commit nel repo condiviso.
- **Test end-to-end non ancora eseguito** (richiede Consul + Kafka + i servizi avviati): da lanciare con
  `./run-test-minimale.sh rest` e `./run-test-minimale.sh kafka`, attendendosi i conteggi 5/6/5 → 5/6/5 → 6/8/7 → 7/5/8.

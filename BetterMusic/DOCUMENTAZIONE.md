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
- **Test end-to-end: eseguito il 25/11/2026** (vedi sezione successiva). `./run-test-minimale.sh rest` e `kafka` danno
  entrambi 5/6/5 → 5/6/5 → 6/8/7 → 7/5/8.

---

## Sessione del 25/11/2026: correzioni Kafka (Attività 1) e cancellazione recensioni (Attività 2)

Commit: da `4d394b3` a `48598fa` (merge della PR #4 `implementazione-delete-recensioni`), autrice Sara.

### Correzioni alla versione Kafka

| # | Cosa / Dove | Problema (sintomo → causa) | Soluzione | Commit |
| --- | --- | --- | --- | --- |
| B1 | `recensioni{, -seguite}/src/main/resources/application.yml` | **Loop infinito del consumer** (log di circa 1 GB in 20 s). Dopo la rinomina del package eventi (`recensioni.producerevent` → `recensioni.api.event`) su Kafka erano rimasti eventi con `__TypeId__` della classe vecchia: il `JsonDeserializer` falliva e rileggeva sempre lo stesso messaggio (*poison pill*) | `value-deserializer: ErrorHandlingDeserializer` con `spring.deserializer.value.delegate.class: JsonDeserializer`: il messaggio illeggibile viene registrato nel log una sola volta e saltato | `4d394b3` |
| B2 | stessi `application.yml` | **Replica vuota dopo il riavvio** di un servizio (7/5/8 → 0/0/0). Le repliche sono in HSQLDB in memoria, ma con un `group-id` fisso il consumer riprendeva dall'offset salvato e non rileggeva lo storico | `group-id: ${spring.application.name}-${random.uuid}` + `auto-offset-reset: earliest`: ogni istanza rilegge tutto lo storico e ricostruisce la replica. Funziona anche con più istanze dello stesso servizio, perché ognuna ha il proprio DB e deve ricevere tutti gli eventi | `534b443` |
| B3 | `start-bettermusic.sh`, `run-test-minimale.sh` | **Kafka non avviato**: i consumer riprovano a collegarsi all'infinito e album si blocca 60 s (`max.block.ms`) per ogni album | Lo start controlla che Docker sia attivo (`docker info`, altrimenti errore ed `exit 1`) e riavvia Kafka da zero (`down -v` + `up -d`), aspettando che risponda. Il test non riavvia più Kafka per conto suo e si ferma se l'avvio fallisce. Kafka viene azzerato perché i DB ripartono vuoti e gli id ripartono da 1: eventi di esecuzioni precedenti creerebbero conflitti di id | `e03db51` |
| B4 | `connessioni-api/.../ConnessioniDomainEvent.java`, `connessioni/.../ConnessioniEventKafkaPublisher.java` | **Messaggi senza chiave**: creazione e cancellazione della stessa connessione potevano finire su partizioni diverse (4 partizioni) ed essere elaborate in ordine inverso | L'interfaccia dichiara `String getUtente()` e il publisher usa `send(channel, event.getUtente(), event)`. La chiave è l'utente e non l'id, perché una connessione ricreata ha un id nuovo. La chiave viene scelta nell'adapter Kafka, non nel dominio | `58fbaad` |

**Nota pratica:** gli script avviano i jar in `*/build/libs/` senza ricompilarli. Dopo ogni pull o merge va eseguito `./gradlew build -x test`, altrimenti gira codice vecchio (è successo due volte).

### Attività 2: cancellazione di una recensione

| # | Cosa / Dove | Scelta e motivazione | Commit |
| --- | --- | --- | --- |
| C1 | `recensioni/.../domain/RecensioniService.java` + `RecensioniServiceRestBasedImpl` / `RecensioniServiceKafkaBasedImpl` | Nuovo metodo `Recensione deleteRecensione(Long id)`, implementato in modo identico nelle due versioni: cerca la recensione, la cancella dal DB, **poi** pubblica `RecensioneCancellataEvent` (stesso ordine *save → publish* di A6). Restituisce `null` se la recensione non esiste. "REST" e "Kafka" indicano solo come `createRecensione` trova l'album: i producer pubblicano sempre | `b7b1266`, `b7d4a19`, `33c9dfd` |
| C2 | `recensioni-api/.../api/event/RecensioneCancellataEvent.java` | Evento con id, recensore, idAlbum, testo, sunto (gli stessi campi di `RecensioneCreataEvent`) | `33c9dfd` |
| C3 | `recensioni/.../rest/RecensioniController.java` | `@DeleteMapping("/recensioni/{recensioneId}")`: 200 con la recensione cancellata, **404** se non esiste (come `getRecensione`). Nessun body: l'id è nel path, quindi `DeleteRecensioneRequest` è stato eliminato | `b7d4a19` |
| C4 | `recensioni-api/.../RecensioniDomainEvent.java`, `recensioni/.../RecensioniEventKafkaPublisher.java` | Chiave dei messaggi = id della recensione (`send(channel, String.valueOf(event.getId()), event)`), così creazione e cancellazione della stessa recensione restano in ordine sulla stessa partizione (verificato: chiave `1`, stessa partizione) | `143221d` |
| C5 | `recensioni-seguite/.../messagelistener/kafka/RecensioniEventKafkaListener.java`, `domain/ReplicheService.java` | Il listener gestisce anche `RecensioneCancellataEvent`, e `onRecensioneCancellata` esegue `recensioneRepository.deleteById(id)`. È idempotente: un evento ricevuto due volte non crea errori | `33c9dfd` |
| C6 | `run-test-cancellazione-recensioni.sh` (nuovo) | Test con la stessa struttura e lo stesso output del test minimale. Gli id vengono cercati per recensore, titolo e artista, quindi il test non dipende dall'ordine di assegnazione. Verifica anche il codice HTTP della `DELETE` | `8dac732` |

**Come recensioni-seguite vede la cancellazione:**
- **REST:** nessuna modifica. Interroga recensioni a ogni richiesta, quindi l'aggiornamento è immediato, ma dipende dalla disponibilità degli altri servizi.
- **Kafka:** `RecensioneCancellataEvent` passa per `recensioni-event-channel`, poi il listener aggiorna la replica locale. L'aggiornamento arriva con un breve ritardo (**consistenza finale**, per questo i test Kafka contengono delle `sleep`), ma recensioni-seguite risponde anche se recensioni è spento.

**Conteggi attesi del test di cancellazione** (partenza: stato finale del test minimale; Alice segue Pink Floyd, The Beatles, Bruce Springsteen; Bob segue Woody e RollingStone; Carlo segue Rock, Pop e Jazz):

| Passo | Recensione cancellata | Chi la segue | Alice | Bob | Carlo |
| --- | --- | --- | --- | --- | --- |
| prima | – | – | 7 | 5 | 8 |
| 1 | Woody – The Dark Side of the Moon | Alice, Bob, Carlo | 6 | 4 | 7 |
| 2 | OndaRock – Thriller | solo Carlo (Pop) | 6 | 4 | 6 |
| 3 | RollingStone – Kind of Blue | Bob, Carlo (Jazz) | 6 | 3 | 5 |
| 4 | Woody – The Wall | Alice, Bob (non Carlo: Progressive Rock) | 5 | 2 | 5 |
| inesistente | di nuovo The Wall → HTTP 404 | – | 5 | 2 | 5 |

### Verifiche eseguite
- `run-test-minimale.sh rest` e `kafka`: **OK**, con gli stessi conteggi.
- `run-test-cancellazione-recensioni.sh rest` e `kafka`: **OK**, con gli stessi conteggi.
- Docker spento: gli script si fermano subito con un messaggio d'errore chiaro.
- Riavvio del solo recensioni-seguite: la replica viene ricostruita (7/5/8 prima e dopo).
- Evento con il package vecchio: viene saltato, senza loop.

### Punti aperti (noti, non sistemati)
- Possibile `NullPointerException` (risposta 500) in `RecensioniServiceKafkaBasedImpl.createRecensione` se l'album non è ancora nella replica. La versione REST si comporta allo stesso modo con un album inesistente.
- I nomi dei topic sono stringhe ripetute nei publisher e nei listener, non costanti nei moduli `-api`.
- **Doppia scrittura DB + Kafka:** se il salvataggio riesce ma la pubblicazione fallisce, l'evento va perso. La soluzione completa è il pattern *Transactional Outbox*.
- **Passaggio a Postgres** (un DB per servizio):
  - in REST non cambia nulla;
  - in Kafka le repliche diventano persistenti, quindi il reset di Kafka e dei DB va fatto in modo coerente;
  - conviene tornare a un `group-id` fisso;
  - il problema della doppia scrittura diventa concreto.

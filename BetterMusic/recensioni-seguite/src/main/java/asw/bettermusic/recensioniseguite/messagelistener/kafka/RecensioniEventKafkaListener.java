package asw.bettermusic.recensioniseguite.messagelistener.kafka;

import asw.bettermusic.recensioni.api.event.RecensioneCreataEvent;
import asw.bettermusic.recensioni.api.event.RecensioniDomainEvent;
import asw.bettermusic.recensioniseguite.domain.ReplicheService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/* Consumatore degli eventi delle recensioni (topic pubblicato dal servizio recensioni). */
@Component
public class RecensioniEventKafkaListener {

    @Autowired
    private ReplicheService replicheService;

    @KafkaListener(topics = "recensioni-event-channel")
    public void listen(ConsumerRecord<String, RecensioniDomainEvent> record) throws Exception {
        RecensioniDomainEvent event = record.value();
        if (event instanceof RecensioneCreataEvent e) {
            this.replicheService.onRecensioneCreata(e);
        }
    }

}

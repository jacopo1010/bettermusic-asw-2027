package asw.bettermusic.recensioniseguite.messagelistener.kafka;

import asw.bettermusic.connessioni.api.event.ConnessioneCancellataEvent;
import asw.bettermusic.connessioni.api.event.ConnessioneCreataEvent;
import asw.bettermusic.connessioni.api.event.ConnessioniDomainEvent;
import asw.bettermusic.recensioniseguite.domain.ReplicheService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/* Consumatore degli eventi delle connessioni (creazioni e cancellazioni). */
@Component
public class ConnessioniEventKafkaListener {

    @Autowired
    private ReplicheService replicheService;

    @KafkaListener(topics = "connessioni-event-channel")
    public void listen(ConsumerRecord<String, ConnessioniDomainEvent> record) throws Exception {
        ConnessioniDomainEvent event = record.value();
        if (event instanceof ConnessioneCreataEvent e) {
            this.replicheService.onConnessioneCreata(e);
        } else if (event instanceof ConnessioneCancellataEvent e) {
            this.replicheService.onConnessioneCancellata(e);
        }
    }

}

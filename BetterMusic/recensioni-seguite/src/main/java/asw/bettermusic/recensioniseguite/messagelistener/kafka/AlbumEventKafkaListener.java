package asw.bettermusic.recensioniseguite.messagelistener.kafka;

import asw.bettermusic.album.api.event.AlbumCreatoEvent;
import asw.bettermusic.album.api.event.AlbumDomainEvent;
import asw.bettermusic.recensioniseguite.domain.ReplicheService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/* Consumatore degli eventi degli album (topic pubblicato dal servizio album). */
@Component
public class AlbumEventKafkaListener {

    @Autowired
    private ReplicheService replicheService;

    @KafkaListener(topics = "album-event-channel")
    public void listen(ConsumerRecord<String, AlbumDomainEvent> record) throws Exception {
        AlbumDomainEvent event = record.value();
        if (event instanceof AlbumCreatoEvent e) {
            this.replicheService.onAlbumCreato(e);
        }
    }

}

package asw.bettermusic.recensioni.messagelistener.kafka;

import asw.bettermusic.album.api.event.AlbumCreatoEvent;
import asw.bettermusic.album.api.event.AlbumDomainEvent;
import asw.bettermusic.recensioni.domain.Album;
import asw.bettermusic.recensioni.domain.AlbumReplicaRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/* Consumatore degli eventi degli album: mantiene aggiornata la replica locale. */
@Component
public class AlbumEventKafkaListener {

    @Autowired
    private AlbumReplicaRepository albumReplicaRepository;

    @KafkaListener(topics = "album-event-channel")
    public void listen(ConsumerRecord<String, AlbumDomainEvent> record) throws Exception {
        AlbumDomainEvent event = record.value();
        if (event instanceof AlbumCreatoEvent e) {
            this.onAlbumCreato(e);
        }
    }

    private void onAlbumCreato(AlbumCreatoEvent event) {
        Album album = new Album(event.getId(), event.getTitolo(), event.getArtista(), event.getGeneri());
        this.albumReplicaRepository.save(album);
    }

}

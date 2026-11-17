package asw.bettermusic.album.eventpublisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.kafka.core.KafkaTemplate;

import asw.bettermusic.album.domain.AlbumEventPublisher;
import asw.bettermusic.album.api.event.AlbumDomainEvent;

@Component 
public class AlbumEventKafkaPublisher implements AlbumEventPublisher {
    private static final String channel = "album-event-channel";
    
    @Autowired
    private KafkaTemplate<String, AlbumDomainEvent> template;

    @Override
    public void publish(AlbumDomainEvent event) {
        template.send(channel, event);
    } 
}

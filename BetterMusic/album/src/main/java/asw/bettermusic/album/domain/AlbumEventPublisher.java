package asw.bettermusic.album.domain;

import asw.bettermusic.album.api.event.AlbumDomainEvent;

public interface AlbumEventPublisher {
    void publish(AlbumDomainEvent event);
}

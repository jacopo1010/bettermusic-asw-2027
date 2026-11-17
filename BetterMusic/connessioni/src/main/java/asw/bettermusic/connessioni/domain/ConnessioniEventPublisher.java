package asw.bettermusic.connessioni.domain;

import asw.bettermusic.connessioni.api.event.ConnessioniDomainEvent;

public interface ConnessioniEventPublisher {

	void publish(ConnessioniDomainEvent event);
}

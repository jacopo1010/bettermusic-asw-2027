package asw.bettermusic.recensioni.domain;

import asw.bettermusic.recensioni.api.event.RecensioniDomainEvent;

public interface RecensioniEventPublisher {

	void publish(RecensioniDomainEvent event);
	
}

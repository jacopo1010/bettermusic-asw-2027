package asw.bettermusic.recensioni.domain;

import asw.bettermusic.recensioni.producerevent.RecensioniDomainEvent;

public interface RecensioniEventPublisher {

	void publish(RecensioniDomainEvent event);
	
}

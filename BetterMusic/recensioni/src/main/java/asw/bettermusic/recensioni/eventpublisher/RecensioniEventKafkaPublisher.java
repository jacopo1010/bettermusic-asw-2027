package asw.bettermusic.recensioni.eventpublisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import asw.bettermusic.recensioni.api.event.RecensioniDomainEvent;
import asw.bettermusic.recensioni.domain.RecensioniEventPublisher;

@Component
public class RecensioniEventKafkaPublisher implements RecensioniEventPublisher{

	@Autowired
	private KafkaTemplate<String, RecensioniDomainEvent> template;
	
	private static final String channel = "recensioni-event-channel";

	@Override
	public void publish(RecensioniDomainEvent event) {
		 template.send(channel,event);
	}
	
	

}

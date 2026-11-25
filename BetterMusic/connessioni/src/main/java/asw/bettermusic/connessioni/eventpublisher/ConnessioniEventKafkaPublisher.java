package asw.bettermusic.connessioni.eventpublisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import asw.bettermusic.connessioni.api.event.ConnessioniDomainEvent;
import asw.bettermusic.connessioni.domain.ConnessioniEventPublisher;


@Component
public class ConnessioniEventKafkaPublisher implements ConnessioniEventPublisher {

	@Autowired private KafkaTemplate<String,ConnessioniDomainEvent> template;
	
	private static final String channel = "connessioni-event-channel";
	
	@Override
	public void publish(ConnessioniDomainEvent event) {
		template.send(channel, event.getUtente(), event);
	}

}

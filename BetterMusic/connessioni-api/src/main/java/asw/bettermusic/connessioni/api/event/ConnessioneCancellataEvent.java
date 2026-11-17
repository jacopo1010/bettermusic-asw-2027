package asw.bettermusic.connessioni.api.event;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class ConnessioneCancellataEvent implements ConnessioniDomainEvent{
	
	private Long id; 
	private String utente;
	private String seguito; 
	private String ruolo; 

}

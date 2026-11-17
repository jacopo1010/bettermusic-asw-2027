package asw.bettermusic.connessioni.api.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ConnessioneCreataEvent {
	
	private Long id; 
	private String utente;
	private String seguito; 
	private String ruolo; 

}

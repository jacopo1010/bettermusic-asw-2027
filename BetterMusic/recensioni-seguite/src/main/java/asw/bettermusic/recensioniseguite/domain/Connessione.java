package asw.bettermusic.recensioniseguite.domain;

import jakarta.persistence.*;

import lombok.*; 

/* Replica locale di una connessione (aggiornata dagli eventi del servizio connessioni). */  
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Connessione {

	/* id della connessione (assegnato dal servizio connessioni) */ 
	@Id
	private Long id; 
	/* utente che segue */ 
	private String utente; 
	/* chi o cosa è seguito (un artista o uno che scrive recensioni oppure un genere musicale) */ 
	private String seguito; 
	/* ruolo del seguito: può essere ARTISTA oppure RECENSORE oppure GENERE */ 
	private String ruolo; 
	
}

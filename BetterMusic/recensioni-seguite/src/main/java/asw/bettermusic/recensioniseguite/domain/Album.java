package asw.bettermusic.recensioniseguite.domain;

import jakarta.persistence.*;

import lombok.*; 

import java.util.*; 

/* Replica locale di un album (aggiornata dagli eventi del servizio album). */  
@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Album {

	/* id dell'album (assegnato dal servizio album) */ 
	@Id
	private Long id; 
	/* titolo dell'album */ 
	private String titolo; 
	/* artista dell'album */ 
	private String artista; 
	/* generi dell'album */ 
	@ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
	private Set<String> generi; 
	
}
